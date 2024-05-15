bondtype="bond01"
bondid="$1"
tokentype="t2"
amount=$2

NETWORK_NAME=Corda_Network2 CORDA_PORT=30012 ./clients/build/install/clients/bin/clients issue-asset-state $amount $tokentype
NETWORK_NAME=Corda_Network CORDA_PORT=10009 ./clients/build/install/clients/bin/clients bond issue-asset $bondid $bondtype

# 10006: PartyA (issuer), 10009: PartyB (borrower), 10012: PartyC (lender) Asset Network 1
# 30006: PartyA (issuer), 30009: PartyB (borrower), 30012: PartyC (lender) Token Network 2

# Step 2
CORDA_PORT=10009 ./clients/build/install/clients/bin/clients lock-asset --hashBase64=ivHErp1x4bJDKuRo6L5bApO/DdoyD/dG0mAZrzLZEIs= --timeout=60 --recipient="O=PartyC, L=London, C=GB" --param="$bondtype:$bondid" 1> tmp.out
cat tmp.out
CID1=$(cat tmp.out | grep "HTLC Lock State created with contract ID Right" | sed -e 's/.*Right(b=\(.*\))\./\1/')
echo "BondAsset Contract ID: $CID1"
echo -e "\n"
if [ -z "${CID1}" ]; then exit 1; fi

# Step 3
NETWORK_NAME=Corda_Network2 CORDA_PORT=30012 ./clients/build/install/clients/bin/clients lock-asset -f -h64 ivHErp1x4bJDKuRo6L5bApO/DdoyD/dG0mAZrzLZEIs= -t 30 -r "O=PartyB, L=London, C=GB" -p "$tokentype:$amount" 1> tmp.out
cat tmp.out
CID2=$(cat tmp.out | grep "HTLC Lock State created with contract ID Right" | sed -e 's/.*Right(b=\(.*\))\./\1/')
echo "TokenAsset Contract ID: $CID2"
echo "\n"

# Step 4
NETWORK_NAME=Corda_Network2 CORDA_PORT=30009 ./clients/build/install/clients/bin/clients claim-asset -f -cid $CID2 -s "secrettext" 1> tmp.out
cat tmp.out

# Step 5
CORDA_PORT=10012 ./clients/build/install/clients/bin/clients loan claim-and-pledge -cid $CID1 -s "secrettext" -tlid "Corda_Network2" -tt $tokentype --repayment-amount $amount -tl "O=PartyC, L=London, C=GB" -tb "O=PartyB, L=London, C=GB" -l "3600" 1> tmp.out
cat tmp.out
PID1=$(cat tmp.out | grep "Claim and Pledging asset was successful and the pledge state was stored with pledgeId" | awk -F "pledgeId " '{print $2}' | awk -F "." '{print $1}')
echo "Asset pledge ID: $PID1"

# Step 6
NETWORK_NAME=Corda_Network2 CORDA_PORT=30009 ./clients/build/install/clients/bin/clients loan pledge-asset -t 60 -l 'O=PartyC, L=London, C=GB' -alid 'Corda_Network' -at $bondtype -aid $bondid -ab 'O=PartyB, L=London, C=GB' -al 'O=PartyC, L=London, C=GB' -o 'O=PartyC, L=London, C=GB' -p "$tokentype:$amount" 1> tmp.out
cat tmp.out
PID2=$(cat tmp.out | grep "AssetPledgeState created with pledge-id " | awk -F "'" '{print $2}')
echo "Tokens pledge ID: $PID2"
if [ -z "${PID2}" ]; then exit 1; fi

# Step 7
CORDA_PORT=10009 ./clients/build/install/clients/bin/clients loan claim-asset -pid $PID1 -rpid $PID2 -l 'O=PartyC, L=London, C=GB' -ar 'localhost:9081' -tlt 'corda' -tlid 'Corda_Network2' 1> tmp.out
cat tmp.out
(cat tmp.out | grep "Loaned asset claim by borrower successful" ) || exit 1

# Step 8
NETWORK_NAME=Corda_Network2 CORDA_PORT=30012 ./clients/build/install/clients/bin/clients loan claim-repayment -pid $PID2 -rpid $PID1 -b 'O=PartyB, L=London, C=GB' -tr 'localhost:9082' -alt 'corda' -alid 'Corda_Network' 1> tmp.out
cat tmp.out
