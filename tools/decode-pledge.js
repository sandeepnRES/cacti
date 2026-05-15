#!/usr/bin/env node
// Decode a base64-encoded AssetPledge protobuf using the local protos-js build.
//
// Usage:
//   node tools/decode-pledge.js <base64-pledge-string>
//   echo '<base64...>' | node tools/decode-pledge.js
//
// Requires: weaver/common/protos-js to be built (`cd weaver/common/protos-js && make build`)
// so that the compiled JS classes are available.

const path = require('path');

// Resolve the local protos-js workspace next to this script.
const protosJsRoot = path.resolve(__dirname, '..', 'weaver', 'common', 'protos-js');

let assetTransferPb;
try {
  assetTransferPb = require(path.join(protosJsRoot, 'common', 'asset_transfer_pb.js'));
} catch (err) {
  console.error('Failed to load protos-js. Build it first:');
  console.error(`  cd ${protosJsRoot} && make build`);
  console.error('Underlying error:', err.message);
  process.exit(2);
}

const { AssetPledge } = assetTransferPb;

function read() {
  if (process.argv[2]) return process.argv[2].trim();
  // Else read all of stdin.
  const chunks = [];
  return new Promise((resolve) => {
    process.stdin.setEncoding('utf8');
    process.stdin.on('data', (c) => chunks.push(c));
    process.stdin.on('end', () => resolve(chunks.join('').trim()));
  });
}

(async () => {
  const b64 = await read();
  if (!b64) {
    console.error('Provide the base64 string as arg1 or via stdin.');
    process.exit(1);
  }

  const buf = Buffer.from(b64, 'base64');
  const pledge = AssetPledge.deserializeBinary(buf);

  const recipientCert = pledge.getRecipient();
  // Try to render the recipient as PEM if it's already a PEM cert (the chaincode
  // typically stores cert PEM here; sometimes double-base64-encoded for transport).
  let recipientPretty = recipientCert;
  if (/^[A-Za-z0-9+/=\s]+$/.test(recipientCert) && !recipientCert.startsWith('-----BEGIN')) {
    try {
      recipientPretty = Buffer.from(recipientCert, 'base64').toString('utf8');
    } catch (_) { /* keep raw */ }
  }

  const expiry = Number(pledge.getExpirytimesecs());
  const expiryDate = new Date(expiry * 1000);
  const nowSecs = Math.floor(Date.now() / 1000);

  console.log(JSON.stringify({
    localNetworkID:  pledge.getLocalnetworkid(),
    remoteNetworkID: pledge.getRemotenetworkid(),
    expiryTimeSecs:  expiry,
    expiryUTC:       expiryDate.toISOString(),
    secondsRemaining: expiry - nowSecs,
    expired:         (nowSecs >= expiry),
    assetDetails:    Buffer.from(pledge.getAssetdetails()).toString('utf8'),
    pledgeCondition: pledge.getPledgecondition_asU8().length > 0
      ? Buffer.from(pledge.getPledgecondition()).toString('utf8')
      : '',
    recipient:       recipientPretty,
  }, null, 2));
})();
