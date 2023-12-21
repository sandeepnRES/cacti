/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package main

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"strconv"
	"time"

	"github.com/golang/protobuf/proto"
	"github.com/hyperledger/cacti/weaver/common/protos-go/v2/common"
	"github.com/hyperledger/cacti/weaver/core/network/fabric-interop-cc/libs/assetexchange/v2"
	wutils "github.com/hyperledger/cacti/weaver/core/network/fabric-interop-cc/libs/utils/v2"
	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

/*
Pledge Step 5 & Claim 7:
Locker: Lender of AssetLedger
Recipient: Borrower of TokenLedger
Pledge Step 6 & Claim 8:
Locker: Borrower of TokenLedger
Recipient: Lender of AssetLedger
*/

type LoanRepaymentCondition struct {
	TokenType         			string    `json:"tokenType"`
	TokenQuantity           	uint64	  `json:"tokenQuantity"`
	TokenLedgerId        		string    `json:"tokenLedgerId"`
	TokenLedgerLenderCert       string    `json:"tokenLedgerLenderCert"`
	TokenLedgerBorrowerCert     string    `json:"tokenLedgerBorrowerCert"`
	AssetType         			string    `json:"assetType"`
	AssetId           			string    `json:"assetId"`
	AssetLedgerId        		string    `json:"assetLedgerId"`
	AssetLedgerLenderCert       string    `json:"assetLedgerLenderCert"`
	AssetLedgerBorrowerCert     string    `json:"assetLedgerBorrowerCertsuer"`
}

// Step 5: First HTLC claim and then Pledge
func (s *SmartContract) ClaimAndPledgeAsset(ctx contractapi.TransactionContextInterface, contractId, claimInfoSerializedProto64 string, loanPeriod uint64, loanRepaymentConditionJSONStr string) (string, error) {
	claimed := false
	err := assetexchange.ClaimAssetUsingContractId(ctx, contractId, claimInfoSerializedProto64)
	if err != nil {
		return "", logThenErrorf(err.Error())
	} else {
		claimed = true
	}
	if claimed {
		// Fetch the contracted bond asset type from the ledger
		assetType, err := s.FetchAssetTypeFromContractIdAssetLookupMap(ctx, contractId)
		if err != nil {
			return "", logThenErrorf(err.Error())
		}
		// Fetch the contracted bond asset id from the ledger
		id, err := s.FetchAssetIdFromContractIdAssetLookupMap(ctx, contractId)
		if err != nil {
			return "", logThenErrorf(err.Error())
		}

		loanRepaymentConditionJSON := []byte(loanRepaymentConditionJSONStr)
		lenderECertBase64, err := wutils.GetECertOfTxCreatorBase64(ctx)
		localNetworkId, err := ctx.GetStub().GetState(wutils.GetLocalNetworkIDKey())
		if err != nil {
			return "", logThenErrorf(err.Error())
		}
		asset, err := s.ReadAsset(ctx, assetType, id, true)
		if err != nil {
			return "", logThenErrorf(err.Error())
		}
		assetJSON, err := json.Marshal(asset)
		if err != nil {
			return "", err
		}
		borrowerECertBase64 := asset.Owner
        fmt.Printf("Owner: %s\n", asset.Owner)		
		// Check if asset is pledged already
		bondAssetPledgeMap, err := getAssetPledgeIdMap(ctx, assetType, id)
		if err == nil {
			return bondAssetPledgeMap.PledgeID, fmt.Errorf("asset %s is already pledged with pledgeId %s for different recipient", id, bondAssetPledgeMap.PledgeID)
		}
		
		loanRepaymentCondition := &LoanRepaymentCondition{}
		err = json.Unmarshal(loanRepaymentConditionJSON, loanRepaymentCondition)
		
		loanRepaymentCondition.AssetType = assetType
		loanRepaymentCondition.AssetId = id
		loanRepaymentCondition.AssetLedgerId = string(localNetworkId)
		loanRepaymentCondition.AssetLedgerLenderCert = lenderECertBase64
		loanRepaymentCondition.AssetLedgerBorrowerCert = borrowerECertBase64
		
		loanRepaymentConditionJSON, err = json.Marshal(loanRepaymentCondition)
		remoteNetworkId := loanRepaymentCondition.TokenLedgerId

		// Pledge the asset using common (library) logic
		if pledgeId, err := wutils.PledgeAsset(ctx, assetJSON, assetType, id, remoteNetworkId, loanRepaymentCondition.TokenLedgerLenderCert, loanPeriod, loanRepaymentConditionJSON); err == nil {
			// Delete asset state using app-specific logic
			err = deleteAsset(ctx, assetType, id)
			if err != nil {
				return pledgeId, err
			}
			err = createAssetPledgeIdMap(ctx, pledgeId, assetType, id, remoteNetworkId, borrowerECertBase64)
			return pledgeId, err
		} else {
			return "", err
		}
	} else {
		return "", logThenErrorf("failed to claim bond asset with contract id %s", contractId)
	}
}

