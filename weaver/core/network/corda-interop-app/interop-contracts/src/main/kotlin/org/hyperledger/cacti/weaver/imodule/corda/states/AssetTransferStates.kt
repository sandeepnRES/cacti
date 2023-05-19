/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.cacti.weaver.imodule.corda.states

import org.hyperledger.cacti.weaver.imodule.corda.contracts.AssetTransferContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.Instant
import net.corda.core.contracts.StaticPointer

/**
 * The AssetPledgeState stores the details about the pledge of an asset in the source/exporting network for asset transfer.
 *
 * The AssetPledgeState is generated while pledging an asset during asset transfer.
 * This state is used to create proof about pledge (as part of interop query) for the importing network while claiming the asset.
 *
 * @property assetStatePointer Pointer to asset state pledged for asset-transfer. If there is no asset pledged with a given
 *           pledgeId passed as part an interop-query, then an empty AssetPledgeState object (after setting the value 'null' set
 *           to this this property will be returned to the importing network.
 * @property locker The owner of asset before transfer in the exporting network who is pledging the asset.
 * @property lockerCert The certificate of the owner of asset in base64 form before transfer in the exporting network.
 * @property recipientCert Certificate of the asset recipient (in base64 format) in the importing network.
 * @property expiryTimeSecs The future time in epoch seconds till when the pledge on the asset holds good.
 * @property localNetworkId The id of the network in which the pledge is made.
 * @property remoteNetworkId The id of the network in which the pledged asset will be claimed.
 * @property recipient Asset recipient Party in the exporting network.
 * @property linearId The unique identifier for this state object in the vault.
 */
@BelongsToContract(AssetTransferContract::class)
data class AssetPledgeState(
    val assetStatePointer: StaticPointer<ContractState>?,
    val locker: Party,
    val lockerCert: String,
    val recipientCert: String,  // Remote
    val expiryTimeSecs: Long,
    val localNetworkId: String,
    val remoteNetworkId: String,
    val pledgeCondition: ByteArray,
    val recipient: Party? = null,
    override val linearId: UniqueIdentifier = UniqueIdentifier(assetStatePointer.hashCode().toString())
) : LinearState {
    // recipient is not a participant as that party may not be part of the exporting network
    override val participants: List<AbstractParty> get() = if (recipient == null) listOf(locker) else listOf(locker, recipient)
}

/*
 * Since there is a limit on the number of parameters to the workflow
 * This data class is used as parameter.
 */
@CordaSerializable
data class AssetPledgeParameters(
    var assetType: String,
    var assetIdOrQuantity: Any,
    var localNetworkId: String,
    var remoteNetworkId: String,
    var recipientCert: String,
    var expiryTimeSecs: Long,
    var getAssetStateAndRefFlow: String,
    var deleteAssetStateCommand: CommandData,
    val pledgeCondition: ByteArray,
    val recipient: Party? = null,
    var issuer: Party,
    var observers: List<Party>
)

/**
 * The AssetClaimStatusState stores the details about the asset claim status in the remote/importing network for asset transfer.
 *
 * The AssetClaimStatusState is created after performing the asset claim in the remote/importing newtork during asset transfer.
 * This state is queried by the local/exporting network to check if the asset is claimed in the remote/importing network. If the asset is
 * NOT claimed in the remote/importing network, then the asset will be reclaimed in the local/exporting network after the pledge timeout.
 *
 * @property pledgeId The unique identifier representing the pledge on an asset for transfer, in the exporting n/w.
 * @property assetDetails String containing the marshalled asset (using JSON encoding) being transferred.
 * @property localNetworkID The id of the network into which the asset is claimed (i.e., importing n/w).
 * @property remoteNetworkID The id of the network in which the asset is pledged (i.e., exporting n/w).
 * @property recipient The owner of the asset after transfer.
 * @property recipientCert Certificate of the asset recipient (in base64 format) in the importing network.
 * @property claimStatus Boolean value to convey if asset is claimed in the remote/importing network or not.
 * @property expiryTimeSecs The future time in epoch seconds before which the asset claim in the remote/importing network is to be performed.
 * @property expirationStatus Boolean variable to convey if the time in the remote/importing network is past expiryTimeSecs or not.
 * @property linearId The unique identifier for this state object in the vault.
 */
@BelongsToContract(AssetTransferContract::class)
data class AssetClaimStatusState(
    // Only if the pledge happens on a Corda network, the pledgeId can be used as the linearId by the importing Corda network.
    // However, the pledge might happen on a Fabric network with asset being imported into a Corda network.
    // In this scenario, the pledgeId generated by a Fabric network cannot be used as the linearId in Corda network.
    // Hence we need the pledgeId to be stored explicitly along with this state.
    val pledgeId: String,
    val assetDetails: String,
    val localNetworkID: String,
    val remoteNetworkID: String,
    val recipient: Party,
    val recipientCert: String,
    val claimStatus: Boolean,
    val expiryTimeSecs: Long,
    val expirationStatus: Boolean,
    val pledgeCondition: ByteArray,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState {
    // locker/pledger is not a participant as that party may not be part of the importing network
    override val participants: List<AbstractParty> get() = listOf(recipient)
}

@CordaSerializable
data class AssetClaimParameters(
    val pledgeId: String,
    val createAssetStateCommand: CommandData,
    val pledgeStatusLinearId: String,
    val getAssetAndContractIdFlowName: String,
    val assetType: String,
    val assetIdOrQuantity: Any,
    val pledgerCert: String,
    val recipientCert: String,
    val pledgeCondition: ByteArray,
    val issuer: Party,
    val observers: List<Party>
)

@CordaSerializable
data class AssetReclaimParameters(
    val pledgeId: String,
    val createAssetStateCommand: CommandData,
    val claimStatusLinearId: String,
    val issuer: Party,
    val observers: List<Party>
)
