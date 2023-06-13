/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cordaSimpleApplication.client

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.default
import java.io.File
import java.lang.Exception
import kotlinx.coroutines.runBlocking
import com.google.protobuf.util.JsonFormat
import java.util.Base64
import java.time.Instant
import kotlin.system.exitProcess
// Corda
import net.corda.core.utilities.OpaqueBytes
import net.corda.core.crypto.sha256
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.CordaRPCOps
// cacti weaver sdk
import org.hyperledger.cacti.weaver.sdk.corda.AssetManager
import org.hyperledger.cacti.weaver.sdk.corda.HashFunctions
// cacti weaver imodule
import org.hyperledger.cacti.weaver.imodule.corda.states.AssetClaimHTLCData
import org.hyperledger.cacti.weaver.imodule.corda.flows.getHashMechanism
import org.hyperledger.cacti.weaver.imodule.corda.flows.RetrieveNetworkId
// sample cordapp
import com.cordaSimpleApplication.state.AssetState
import com.cordaSimpleApplication.state.LoanRepaymentCondition
import com.cordaSimpleApplication.contract.AssetContract
import com.cordaSimpleApplication.flow.ClaimAndPledgeAssetStateInitiator
import com.cordaSimpleApplication.flow.ClaimLoanedAssetInitiator
import com.cordaSimpleApplication.flow.ClaimLoanRepaymentInitiator

object AssetLoanManager {
    class LoanCommand : CliktCommand(name = "loan", help ="Manages simple asset loan") {
        override fun run() {
        }
    }

