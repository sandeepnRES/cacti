# Copyright IBM Corp. All Rights Reserved.
#
# SPDX-License-Identifier: Apache-2.0

./bin/fabric-cli chaincode invoke --local-network=network1 --user=alice mychannel simpleasset GetMyAssets '[]'
./bin/fabric-cli chaincode invoke --local-network=network1 --user=bob mychannel simpleasset GetMyAssets '[]'
./bin/fabric-cli chaincode invoke --local-network=network2 --user=alice mychannel simpleasset GetMyWallet '[]'
./bin/fabric-cli chaincode invoke --local-network=network2 --user=bob mychannel simpleasset GetMyWallet '[]'
