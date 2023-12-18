/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cordaSimpleApplication.state

import com.cordaSimpleApplication.contract.BondAssetContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import com.google.gson.annotations.*

/*
This pledge condition can contain remote network's pledgeId, to avoid
storing asset id and their owners.
*/

data class LoanRepaymentCondition(
    val tokenType: String,
    val tokenQuantity: Long,
    val tokenLedgerId: String,
    val tokenLedgerLenderCert: String,
    val tokenLedgerBorrowerCert: String,
    val assetType: String,
    val assetId: String,
    val assetLedgerId: String,
    val assetLedgerLenderCert: String,
    val assetLedgerBorrowerCert: String
)