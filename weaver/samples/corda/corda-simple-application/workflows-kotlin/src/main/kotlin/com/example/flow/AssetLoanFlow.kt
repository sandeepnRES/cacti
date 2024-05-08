/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cordaSimpleApplication.flow

import arrow.core.*
import co.paralleluniverse.fibers.Suspendable
import javassist.NotFoundException
import sun.security.x509.UniqueIdentity
import java.util.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.protobuf.ByteString
import java.time.Instant
import java.security.cert.X509Certificate

import net.corda.core.contracts.StaticPointer
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.contracts.ReferencedStateAndRef
import net.corda.core.contracts.TimeWindow
import net.corda.core.contracts.Command
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.Party
import net.corda.core.flows.*
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.StatesToRecord
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.utilities.unwrap

import com.cordaSimpleApplication.state.AssetState
import com.cordaSimpleApplication.state.AssetStateJSON
import com.cordaSimpleApplication.state.BondAssetState
import com.cordaSimpleApplication.state.BondAssetStateJSON
import com.cordaSimpleApplication.state.LoanRepaymentCondition
import com.cordaSimpleApplication.contract.BondAssetContract
import com.cordaSimpleApplication.contract.AssetContract

import org.hyperledger.cacti.weaver.imodule.corda.flows.GetAssetClaimStatusState
import org.hyperledger.cacti.weaver.imodule.corda.flows.GetAssetPledgeStatus
import org.hyperledger.cacti.weaver.imodule.corda.flows.AssetPledgeStateToProtoBytes
import org.hyperledger.cacti.weaver.imodule.corda.flows.GetAssetPledgeStateById
import org.hyperledger.cacti.weaver.imodule.corda.flows.GetExternalStateAndRefByLinearId
import org.hyperledger.cacti.weaver.imodule.corda.flows.GetExternalStateByLinearId
import org.hyperledger.cacti.weaver.imodule.corda.flows.RetrieveNetworkIdStateAndRef
import org.hyperledger.cacti.weaver.imodule.corda.flows.GetAssetExchangeHTLCStateById
import org.hyperledger.cacti.weaver.imodule.corda.flows.parseExternalStateForPledgeStatus
import org.hyperledger.cacti.weaver.imodule.corda.flows.parseExternalStateForClaimStatus

import org.hyperledger.cacti.weaver.imodule.corda.contracts.AssetTransferContract
import org.hyperledger.cacti.weaver.imodule.corda.contracts.AssetExchangeHTLCStateContract

import org.hyperledger.cacti.weaver.imodule.corda.states.AssetPledgeState
import org.hyperledger.cacti.weaver.imodule.corda.states.AssetExchangeHTLCState
import org.hyperledger.cacti.weaver.imodule.corda.states.AssetClaimStatusState
import org.hyperledger.cacti.weaver.imodule.corda.states.NetworkIdState
import org.hyperledger.cacti.weaver.imodule.corda.states.AssetClaimHTLCData
import org.hyperledger.cacti.weaver.imodule.corda.states.ExternalState

import org.hyperledger.cacti.weaver.protos.corda.ViewDataOuterClass
import org.hyperledger.cacti.weaver.protos.common.asset_transfer.AssetTransfer


/**
 * Enum for communicating the role of the responder from initiator flow to responder flow.
 */
@CordaSerializable
enum class AssetLoanResponderRole {
    BORROWER, LENDER, SIGNER, OBSERVER
}

/*
Pledge Step 5 & Claim 7:
Locker: Lender of AssetLedger
Recipient: Borrower of TokenLedger
Pledge Step 6 & Claim 8:
Locker: Borrower of TokenLedger
Recipient: Lender of AssetLedger
*/


/**
 */
