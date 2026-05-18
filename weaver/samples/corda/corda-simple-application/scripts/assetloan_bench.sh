#!/bin/bash
# Run scripts/assetloan.sh NUM_RUNS times with incrementing bondids and
# aggregate per-command timings (parsed from instrumented `<label>: X.XXXs`
# lines emitted by the Corda CLI). Mirrors fabric-cli's assetloandemo_bench.sh.
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
LOG_DIR="${SCRIPT_DIR}/bench_logs"
mkdir -p "$LOG_DIR"
rm -f "$LOG_DIR"/*.log

START=${START:-1}                # b01 .. b10 by default
NUM_RUNS=${NUM_RUNS:-10}
AMOUNT=${AMOUNT:-100}

cd "$APP_DIR"

for i in $(seq 0 $((NUM_RUNS - 1))); do
    bondid=$(printf "b%02d" $((START + i)))
    LOG="$LOG_DIR/run_${bondid}.log"
    echo "===== Run $((i + 1)) / $NUM_RUNS  bondid=$bondid amount=$AMOUNT ====="
    ./scripts/assetloan.sh "$bondid" "$AMOUNT" > "$LOG" 2>&1
    rc=$?
    if [ $rc -ne 0 ]; then
        echo "  WARN: run $bondid exited rc=$rc — check $LOG"
    fi
    grep -E "^[a-zA-Z][a-zA-Z -]+: [0-9]+(\.[0-9]+)?(s|ms)$" "$LOG" | sed 's/^/    /'
done

echo
echo "===== Aggregated timings across $NUM_RUNS runs ====="
awk '
match($0, /^([a-zA-Z][a-zA-Z -]*): ([0-9]+(\.[0-9]+)?)(s|ms)$/, m) {
    label = m[1]
    val   = m[2] + 0
    unit  = m[4]
    if (unit == "ms") val = val / 1000.0
    sum[label]   += val
    cnt[label]   += 1
    order[label]  = (label in order) ? order[label] : (++n)
}
END {
    printf "%-26s %8s %12s\n", "command", "count", "avg (s)"
    printf "%-26s %8s %12s\n", "--------------------------", "--------", "------------"
    for (k in order) idx[order[k]] = k
    for (i = 1; i <= n; i++) {
        k = idx[i]
        printf "%-26s %8d %12.3f\n", k, cnt[k], sum[k] / cnt[k]
    }
}
' "$LOG_DIR"/*.log