    /**
     * Claim and Pledge the asset
     */
    class ClaimAndPledgeCommand : CliktCommand(
            help = "Claim And Pledge an asset. claim-and-pledge --secret=secret --loan_period=10 --recipient='PartyA'") {
        val config by requireObject<Map<String, String>>()
        val contractId: String? by option("-cid", "--contract-id", help="Contract/Linear Id for HTLC State")
        val hash_fn: String? by option("-hfn", "--hash-fn", help="Hash Function to be used. Default: SHA256")
        val secret: String? by option("-s", "--secret", help="Hash Pre-Image for the HTLC Claim")
        val token_ledger: String? by option("-tl", "--token-ledger", help="Token Ledger Id")
        val token_type: String? by option("-tt", "--token-type", help="Token Type")
        val repayment_amount: String? by option("-ra", "--repayment-amount", help="Amount of tokens for repayment")
        val loan_period: String? by option("-l", "--loan-period", help="Asset Loan period in days")
        override fun run() = runBlocking {
            var hash: HashFunctions.Hash = HashFunctions.SHA256()
            if(hash_fn == "SHA256") {
                hash = HashFunctions.SHA256()
            } else if ( hash_fn == "SHA512") {
                hash = HashFunctions.SHA512()
            }
            if (contractId == null || secret == null) {
                println("Arguments required: --contract-id and --secret.")
            } else {
                val rpc = NodeRPCConnection(
                        host = config["CORDA_HOST"]!!,
                        username = "clientUser1",
                        password = "test",
                        rpcPort = config["CORDA_PORT"]!!.toInt())
                try {
                    val issuer = rpc.proxy.wellKnownPartyFromX500Name(CordaX500Name.parse("O=PartyA,L=London,C=GB"))!!
                    hash.setPreimage(secret!!)
                    val claimInfoData = AssetClaimHTLCData(
                        hashMechanism = getHashMechanism(hash.HASH_MECHANISM),
                        hashPreimage = OpaqueBytes(hash.getPreimage()!!.toByteArray())
                    )
                    val pledgeCondition = LoanRepaymentCondition(
                        tokenType = token_type!!,
                        tokenQuantity = repayment_amount!!.toLong(),
                        tokenLedgerId = token_ledger!!,
                        borrowerCert = "",
                        assetType = "",
                        assetId = "",
                        assetLedgerId = "",
                        lenderCert = ""
                    )
                    val result = runCatching {
                        rpc.proxy.startFlow(::ClaimAndPledgeAssetStateInitiator, contractId!!, claimInfoData, pledgeCondition, loan_period!!.toLong() * 24L * 3600L, issuer, listOf<Party>())
                            .returnValue.get()
                    }.fold({ it ->
                        it.map { pledgeId ->
                            println("Claim and Pledging asset was successful and the pledge state was stored with pledgeId $pledgeId.\n")
                            pledgeId.toString()
                        }
                    }, { it -> 
                        println("Corda Network Error: Error running ClaimAndPledgeAssetState flow: ${it.message}\n")
                    })
                    println("Asset ClaimAndPledgeAssetState Response: ${result}")
                } catch (e: Exception) {
                  println("Error: ${e.toString()}")
                } finally {
                    rpc.close()
                }
            }
        }
    }
    /**
     * Claim the loaned asset
     */
    class LoanClaimAssetCommand : CliktCommand(
            help = "Claim a loaned asset. claim-asset --pledge-id=abc --recipient='PartyA'") {
        val config by requireObject<Map<String, String>>()
        val pledgeId: String? by option("-pid", "--pledge-id", help="Pledge id for asset loan pledge state")
        val remotePledgeId: String? by option("-rpid", "--remote-pledge-id", help="Pledge id for token pledged state")
        val tokenLedgerId: String? by option ("-tlid", "--token-ledger-id", help="Token ledger id of pledged asset for asset loan")
        val lender: String? by option("-l", "--lender", help="X500 name for lender Party in this asset ledger")
        val tokenRelayAddress: String? by option ("-tr", "--token-relay", help="Token ledger relay address")
        val tokenLedgerType: String? by option("-tlt", "--token-ledger-type", help="DLT type of remote network: fabric|corda|besu")
        val observer: String? by option("-o", "--observer", help="Party Name for Observer")
        override fun run() = runBlocking {
            if (pledgeId == null || remotePledgeId == null || tokenRelayAddress == null || tokenLedgerId == null) {
                println("Arguments required: --pledge-id, --remote-pledge-id, --token-ledger-id and --token-relay.")
            } else {
                val rpc = NodeRPCConnection(
                        host = config["CORDA_HOST"]!!,
                        username = "clientUser1",
                        password = "test",
                        rpcPort = config["CORDA_PORT"]!!.toInt())
                try {
                    // "thisParty" is set to the token "issuer" in case fungible house token; since we are using the same
                    // SDK function claimPledgeFungibleAsset and Interop application for both the "Simple Asset" and
                    // the "Fungible house token" corDapps, we pass the Identity of the party submitting the claim here.
                    val issuer = rpc.proxy.wellKnownPartyFromX500Name(CordaX500Name.parse("O=PartyA,L=London,C=GB"))!!
                    val thisParty: Party = rpc.proxy.nodeInfo().legalIdentities.get(0)
                    val borrowerCert: String = fetchCertBase64Helper(rpc.proxy)
                    var obs = listOf<Party>()
                    if (observer != null)   {
                        obs += rpc.proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(observer!!))!!
                    }

                    // Obtain the locker certificate from the name of the locker
                    val lenderCert = ""
                    //val lenderCert: String = getUserCertFromFile(lender!!, tokenLedgerId!!)
                    val lender = rpc.proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(lender!!))!!

                    val localNetworkId: String? = rpc.proxy.startFlow(::RetrieveNetworkId).returnValue.get()
                    var externalStateAddress: String = getClaimLoanedAssetViewAddress(tokenLedgerType!!, pledgeId!!, localNetworkId!!, tokenLedgerId!!, borrowerCert, lenderCert)

                    // 1. While exercising 'data transfer' initiated by a Corda network, the localRelayAddress is obtained directly from user.
                    // 2. While exercising 'asset transfer' initiated by a Fabric network, the localRelayAddress is obtained from config.json file
                    // 3. While exercising 'asset transfer' initiated by a Corda network (this case), the localRelayAddress is obtained
                    //    below from the remote-network-config.json file
                    //val networkConfig: JSONObject = getRemoteNetworkConfig(importNetworkId)
                    //val importRelayAddress: String = networkConfig.getString("relayEndpoint")
                    val pledgeStatusLinearId: String = requestStateFromRemoteNetwork(tokenRelayAddress!!, externalStateAddress, rpc.proxy, config, listOf(issuer))

                    val result = runCatching {
                        rpc.proxy.startFlow(::ClaimLoanedAssetInitiator, pledgeId!!, pledgeStatusLinearId, borrowerCert, lender, issuer, listOf<Party>())
                            .returnValue.get()
                    }.fold({ it ->
                        it.map { pledgeId ->
                            println("Claim loaned asset was successful and claim status was stored with id $pledgeId.\n")
                            pledgeId.toString()
                        }
                    }, { it -> 
                        println("Corda Network Error: Error running ClaimLoanedAsset flow: ${it.message}\n")
                    })
                    println("Loaned asset claim by borrower response: ${result}")
                } catch (e: Exception) {
                    println("Error: ${e.toString()}")
                    // exit the process throwing error code
                    exitProcess(1)
                } finally {
                    rpc.close()
                }
            }
        }
    }
    /**
     * Claim repayment
     */
    /* class LoanClaimRepaymentCommand : CliktCommand(
            help = "Claim a loaned payment. claim-repayment --pledge-id=abc --recipient='PartyA'") {
        val config by requireObject<Map<String, String>>()
        val pledgeId: String? by option("-pid", "--pledge-id", help="Pledge id for asset loan pledge state")
        val remotePledgeId: String? by option("-rpid", "--remote-pledge-id", help="Pledge id for token pledged state")
        val assetLedgerId: String? by option ("-tlid", "--asset-ledger-id", help="asset ledger id of pledged asset for asset loan")
        val lender: String? by option("-l", "--lender", help="X500 name for lender Party in this asset ledger")
        val assetRelayAddress: String? by option ("-tr", "--asset-relay", help="asset ledger relay address")
        val assetLedgerType: String? by option("-tlt", "--asset-ledger-type", help="DLT type of remote network: fabric|corda|besu")
        override fun run() = runBlocking {
            if (pledgeId == null || importRelayAddress == null || exportNetworkId == null) {
                println("Arguments required: --pledge-id, --asset-ledger-id and --asset-relay.")
            } else {
                val rpc = NodeRPCConnection(
                        host = config["CORDA_HOST"]!!,
                        username = "clientUser1",
                        password = "test",
                        rpcPort = config["CORDA_PORT"]!!.toInt())
                try {
                    // "thisParty" is set to the token "issuer" in case fungible house token; since we are using the same
                    // SDK function claimPledgeFungibleAsset and Interop application for both the "Simple Asset" and
                    // the "Fungible house token" corDapps, we pass the Identity of the party submitting the claim here.
                    val issuer = rpc.proxy.wellKnownPartyFromX500Name(CordaX500Name.parse("O=PartyA,L=London,C=GB"))!!
                    val thisParty: Party = rpc.proxy.nodeInfo().legalIdentities.get(0)
                    val borrowerCert: String = fetchCertBase64Helper(rpc.proxy)
                    val params = param!!.split(":").toTypedArray()
                    if (params.size != 2) {
                        println("Invalid argument --param $param")
                        throw IllegalStateException("Invalid argument --param $param")
                    }
                    println("params[0]: ${params[0]} and params[1]: ${params[1]}")
                    var obs = listOf<Party>()
                    if (observer != null)   {
                        obs += rpc.proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(observer!!))!!
                    }

                    // Obtain the locker certificate from the name of the locker
                    //val lenderCert: String = getUserCertFromFile(lender!!, tokenLedgerId!!)
                    val lender = rpc.proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(lender))!!

                    val localNetworkId: String? = rpc.proxy.startFlow(::RetrieveNetworkId).returnValue.get()
                    var externalStateAddress: String = getClaimRepaymentViewAddress(assetLedgerType!!, pledgeId!!, lenderCert, assetLedgerId!!, borrowerCert, localNetworkId!!)

                    // 1. While exercising 'data transfer' initiated by a Corda network, the localRelayAddress is obtained directly from user.
                    // 2. While exercising 'asset transfer' initiated by a Fabric network, the localRelayAddress is obtained from config.json file
                    // 3. While exercising 'asset transfer' initiated by a Corda network (this case), the localRelayAddress is obtained
                    //    below from the remote-network-config.json file
                    //val networkConfig: JSONObject = getRemoteNetworkConfig(importNetworkId)
                    //val importRelayAddress: String = networkConfig.getString("relayEndpoint")
                    val pledgeStatusLinearId: String = requestStateFromRemoteNetwork(assetRelayAddress!!, externalStateAddress, rpc.proxy, config, listOf(issuer))

                    val result = runCatching {
                        rpc.proxy.startFlow(::ClaimLoanRepaymentInitiator, pledgeId!!, pledgeStatusLinearId, borrowerCert, lender, issuer, listOf<Party>())
                            .returnValue.get()
                    }.fold({ it ->
                        it.map { pledgeId ->
                            println("Claim loaned asset was successful and claim status was stored with id $pledgeId.\n")
                            pledgeId.toString()
                        }
                    }, { it -> 
                        println("Corda Network Error: Error running ClaimLoanedAsset flow: ${it.message}\n")
                    })
                    println("Loaned asset claim by borrower response: ${result}")
                } catch (e: Exception) {
                    println("Error: ${e.toString()}")
                    // exit the process throwing error code
                    exitProcess(1)
                } finally {
                    rpc.close()
                }
            }
        }
    } */
}

