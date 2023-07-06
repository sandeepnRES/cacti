import http from "http";
import type { AddressInfo } from "net";
import { v4 as uuidv4 } from "uuid";
import express from "express";
import "jest-extended";
import bodyParser from "body-parser";
import Web3 from "web3";
import {
  Configuration,
  DefaultApi as BesuApi,
  IPluginHtlcEthBesuOptions,
  PluginFactoryHtlcEthBesu,
  NewContractObj,
  InitializeRequest,
  RefundReq,
} from "@hyperledger/cactus-plugin-htlc-eth-besu";
import {
  EthContractInvocationType,
  PluginFactoryLedgerConnector,
  PluginLedgerConnectorBesu,
  Web3SigningCredential,
  Web3SigningCredentialType,
} from "@hyperledger/cactus-plugin-ledger-connector-besu";
import {
  LogLevelDesc,
  IListenOptions,
  Servers,
} from "@hyperledger/cactus-common";
import { PluginRegistry } from "@hyperledger/cactus-core";
import { PluginImportType } from "@hyperledger/cactus-core-api";
import {
  BesuTestLedger,
  pruneDockerAllIfGithubAction,
} from "@hyperledger/cactus-test-tooling";
import { PluginKeychainMemory } from "@hyperledger/cactus-plugin-keychain-memory";
import { DataTest } from "../data-test";
import DemoHelperJSON from "../../../solidity/contracts/DemoHelpers.json";
import HashTimeLockJSON from "../../../../../../cactus-plugin-htlc-eth-besu/src/main/solidity/contracts/HashTimeLock.json";

const connectorId = uuidv4();
const logLevel: LogLevelDesc = "INFO";
const testCase = "Test refund";

const FORTY_TWO_AS_HEX_STRING =
  "0x0000000000000000000000000000000000000000000000000000000000003432";

