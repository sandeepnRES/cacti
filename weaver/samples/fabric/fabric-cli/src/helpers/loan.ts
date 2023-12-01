/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import { GluegunCommand } from 'gluegun'
import { Toolbox } from 'gluegun/build/types/domain/toolbox'
import { GluegunPrint } from 'gluegun/build/types/toolbox/print-types'
import { getKeyAndCertForRemoteRequestbyUserName, fabricHelper, invoke, query, InvocationSpec } from './fabric-functions'
import { getNetworkConfig, getUserCertFromFile } from './helpers'
import { AssetPledge } from "@hyperledger/cacti-weaver-protos-js/common/asset_transfer_pb"
import { AssetManager, HashFunctions } from "@hyperledger/cacti-weaver-sdk-fabric"
import { InteroperableHelper } from '@hyperledger/cacti-weaver-sdk-fabric'
import * as crypto from 'crypto'
import { promisify } from 'util'
import * as fs from 'fs'
import * as path from 'path'
import * as dotenv from 'dotenv'
import logger from './logger'
dotenv.config({ path: path.resolve(__dirname, '../../.env') })

// Basic function to pledge an asset in one network to another, it assumes function is PledgeAsset
// TODO: Pass function name as parameter
const claimAndPledgeAsset = async ({
    assetNetworkName,
    tokenNetworkName,
    contractId,
    hash,
    lender,
    borrower,
    loanPeriod,
    repaymentAmount,
    repaymentTokenType,
    connProfilePath,
    channelName,
    contractName,
    mspId = global.__DEFAULT_MSPID__,
    logger
}: {
    assetNetworkName: string
    tokenNetworkName: string
    contractId: string
    hash: HashFunctions
    lender: string
    borrower: string
    loanPeriod: number
    repaymentAmount: number
    repaymentTokenType: string
    connProfilePath: string
    channelName: string
    contractName: string
    mspId?: string
    logger?: any
}): Promise<any> => {
    const claimInfo64 = AssetManager.createAssetClaimInfoSerialized(hash)
    const { gateway, contract, wallet } = await fabricHelper({
        channel: channelName,
        contractName: contractName,
        connProfilePath: connProfilePath,
        networkName: assetNetworkName,
        mspId: mspId,
        userString: lender,
        registerUser: false
    })
    const lenderTokenCert = getUserCertFromFile(lender, tokenNetworkName)
    const borrowerTokenCert = getUserCertFromFile(borrower, tokenNetworkName)
    const expirationTime = (Math.floor(Date.now()/1000 + loanPeriod)).toString()
    
    const loanRepaymentConditionJSON = {
        tokenType: repaymentTokenType,
        tokenQuantity: repaymentAmount,
    	tokenLedgerId: tokenNetworkName,
    	tokenLedgerLenderCert: lenderTokenCert,
    	tokenLedgerBorrowerCert: borrowerTokenCert,
    	assetType: "",
    	assetId: "",
    	assetLedgerId: "",
    	assetLedgerLenderCert: "",
    	assetLedgerBorrowerCertsuer: ""
    }
    const loanRepaymentConditionJSONStr = JSON.stringify(loanRepaymentConditionJSON)

    const ccFunc = 'ClaimAndPledgeAsset'
    const args = [contractId, claimInfo64, expirationTime, loanRepaymentConditionJSONStr]
    console.log(ccFunc)
    console.log(args)
    try {
        const read = await contract.submitTransaction(ccFunc, ...args)
        const state = Buffer.from(read).toString()
        if (state == "") {
            logger.debug(`Response From Network: ${state}`)
        } else {
            logger.debug('No Response from network')
        }

        // Disconnect from the gateway.
        await gateway.disconnect()
        return state
    } catch (error) {
        console.error(`Failed to submit transaction: ${error}`)
        throw new Error(error)
    }
}

const pledgeTokens = async ({
    assetNetworkName,
    tokenNetworkName,
    lender,
    borrower,
    expiryTimeSecs,
    repaymentAmount,
    repaymentTokenType,
    loanedAssetId,
    loanedAssetType,
    connProfilePath,
    channelName,
    contractName,
    mspId = global.__DEFAULT_MSPID__,
    logger
}: {
    assetNetworkName: string
    tokenNetworkName: string
    lender: string
    borrower: string
    expiryTimeSecs: number
    repaymentAmount: number
    repaymentTokenType: string
    loanedAssetId: string
    loanedAssetType: string
    connProfilePath: string
    channelName: string
    contractName: string
    mspId?: string
    logger?: any
}): Promise<any> => {
    //const claimInfo64 = AssetManager.createAssetClaimInfoSerialized(hash)
    const { gateway, contract, wallet } = await fabricHelper({
        channel: channelName,
        contractName: contractName,
        connProfilePath: connProfilePath,
        networkName: tokenNetworkName,
        mspId: mspId,
        userString: borrower,
        registerUser: false
    })
    const lenderAssetCert = getUserCertFromFile(lender, assetNetworkName)
    const lenderTokenCert = getUserCertFromFile(lender, tokenNetworkName)
    const borrowerAssetCert = getUserCertFromFile(borrower, assetNetworkName)
    const expirationTime = (Math.floor(Date.now()/1000 + expiryTimeSecs)).toString()
    
    const loanRepaymentConditionJSON = {
        tokenType: "",
        tokenQuantity: "",
    	tokenLedgerId: "",
    	tokenLedgerLenderCert: lenderTokenCert,
    	tokenLedgerBorrowerCert: "",
    	assetType: loanedAssetType,
    	assetId: loanedAssetId,
    	assetLedgerId: assetNetworkName,
    	assetLedgerLenderCert: lenderAssetCert,
    	assetLedgerBorrowerCertsuer: borrowerAssetCert
    }
    const loanRepaymentConditionJSONStr = JSON.stringify(loanRepaymentConditionJSON)

    const ccFunc = 'PledgeRepaymentTokens'
    const args = [repaymentTokenType, repaymentAmount.toString(), assetNetworkName, expirationTime, loanRepaymentConditionJSONStr]
    console.log(ccFunc)
    console.log(args)
    try {
        const read = await contract.submitTransaction(ccFunc, ...args)
        const state = Buffer.from(read).toString()
        if (state == "") {
            logger.debug(`Response From Network: ${state}`)
        } else {
            logger.debug('No Response from network')
        }

        // Disconnect from the gateway.
        await gateway.disconnect()
        return state
    } catch (error) {
        console.error(`Failed to submit transaction: ${error}`)
        throw new Error(error)
    }
}

const getLoanRepaymentCondition = async ({
    networkName,
    pledgeId,
    caller,
    logger
}: {
    networkName: string
    pledgeId: string
    caller: string
    logger?: any
}): Promise<any> => {
    const netConfig = getNetworkConfig(networkName)

    const currentQuery = {
        channel: netConfig.channelName,
        contractName: netConfig.chaincode
            ? netConfig.chaincode
            : 'simpleassetloan',
        ccFunc: '',
        args: []
    }

    currentQuery.ccFunc = 'GetLoanRepaymentCondition'
    currentQuery.args = [...currentQuery.args, pledgeId]
    console.log(currentQuery)
    try {
        const res = await query(currentQuery,
            netConfig.connProfilePath,
            networkName,
            netConfig.mspId,
            logger,
            caller,
            false
        )
        const resJson = JSON.parse(res)
        return resJson
    } catch (error) {
        console.error(`Failed to get Loan Repayment Condition: ${error}`)
        throw new Error(error)
    }
}

export {
    claimAndPledgeAsset,
    pledgeTokens,
    getLoanRepaymentCondition
}