fun getClaimLoanedAssetViewAddress(tokenLedgerType: String, remotePledgeId: String, localNetworkId: String, remoteNetworkId: String, pledgerCert: String?, recipientCert: String?): String {
    if (tokenLedgerType.equals("corda")) {
        return generateViewAddressFromRemoteConfig(remoteNetworkId, "GetAssetPledgeStatusByPledgeId", listOf(remotePledgeId, localNetworkId))
    } else if (tokenLedgerType.equals("fabric")) {
        throw Error("Unsupported ledger type: ${tokenLedgerType}")
        //return generateViewAddressFromRemoteConfig(remoteNetworkId, "GetAssetPledgeStatus", listOf(remotePledgeId, pledgerCert!!, localNetworkId, recipientCert!!))
    } else {
        throw Error("Unsupported ledger type: ${tokenLedgerType}")
    }
}
fun getClaimRepaymentViewAddress(assetLedgerType: String, remotePledgeId: String, expiryTimeSecs: String, localNetworkId: String, remoteNetworkId: String, assetType: String?, assetId: String?, pledgerCert: String?, recipientCert: String?): String {
    if (assetLedgerType.equals("corda")) {
        return generateViewAddressFromRemoteConfig(remoteNetworkId, "GetBondAssetClaimStatusByPledgeId", listOf(remotePledgeId, expiryTimeSecs))
    } else if (assetLedgerType.equals("fabric")) {
        throw Error("Unsupported ledger type: ${assetLedgerType}")
        //return generateViewAddressFromRemoteConfig(remoteNetworkId, "GetAssetClaimStatus", listOf(remotePledgeId, assetType!!, assetId!!, recipientCert!!, pledgerCert!!, remoteNetworkId, expiryTimeSecs))
    } else {
        throw Error("Unsupported ledger type: ${assetLedgerType}")
    }
}