// Step 6: Pledge Tokens for repayment.
func (s *SmartContract) PledgeRepaymentTokens(ctx contractapi.TransactionContextInterface, assetType string, numUnits uint64, remoteNetworkId string, expiryTimeSecs uint64, loanRepaymentConditionJSONStr string) (string, error) {
	// Verify asset balance for this transaction's client using app-specific-logic
	lockerHasEnoughTokens, err := s.TokenAssetsExist(ctx, assetType, numUnits)
	if err != nil {
		return "", err
	}
	if !lockerHasEnoughTokens {
		return "", fmt.Errorf("cannot pledge token asset of type %s as there are not enough tokens", assetType)
	}

	// Get asset owner (this transaction's client) using app-specific-logic
	owner, err := wutils.GetECertOfTxCreatorBase64(ctx)
	if err != nil {
		return "", err
	}

	asset := TokenAsset{
		Type:     assetType,
		Owner:    owner,
		NumUnits: numUnits,
	}
	assetJSON, err := json.Marshal(asset)
	if err != nil {
		return "", err
	}
	
	localNetworkId, err := ctx.GetStub().GetState(wutils.GetLocalNetworkIDKey())
	borrowerECertBase64, err := wutils.GetECertOfTxCreatorBase64(ctx)
	
	loanRepaymentConditionJSON := []byte(loanRepaymentConditionJSONStr)
	loanRepaymentCondition := &LoanRepaymentCondition{}
	err = json.Unmarshal(loanRepaymentConditionJSON, loanRepaymentCondition)
	
	loanRepaymentCondition.TokenType = assetType
	loanRepaymentCondition.TokenQuantity = numUnits
	loanRepaymentCondition.TokenLedgerId = string(localNetworkId)
	loanRepaymentCondition.TokenLedgerBorrowerCert = borrowerECertBase64
	
	loanRepaymentConditionJSON, err = json.Marshal(loanRepaymentCondition)

	// Pledge the asset using common (library) logic
	if pledgeId, err := wutils.PledgeAsset(ctx, assetJSON, assetType, strconv.Itoa(int(numUnits)), remoteNetworkId, loanRepaymentCondition.AssetLedgerBorrowerCert, expiryTimeSecs, loanRepaymentConditionJSON); err == nil {
		// Deduce asset balance using app-specific logic
		return pledgeId, s.DeleteTokenAssets(ctx, assetType, numUnits)
	} else {
		return "", err
	}
}

// Step 8: Asset Transfer Tokens Claim with Pledge condition verification
// Since pledge is deleted in Step 7, hence we need to use claim status of
// Step 7 as proof for Step 8 claim.
func (s *SmartContract) ClaimLoanRepayment(ctx contractapi.TransactionContextInterface, pledgeId, remoteNetworkId, remoteClaimStatusBytes64 string) error {
	// (Optional) Ensure that this function is being called by the Fabric Interop CC

	// Claim the asset using common (library) logic (ideally Lender)
	claimer, err := wutils.GetECertOfTxCreatorBase64(ctx)
	if err != nil {
		return err
	}
	
	_, tokenPledgeBytes64, err := wutils.GetAssetPledgeDetails(ctx, pledgeId)
	token, err := getTokenAssetFromPledge(remoteClaimStatusBytes64)
	pledgeConditionFromTokenLedger, err := getLoanRepaymentConditionFromPledge(tokenPledgeBytes64)
	if err != nil {
		return err
	}
	
	pledgeConditionFromAssetLedger, err := getLoanRepaymentConditionFromClaimStatus(remoteClaimStatusBytes64)
	if err != nil {
		return err
	}
	asset, err := getBondAssetFromClaimStatus(remoteClaimStatusBytes64)
	if err != nil {
		return err
	}
	
	// Validate pledged asset details using app-specific-logic
	if (pledgeConditionFromAssetLedger != pledgeConditionFromTokenLedger) {
		return fmt.Errorf("repayment condition doesn't match in asset and token pledges")
	}
	if (asset.Type != pledgeConditionFromAssetLedger.AssetType && asset.ID != pledgeConditionFromAssetLedger.AssetId) {
		return fmt.Errorf("inconsistent type and id of asset pledged as compared to repayment condition")
	}
	if pledgeConditionFromAssetLedger.TokenLedgerLenderCert != claimer {
		return fmt.Errorf("cannot claim %d %s tokens as it has not been pledged to the claimer", token.NumUnits,  token.Type)
	}
	
	remotePledgeBytes64, claimStatusBool, err := createPledgeDataFromClaimStatus(remoteClaimStatusBytes64)
	if err != nil {
		return err
	}
	// ClaimStatus should be True
	if !claimStatusBool {
		return fmt.Errorf("cannot claim %d %s tokens as the asset has not been claimed by borrower", token.NumUnits, token.Type)
	}
	_, err = wutils.ClaimRemoteAsset(ctx, pledgeId, remoteNetworkId, remotePledgeBytes64)
	if err != nil {
		return err
	}

	// Recreate the asset in this network and chaincode using app-specific logic: make the recipient the owner of the asset
	return s.IssueTokenAssets(ctx, token.Type, token.NumUnits, claimer)
}

