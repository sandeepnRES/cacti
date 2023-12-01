/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package main

import (
	"encoding/base64"
	"encoding/json"

	"github.com/golang/protobuf/proto"
	"github.com/hyperledger/cacti/weaver/common/protos-go/v2/common"
	"github.com/hyperledger/cacti/weaver/core/network/fabric-interop-cc/libs/assetexchange/v2"
	"github.com/hyperledger/fabric-contract-api-go/contractapi"
	log "github.com/sirupsen/logrus"
)