@InitiatingFlow
@StartableByRPC
class ClaimAndPledgeAssetStateInitiator
@JvmOverloads
constructor(
    val contractId: String,
    val claimInfo: AssetClaimHTLCData,
    val pledgeConditionArgs: LoanRepaymentCondition,
    val loanPeriod: Long,       // duration in seconds
    val issuer: Party,
    val observers: List<Party> = listOf<Party>()
) : FlowLogic<Either<Error, UniqueIdentifier>>() {
    @Suspendable
    override fun call(): Either<Error, UniqueIdentifier> {
        /*
        1. From the contractId, fetch the HTLCState.
        2. Create PledgeState (with expiryTime L) for the asset using the pointer from HTLCState.
        3. Create a transaction:
            a. Input State:
                i.  HTLCState (AssetPointer->owner->Borrower)
            b. OutputState:
                i.  PledgeState (AssetPointer->owner->Borrower, assume pledger is current owner)
            c. Commands:
                i.  ClaimAsset (HTLC) with pre-image with Lender as signer
                ii. PledgeAsset with Borrower and Lender as signer
                    1) pledgeCondition with repayment amount.
            d. TimeWindow: HTLC expiry Time T.
        */
        val linearId = getLinearIdFromString(contractId)
        return subFlow(GetAssetExchangeHTLCStateById(contractId)).fold({
            println("AssetExchangeHTLCState for Id: ${linearId} not found.")
            Left(Error("AssetExchangeHTLCState for Id: ${linearId} not found."))            
        }, {
            val inputState = it
            val assetExchangeHTLCState = inputState.state.data
            val loanPeriodAbs: Instant = Instant.now().plusSeconds(loanPeriod)
            if (!ourIdentity.equals(assetExchangeHTLCState.recipient)) {
                println("Error: Only recipient can call claim.")
                Left(Error("Error: Only recipient can call claim"))        
            } else {
                val networkIdStateRef = subFlow(RetrieveNetworkIdStateAndRef())!!
                val notary = networkIdStateRef.state.notary
                val borrower = assetExchangeHTLCState.locker
                val pledgeCmd = Command(AssetTransferContract.Commands.Pledge(),
                    listOf(
                        ourIdentity.owningKey
                    )
                )
                val assetLoanPledgeCmd = Command(BondAssetContract.Commands.LoanPledge(),
                    setOf(
                        ourIdentity.owningKey,
                        issuer.owningKey
                    ).toList()
                )
                val assetHTLCClaimCmd = Command(AssetExchangeHTLCStateContract.Commands.Claim(claimInfo),
                    setOf(
                        ourIdentity.owningKey
                    ).toList()
                )
                val lenderCert = Base64.getEncoder().encodeToString(x509CertToPem(ourIdentityAndCert.certificate).toByteArray())
                val borrowerCert = Base64.getEncoder().encodeToString(x509CertToPem(getPartyCertificate(borrower, serviceHub)).toByteArray())
                
                // Get asset and update pledgeCondition
                val bondAsset = assetExchangeHTLCState.assetStatePointer!!.resolve(serviceHub).state.data as BondAssetState
                val pledgeCondition = pledgeConditionArgs.copy(
                    assetType = bondAsset.type,
                    assetId = bondAsset.id,
                    assetLedgerId = networkIdStateRef.state.data.networkId,
                    assetLedgerLenderCert = lenderCert,
                    assetLedgerBorrowerCert = borrowerCert
                )
                
                // 1. Create the asset pledge state
                val gson = GsonBuilder().create();
                var marshalledPledgeCondition = gson.toJson(pledgeCondition, LoanRepaymentCondition::class.java)
                println("Pledge Condition JSON: ${marshalledPledgeCondition}")
                val assetPledgeState = AssetPledgeState(
                    assetExchangeHTLCState.assetStatePointer, // @property assetStatePointer
                    ourIdentity, // @property locker
                    pledgeCondition.assetLedgerId, // @property locker
                    pledgeCondition.assetLedgerBorrowerCert, // @property recipient
                    loanPeriodAbs.getEpochSecond(),
                    pledgeCondition.assetLedgerId,
                    pledgeCondition.tokenLedgerId,
                    marshalledPledgeCondition.toByteArray(),
                    borrower
                )
                
                val txBuilder = TransactionBuilder(notary)
                    .addInputState(inputState)
                    .addOutputState(assetPledgeState, AssetTransferContract.ID)
                    .addCommand(assetHTLCClaimCmd)
                    .addCommand(assetLoanPledgeCmd)
                    .addCommand(pledgeCmd)
                    .addReferenceState(ReferencedStateAndRef(networkIdStateRef))
                    .setTimeWindow(TimeWindow.untilOnly(assetExchangeHTLCState.lockInfo.expiryTime))
                
                // Verify and collect signatures on the transaction
                txBuilder.verify(serviceHub)
                val partSignedTx = serviceHub.signInitialTransaction(txBuilder)
                println("Recipient signed transaction.")

                var sessions = listOf<FlowSession>()

                if (!ourIdentity.equals(issuer)) {
                    val issuerSession = initiateFlow(issuer)
                    issuerSession.send(AssetLoanResponderRole.SIGNER)
                    sessions += issuerSession
                }
                
                val borrowerSession = initiateFlow(borrower)
                borrowerSession.send(AssetLoanResponderRole.BORROWER)
                sessions += borrowerSession

                val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions))
                
                var observerSessions = listOf<FlowSession>()
                for (obs in observers) {
                    val obsSession = initiateFlow(obs)
                    obsSession.send(AssetLoanResponderRole.OBSERVER)
                    observerSessions += obsSession
                }
                val storedAssetPledgeState = subFlow(FinalityFlow(
                    fullySignedTx, 
                    sessions + observerSessions
                )).tx.outputStates.first() as AssetPledgeState
                println("Successfully stored: $storedAssetPledgeState\n")
                Right(storedAssetPledgeState.linearId)
            }
        })
    }
}