// Step 7: Asset Transfer Claim with Pledge condition verification
func (s *SmartContract) ClaimLoanedAsset(ctx contractapi.TransactionContextInterface, pledgeId, remoteNetworkId, remotePledgeBytes64 string) error {
	// (Optional) Ensure that this function is being called by the Fabric Interop CC

	// Claim the asset using common (library) logic
	claimer, err := wutils.GetECertOfTxCreatorBase64(ctx)
	if err != nil {
		return err
	}
	
	_, assetPledgeBytes64, err := wutils.GetAssetPledgeDetails(ctx, pledgeId)
	asset, err := getBondAssetFromPledge(assetPledgeBytes64)
	pledgeConditionFromAssetLedger, err := getLoanRepaymentConditionFromPledge(assetPledgeBytes64)
	if err != nil {
		return err
	}
	
	pledgeConditionFromTokenLedger, err := getLoanRepaymentConditionFromPledge(remotePledgeBytes64)
	if err != nil {
		return err
	}
	
	tokens, err := getTokenAssetFromPledge(remotePledgeBytes64)
	if err != nil {
		return err
	}
	
	if (pledgeConditionFromAssetLedger != pledgeConditionFromTokenLedger) {
		return fmt.Errorf("repayment condition doesn't match in asset and token pledges")
	}
	if (tokens.Type != pledgeConditionFromAssetLedger.TokenType && tokens.NumUnits != pledgeConditionFromAssetLedger.TokenQuantity) {
		return fmt.Errorf("inconsistent type and quantity of tokens pledged as compared to repayment condition")
	}
	
	// Validate pledged asset details using app-specific-logic
	if pledgeConditionFromAssetLedger.AssetLedgerBorrowerCert != claimer {
	    fmt.Printf("borrower: %s, claimer: %s \n", pledgeConditionFromAssetLedger.AssetLedgerBorrowerCert, claimer)
    	return fmt.Errorf("cannot claim asset %s as it has not been pledged to the claimer", asset.ID)
	}
	
	// Question in PR: Is following return `pledgeAssetDetails` required from utils?
	_, err = wutils.ClaimRemoteAsset(ctx, pledgeId, remoteNetworkId, remotePledgeBytes64)
	if err != nil {
		return err
	}

	// Recreate the asset in this network and chaincode using app-specific logic: make the recipient the owner of the asset
	return s.CreateAsset(ctx, asset.Type, asset.ID, claimer, asset.Issuer, asset.FaceValue, asset.MaturityDate.Format(time.RFC822))
}

func (s *SmartContract) GetLoanRepaymentCondition(ctx contractapi.TransactionContextInterface, pledgeId string) (string, error) {
	_, assetPledgeBytes64, err := wutils.GetAssetPledgeDetails(ctx, pledgeId)
	pledgeConditionFromAssetLedger, err := getLoanRepaymentConditionFromPledge(assetPledgeBytes64)
	if err != nil {
		return "", err
	}
	pledgeConditionFromAssetLedgerJSON, err := json.Marshal(pledgeConditionFromAssetLedger)
	if err != nil {
		return "", err
	}
	return string(pledgeConditionFromAssetLedgerJSON), nil
}

