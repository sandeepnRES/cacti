/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

var Migrations = artifacts.require("./Migrations.sol")

module.exports = function (deployer) {
  deployer.deploy(Migrations)
}