@InitiatedBy(ClaimAndPledgeAssetStateInitiator::class)
class ClaimAndPledgeAssetStateAcceptor(val session: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val role = session.receive<AssetLoanResponderRole>().unwrap { it }
        fun checkLoanPledge(tx: LedgerTransaction): Boolean {
            
            return true
        }
        if (role == AssetLoanResponderRole.SIGNER) {
            val signTransactionFlow = object : SignTransactionFlow(session) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val lTx = stx.tx.toLedgerTransaction(serviceHub)
                    val assetStates = lTx.inputsOfType<BondAssetState>()
                    val htlcStates = lTx.inputsOfType<AssetExchangeHTLCState>()
                    "There should be either BondAssetState or HTLC State as input." using (assetStates.size == 1 || htlcStates.size == 1)
                    "There should be one output AssetPledgeState." using (lTx.outputsOfType<AssetPledgeState>().size == 1)
                    
                    val assetState = if (assetStates.size == 1) assetStates[0] else htlcStates[0].assetStatePointer.resolve(lTx).state.data as BondAssetState
                    val pledgeState = lTx.outputsOfType<AssetPledgeState>()[0]
                    val pledgeCondition = Gson().fromJson(ByteString.copyFrom(pledgeState.pledgeCondition).toStringUtf8(), LoanRepaymentCondition::class.java)
                    
                    "Pledge asset should be same as in pledge condition." using (assetState.id == pledgeCondition.assetId
                        && assetState.type == pledgeCondition.assetType
                    )
                    
                    "Lender should be the pledger in pledge condition." using (pledgeState.lockerCert == pledgeCondition.assetLedgerLenderCert)
                    "Borrower should be the recipient in pledge condition." using (pledgeState.recipientCert == pledgeCondition.tokenLedgerBorrowerCert)
                    
                    val inReferences = lTx.referenceInputRefsOfType<NetworkIdState>()
                    "There should be a single reference input network id." using (inReferences.size == 1)

                    val validNetworkIdState = inReferences.get(0).state.data
                    "Asset ledger should be correct in pledge condition." using (pledgeCondition.assetLedgerId.equals(validNetworkIdState.networkId))
                    //"Loan Pledge conditions should be satisified" using (checkLoanPledge(lTx) == true)
                }
            }
            try {
                val txId = subFlow(signTransactionFlow).id
                println("Issuer signed transaction.")
                return subFlow(ReceiveFinalityFlow(session, expectedTxId = txId))
            } catch (e: Exception) {
                println("Error signing claim asset transaction by issuer: ${e.message}\n")
                return subFlow(ReceiveFinalityFlow(session))
            }
        } else if (role == AssetLoanResponderRole.BORROWER) {
            val signTransactionFlow = object : SignTransactionFlow(session) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val lTx = stx.tx.toLedgerTransaction(serviceHub)
                    val assetStates = lTx.inputsOfType<BondAssetState>()
                    val htlcStates = lTx.inputsOfType<AssetExchangeHTLCState>()
                    "There should be either BondAssetState or HTLC State as input." using (assetStates.size == 1 || htlcStates.size == 1)
                    "There should be one output AssetPledgeState." using (lTx.outputsOfType<AssetPledgeState>().size == 1)
                    
                    val assetState = if (assetStates.size == 1) assetStates[0] else htlcStates[0].assetStatePointer.resolve(lTx).state.data as BondAssetState
                    val pledgeState = lTx.outputsOfType<AssetPledgeState>()[0]
                    val pledgeCondition = Gson().fromJson(ByteString.copyFrom(pledgeState.pledgeCondition).toStringUtf8(), LoanRepaymentCondition::class.java)
                    
                    "Pledge asset should be same as in pledge condition." using (assetState.id == pledgeCondition.assetId
                        && assetState.type == pledgeCondition.assetType
                    )
                    
                    "Lender should be the pledger in pledge condition." using (pledgeState.lockerCert == pledgeCondition.assetLedgerLenderCert)
                    "Borrower should be the recipient in pledge condition." using (pledgeState.recipientCert == pledgeCondition.tokenLedgerBorrowerCert)
                    
                    val inReferences = lTx.referenceInputRefsOfType<NetworkIdState>()
                    "There should be a single reference input network id." using (inReferences.size == 1)

                    val validNetworkIdState = inReferences.get(0).state.data
                    "Asset ledger should be correct in pledge condition." using (pledgeCondition.assetLedgerId.equals(validNetworkIdState.networkId))
                    //"Loan Pledge conditions should be satisified" using (checkLoanPledge(lTx) == true)
                    val htlcState = lTx.inputsOfType<AssetExchangeHTLCState>()[0]
                    //val pledgeState = lTx.outputsOfType<AssetPledgeState>()[0]
                    val myCert = Base64.getEncoder().encodeToString(x509CertToPem(ourIdentityAndCert.certificate).toByteArray())
                    //val pledgeCondition = Gson().fromJson(ByteString.copyFrom(pledgeState.pledgeCondition).toStringUtf8(), LoanRepaymentCondition::class.java)
                    
                    "I should be the recipient of pledge" using (htlcState.locker == ourIdentity && pledgeState.recipient == ourIdentity && pledgeState.recipientCert == myCert)
                    "I should be the borrower of pledge condition" using (pledgeCondition.assetLedgerBorrowerCert == myCert)
                }
            }
            try {
                val txId = subFlow(signTransactionFlow).id
                println("Borrower signed transaction.")
                return subFlow(ReceiveFinalityFlow(session, expectedTxId = txId))
            } catch (e: Exception) {
                println("Error signing claim and pledge asset transaction by borrower: ${e.message}\n")
                return subFlow(ReceiveFinalityFlow(session))
            }
        } else if (role == AssetLoanResponderRole.OBSERVER) {
            val sTx = subFlow(ReceiveFinalityFlow(session, statesToRecord = StatesToRecord.ALL_VISIBLE))
            println("Received Tx: ${sTx} and recorded states.")
            return sTx
        } else {
            println("Incorrect Responder Role.")
            throw IllegalStateException("Incorrect Responder Role.")
        }
    }
}

/**
 *
 */
