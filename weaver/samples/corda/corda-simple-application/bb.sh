NETWORK_NAME='Corda_Network' CORDA_PORT=10006 ./clients/build/install/clients/bin/clients issue-asset-state 5 t1 1> tmp.out
cat tmp.out | grep "AssetState(quantity=5, tokenType=t1, owner=O=PartyA" && COUNT=$(( COUNT + 1 )) && echo "PASS"
cat tmp.out

# CORDA2-CORDA
# Pledge Asset
NETWORK_NAME='Corda_Network' CORDA_PORT=10006 ./clients/build/install/clients/bin/clients transfer pledge-asset --fungible --timeout="3600" --import-network-id='Corda_Network2' --recipient='O=PartyA, L=London, C=GB' --param='t1:5' 1> tmp.out
cat tmp.out | grep "AssetPledgeState created with pledge-id" && COUNT=$(( COUNT + 1 )) && echo "PASS"
cat tmp.out

PID=$(cat tmp.out | grep "AssetPledgeState created with pledge-id " | awk -F "'" '{print $2}')

# Is Asset Pledged
CORDA_PORT=10006 ./clients/build/install/clients/bin/clients transfer is-asset-pledged -pid $PID 1> tmp.out
cat tmp.out | grep "Is asset pledged for transfer response: true" && COUNT=$(( COUNT + 1 )) && echo "PASS"
cat tmp.out

# Claim Remote Asset
NETWORK_NAME='Corda_Network2' CORDA_PORT=30006 ./clients/build/install/clients/bin/clients transfer claim-remote-asset --pledge-id=$PID --locker='O=PartyA, L=London, C=GB' --transfer-category='token.corda' --export-network-id='Corda_Network' --param='t1:5' --import-relay-address='localhost:9082' 1> tmp.out
cat tmp.out | grep "Pledged asset claim response: Right(b=SignedTransaction(id=" && COUNT=$(( COUNT + 1 )) && echo "PASS"
cat tmp.out
