/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import { GluegunCommand } from 'gluegun'
import * as path from 'path'
import { commandHelp, getNetworkConfig } from '../../../helpers/helpers'
import { fabricHelper } from '../../../helpers/fabric-functions'
import { pledgeTokens } from '../../../helpers/loan'

import logger from '../../../helpers/logger'
import * as dotenv from 'dotenv'
dotenv.config({ path: path.resolve(__dirname, '../../../.env') })

const delay = ms => new Promise(res => setTimeout(res, ms))

const command: GluegunCommand = {
  name: 'pledge-repayment',
  description:
    'Pledges repayment tokens for loaned asset',
  run: async toolbox => {
    const {
      print,
      parameters: { options, array }
    } = toolbox
    if (options.help || options.h) {
      commandHelp(
        print,
        toolbox,
        'fabric-cli asset loan pledge-repayment --token-network=network1 --asset-network=network2 --recipient=bob --expiry-secs=3600 --type=bond --ref=a03 --data-file=src/data/assets.json\r\nfabric-cli asset loan pledge-repayment --token-network=network1 --asset-network=network2 --recipient=bob --expiry-secs=3600 --type=token --owner=alice --units=50 --data-file=src/data/assets.json',
        'fabric-cli asset loan pledge-repayment --token-network=<token-network-name> --asset-network=<asset-network-name> --recipient=<recipient-id> --expiry-secs=<expiry-in-seconds> --type=<bond|token> [--owner=<owner-id>] [--ref=<asset-id>] [--units=<number-of-units>] --data-file=<path-to-data-file>>',
        [
          {
            name: '--debug',
            description:
              'Shows debug logs when running. Disabled by default. To enable --debug=true'
          },
          {
            name: '--token-network',
            description:
              'Network where the asset is currently present. <network1|network2>'
          },
          {
            name: '--asset-network',
            description:
              'Network where the asset is to be transferred. <network1|network2>'
          },
          {
            name: '--lender',
            description:
              'Lender name'
          },
          {
            name: '--borrower',
            description:
              'Borrower name'
          },
          {
            name: '--expiry-secs',
            description:
              'How long (in seconds) is the pledge valid for'
          },
          {
            name: '--token-type',
            description:
              'Type of token'
          },
          {
            name: '--amount',
            description:
              'Amount for repayment'
          },
          {
            name: '--loaned-asset-id',
            description:
              'Loaned asset id'
          },
          {
            name: '--loaned-asset-type',
            description:
              'Loaned asset type'
          }
        ],
        command,
        ['asset', 'loan', 'pledge-repayment']
      )
      return
    }

    if (options.debug === 'true') {
      logger.level = 'debug'
      logger.debug('Debugging is enabled')
    }
    if (!options['token-network'])
    {
      print.error('--token-network needs to be specified')
      return
    }
    if (!options['asset-network'])
    {
      print.error('--asset-network needs to be specified')
      return
    }
    if (!options['lender'])
    {
      print.error('--lender needs to be specified')
      return
    }
    if (!options['borrower'])
    {
      print.error('--borrower needs to be specified')
      return
    }
    if (!options['expiry-secs'])
    {
      print.error('--expiry-secs needs to be specified')
      return
    }
    if (!options['token-type'])
    {
      print.error('--type of token needs to be specified')
      return
    }
    if (!options['amount'] && isNaN(options['amount'])
    {
      print.error('--amount must be an integer')
      return
    }
    if (!options['loaned-asset-id'])
    {
      print.error('--loaned-asset-id needs to be specified')
      return
    }
    if (!options['loaned-asset-type'])
    {
      print.error('--loaned-asset-type needs to be specified')
      return
    }

    const netConfig = getNetworkConfig(options['token-network'])

    if (!netConfig.connProfilePath || !netConfig.channelName || !netConfig.chaincode) {
      print.error(
        `Please use a valid --token-network. No valid environment found for ${options['token-network']} `
      )
      return
    }

    const pledgeResult = await pledgeTokens({
        assetNetworkName: options['asset-network'],
        tokenNetworkName: options['token-network'],
        lender: options['lender'],
        borrower: options['borrower'],
        expiryTimeSecs: options['expiry-secs'],
        repaymentAmount: options['amount'],
        repaymentTokenType: options['token-type'],
        loanedAssetId: options['loaned-asset-id'],
        loanedAssetType: options['loaned-asset-type'],
        connProfilePath: netConfig.connProfilePath,
        channelName: netConfig.channelName,
        contractName: netConfig.chaincode,
        mspId: netConfig.mspId,
        logger: logger
    })
    if (pledgeResult) {
      console.log('Asset pledged with ID', pledgeResult)
    }
  }
}

module.exports = command