@InitiatingFlow
@StartableByRPC
class ClaimLoanedAssetInitiator
@JvmOverloads
constructor(
    val pledgeId: String,
    val pledgeStatusLinearId: String,
    val issuer: Party,
    val observers: List<Party> = listOf<Party>()
) : FlowLogic<Either<Error, SignedTransaction>>() {
    @Suspendable
    override fun call(): Either<Error, SignedTransaction> = try {
        /*
        1. Fetch the local pledge state using pledgeId
        2. Fetch the remote pledge state (step 6) using pledgeStatusLinearId
        3. Create a transaction:
            a. input: - pledge state of step 5
                      - externalstate containing remote pledge state of step 6
            b. output: - loaned asset state with updated owner as borrower
                       - claimStatusState
            d. contracts:
                i. ClaimRemoteAsset contract
                ii. contract which checks 
                    - if repayment amount is correct
                    - repayment is for correct asset id
                    - output loaned asset state is same as the one in input pledge state
                iii. IssueAsset contract
            e. Expiry time in time window.
        4. Borrower and Issuer should be signer
        */
        val localPledgeRef = subFlow(GetAssetPledgeStateById(pledgeId))!!
        val remotePledgeRef = subFlow(GetExternalStateAndRefByLinearId(pledgeStatusLinearId))
        val remotePledge = parseExternalStateForPledgeStatus(remotePledgeRef.state.data)
        val borrowerCert = Base64.getEncoder().encodeToString(x509CertToPem(ourIdentityAndCert.certificate).toByteArray())
        val lender = localPledgeRef.state.data.locker
        val lenderCert = Base64.getEncoder().encodeToString(x509CertToPem(getPartyCertificate(lender, serviceHub)).toByteArray())
        
        val pledgeConditionFromTokenLedger = Gson().fromJson(remotePledge.pledgeCondition.toStringUtf8(), LoanRepaymentCondition::class.java)
        val pledgeConditionFromAssetLedger = Gson().fromJson(ByteString.copyFrom(localPledgeRef.state.data.pledgeCondition).toStringUtf8(), LoanRepaymentCondition::class.java)
        
        val claimAssetState = subFlow(UpdateBondAssetOwnerFromPointer(
            localPledgeRef.state.data.assetStatePointer!! as StaticPointer<BondAssetState>
        ))
        
        var currentTimeSecs: Long
        val calendar = Calendar.getInstance()
        currentTimeSecs = calendar.timeInMillis / 1000
        
        val networkIdStateRef = subFlow(RetrieveNetworkIdStateAndRef())
        val fetchedNetworkIdState = networkIdStateRef!!.state.data
        
        if (pledgeConditionFromAssetLedger != pledgeConditionFromTokenLedger) {
            println("Repayment condition doesn't match in asset and token pledges")
            Left(Error("Repayment condition doesn't match in asset and token pledges")) 
        } else if (currentTimeSecs >= localPledgeRef.state.data.expiryTimeSecs) {
            println("Cannot claim loaned asset with pledgeId ${pledgeId} as the expiry time has elapsed.")
            Left(Error("Cannot claim loaned asset with pledged ${pledgeId} as the expiry time has elapsed."))
        } else if (pledgeConditionFromTokenLedger.assetLedgerLenderCert != lenderCert) {
            println("Cannot claim loaned asset with pledgeId ${pledgeId} as it has not been pledged to the the lender.")
            Left(Error("Cannot claim loaned asset with pledged ${pledgeId} as it has not been pledged to the lender."))
        } else if (pledgeConditionFromTokenLedger.assetLedgerBorrowerCert != borrowerCert) {
            println("Cannot claim loaned asset with pledgeId ${pledgeId} as it has not been pledged by the the borrower.")
            Left(Error("Cannot claim loaned asset with pledged ${pledgeId} as it has not been pledged by the the borrower."))
        } else if (fetchedNetworkIdState.networkId != remotePledge.remoteNetworkID) {
            println("Cannot claim loaned asset with pledgeId ${pledgeId} as it has not been pledged to a claimer in this network.")
            Left(Error("Cannot claim loaned asset with pledged ${pledgeId} as it has not been pledged to a claimer in this network."))
        }
        
        val marshalledBondAsset = getBondAssetJsonStringFromStatePointer(localPledgeRef.state.data, serviceHub)
        
        val assetClaimStatusState = AssetClaimStatusState(
            pledgeId,
            // must have used copyFromUtf8() at the time of serialization of the protobuf @property assetDetails
            marshalledBondAsset, // @property assetDetails
            fetchedNetworkIdState.networkId, // @property localNetworkID
            remotePledge.localNetworkID, // @property remoteNetworkID
            ourIdentity, // @property recipient
            borrowerCert,
            true, // @property claimStatus
            remotePledge.expiryTimeSecs, // @property expiryTimeSecs
            false, // @property expirationStatus
            localPledgeRef.state.data.pledgeCondition
        )
        
        val notary = networkIdStateRef.state.notary
        val claimCmd = Command(AssetTransferContract.Commands.ClaimRemoteAsset(),
            listOf(
                ourIdentity.owningKey
            )
        )
        val assetCreateCmd = Command(BondAssetContract.Commands.Issue(),
            setOf(
                ourIdentity.owningKey,
                issuer.owningKey
            ).toList()
        )
        
        val txBuilder = TransactionBuilder(notary)
            .addInputState(localPledgeRef)
            .addInputState(remotePledgeRef)
            .addOutputState(claimAssetState, BondAssetContract.ID)
            .addOutputState(assetClaimStatusState, AssetTransferContract.ID)
            .addCommand(claimCmd)
            .addReferenceState(ReferencedStateAndRef(networkIdStateRef))
            .addCommand(assetCreateCmd)
            .setTimeWindow(TimeWindow.fromOnly(Instant.ofEpochSecond(localPledgeRef.state.data.expiryTimeSecs).plusNanos(1)))
        
        // Verify and collect signatures on the transaction
        txBuilder.verify(serviceHub)
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)
        println("Recipient signed transaction.")

        var sessions = listOf<FlowSession>()

        if (!ourIdentity.equals(issuer)) {
            val issuerSession = initiateFlow(issuer)
            issuerSession.send(AssetLoanResponderRole.SIGNER)
            sessions += issuerSession
        }

        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions))
        
        var observerSessions = listOf<FlowSession>()
        
        val lockerSession = initiateFlow(lender)
        lockerSession.send(AssetLoanResponderRole.OBSERVER)
        observerSessions += lockerSession

        for (obs in observers) {
            val obsSession = initiateFlow(obs)
            obsSession.send(AssetLoanResponderRole.OBSERVER)
            observerSessions += obsSession
        }
        Right(subFlow(FinalityFlow(fullySignedTx, sessions + observerSessions)))
    } catch (e: Exception) {
        println("Error in claiming the loaned asset: ${e.message}\n")
        Left(Error("Failed to claim loaned asset: ${e.message}"))
    }
}
@InitiatedBy(ClaimLoanedAssetInitiator::class)
class ClaimLoanedAssetAcceptor(val session: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val role = session.receive<AssetLoanResponderRole>().unwrap { it }
        if (role == AssetLoanResponderRole.SIGNER) {
            val signTransactionFlow = object : SignTransactionFlow(session) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val tx = stx.tx.toLedgerTransaction(serviceHub)
                    // Generic constraints around the transaction that transfers ownership of an asset from one Party to other Party
                    "There should be one input AssetPledgeState." using (tx.inputsOfType<AssetPledgeState>().size == 1)
                    "One output state only should be created." using (tx.outputsOfType<BondAssetState>().size == 1)
                    val pledgeState = tx.inputsOfType<AssetPledgeState>()[0]
                    val assetState = tx.outputsOfType<BondAssetState>()[0]
                    val remotePledgeStatus = parseExternalStateForPledgeStatus(
                        tx.inputsOfType<ExternalState>()[0]
                    )
                    
                    /*
                    * - if repayment amount is correct
                    * - repayment is for correct asset id
                    * - output loaned asset state is same as the one in input pledge state
                    * - output loaned asset state is same as asset in claim status
                    */
                    
                    val assetPledged = pledgeState.assetStatePointer!!.resolve(tx).state.data as BondAssetState
                    
                    "Asset pledged and output asset should be same" using (assetState.id == assetPledged.id
                        && assetState.type == assetPledged.type
                        && assetState.linearId == assetPledged.linearId
                    )
                    
                    val claimState = tx.outputsOfType<AssetClaimStatusState>()[0]
                    val marshalledAsset = Gson().fromJson(claimState.assetDetails, BondAssetStateJSON::class.java)
                    
                    "Asset claimed and output asset should be same" using (assetState.id == marshalledAsset.id
                        && assetState.type == marshalledAsset.type
                    )
                    
                    // Here assuming that when pledges were created 
                    // it was verified that correct asset is present in pledgeCondition
                    val pledgeConditionFromAssetLedger = Gson().fromJson(ByteString.copyFrom(pledgeState.pledgeCondition).toStringUtf8(), LoanRepaymentCondition::class.java)
                    val pledgeConditionFromTokenLedger = Gson().fromJson(remotePledgeStatus.pledgeCondition.toStringUtf8(), LoanRepaymentCondition::class.java)
                    "Pledge condition on both pledges should match" using (pledgeConditionFromAssetLedger == pledgeConditionFromTokenLedger)
                    
                    val borrowerCert = Base64.getEncoder().encodeToString(x509CertToPem(getPartyCertificate(pledgeState.locker, serviceHub)).toByteArray())
                    val lenderCert = Base64.getEncoder().encodeToString(x509CertToPem(getPartyCertificate(pledgeState.recipient!!, serviceHub)).toByteArray())
                    
                    "Borrower and Lender parties of Asset in pledgeCondition should be correct" using (pledgeConditionFromTokenLedger.assetLedgerLenderCert == lenderCert && pledgeConditionFromTokenLedger.assetLedgerBorrowerCert == borrowerCert)
                }
            }
            try {
                val txId = subFlow(signTransactionFlow).id
                println("Issuer signed transaction.")
                return subFlow(ReceiveFinalityFlow(session, expectedTxId = txId))
            } catch (e: Exception) {
                println("Error signing claim asset transaction by issuer: ${e.message}\n")
                return subFlow(ReceiveFinalityFlow(session))
            }
        } else if (role == AssetLoanResponderRole.OBSERVER) {
            val sTx = subFlow(ReceiveFinalityFlow(session, statesToRecord = StatesToRecord.ALL_VISIBLE))
            println("Received Tx: ${sTx} and recorded states.")
            return sTx
        } else {
            println("Incorrect Responder Role.")
            throw IllegalStateException("Incorrect Responder Role.")
        }
    }
}

