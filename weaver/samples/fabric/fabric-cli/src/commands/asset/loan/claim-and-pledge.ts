/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import { GluegunCommand } from 'gluegun'
import * as path from 'path'
import { commandHelp, getNetworkConfig } from '../../../helpers/helpers'
import { fabricHelper } from '../../../helpers/fabric-functions'
import { claimAndPledgeAsset } from '../../../helpers/loan'
import { AssetManager, HashFunctions } from '@hyperledger/cacti-weaver-sdk-fabric'
import { EventsManager } from '@hyperledger/cacti-weaver-sdk-fabric'
import { EventSubscriptionState, EventType } from "@hyperledger/cacti-weaver-protos-js/common/events_pb";

import logger from '../../../helpers/logger'
import * as dotenv from 'dotenv'
dotenv.config({ path: path.resolve(__dirname, '../../../.env') })

const delay = ms => new Promise(res => setTimeout(res, ms))

const command: GluegunCommand = {
  name: 'claim-and-pledge',
  description:
    'Claim and Pledges asset an asset',
  run: async toolbox => {
    const {
      print,
      parameters: { options, array }
    } = toolbox
    if (options.help || options.h) {
      commandHelp(
        print,
        toolbox,
        'fabric-cli asset loan claim-and-pledge --asset-network=network1 --token-network=network2 --recipient=bob --expiry-secs=3600 --type=bond --ref=a03 --data-file=src/data/assets.json\r\nfabric-cli asset loan claim-and-pledge --asset-network=network1 --token-network=network2 --recipient=bob --expiry-secs=3600 --type=token --owner=alice --units=50 --data-file=src/data/assets.json',
        'fabric-cli asset loan claim-and-pledge --asset-network=<asset-network-name> --token-network=<token-network-name> --recipient=<recipient-id> --expiry-secs=<expiry-in-seconds> --type=<bond|token> [--owner=<owner-id>] [--ref=<asset-id>] [--units=<number-of-units>] --data-file=<path-to-data-file>>',
        [
          {
            name: '--debug',
            description:
              'Shows debug logs when running. Disabled by default. To enable --debug=true'
          },
          {
            name: '--asset-network',
            description:
              'Network where the asset is managed. <network1|network2>'
          },
          {
            name: '--token-network',
            description:
              'Network where the token is managed. <network1|network2>'
          },
          {
            name: '--contract-id',
            description:
              'ContractId for locked asset to be loaned.'
          },
          {
            name: '--hash_fn',
            description:
              'hash function to be used for HTLC. Supported: SHA256, SHA512. (Optional: Default: SHA256)'
          },
          {
            name: '--secret',
            description:
              'secret text to be used by Asset owner to hash lock'
          },
          {
            name: '--loan-period',
            description:
              'How long (in seconds) is the loan valid for'
          },
          {
            name: '--lender',
            description:
              'Lender name'
          },
          {
            name: '--token-lender',
            description:
              'Borrower name'
          },
          {
            name: '--token-borrower',
            description:
              'Borrower name'
          },
          {
            name: '--loan-token-type',
            description:
              'Token Type for loan repayment'
          },
          {
            name: '--loan-amount',
            description:
              'Loan repayment amount'
          }
        ],
        command,
        ['asset', 'loan', 'claim-and-pledge']
      )
      return
    }

    if (options.debug === 'true') {
      logger.level = 'debug'
      logger.debug('Debugging is enabled')
    }
    if (!options['asset-network'])
    {
      print.error('--asset-network needs to be specified')
      return
    }
    if (!options['token-network'])
    {
      print.error('--token-network needs to be specified')
      return
    }
    if (!options['lender'])
    {
      print.error('--lender needs to be specified')
      return
    }
    if (!options['token-lender'])
    {
        options['token-lender'] = options['lender']
    }
    if (!options['token-borrower'])
    {
      print.error('--token-borrower needs to be specified')
      return
    }
    if (!options['loan-period'])
    {
      print.error('--loan-period needs to be specified')
      return
    }
    if (!options['loan-token-type'])
    {
      print.error('--loan-token-type needs to be specified')
      return
    }
    if (!options['loan-amount'])
    {
      print.error('--loan-amount needs to be specified')
      return
    }
    if (isNaN(options['loan-amount']))
    {
      print.error('--loan-amount must be an integer')
      return
    }

    const netConfig = getNetworkConfig(options['asset-network'])

    if (!netConfig.connProfilePath || !netConfig.channelName || !netConfig.chaincode) {
      print.error(
        `Please use a valid --asset-network. No valid environment found for ${options['asset-network']} `
      )
      return
    }
    
    // Hash
    let hash: HashFunctions.Hash
    if(options['hash_fn'] == 'SHA512') {
        hash = new HashFunctions.SHA512()
    } else {
        hash = new HashFunctions.SHA256()
    }
    hash.setPreimage(options['secret'])

    console.time('claimAndPledge');
    const pledgeResult = await claimAndPledgeAsset({
        assetNetworkName: options['asset-network'],
        tokenNetworkName: options['token-network'],
        contractId: options['contract-id'],
        hash: hash,
        lender: options['lender'],
        tokenLender: options['token-lender'],
        tokenBorrower: options['token-borrower'],
        loanPeriod: options['loanPeriod'],
        repaymentAmount: options['loan-amount'],
        repaymentTokenType: options['loan-token-type'],
        connProfilePath: netConfig.connProfilePath,
        channelName: netConfig.channelName,
        contractName: netConfig.chaincode,
        mspId: netConfig.mspId,
        logger: logger
    })
    console.timeEnd('claimAndPledge');
    if (pledgeResult) {
      console.log('Asset pledged with ID', pledgeResult)
    }
    //await subscribeDelete(options['lender'], options['asset-network'], pledgeResult, netConfig)
  }
}

