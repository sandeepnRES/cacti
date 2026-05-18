#!/bin/bash
set -o pipefail

assettype="bond11"
tokenamt=100
loanperiod=600000
timeout=100
timeout2x=$((timeout * 2))

borrower="alice"
lender="bob"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${SCRIPT_DIR}/bench_logs"
mkdir -p "$LOG_DIR"
rm -f "$LOG_DIR"/*.log

START_ID=${START_ID:-201}
NUM_RUNS=${NUM_RUNS:-10}
MAX_ATTEMPTS=${MAX_ATTEMPTS:-30}

asset_exists() {
    local atype=$1
    local aid=$2
    ./bin/fabric-cli chaincode query --local-network=network1 --user=alice mychannel simpleasset_loan ReadAsset "[\"$atype\",\"$aid\",\"false\"]" 2>&1 \
        | grep -q "Result from network query"
}

run_one() {
    local run_idx=$1
    local assetid=$2
    local LOG="$LOG_DIR/run_${run_idx}.log"
    : > "$LOG"

    echo "===== Run $run_idx / $NUM_RUNS  assetid=$assetid =====" | tee -a "$LOG"

    python3 - <<EOF
import json
p = "${SCRIPT_DIR}/asset.json"
with open(p) as f:
    d = json.load(f)
inner = next(iter(d.values()))
inner["assetType"] = "${assettype}"
inner["id"] = "${assetid}"
with open(p, "w") as f:
    json.dump({"${assetid}": inner}, f, indent=2)
EOF

    ./bin/fabric-cli configure asset add --target-network=network1 --type=bond --data-file=./scripts/asset.json 2>&1 | tee -a "$LOG"
    ./bin/fabric-cli chaincode query --local-network=network1 --user=alice mychannel simpleasset_loan ReadAsset "[\"$assettype\",\"$assetid\",\"false\"]" 2>&1 | tee -a "$LOG"

    ./bin/fabric-cli asset loan lock --timeout-duration=$timeout2x --locker=$borrower --recipient=$lender --hashBase64=ivHErp1x4bJDKuRo6L5bApO/DdoyD/dG0mAZrzLZEIs= --target-network=network1 --param=${assettype}:${assetid} 2>&1 | tee tmp.out | tee -a "$LOG"
    local CID=$(grep "Locked with Contract Id: " tmp.out | sed -e 's/.*Locked with Contract Id: //' | tr -d ',')
    echo "CID=$CID" | tee -a "$LOG"

    ./bin/fabric-cli asset loan lock --fungible --timeout-duration=$timeout --locker=$lender --recipient=$borrower --hashBase64=ivHErp1x4bJDKuRo6L5bApO/DdoyD/dG0mAZrzLZEIs= --target-network=network2 --param=token1:${tokenamt} 2>&1 | tee tmp.out | tee -a "$LOG"
    local CID2=$(grep "Locked with Contract Id: " tmp.out | sed -e 's/.*Locked with Contract Id: //' | tr -d ',')
    echo "CID2=$CID2" | tee -a "$LOG"

    ./bin/fabric-cli asset loan claim --fungible --recipient=$borrower --target-network=network2 --contract-id=$CID2 --secret=secrettext 2>&1 | tee -a "$LOG"

    ./bin/fabric-cli asset loan claim-and-pledge --asset-network=network1 --token-network=network2 --lender=$lender --token-lender=$lender --token-borrower=$borrower --secret=secrettext --contract-id=$CID --loan-period=${loanperiod} --loan-token-type=token1 --loan-amount=${tokenamt} 2>&1 | tee tmp.out | tee -a "$LOG"
    local PID=$(grep "Asset pledged with ID " tmp.out | sed -e 's/.*Asset pledged with ID //')
    echo "PID=$PID" | tee -a "$LOG"

    ./bin/fabric-cli asset loan pledge-repayment --token-network=network2 --asset-network=network1 --lender=$lender --borrower=$borrower --expiry-secs=$timeout --token-type=token1 --amount=${tokenamt} --loaned-asset-id=${assetid} --loaned-asset-type=${assettype} 2>&1 | tee tmp.out | tee -a "$LOG"
    local PID2=$(grep "Asset pledged with ID " tmp.out | sed -e 's/.*Asset pledged with ID //')
    echo "PID2=$PID2" | tee -a "$LOG"

    ./bin/fabric-cli asset loan claim-asset --token-network=network2 --asset-network=network1 --borrower=$borrower --token-borrower=$borrower --token-ledger-type=fabric --pledge-id=$PID --token-pledge-id=$PID2 2>&1 | tee -a "$LOG"

    ./bin/fabric-cli asset loan claim-repayment --token-network=network2 --asset-network=network1 --token-borrower=$borrower --lender=$lender --asset-ledger-type=fabric --pledge-id=$PID2 --asset-pledge-id=$PID 2>&1 | tee -a "$LOG"
}

successful=0
attempt=0
while [ $successful -lt $NUM_RUNS ] && [ $attempt -lt $MAX_ATTEMPTS ]; do
    assetid="a$((START_ID + attempt))"
    attempt=$((attempt + 1))
    if asset_exists "$assettype" "$assetid"; then
        echo ">>> Skipping $assetid (already exists on network1)"
        continue
    fi
    successful=$((successful + 1))
    run_one $successful "$assetid"
done

if [ $successful -lt $NUM_RUNS ]; then
    echo "WARNING: only completed $successful / $NUM_RUNS runs after $attempt id attempts"
fi

echo
echo "===== Aggregated timings across $successful runs ====="
# Timing line format: "<label>: <num>(s|ms)$"  -- label may contain spaces.
# We normalize ms -> s and average per label across all run logs.
awk '
match($0, /^([a-zA-Z][a-zA-Z ]*): ([0-9]+(\.[0-9]+)?)(s|ms)$/, m) {
    label = m[1]
    val   = m[2] + 0
    unit  = m[4]
    if (unit == "ms") val = val / 1000.0
    sum[label]   += val
    cnt[label]   += 1
    order[label]  = (label in order) ? order[label] : (++n)
}
END {
    printf "%-22s %8s %12s\n", "command", "count", "avg (s)"
    printf "%-22s %8s %12s\n", "----------------------", "--------", "------------"
    # print in first-seen order
    for (k in order) idx[order[k]] = k
    for (i = 1; i <= n; i++) {
        k = idx[i]
        printf "%-22s %8d %12.3f\n", k, cnt[k], sum[k] / cnt[k]
    }
}
' "$LOG_DIR"/*.log