/**
 *
 */
@InitiatingFlow
@StartableByRPC
class ClaimLoanRepaymentInitiator
@JvmOverloads
constructor(
    val pledgeId: String,
    val claimStatusLinearId: String,
    val issuer: Party,
    val observers: List<Party> = listOf<Party>()
) : FlowLogic<Either<Error, SignedTransaction>>() {
    @Suspendable
    override fun call(): Either<Error, SignedTransaction> = try {
        /*
        1. Fetch the pledge state using pledgeId
        2. Fetch the remote claim state (step 7) using claimStatusLinearId
        3. Create a transaction:
            a. input: pledge state of step 6
            b. output: - Repayment fungible asset state with owner as lender
                       - claimStatusState
            c. reference state: remote claim state
            d. contracts:
                i. ClaimRemoteAsset contract
                ii. contract which checks if repayment amount is correct
            e. Expiry time in time window.
        4. Borrower and Issuer should be signer
        */
        val localPledgeRef = subFlow(GetAssetPledgeStateById(pledgeId))!!
        val remoteClaimStatusRef = subFlow(GetExternalStateAndRefByLinearId(claimStatusLinearId))
        val remoteClaimStatus = parseExternalStateForClaimStatus(remoteClaimStatusRef.state.data)
        val remotePledgeCondition = Gson().fromJson(remoteClaimStatus.pledgeCondition.toStringUtf8(), LoanRepaymentCondition::class.java)
        
        
        val claimAssetState = subFlow(UpdateAssetOwnerFromPointer(
            localPledgeRef.state.data.assetStatePointer!! as StaticPointer<AssetState>
        ))
        
        var currentTimeSecs: Long
        val calendar = Calendar.getInstance()
        currentTimeSecs = calendar.timeInMillis / 1000
        
        val networkIdStateRef = subFlow(RetrieveNetworkIdStateAndRef())
        val fetchedNetworkIdState = networkIdStateRef!!.state.data
        
        val lenderCert = Base64.getEncoder().encodeToString(x509CertToPem(ourIdentityAndCert.certificate).toByteArray())
        val borrower = localPledgeRef.state.data.locker
        val borrowerCert = Base64.getEncoder().encodeToString(x509CertToPem(getPartyCertificate(borrower, serviceHub)).toByteArray())
        
        
        if (currentTimeSecs >= localPledgeRef.state.data.expiryTimeSecs) {
            println("Cannot claim loaned asset with pledgeId ${pledgeId} as the expiry time has elapsed.")
            Left(Error("Cannot claim loaned asset with pledged ${pledgeId} as the expiry time has elapsed."))
        } else if (remotePledgeCondition.tokenLedgerLenderCert != lenderCert) {
            println("Cannot claim loaned asset with pledgeId ${pledgeId} as it has not been pledged to the the lender.")
            Left(Error("Cannot claim loaned asset with pledged ${pledgeId} as it has not been pledged to the lender."))
        } else if (remotePledgeCondition.tokenLedgerBorrowerCert != borrowerCert) {
            println("Cannot claim loaned asset with pledgeId ${pledgeId} as it has not been pledged by the the borrower.")
            Left(Error("Cannot claim loaned asset with pledged ${pledgeId} as it has not been pledged by the the borrower."))
        } else if (fetchedNetworkIdState.networkId != remoteClaimStatus.remoteNetworkID) {
            println("Cannot claim loaned asset with pledgeId ${pledgeId} as it has not been pledged to a claimer in this network.")
            Left(Error("Cannot claim loaned asset with pledged ${pledgeId} as it has not been pledged to a claimer in this network."))
        }
        
        val marshalledAsset = getAssetJsonStringFromStatePointer(localPledgeRef.state.data, serviceHub)
        
        val assetClaimStatusState = AssetClaimStatusState(
            pledgeId,
            // must have used copyFromUtf8() at the time of serialization of the protobuf @property assetDetails
            marshalledAsset, // @property assetDetails
            fetchedNetworkIdState.networkId, // @property localNetworkID
            remoteClaimStatus.localNetworkID, // @property remoteNetworkID
            ourIdentity, // @property recipient
            lenderCert,
            true, // @property claimStatus
            remoteClaimStatus.expiryTimeSecs, // @property expiryTimeSecs
            false, // @property expirationStatus
            localPledgeRef.state.data.pledgeCondition
        )
        
        val notary = networkIdStateRef.state.notary
        val claimCmd = Command(AssetTransferContract.Commands.ClaimRemoteAsset(),
            listOf(
                ourIdentity.owningKey
            )
        )
        val assetCreateCmd = Command(AssetContract.Commands.Issue(),
            setOf(
                ourIdentity.owningKey,
                issuer.owningKey
            ).toList()
        )
        
        val txBuilder = TransactionBuilder(notary)
            .addInputState(localPledgeRef)
            .addInputState(remoteClaimStatusRef)
            .addOutputState(claimAssetState, BondAssetContract.ID)
            .addOutputState(assetClaimStatusState, AssetTransferContract.ID)
            .addCommand(claimCmd)
            .addReferenceState(ReferencedStateAndRef(networkIdStateRef))
            .addCommand(assetCreateCmd)
            .setTimeWindow(TimeWindow.fromOnly(Instant.ofEpochSecond(localPledgeRef.state.data.expiryTimeSecs).plusNanos(1)))
        
        // Verify and collect signatures on the transaction
        txBuilder.verify(serviceHub)
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)
        println("Recipient signed transaction.")

        var sessions = listOf<FlowSession>()

        if (!ourIdentity.equals(issuer)) {
            val issuerSession = initiateFlow(issuer)
            issuerSession.send(AssetLoanResponderRole.SIGNER)
            sessions += issuerSession
        }

        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions))
        
        var observerSessions = listOf<FlowSession>()
        val lockerSession = initiateFlow(localPledgeRef.state.data.locker)
        lockerSession.send(AssetLoanResponderRole.OBSERVER)
        observerSessions += lockerSession

        for (obs in observers) {
            val obsSession = initiateFlow(obs)
            obsSession.send(AssetLoanResponderRole.OBSERVER)
            observerSessions += obsSession
        }
        Right(subFlow(FinalityFlow(fullySignedTx, sessions + observerSessions)))
    } catch (e: Exception) {
        println("Error in claiming the loaned asset: ${e.message}\n")
        Left(Error("Failed to claim loaned asset: ${e.message}"))
    }
}
@InitiatedBy(ClaimLoanRepaymentInitiator::class)
class ClaimLoanRepaymentAcceptor(val session: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val role = session.receive<AssetLoanResponderRole>().unwrap { it }
        if (role == AssetLoanResponderRole.SIGNER) {
            val signTransactionFlow = object : SignTransactionFlow(session) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val tx = stx.tx.toLedgerTransaction(serviceHub)
                    "There should be one input AssetPledgeState." using (tx.inputsOfType<AssetPledgeState>().size == 1)
                    "One output state only should be created." using (tx.outputsOfType<AssetState>().size == 1)
                    val pledgeState = tx.inputsOfType<AssetPledgeState>()[0]
                    val assetState = tx.outputsOfType<AssetState>()[0]
                    val remoteClaimStatus = parseExternalStateForClaimStatus(
                        tx.inputsOfType<ExternalState>()[0]
                    )
                    
                    /*
                    * - if repayment amount is correct
                    * - repayment is for correct asset id
                    * - output loaned asset state is same as the one in input pledge state
                    * - output loaned asset state is same as asset in claim status
                    */
                    
                    val assetPledged = pledgeState.assetStatePointer!!.resolve(tx).state.data as AssetState
                    
                    "Asset pledged and output asset should be same" using (assetState.quantity == assetPledged.quantity
                        && assetState.tokenType == assetPledged.tokenType
                    )
                    
                    val claimState = tx.outputsOfType<AssetClaimStatusState>()[0]
                    val marshalledAsset = Gson().fromJson(claimState.assetDetails, AssetStateJSON::class.java)
                    
                    "Asset claimed and output asset should be same" using (assetState.quantity == marshalledAsset.quantity
                        && assetState.tokenType == marshalledAsset.tokenType
                    )
                    
                    // Here assuming that when pledges were created 
                    // it was verified that correct asset is present in pledgeCondition
                    val pledgeCondition = Gson().fromJson(ByteString.copyFrom(pledgeState.pledgeCondition).toStringUtf8(), LoanRepaymentCondition::class.java)
                    val remotePledgeCondition = Gson().fromJson(remoteClaimStatus.pledgeCondition.toStringUtf8(), LoanRepaymentCondition::class.java)
                    "Pledge condition on both pledges should match" using (pledgeCondition == remotePledgeCondition)
                    
                    val borrowerCert = Base64.getEncoder().encodeToString(x509CertToPem(getPartyCertificate(pledgeState.recipient!!, serviceHub)).toByteArray())
                    val lenderCert = Base64.getEncoder().encodeToString(x509CertToPem(getPartyCertificate(pledgeState.locker, serviceHub)).toByteArray())
                    
                    "Borrower and Lender parties of Token in pledgeCondition should be correct" using (remotePledgeCondition.tokenLedgerLenderCert == lenderCert && remotePledgeCondition.tokenLedgerBorrowerCert == borrowerCert)
                }
            }
            try {
                val txId = subFlow(signTransactionFlow).id
                println("Issuer signed transaction.")
                return subFlow(ReceiveFinalityFlow(session, expectedTxId = txId))
            } catch (e: Exception) {
                println("Error signing claim asset transaction by issuer: ${e.message}\n")
                return subFlow(ReceiveFinalityFlow(session))
            }
        } else if (role == AssetLoanResponderRole.OBSERVER) {
            val sTx = subFlow(ReceiveFinalityFlow(session, statesToRecord = StatesToRecord.ALL_VISIBLE))
            println("Received Tx: ${sTx} and recorded states.")
            return sTx
        } else {
            println("Incorrect Responder Role.")
            throw IllegalStateException("Incorrect Responder Role.")
        }
    }
}