/*
async function subscribeDelete(user, networkName, pledgeId, netConfig) {
    const data = JSON.parse(`{   
        "event_matcher": {
            "eventType": 0,
            "eventClassId": "LoanRepaymentClaimed", 
            "transactionLedgerId": "mychannel",
            "transactionContractId": "simpleasset_loan",
            "transactionFunc": ""
        },  
        "event_publication_spec": {
            "ccArgs": ["${pledgeId}", ""],
            "chaincodeId": "simpleasset_loan",
            "driverId": "network1",
            "ccFunc": "DeleteLoanPledge",
            "channelId": "mychannel",
            "replaceArgIndex": 1
        },  
        "view_address": "relay-network2:9083/network2/mychannel:simpleasset_loan:GetLoanClaimStatusBool:?",
        "confidential": false
    }`)

    console.log("Subscribing event:")
    console.log(data)

    const eventMatcher = EventsManager.createEventMatcher(data.event_matcher)
    const eventPublicationSpec = EventsManager.createEventPublicationSpec(data.event_publication_spec)

    const { gateway, wallet, contract } = await fabricHelper({
        channel: netConfig.channelName,
        contractName: process.env.DEFAULT_CHAINCODE ? process.env.DEFAULT_CHAINCODE : 'interop',
        connProfilePath: netConfig.connProfilePath,
        networkName,
        mspId: netConfig.mspId,
        logger,
        discoveryEnabled: true,
        userString: user
    })

    try {
        const response = await EventsManager.subscribeRemoteEvent(
            contract,
            eventMatcher,
            eventPublicationSpec,
            networkName,
            netConfig.mspId,
            netConfig.relayEndpoint,
            { address: data[i].view_address, Sign: true },
            keyCert
        )

        if (response.getStatus() == EventSubscriptionState.STATUS.SUBSCRIBED) {
            console.log("Event Subscription Status Success with requestId:", response.getRequestId(), 'and event matcher:', JSON.stringify(eventMatcher.toObject()))
        } else {
            console.log("Unknown error")
        }
    } catch(e) {
        console.log("Error: ", e.toString())
    }

}*/

module.exports = command
