window.BENCHMARK_DATA = {
  "lastUpdate": 1788438993297,
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
          "id": "9e51cafdfefddd59d5efb0777850ef81deda98b4",
          "message": "ci(docs): version docs publishing with mike\n\nPublish versioned docs via mike instead of mkdocs gh-deploy: tag pushes\ndeploy <version> with a moving `latest` alias (default), main pushes\nrefresh `dev`. main is gated on doc-path changes; tags always publish.\n\nAssisted-by: Claude Opus 4.8\n\nSigned-off-by: Sandeep Nishad <sandeepn.official@gmail.com>",
          "timestamp": "2026-09-01T20:40:00Z",
          "url": "https://github.com/sandeepnRES/cacti/commit/9e51cafdfefddd59d5efb0777850ef81deda98b4"
        },
        "date": 1788438569611,
        "tool": "benchmarkjs",
        "benches": [
          {
            "name": "cmd-api-server_HTTP_GET_getOpenApiSpecV1",
            "value": 636,
            "range": "±3.51%",
            "unit": "ops/sec",
            "extra": "174 samples"
          },
          {
            "name": "cmd-api-server_gRPC_GetOpenApiSpecV1",
            "value": 653,
            "range": "±1.98%",
            "unit": "ops/sec",
            "extra": "185 samples"
          }
        ]
      },
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
          "id": "9e51cafdfefddd59d5efb0777850ef81deda98b4",
          "message": "ci(docs): version docs publishing with mike\n\nPublish versioned docs via mike instead of mkdocs gh-deploy: tag pushes\ndeploy <version> with a moving `latest` alias (default), main pushes\nrefresh `dev`. main is gated on doc-path changes; tags always publish.\n\nAssisted-by: Claude Opus 4.8\n\nSigned-off-by: Sandeep Nishad <sandeepn.official@gmail.com>",
          "timestamp": "2026-09-01T20:40:00Z",
          "url": "https://github.com/sandeepnRES/cacti/commit/9e51cafdfefddd59d5efb0777850ef81deda98b4"
        },
        "date": 1788438989472,
        "tool": "benchmarkjs",
        "benches": [
          {
            "name": "plugin-ledger-connector-besu_HTTP_GET_getOpenApiSpecV1",
            "value": 1186,
            "range": "±2.95%",
            "unit": "ops/sec",
            "extra": "178 samples"
          }
        ]
      }
    ]
  }
}