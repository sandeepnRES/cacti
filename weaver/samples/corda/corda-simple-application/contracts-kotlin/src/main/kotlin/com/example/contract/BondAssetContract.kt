/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cordaSimpleApplication.contract

import com.cordaSimpleApplication.state.BondAssetState
import com.cordaSimpleApplication.state.BondAssetStateJSON
import com.cordaSimpleApplication.state.LoanRepaymentCondition
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import com.google.gson.Gson
import com.google.protobuf.ByteString

import org.hyperledger.cacti.weaver.imodule.corda.states.AssetPledgeState
import org.hyperledger.cacti.weaver.imodule.corda.states.AssetExchangeHTLCState
import org.hyperledger.cacti.weaver.imodule.corda.states.NetworkIdState

/**
 * An implementation of a sample bond (non-fungible) asset in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [BondAssetState], and operations on [BondAssetState].
 *
 * For a new [BondAssetState] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [BondAssetState].
 * - An Create() command with the public keys of the owner of the asset.
 *
 */
class BondAssetContract : Contract {
    companion object {
        @JvmStatic
        val ID = "com.cordaSimpleApplication.contract.BondAssetContract"
    }

    /**
     * The verify() function of any of the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<BondAssetContract.Commands>()
        when (command.value) {
            is BondAssetContract.Commands.Issue -> requireThat {
                // Generic constraints around the fungible token asset issuance transaction.
                "No inputs should be consumed when issuing an asset." using (tx.inputsOfType<BondAssetState>().isEmpty())
                "Only one output state should be created." using (tx.outputsOfType<BondAssetState>().size == 1)
                val outputState = tx.outputsOfType<BondAssetState>().single()
                val requiredSigners = outputState.participants.map { it.owningKey }
                "The participants must be the signers." using (command.signers.containsAll(requiredSigners))
            }
            is BondAssetContract.Commands.Delete -> requireThat {
                // Generic constraints around the asset deletion transaction
                "Only one input state should be consumed with deletion of an asset." using (tx.inputsOfType<BondAssetState>().size == 1)
                "No output state should be created." using (tx.outputsOfType<BondAssetState>().isEmpty())
                val inputState = tx.inputsOfType<BondAssetState>()[0]
                val requiredSigners = listOf(inputState.owner.owningKey)
                "The asset owner must be the signer." using (command.signers.containsAll(requiredSigners))
            }
            is BondAssetContract.Commands.Transfer -> requireThat {
                // Generic constraints around the transaction that transfers ownership of an asset from one Party to other Party
                "One input state should be consumed for transferring." using (tx.inputsOfType<BondAssetState>().size == 1)
                val inputState = tx.inputsOfType<BondAssetState>()[0]
                "One output state only should be created." using (tx.outputsOfType<BondAssetState>().size == 1)
                val outputState = tx.outputsOfType<BondAssetState>()[0]
                "The input and output states part of the transfer should have the same id." using (inputState.id == outputState.id)
                "The input and output states part of the transfer should be of same bond/non-fungible asset type." using (inputState.type == outputState.type)
                "The input and output states part of the transfer should not belong to the same owner." using (inputState.owner != outputState.owner)
                val requiredSigners = listOf(inputState.owner.owningKey, outputState.owner.owningKey)
                "The owners of the input and output assets must be the signers." using (command.signers.containsAll(requiredSigners))
            }
            is BondAssetContract.Commands.LoanPledge -> requireThat {
                // Generic constraints around the transaction that transfers ownership of an asset from one Party to other Party
                val assetStates = tx.inputsOfType<BondAssetState>()
                val htlcStates = tx.inputsOfType<AssetExchangeHTLCState>()
                "There should be either BondAssetState or HTLC State as input." using (assetStates.size == 1 || htlcStates.size == 1)
                "There should be one output AssetPledgeState." using (tx.outputsOfType<AssetPledgeState>().size == 1)
                
                val assetState = if (assetStates.size == 1) assetStates[0] else htlcStates[0].assetStatePointer.resolve(tx).state.data as BondAssetState
                val pledgeState = tx.outputsOfType<AssetPledgeState>()[0]
                val pledgeCondition = Gson().fromJson(ByteString.copyFrom(pledgeState.pledgeCondition).toStringUtf8(), LoanRepaymentCondition::class.java)
                
                "Pledge asset should be same as in pledge condition." using (assetState.id == pledgeCondition.assetId
                    && assetState.type == pledgeCondition.assetType
                )
                
                "Lender should be the pledger in pledge condition." using (pledgeState.lockerCert == pledgeCondition.assetLedgerLenderCert)
                "Token Lender should be the recipient in pledge condition." using (pledgeState.recipientCert == pledgeCondition.tokenLedgerLenderCert)
                
                val inReferences = tx.referenceInputRefsOfType<NetworkIdState>()
                "There should be a single reference input network id." using (inReferences.size == 1)

                val validNetworkIdState = inReferences.get(0).state.data
                "Asset ledger should be correct in pledge condition." using (pledgeCondition.assetLedgerId.equals(validNetworkIdState.networkId))
            }
        }
    }

    /**
     * This contract implements the commands: Issue, Delete and Transfer.
     */
    interface Commands : CommandData {
        class Issue : Commands
        class Delete : Commands
        class Transfer : Commands
        class LoanPledge: Commands
    }
}