/**
 * The ReclaimPledgedAsset flow is used to reclaim an asset that was pledged earlier in the same (exporting) corda network.
 *
 * @property pledgeId The unique identifier representing the pledge on an asset for transfer, in the exporting n/w.
 * @property claimStatusLinearId The unique identifier of the vault state representing asset claim details fetched via interop-query.
 * @property issuer The issuing authority of the pledged fungible asset, if applicable (e.g., fungible house token).
 *                      Otherwise this will be same as the party submitting the transaction.
 * @property observers The parties who are not transaction participants but only observers (can be empty list).
 */
@InitiatingFlow
@StartableByRPC
class ReclaimPledgedLoanedAssetInitiator
@JvmOverloads
constructor(
    val pledgeId: String,
    val claimStatusLinearId: String,
    val issuer: Party,
    val observers: List<Party> = listOf<Party>()
) : FlowLogic<Either<Error, SignedTransaction>>() {
    /**
     * The call() method captures the logic to create a new asset state (that was redeemed earlier via a 
     * pledge transaction) by consumes the [AssetPledgeState].
     *
     * @return Returns SignedTransaction.
     */
    @Suspendable
    override fun call(): Either<Error, SignedTransaction> = try {
        val linearId = getLinearIdFromString(pledgeId)

        val externalStateAndRef = subFlow(GetExternalStateAndRefByLinearId(claimStatusLinearId))
        val viewData = subFlow(GetExternalStateByLinearId(claimStatusLinearId))
        val externalStateView = ViewDataOuterClass.ViewData.parseFrom(viewData)
        val payloadDecoded = Base64.getDecoder().decode(externalStateView.notarizedPayloadsList[0].payload.toByteArray())
        val assetClaimStatus = AssetTransfer.AssetClaimStatus.parseFrom(payloadDecoded)
        println("Asset claim status details obtained via interop query: ${assetClaimStatus}")
        

        val assetPledgeStateAndRefs = serviceHub.vaultService.queryBy<AssetPledgeState>(
            QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        ).states
        if (assetPledgeStateAndRefs.isEmpty()) {
            println("AssetPledgeState for Id: ${linearId} not found.")
            Left(Error("AssetPledgeState for Id: ${linearId} not found."))
        } else {
            val assetPledgeStateAndRef: StateAndRef<AssetPledgeState> = assetPledgeStateAndRefs.first()
            val assetPledgeState: AssetPledgeState = assetPledgeStateAndRef.state.data
            println("Party: ${ourIdentity} ReclaimPledgeState: ${assetPledgeState}")

            if (assetClaimStatus.expiryTimeSecs != assetPledgeState.expiryTimeSecs) {
                println("Cannot perform reclaim for pledgeId $pledgeId as the expiration timestamps in the pledge and the claim don't match.")
                Left(Error("Cannot perform reclaim for pledged $pledgeId as the expiration timestamps in the pledge and the claim don't match."))
            } else if (assetClaimStatus.claimStatus) {
                println("Cannot perform reclaim for pledgeId $pledgeId as the asset was claimed in remote network.")
                Left(Error("Cannot perform reclaim for pledged $pledgeId as the asset was claimed in remote network."))
            } else if (!assetClaimStatus.expirationStatus) {
                println("Cannot perform reclaim for pledgeId $pledgeId as the asset pledge has not yet expired.")
                Left(Error("Cannot perform reclaim for pledged $pledgeId as the asset pledge has not yet expired."))
            } else {
                val notary = assetPledgeStateAndRef.state.notary
                val reclaimCmd = Command(AssetTransferContract.Commands.ReclaimPledgedAsset(),
                    listOf(
                        assetPledgeState.locker.owningKey
                    )
                )
                val assetCreateCmd = Command(BondAssetContract.Commands.Issue(),
                    setOf(
                        assetPledgeState.locker.owningKey,
                        issuer.owningKey
                    ).toList()
                )

                val networkIdStateRef = subFlow(RetrieveNetworkIdStateAndRef())

                // Typically, when [AssetPledgeState].assetStatePointer is null, that means the pledge details
                // are not available on the ledger and the execution will return in the if block above.
                val reclaimAssetStateAndRef = assetPledgeState.assetStatePointer!!.resolve(serviceHub)
                val reclaimAssetStateOld = reclaimAssetStateAndRef.state.data as BondAssetState
                
                val pledgeCondition = Gson().fromJson(ByteString.copyFrom(assetPledgeState.pledgeCondition).toStringUtf8(), LoanRepaymentCondition::class.java)
                if (pledgeCondition.assetType != reclaimAssetStateOld.type && pledgeCondition.assetId != reclaimAssetStateOld.id) {
                    println("Cannot perform reclaim for pledgeId $pledgeId as the asset is not pledged for loan.")
                    Left(Error("Cannot perform reclaim for pledged $pledgeId as the asset is not pledged for loan."))
                } else {
                    val reclaimAssetState = reclaimAssetStateOld.copy(owner=ourIdentity)
                    val txBuilder = TransactionBuilder(notary)
                        .addInputState(assetPledgeStateAndRef)
                        .addInputState(externalStateAndRef)
                        .addOutputState(reclaimAssetState, BondAssetContract.ID)
                        .addCommand(reclaimCmd).apply {
                            networkIdStateRef!!.let {
                                this.addReferenceState(ReferencedStateAndRef(networkIdStateRef))
                            }
                        }
                        .addCommand(assetCreateCmd)
                        .setTimeWindow(TimeWindow.fromOnly(Instant.ofEpochSecond(assetPledgeState.expiryTimeSecs).plusNanos(1)))

                    // Verify and collect signatures on the transaction
                    txBuilder.verify(serviceHub)
                    var partSignedTx = serviceHub.signInitialTransaction(txBuilder)
                    println("${ourIdentity} signed transaction.")

                    var sessions = listOf<FlowSession>()

                    if (!ourIdentity.equals(issuer)) {
                        val issuerSession = initiateFlow(issuer)
                        issuerSession.send(AssetLoanResponderRole.SIGNER)
                        sessions += issuerSession
                    }
                    val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions))

                    var observerSessions = listOf<FlowSession>()
                    val borrowerSession = initiateFlow(reclaimAssetStateOld.owner)
                    borrowerSession.send(AssetLoanResponderRole.BORROWER)
                    observerSessions += borrowerSession
                    for (obs in observers) {
                        val obsSession = initiateFlow(obs)
                        obsSession.send(AssetLoanResponderRole.OBSERVER)
                        observerSessions += obsSession
                    }
                    Right(subFlow(FinalityFlow(fullySignedTx, sessions + observerSessions)))
                }
            }
        }
    } catch (e: Exception) {
        println("Error in reclaiming the pledged asset: ${e.message}\n")
        Left(Error("Failed to reclaim the pledged asset: ${e.message}"))
    }
}

