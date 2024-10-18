window.BENCHMARK_DATA = {
  "lastUpdate": 1729242977176,
  "repoUrl": "https://github.com/sandeepnRES/cacti",
  "entries": {
    "Benchmark": [
      {
        "commit": {
          "author": {
            "email": "peter.somogyvari@accenture.com",
            "name": "Peter Somogyvari",
            "username": "petermetz"
          },
          "committer": {
            "email": "petermetz@users.noreply.github.com",
            "name": "Peter Somogyvari",
            "username": "petermetz"
          },
          "distinct": true,
          "id": "11dacbcef25ba3e7fa9f9880f60655be1e2396ef",
          "message": "fix(besu): deployContractSolBytecodeNoKeychainV1 requires keychainId\n\nIn the DeployContractSolidityBytecodeNoKeychainV1Request of\n`packages/cactus-plugin-ledger-connector-besu/src/main/json/openapi.tpl.json`\nthere are parameters that are required despite the entire point of this\noperation is to not need them (e.g. keychainId and contract JSON object).\n\nFixes #3586\n\nSigned-off-by: Peter Somogyvari <peter.somogyvari@accenture.com>",
          "timestamp": "2024-10-17T16:16:32-07:00",
          "tree_id": "5802f84f4083cdb62a4cb6a485110b28a97e5386",
          "url": "https://github.com/sandeepnRES/cacti/commit/11dacbcef25ba3e7fa9f9880f60655be1e2396ef"
        },
        "date": 1729242974722,
        "tool": "benchmarkjs",
        "benches": [
          {
            "name": "plugin-ledger-connector-besu_HTTP_GET_getOpenApiSpecV1",
            "value": 736,
            "range": "±3.12%",
            "unit": "ops/sec",
            "extra": "181 samples"
          }
        ]
      }
    ]
  }
}