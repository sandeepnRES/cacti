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
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.utilities.unwrap

import com.cordaSimpleApplication.state.AssetState
import com.cordaSimpleApplication.state.BondAssetState
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

import org.hyperledger.cacti.weaver.imodule.corda.contracts.AssetTransferContract
import org.hyperledger.cacti.weaver.imodule.corda.contracts.AssetExchangeHTLCStateContract
import org.hyperledger.cacti.weaver.imodule.corda.contracts.parseExternalStateForPledgeStatus
import org.hyperledger.cacti.weaver.imodule.corda.contracts.parseExternalStateForClaimStatus

import org.hyperledger.cacti.weaver.imodule.corda.states.AssetPledgeState
import org.hyperledger.cacti.weaver.imodule.corda.states.AssetExchangeHTLCState
import org.hyperledger.cacti.weaver.imodule.corda.states.AssetClaimStatusState
import org.hyperledger.cacti.weaver.imodule.corda.states.NetworkIdState
import org.hyperledger.cacti.weaver.imodule.corda.states.AssetClaimHTLCData

import org.hyperledger.cacti.weaver.protos.corda.ViewDataOuterClass
import org.hyperledger.cacti.weaver.protos.common.asset_transfer.AssetTransfer


/**
 * Enum for communicating the role of the responder from initiator flow to responder flow.
 */