@InitiatedBy(ReclaimPledgedLoanedAssetInitiator::class)
class ReclaimPledgedLoanedAssetAcceptor(val session: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val role = session.receive<AssetLoanResponderRole>().unwrap { it }
        if (role == AssetLoanResponderRole.SIGNER) {
            val signTransactionFlow = object : SignTransactionFlow(session) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                }
            }
            try {
                val txId = subFlow(signTransactionFlow).id
                println("Issuer signed transaction.")
                return subFlow(ReceiveFinalityFlow(session, expectedTxId = txId))
            } catch (e: Exception) {
                println("Error signing reclaim asset transaction by Issuer: ${e.message}\n")
                return subFlow(ReceiveFinalityFlow(session))
            }
        } else if (role == AssetLoanResponderRole.BORROWER) {
            val signTransactionFlow = object : SignTransactionFlow(session) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                }
            }
            try {
                val txId = subFlow(signTransactionFlow).id
                println("Locker signed transaction.")
                return subFlow(ReceiveFinalityFlow(session, expectedTxId = txId))
            } catch (e: Exception) {
                println("Error signing reclaim asset transaction by Locker: ${e.message}\n")
                return subFlow(ReceiveFinalityFlow(session))
            }
        } else if (role == AssetLoanResponderRole.OBSERVER) {
            val sTx = subFlow(ReceiveFinalityFlow(session, statesToRecord = StatesToRecord.ALL_VISIBLE))
            println("Received Tx: ${sTx}")
            return sTx
        } else {
            println("Incorrect Responder Role.")
            throw IllegalStateException("Incorrect Responder Role.")
        }
    }
}

