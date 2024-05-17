assettype="bond03"
assetid=a11
tokenamt=100
loanperiod=600000

borrower="alice"
lender="bob"

for cc in 1 2
do

    ./bin/fabric-cli asset loan lock --timeout-duration=3600 --locker=$borrower --recipient=$lender --hashBase64=ivHErp1x4bJDKuRo6L5bApO/DdoyD/dG0mAZrzLZEIs= --target-network=network1 --param=${assettype}:${assetid} &> tmp.out
    tmp=$(cat tmp.out | grep "Locked with Contract Id: " | sed -e 's/.*Locked with Contract Id: //')
    CID=(${tmp//,/})
    cat tmp.out
    echo $CID

    ./bin/fabric-cli asset loan lock --fungible --timeout-duration=1800 --locker=$lender --recipient=$borrower --hashBase64=ivHErp1x4bJDKuRo6L5bApO/DdoyD/dG0mAZrzLZEIs= --target-network=network2 --param=token1:${tokenamt} &> tmp.out
    tmp=$(cat tmp.out | grep "Locked with Contract Id: " | sed -e 's/.*Locked with Contract Id: //')
    CID2=(${tmp//,/})
    cat tmp.out
    echo $CID2

    ./bin/fabric-cli asset loan claim --fungible --recipient=$borrower --target-network=network2 --contract-id=$CID2 --secret=secrettext
    ./bin/fabric-cli asset loan claim-and-pledge --asset-network=network1 --token-network=network2 --lender=$lender --token-lender=$lender --token-borrower=$borrower --secret=secrettext --contract-id=$CID --loan-period=${loanperiod} --loan-token-type=token1 --loan-amount=${tokenamt} &> tmp.out
    cat tmp.out
    PID=$(cat tmp.out | grep "Asset pledged with ID " | sed -e 's/Asset pledged with ID //')
    echo $PID

    ./bin/fabric-cli asset loan pledge-repayment --token-network=network2 --asset-network=network1 --lender=$lender --borrower=$borrower --expiry-secs=1800  --token-type=token1 --amount=${tokenamt} --loaned-asset-id=${assetid} --loaned-asset-type=${assettype} &> tmp.out
    cat tmp.out
    PID2=$(cat tmp.out | grep "Asset pledged with ID " | sed -e 's/Asset pledged with ID //')
    echo $PID2

    ./bin/fabric-cli asset loan claim-asset --token-network=network2 --asset-network=network1 --borrower=$borrower --token-borrower=$borrower --token-ledger-type=fabric --pledge-id=$PID --token-pledge-id=$PID2

    ./bin/fabric-cli asset loan claim-repayment --token-network=network2 --asset-network=network1 --token-borrower=$borrower --lender=$lender --asset-ledger-type=fabric --pledge-id=$PID2 --asset-pledge-id=$PID
    
done
    #echo "./bin/fabric-cli asset loan claim-repayment --token-network=network2 --asset-network=network1 --borrower=$borrower --lender=$lender --type=bond --pledge-id=$PID2 --asset-pledge-id=$PID --loan-period=${loanperiod}"