@CordaSerializable
enum class AssetLoanResponderRole {
    BORROWER, LENDER, ISSUER, OBSERVER
}


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
                    lenderCert = lenderCert,
                    borrowerCert = borrowerCert
                )
                
                
                // 1. Create the asset pledge state
                val gson = GsonBuilder().create();
                var marshalledPledgeCondition = gson.toJson(pledgeCondition, LoanRepaymentCondition::class.java)
                println("Pledge Condition JSON: ${marshalledPledgeCondition}")
                val assetPledgeState = AssetPledgeState(
                    assetExchangeHTLCState.assetStatePointer, // @property assetStatePointer
                    ourIdentity, // @property lender
                    pledgeCondition.assetLedgerId, // @property lender
                    pledgeCondition.borrowerCert,
                    loanPeriodAbs.getEpochSecond(),
                    pledgeCondition.assetLedgerId,
                    pledgeCondition.tokenLedgerId,
                    marshalledPledgeCondition.toByteArray()
                )
                
                val txBuilder = TransactionBuilder(notary)
                    .addInputState(inputState)
                    .addOutputState(assetPledgeState, AssetTransferContract.ID)
                    .addCommand(assetHTLCClaimCmd)
                    .addCommand(assetLoanPledgeCmd).apply {
                        networkIdStateRef.let {
                            this.addReferenceState(ReferencedStateAndRef(networkIdStateRef))
                        }
                    }
                    .addCommand(pledgeCmd).apply {
                        networkIdStateRef.let {
                            this.addReferenceState(ReferencedStateAndRef(networkIdStateRef))
                        }
                    }
                    .setTimeWindow(TimeWindow.untilOnly(assetExchangeHTLCState.lockInfo.expiryTime))
                
                // Verify and collect signatures on the transaction
                txBuilder.verify(serviceHub)
                val partSignedTx = serviceHub.signInitialTransaction(txBuilder)
                println("Recipient signed transaction.")

                var sessions = listOf<FlowSession>()

                if (!ourIdentity.equals(issuer)) {
                    val issuerSession = initiateFlow(issuer)
                    issuerSession.send(AssetLoanResponderRole.ISSUER)
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
        if (role == AssetLoanResponderRole.ISSUER) {
            val signTransactionFlow = object : SignTransactionFlow(session) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
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
                    val htlcState = lTx.inputsOfType<AssetExchangeHTLCState>()[0]
                    val pledgeState = lTx.outputsOfType<AssetPledgeState>()[0]
                    val myCert = Base64.getEncoder().encodeToString(x509CertToPem(ourIdentityAndCert.certificate).toByteArray())
                    
                    "I should be the borrower and recipient of pledge" using (htlcState.locker == ourIdentity && pledgeState.recipientCert == myCert)
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
                        issuerSession.send(AssetLoanResponderRole.ISSUER)
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
        if (role == AssetLoanResponderRole.ISSUER) {
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
    val borrowerCert: String,
    val lender: Party,
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
        
        val claimAssetState = subFlow(UpdateBondAssetOwnerFromPointer(
            localPledgeRef.state.data.assetStatePointer!! as StaticPointer<BondAssetState>
        ))
        
        var currentTimeSecs: Long
        val calendar = Calendar.getInstance()
        currentTimeSecs = calendar.timeInMillis / 1000
        
        val networkIdStateRef = subFlow(RetrieveNetworkIdStateAndRef())
        val fetchedNetworkIdState = networkIdStateRef!!.state.data
        
        if (currentTimeSecs >= localPledgeRef.state.data.expiryTimeSecs) {
            println("Cannot claim loaned asset with pledgeId ${pledgeId} as the expiry time has elapsed.")
            Left(Error("Cannot claim loaned asset with pledged ${pledgeId} as the expiry time has elapsed."))
        } else if (remotePledge.recipient != borrowerCert) {
            println("Cannot claim loaned asset with pledgeId ${pledgeId} as it has not been pledged to the the recipient.")
            Left(Error("Cannot claim loaned asset with pledged ${pledgeId} as it has not been pledged to the recipient."))
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
        val assetLoanClaimCmd = Command(BondAssetContract.Commands.LoanClaim(),
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
            .addCommand(claimCmd).apply {
                networkIdStateRef.let {
                    this.addReferenceState(ReferencedStateAndRef(networkIdStateRef))
                }
            }
            .addCommand(assetCreateCmd)
            .addCommand(assetLoanClaimCmd)
            .setTimeWindow(TimeWindow.fromOnly(Instant.ofEpochSecond(localPledgeRef.state.data.expiryTimeSecs).plusNanos(1)))
        
        // Verify and collect signatures on the transaction
        txBuilder.verify(serviceHub)
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)
        println("Recipient signed transaction.")

        var sessions = listOf<FlowSession>()

        if (!ourIdentity.equals(issuer)) {
            val issuerSession = initiateFlow(issuer)
            issuerSession.send(AssetLoanResponderRole.ISSUER)
            sessions += issuerSession
        }

        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions))
        
        var observerSessions = listOf<FlowSession>()
        
        val lockerSession = initiateFlow(localPledgeRef.state.data.locker)
        lockerSession.send(AssetLoanResponderRole.LENDER)
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
        if (role == AssetLoanResponderRole.ISSUER) {
            val signTransactionFlow = object : SignTransactionFlow(session) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
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
        } else if (role == AssetLoanResponderRole.LENDER) {
            val sTx = subFlow(ReceiveFinalityFlow(session, statesToRecord = StatesToRecord.ALL_VISIBLE))
            println("Received Tx: ${sTx} and recorded states.")
            return sTx
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
    val getAssetAndContractIdFlowName: String,
    val assetType: String,
    val assetIdOrQuantity: Any,
    val lockerCert: String,
    val recipientCert: String,
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
        
        val claimAssetState = subFlow(UpdateAssetOwnerFromPointer(
            localPledgeRef.state.data.assetStatePointer!! as StaticPointer<AssetState>
        ))
        
        var currentTimeSecs: Long
        val calendar = Calendar.getInstance()
        currentTimeSecs = calendar.timeInMillis / 1000
        
        val networkIdStateRef = subFlow(RetrieveNetworkIdStateAndRef())
        val fetchedNetworkIdState = networkIdStateRef!!.state.data
        
        if (currentTimeSecs >= localPledgeRef.state.data.expiryTimeSecs) {
            println("Cannot claim loaned asset with pledgeId ${pledgeId} as the expiry time has elapsed.")
            Left(Error("Cannot claim loaned asset with pledged ${pledgeId} as the expiry time has elapsed."))
        } else if (remoteClaimStatus.recipient != recipientCert) {
            println("Cannot claim loaned asset with pledgeId ${pledgeId} as it has not been pledged to the the recipient.")
            Left(Error("Cannot claim loaned asset with pledged ${pledgeId} as it has not been pledged to the recipient."))
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
            recipientCert,
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
        val assetLoanClaimCmd = Command(AssetContract.Commands.LoanClaimRepayment(),
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
            .addCommand(claimCmd).apply {
                networkIdStateRef.let {
                    this.addReferenceState(ReferencedStateAndRef(networkIdStateRef))
                }
            }
            .addCommand(assetCreateCmd)
            .addCommand(assetLoanClaimCmd)
            .setTimeWindow(TimeWindow.fromOnly(Instant.ofEpochSecond(localPledgeRef.state.data.expiryTimeSecs).plusNanos(1)))
        
        // Verify and collect signatures on the transaction
        txBuilder.verify(serviceHub)
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)
        println("Recipient signed transaction.")

        var sessions = listOf<FlowSession>()

        if (!ourIdentity.equals(issuer)) {
            val issuerSession = initiateFlow(issuer)
            issuerSession.send(AssetLoanResponderRole.ISSUER)
            sessions += issuerSession
        }

        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions))
        
        var observerSessions = listOf<FlowSession>()
        val lockerSession = initiateFlow(localPledgeRef.state.data.locker)
        lockerSession.send(AssetLoanResponderRole.BORROWER)
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
        if (role == AssetLoanResponderRole.ISSUER) {
            val signTransactionFlow = object : SignTransactionFlow(session) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
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
            val sTx = subFlow(ReceiveFinalityFlow(session, statesToRecord = StatesToRecord.ALL_VISIBLE))
            println("Received Tx: ${sTx} and recorded states.")
            return sTx
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