/*
TODO:
1. Use external state as reference state in Asset Transfer Claim (move the checks from workflow to contract)
2. Change inputs.size and outputs.size to support general transactions.
3. Add pledgeCondition field in both PledgeState and ClaimStatusState
*/

fun getLinearIdFromString(linearId: String): UniqueIdentifier {
    val id = linearId.split("_").toTypedArray()
    if (id.size != 2) {
        // here the id[1] denotes an UUID and id[0] denotes its hash
        println("Invalid linearId: ${linearId}.\n")
        throw IllegalStateException("Invalid linearId: ${linearId} as per HTLC or Asset-Transfer Corda protocol.\n")
    }
    return UniqueIdentifier(externalId=id[0], id = UniqueIdentifier.fromString(id[1]).id)
}

/*
 * The x509CertToPem function extracts the PEM (privacy enhanced mail) certificate from the input X509Certificate
 */
fun x509CertToPem(cert: X509Certificate): String {
    val cert_begin = "-----BEGIN CERTIFICATE-----\n";
    val end_cert = "\n-----END CERTIFICATE-----";

    val derCert = cert.getEncoded();
    val pemCertPre = Base64.getEncoder().encodeToString(derCert).replace("(.{64})".toRegex(), "$1\n")
    val pemCert = cert_begin + pemCertPre + end_cert;
    return pemCert;
}

fun getPartyCertificate(party: Party, serviceHub: ServiceHub): X509Certificate {
    return serviceHub.networkMapCache.getNodeByLegalIdentity(party)!!.legalIdentitiesAndCerts.first().certificate
}