describe(testCase, () => {
  const besuTestLedger = new BesuTestLedger({ logLevel });
  const expressApp = express();
  expressApp.use(bodyParser.json({ limit: "250mb" }));
  const server = http.createServer(expressApp);
  const listenOptions: IListenOptions = {
    hostname: "localhost",
    port: 0,
    server,
  };

  beforeAll(async () => {
    const pruning = pruneDockerAllIfGithubAction({ logLevel });
    await expect(pruning).resolves.toBeTruthy();
  });

  afterAll(async () => {
    const pruning = pruneDockerAllIfGithubAction({ logLevel });
    await expect(pruning).resolves.toBeTruthy();
  });

  afterAll(async () => {
    await besuTestLedger.stop();
    await besuTestLedger.destroy();
    await pruneDockerAllIfGithubAction({ logLevel });
  });

  afterAll(async () => await Servers.shutdown(server));

  beforeAll(async () => {
    await besuTestLedger.start();
  });

  test(testCase, async () => {
    const timeout = (ms: number) => {
      return new Promise((resolve) => setTimeout(resolve, ms));
    };
    const rpcApiHttpHost = await besuTestLedger.getRpcApiHttpHost();
    const rpcApiWsHost = await besuTestLedger.getRpcApiWsHost();
    const firstHighNetWorthAccount = besuTestLedger.getGenesisAccountPubKey();
    const privateKey = besuTestLedger.getGenesisAccountPrivKey();
    const web3SigningCredential: Web3SigningCredential = {
      ethAccount: firstHighNetWorthAccount,
      secret: privateKey,
      type: Web3SigningCredentialType.PrivateKeyHex,
    } as Web3SigningCredential;

    const keychainId = uuidv4();
    const keychainPlugin = new PluginKeychainMemory({
      instanceId: uuidv4(),
      keychainId,
      // pre-provision keychain with mock backend holding the private key of the
      // test account that we'll reference while sending requests with the
      // signing credential pointing to this keychain entry.
      backend: new Map([
        [DemoHelperJSON.contractName, JSON.stringify(DemoHelperJSON)],
      ]),
      logLevel,
    });
    keychainPlugin.set(
      HashTimeLockJSON.contractName,
      JSON.stringify(HashTimeLockJSON),
    );

    const factory = new PluginFactoryLedgerConnector({
      pluginImportType: PluginImportType.Local,
    });

    const pluginRegistry = new PluginRegistry({});
    const connector: PluginLedgerConnectorBesu = await factory.create({
      rpcApiHttpHost,
      rpcApiWsHost,
      logLevel,
      instanceId: connectorId,
      pluginRegistry: new PluginRegistry({ plugins: [keychainPlugin] }),
    });

    pluginRegistry.add(connector);
    const pluginOptions: IPluginHtlcEthBesuOptions = {
      logLevel,
      instanceId: uuidv4(),
      pluginRegistry,
    };

    const factoryHTLC = new PluginFactoryHtlcEthBesu({
      pluginImportType: PluginImportType.Local,
    });

    const pluginHtlc = await factoryHTLC.create(pluginOptions);
    pluginRegistry.add(pluginHtlc);

    const addressInfo = (await Servers.listen(listenOptions)) as AddressInfo;
    const { address, port } = addressInfo;
    const apiHost = `http://${address}:${port}`;

    const configuration = new Configuration({ basePath: apiHost });
    const api = new BesuApi(configuration);

    await pluginHtlc.getOrCreateWebServices();
    await pluginHtlc.registerWebServices(expressApp);

    const web3 = new Web3(rpcApiHttpHost);

    const initRequest: InitializeRequest = {
      connectorId,
      keychainId,
      constructorArgs: [],
      web3SigningCredential,
      gas: DataTest.estimated_gas,
    };
    const deployOut = await pluginHtlc.initialize(initRequest);
    expect(deployOut.transactionReceipt).toBeTruthy();
    expect(deployOut.transactionReceipt.contractAddress).toBeTruthy();
    const hashTimeLockAddress = deployOut.transactionReceipt
      .contractAddress as string;

    //Deploy DemoHelpers
    const deployOutDemo = await connector.deployContract({
      contractName: DemoHelperJSON.contractName,
      contractAbi: DemoHelperJSON.abi,
      bytecode: DemoHelperJSON.bytecode,
      web3SigningCredential,
      keychainId,
      constructorArgs: [],
      gas: DataTest.estimated_gas,
    });
    expect(deployOutDemo).toBeTruthy();
    expect(deployOutDemo.transactionReceipt).toBeTruthy();
    expect(deployOutDemo.transactionReceipt.contractAddress).toBeTruthy();

    const balance1 = await web3.eth.getBalance(firstHighNetWorthAccount);

    const { callOutput } = await connector.invokeContract({
      contractName: DemoHelperJSON.contractName,
      keychainId,
      signingCredential: web3SigningCredential,
      invocationType: EthContractInvocationType.Call,
      methodName: "getTimestamp",
      params: [],
    });
    expect(callOutput).toBeTruthy();
    let timestamp = 0;
    timestamp = callOutput as number;
    timestamp = +timestamp + +10;

    const bodyObj: NewContractObj = {
      contractAddress: hashTimeLockAddress,
      inputAmount: 10,
      outputAmount: 0x04,
      expiration: timestamp,
      hashLock: FORTY_TWO_AS_HEX_STRING,
      receiver: DataTest.receiver,
      outputNetwork: "BTC",
      outputAddress: "1AcVYm7M3kkJQH28FXAvyBFQzFRL6xPKu8",
      connectorId: connectorId,
      web3SigningCredential,
      keychainId,
      gas: DataTest.estimated_gas,
    };
    const resp = await api.newContractV1(bodyObj);
    expect(resp).toBeTruthy();
    expect(resp.status).toEqual(200);

    const id = web3.utils.soliditySha3(
      web3.eth.abi.encodeParameters(
        ["address", "address", "uint256", "bytes32", "uint256"],
        [
          firstHighNetWorthAccount,
          DataTest.receiver,
          10,
          FORTY_TWO_AS_HEX_STRING,
          timestamp,
        ],
      ),
    );
    expect(id).toBeTruthy();
    expect(id).toBeString();

    await timeout(6000);
    const refundRequest: RefundReq = {
      id: id as string,
      web3SigningCredential,
      connectorId,
      keychainId,
    };
    const refundResponse = await api.refundV1(refundRequest);
    expect(refundResponse.status).toEqual(200);

    const balance = await web3.eth.getBalance(firstHighNetWorthAccount);
    expect(parseInt(balance)).toEqual(parseInt(balance1) - 10);
    const balance2 = await web3.eth.getBalance(firstHighNetWorthAccount);
    expect(balance1).toEqual(balance2);
    const res = await api.getSingleStatusV1({
      id: id as string,
      web3SigningCredential,
      connectorId,
      keychainId,
    });
    expect(res.status).toEqual(200);
    expect(res.data).toEqual(2);
  });
});