// GetAssetClaimStatus returns the asset claim status and present time (of invocation).
func (s *SmartContract) GetAssetLoanClaimStatus(ctx contractapi.TransactionContextInterface, pledgeId string) (string, error) {
	// (Optional) Ensure that this function is being called by the relay via the Fabric Interop CC

	// Create blank asset details using app-specific-logic
	blankAsset := BondAsset{
		Type:         "",
		ID:           "",
		Owner:        "",
		Issuer:       "",
		FaceValue:    0,
		MaturityDate: time.Unix(0, 0),
	}
	blankAssetJSON, err := json.Marshal(blankAsset)
	if err != nil {
		return "", err
	}
	
	
	_, assetPledgeBytes64, err := wutils.GetAssetPledgeDetails(ctx, pledgeId)
	pledge := &common.AssetPledge{}
	assetPledgeSerialized, err := base64.StdEncoding.DecodeString(assetPledgeBytes64)
	if err != nil {
		return "", err
	}
	if len(assetPledgeSerialized) == 0 {
		return "", fmt.Errorf("empty asset pledge")
	}
	err = proto.Unmarshal([]byte(assetPledgeSerialized), pledge)
	var loanRepaymentCondition LoanRepaymentCondition
	err = json.Unmarshal(pledge.PledgeCondition, &loanRepaymentCondition)
	if err != nil {
		return "", err
	}

	// Fetch asset claim details using common (library) logic
	claimAssetDetails, claimBytes64, blankClaimBytes64, err := wutils.GetAssetClaimStatus(ctx, pledgeId, pledge.Recipient, loanRepaymentCondition.AssetLedgerLenderCert, pledge.RemoteNetworkID, pledge.ExpiryTimeSecs, blankAssetJSON)
	if err != nil {
		return blankClaimBytes64, err
	}
	if claimAssetDetails == nil {
		// represents the scenario that the asset was not claimed by the remote network
		return blankClaimBytes64, nil
	}

	// Validate returned asset details using app-specific-logic
	// It's not possible to check for the existance of the claimed asset on the ledger, since that asset might got spent already.

	// Match pledger identity in claim with request parameters
	var lookupClaimAsset BondAsset
	err = json.Unmarshal(claimAssetDetails, &lookupClaimAsset)
	if err != nil {
		return blankClaimBytes64, err
	}

	// represents the scenario that the asset was claimed by the remote network
	return claimBytes64, nil
}

func getLoanRepaymentConditionFromPledge(pledgeBytes64 string) (LoanRepaymentCondition, error) {
	var loanRepaymentCondition LoanRepaymentCondition
	pledge := &common.AssetPledge{}
	assetPledgeSerialized, err := base64.StdEncoding.DecodeString(pledgeBytes64)
	if err != nil {
		return loanRepaymentCondition, err
	}
	if len(assetPledgeSerialized) == 0 {
		return loanRepaymentCondition, fmt.Errorf("empty asset pledge")
	}
	err = proto.Unmarshal([]byte(assetPledgeSerialized), pledge)
	if err != nil {
		return loanRepaymentCondition, err
	}
	err = json.Unmarshal(pledge.PledgeCondition, &loanRepaymentCondition)
	return loanRepaymentCondition, err
}
func getLoanRepaymentConditionFromClaimStatus(claimStausBytes64 string) (LoanRepaymentCondition, error) {
	var loanRepaymentCondition LoanRepaymentCondition
	claimStatus := &common.AssetClaimStatus{}
	assetClaimStatusSerialized, err := base64.StdEncoding.DecodeString(claimStausBytes64)
	if err != nil {
		return loanRepaymentCondition, err
	}
	if len(assetClaimStatusSerialized) == 0 {
		return loanRepaymentCondition, fmt.Errorf("empty asset claim status")
	}
	err = proto.Unmarshal([]byte(assetClaimStatusSerialized), claimStatus)
	if err != nil {
		return loanRepaymentCondition, err
	}
	err = json.Unmarshal(claimStatus.PledgeCondition, &loanRepaymentCondition)
	return loanRepaymentCondition, err
}
func createPledgeDataFromClaimStatus(claimStausBytes64 string) (string, bool, error) {
	claimStatus := &common.AssetClaimStatus{}
	assetClaimStatusSerialized, err := base64.StdEncoding.DecodeString(claimStausBytes64)
	if err != nil {
		return "", false, err
	}
	if len(assetClaimStatusSerialized) == 0 {
		return "", false, fmt.Errorf("empty asset claim status")
	}
	err = proto.Unmarshal([]byte(assetClaimStatusSerialized), claimStatus)
	if err != nil {
		return "", false, err
	}
	pledge := &common.AssetPledge{
		AssetDetails: claimStatus.AssetDetails,
		LocalNetworkID: claimStatus.LocalNetworkID,
		RemoteNetworkID: claimStatus.RemoteNetworkID,
		Recipient: claimStatus.Recipient,
		ExpiryTimeSecs: claimStatus.ExpiryTimeSecs,
	}
	assetPledgeBytes, err := proto.Marshal(pledge)
	if err != nil {
		return "", false, err
	}
	pledgeBytes64 := base64.StdEncoding.EncodeToString(assetPledgeBytes)
	return pledgeBytes64, claimStatus.ClaimStatus, nil
}

