window.BENCHMARK_DATA = {
  "lastUpdate": 1782135452852,
  "repoUrl": "https://github.com/sandeepnRES/cacti",
  "entries": {
    "Benchmark": [
      {
        "commit": {
          "author": {
            "name": "Sandeep Nishad",
            "username": "sandeepnRES",
            "email": "sandeepn.official@gmail.com"
          },
          "committer": {
            "name": "Sandeep Nishad",
            "username": "sandeepnRES",
            "email": "sandeepn.official@gmail.com"
          },
          "id": "31ba59a4542d06328e667dd380845560088b27ff",
          "message": "ci: validate PR type and verify PR title matches commit message\n\nAssisted-by: Google:Gemini\nSigned-off-by: Sandeep Nishad <sandeepn.official@gmail.com>",
          "timestamp": "2026-05-18T08:26:42Z",
          "url": "https://github.com/sandeepnRES/cacti/commit/31ba59a4542d06328e667dd380845560088b27ff"
        },
        "date": 1782135450604,
        "tool": "benchmarkjs",
        "benches": [
          {
            "name": "cmd-api-server_HTTP_GET_getOpenApiSpecV1",
            "value": 642,
            "range": "±3.55%",
            "unit": "ops/sec",
            "extra": "176 samples"
          },
          {
            "name": "cmd-api-server_gRPC_GetOpenApiSpecV1",
            "value": 660,
            "range": "±2.13%",
            "unit": "ops/sec",
            "extra": "184 samples"
          }
        ]
      }
    ]
  }
}