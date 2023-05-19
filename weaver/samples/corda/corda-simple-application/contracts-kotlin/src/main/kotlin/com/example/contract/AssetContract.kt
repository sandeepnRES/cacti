/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cordaSimpleApplication.contract

import com.cordaSimpleApplication.state.AssetState
import com.cordaSimpleApplication.state.AssetStateJSON
import com.cordaSimpleApplication.state.LoanRepaymentCondition
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import org.hyperledger.cacti.weaver.imodule.corda.contracts.parseExternalStateForClaimStatus
import com.google.gson.Gson
import com.google.protobuf.ByteString

import org.hyperledger.cacti.weaver.imodule.corda.states.ExternalState
import org.hyperledger.cacti.weaver.imodule.corda.states.AssetPledgeState
import org.hyperledger.cacti.weaver.imodule.corda.states.AssetClaimStatusState
import org.hyperledger.cacti.weaver.imodule.corda.states.NetworkIdState
import org.hyperledger.cacti.weaver.imodule.corda.contracts.parseExternalStateForPledgeStatus

/**
 * An implementation of a sample asset in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [AssetState], and operations on [AssetState].
 *
 * For a new [AssetState] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [AssetState].
 * - An Create() command with the public keys of the owner of the asset.
 *
 */
class AssetContract : Contract {
    companion object {
        @JvmStatic
        val ID = "com.cordaSimpleApplication.contract.AssetContract"
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<AssetContract.Commands>()
        when (command.value) {
            is AssetContract.Commands.Issue -> requireThat {
                // Generic constraints around the fungible token asset issuance transaction.
                "No inputs should be consumed when issuing an asset." using (tx.inputsOfType<AssetState>().isEmpty())
                "Only one output state should be created." using (tx.outputsOfType<AssetState>().size == 1)
                val outputState = tx.outputsOfType<AssetState>().single()
                val requiredSigners = outputState.participants.map { it.owningKey }
                "The participants must be the signers." using (command.signers.containsAll(requiredSigners))
            }
            is AssetContract.Commands.Delete -> requireThat {
                // Generic constraints around the asset deletion transaction
                "Only one input state should be consumed with deletion of an asset." using (tx.inputsOfType<AssetState>().size == 1)
                "No output state should be created." using (tx.outputsOfType<AssetState>().isEmpty())
                val inputState = tx.inputsOfType<AssetState>()[0]
                val requiredSigners = listOf(inputState.owner.owningKey)
                "The asset owner must be the signer." using (command.signers.containsAll(requiredSigners))
            }
            is AssetContract.Commands.Merge -> requireThat {
                // Generic constraints around the transaction that merges two asset states into one
                "Two input states should be consumed for merging." using (tx.inputsOfType<AssetState>().size == 2)
                val inputState1 = tx.inputsOfType<AssetState>()[0]
                val inputState2 = tx.inputsOfType<AssetState>()[1]
                "Both assets to be merged should belong to the same owner." using (inputState1.owner == inputState2.owner)
                "Both assets to be merged should be of same token type." using (inputState1.tokenType == inputState2.tokenType)
                "Only one output state should be created." using (tx.outputsOfType<AssetState>().size == 1)
                val mergedState = tx.outputsOfType<AssetState>().single()
                val requiredSigners = mergedState.participants.map { it.owningKey }
                "The participants must be the signers." using (command.signers.containsAll(requiredSigners))
                "The output state should belong to the same owner as the input states." using (inputState1.owner == mergedState.owner)
                "The number of fungible asset tokens before and after merge should be same." using (inputState1.quantity + inputState2.quantity == mergedState.quantity)
                "The merged asset token type should be same as the input asset token type." using (inputState1.tokenType == mergedState.tokenType)
            }
            is AssetContract.Commands.Split -> requireThat {
                // Generic constraints around the transaction that splits an asset state into two asset states
                "One input state should be consumed for splitting." using (tx.inputsOfType<AssetState>().size == 1)
                val splitState = tx.inputsOfType<AssetState>()[0]
                "Two output states should be created." using (tx.outputsOfType<AssetState>().size == 2)
                val outputState1 = tx.outputsOfType<AssetState>()[0]
                val outputState2 = tx.outputsOfType<AssetState>()[1]
                "Both assets generated by split should belong to the same owner." using (outputState1.owner == outputState2.owner)
                "Both assets generated by split should of the same token type." using (outputState1.tokenType == outputState2.tokenType)
                val requiredSigners = outputState1.participants.map { it.owningKey }
                "The participants must be the signers." using (command.signers.containsAll(requiredSigners))
                "The output states should belong to the same owner as the input states." using (splitState.owner == outputState1.owner)
                "The number of fungible asset tokens before and after split should be same." using (splitState.quantity == outputState1.quantity + outputState2.quantity)
                "The asset token type to be split should be same as the output assets' token type." using (splitState.tokenType == outputState1.tokenType)
            }
            is AssetContract.Commands.Transfer -> requireThat {
                // Generic constraints around the transaction that transfers ownership of an asset from one Party to other Party
                "One input state should be consumed for transferring." using (tx.inputsOfType<AssetState>().size == 1)
                val inputState = tx.inputsOfType<AssetState>()[0]
                "One output state only should be created." using (tx.outputsOfType<AssetState>().size == 1)
                val outputState = tx.outputsOfType<AssetState>()[0]
                "The input and output states part of the transfer should have the same quantity." using (inputState.quantity == outputState.quantity)
                "The input and output states part of the transfer should be of same token type." using (inputState.tokenType == outputState.tokenType)
                "The input and output states part of the transfer should not belong to the same owner." using (inputState.owner != outputState.owner)
                val requiredSigners = listOf(inputState.owner.owningKey, outputState.owner.owningKey)
                "The owners of the input and output assets must be the signers." using (command.signers.containsAll(requiredSigners))
            }
            is AssetContract.Commands.LoanPledge -> requireThat {
                // Generic constraints around the transaction that transfers ownership of an asset from one Party to other Party
                val assetStates = tx.inputsOfType<AssetState>()
                "There should be oneAssetState as input." using (assetStates.size == 1)
                "There should be one output AssetPledgeState." using (tx.outputsOfType<AssetPledgeState>().size == 1)
                
                val assetState = assetStates[0]
                val pledgeState = tx.outputsOfType<AssetPledgeState>()[0]
                val pledgeCondition = Gson().fromJson(ByteString.copyFrom(pledgeState.pledgeCondition).toStringUtf8(), LoanRepaymentCondition::class.java)
                
                "Pledge asset should be same as in pledge condition." using (assetState.quantity == pledgeCondition.tokenQuantity
                    && assetState.tokenType == pledgeCondition.tokenType
                )
                
                "Borrower should be the pledger in pledge condition." using (pledgeState.lockerCert == pledgeCondition.borrowerCert)
                "Lender should be the recipient in pledge condition." using (pledgeState.recipientCert == pledgeCondition.lenderCert)
                
                val inReferences = tx.referenceInputRefsOfType<NetworkIdState>()
                "There should be a single reference input network id." using (inReferences.size == 1)

                val validNetworkIdState = inReferences.get(0).state.data
                "Asset ledger should be correct in pledge condition." using (pledgeCondition.tokenLedgerId.equals(validNetworkIdState.networkId))
                
                val requiredSigners = listOf(assetState.owner.owningKey)
                "The asset owner must be the signer." using (command.signers.containsAll(requiredSigners))
            }
            is AssetContract.Commands.LoanClaimRepayment -> requireThat {
                // Generic constraints around the transaction that transfers ownership of an asset from one Party to other Party
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
                val counterPledgeCondition = Gson().fromJson(remoteClaimStatus.pledgeCondition.toStringUtf8(), LoanRepaymentCondition::class.java)
                "Pledge condition on both pledges should match" using (pledgeCondition == counterPledgeCondition)
            }
        }
    }

    /**
     * This contract implements the commands: Issue, Delete, Merge, Split and Transfer.
     */
    interface Commands : CommandData {
        class Issue : Commands
        class Delete : Commands
        class Merge : Commands
        class Split : Commands
        class Transfer : Commands
        class LoanPledge: Commands
        class LoanClaimRepayment: Commands

        // Flow that will read the total fungible token assets of a given type
    }
}
