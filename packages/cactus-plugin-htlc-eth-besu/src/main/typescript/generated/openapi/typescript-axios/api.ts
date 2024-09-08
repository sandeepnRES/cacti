/* tslint:disable */
/* eslint-disable */
/**
 * Hyperledger Cactus Plugin - HTLC-ETH Besu
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.0.0-rc.4
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


import type { Configuration } from './configuration';
import type { AxiosPromise, AxiosInstance, AxiosRequestConfig } from 'axios';
import globalAxios from 'axios';
// Some imports not used depending on template conditions
// @ts-ignore
import { DUMMY_BASE_URL, assertParamExists, setApiKeyToObject, setBasicAuthToObject, setBearerAuthToObject, setOAuthToObject, setSearchParams, serializeDataIfNeeded, toPathString, createRequestFunction } from './common';
import type { RequestArgs } from './base';
// @ts-ignore
import { BASE_PATH, COLLECTION_FORMATS, BaseAPI, RequiredError } from './base';

/**
 * Defines the parameters for retrieving the single status of the HTLC swap.
 * @export
 * @interface GetSingleStatusRequest
 */
export interface GetSingleStatusRequest {
    /**
     * 
     * @type {string}
     * @memberof GetSingleStatusRequest
     */
    'id': string;
    /**
     * 
     * @type {Web3SigningCredential}
     * @memberof GetSingleStatusRequest
     */
    'web3SigningCredential': Web3SigningCredential;
    /**
     * 
     * @type {string}
     * @memberof GetSingleStatusRequest
     */
    'connectorId': string;
    /**
     * 
     * @type {string}
     * @memberof GetSingleStatusRequest
     */
    'keychainId': string;
}
/**
 * Defines the parameters for retrieving the status of the HTLC swap.
 * @export
 * @interface GetStatusRequest
 */
export interface GetStatusRequest {
    /**
     * 
     * @type {Array<string>}
     * @memberof GetStatusRequest
     */
    'ids': Array<string>;
    /**
     * 
     * @type {Web3SigningCredential}
     * @memberof GetStatusRequest
     */
    'web3SigningCredential': Web3SigningCredential;
    /**
     * 
     * @type {string}
     * @memberof GetStatusRequest
     */
    'connectorId': string;
    /**
     * 
     * @type {string}
     * @memberof GetStatusRequest
     */
    'keychainId': string;
}
/**
 * 
 * @export
 * @interface InitializeRequest
 */
export interface InitializeRequest {
    /**
     * connectorId for the connector besu plugin
     * @type {string}
     * @memberof InitializeRequest
     */
    'connectorId': string;
    /**
     * keychainId for the keychain plugin
     * @type {string}
     * @memberof InitializeRequest
     */
    'keychainId': string;
    /**
     * 
     * @type {Array<any>}
     * @memberof InitializeRequest
     */
    'constructorArgs': Array<any>;
    /**
     * 
     * @type {Web3SigningCredential}
     * @memberof InitializeRequest
     */
    'web3SigningCredential': Web3SigningCredential;
    /**
     * 
     * @type {number}
     * @memberof InitializeRequest
     */
    'gas'?: number;
}
/**
 * 
 * @export
 * @interface InvokeContractV1Response
 */
export interface InvokeContractV1Response {
    /**
     * 
     * @type {Web3TransactionReceipt}
     * @memberof InvokeContractV1Response
     */
    'transactionReceipt'?: Web3TransactionReceipt;
    /**
     * 
     * @type {any}
     * @memberof InvokeContractV1Response
     */
    'callOutput'?: any;
    /**
     * 
     * @type {boolean}
     * @memberof InvokeContractV1Response
     */
    'success': boolean;
}
/**
 * 
 * @export
 * @interface NewContractObj
 */
export interface NewContractObj {
    /**
     * Contract address
     * @type {string}
     * @memberof NewContractObj
     */
    'contractAddress': string;
    /**
     * 
     * @type {number}
     * @memberof NewContractObj
     */
    'inputAmount'?: number;
    /**
     * 
     * @type {number}
     * @memberof NewContractObj
     */
    'outputAmount': number;
    /**
     * 
     * @type {number}
     * @memberof NewContractObj
     */
    'expiration': number;
    /**
     * 
     * @type {string}
     * @memberof NewContractObj
     */
    'hashLock': string;
    /**
     * 
     * @type {string}
     * @memberof NewContractObj
     */
    'receiver'?: string;
    /**
     * 
     * @type {string}
     * @memberof NewContractObj
     */
    'outputNetwork': string;
    /**
     * 
     * @type {string}
     * @memberof NewContractObj
     */
    'outputAddress': string;
    /**
     * connectorId for the connector besu plugin
     * @type {string}
     * @memberof NewContractObj
     */
    'connectorId': string;
    /**
     * 
     * @type {Web3SigningCredential}
     * @memberof NewContractObj
     */
    'web3SigningCredential': Web3SigningCredential;
    /**
     * keychainId for the keychian plugin
     * @type {string}
     * @memberof NewContractObj
     */
    'keychainId': string;
    /**
     * 
     * @type {NewContractObjGas}
     * @memberof NewContractObj
     */
    'gas'?: NewContractObjGas;
}
/**
 * @type NewContractObjGas
 * @export
 */
export type NewContractObjGas = number | string;

/**
 * 
 * @export
 * @interface RefundReq
 */
export interface RefundReq {
    /**
     * Contract htlc id for refund
     * @type {string}
     * @memberof RefundReq
     */
    'id': string;
    /**
     * 
     * @type {Web3SigningCredential}
     * @memberof RefundReq
     */
    'web3SigningCredential': Web3SigningCredential;
    /**
     * connectorId for the connector besu plugin
     * @type {string}
     * @memberof RefundReq
     */
    'connectorId': string;
    /**
     * keychainId for the keychain plugin
     * @type {string}
     * @memberof RefundReq
     */
    'keychainId': string;
    /**
     * 
     * @type {NewContractObjGas}
     * @memberof RefundReq
     */
    'gas'?: NewContractObjGas;
}
/**
 * 
 * @export
 * @interface RunTransactionResponse
 */
export interface RunTransactionResponse {
    /**
     * 
     * @type {Web3TransactionReceipt}
     * @memberof RunTransactionResponse
     */
    'transactionReceipt': Web3TransactionReceipt;
}
/**
 * @type Web3SigningCredential
 * @export
 */
export type Web3SigningCredential = Web3SigningCredentialCactusKeychainRef | Web3SigningCredentialNone | Web3SigningCredentialPrivateKeyHex;

/**
 * 
 * @export
 * @interface Web3SigningCredentialCactusKeychainRef
 */
export interface Web3SigningCredentialCactusKeychainRef {
    /**
     * 
     * @type {Web3SigningCredentialType}
     * @memberof Web3SigningCredentialCactusKeychainRef
     */
    'type': Web3SigningCredentialType;
    /**
     * The ethereum account (public key) that the credential  belongs to. Basically the username in the traditional  terminology of authentication.
     * @type {string}
     * @memberof Web3SigningCredentialCactusKeychainRef
     */
    'ethAccount': string;
    /**
     * The key to use when looking up the the keychain entry holding the secret pointed to by the  keychainEntryKey parameter.
     * @type {string}
     * @memberof Web3SigningCredentialCactusKeychainRef
     */
    'keychainEntryKey': string;
    /**
     * The keychain ID to use when looking up the the keychain plugin instance that will be used to retrieve the secret pointed to by the keychainEntryKey parameter.
     * @type {string}
     * @memberof Web3SigningCredentialCactusKeychainRef
     */
    'keychainId': string;
}


/**
 * Using this denotes that there is no signing required because the transaction is pre-signed.
 * @export
 * @interface Web3SigningCredentialNone
 */
export interface Web3SigningCredentialNone {
    /**
     * 
     * @type {Web3SigningCredentialType}
     * @memberof Web3SigningCredentialNone
     */
    'type': Web3SigningCredentialType;
}


/**
 * 
 * @export
 * @interface Web3SigningCredentialPrivateKeyHex
 */
export interface Web3SigningCredentialPrivateKeyHex {
    /**
     * 
     * @type {Web3SigningCredentialType}
     * @memberof Web3SigningCredentialPrivateKeyHex
     */
    'type': Web3SigningCredentialType;
    /**
     * The ethereum account (public key) that the credential belongs to. Basically the username in the traditional terminology of authentication.
     * @type {string}
     * @memberof Web3SigningCredentialPrivateKeyHex
     */
    'ethAccount': string;
    /**
     * The HEX encoded private key of an eth account.
     * @type {string}
     * @memberof Web3SigningCredentialPrivateKeyHex
     */
    'secret': string;
}


/**
 * 
 * @export
 * @enum {string}
 */

export const Web3SigningCredentialType = {
    CactusKeychainRef: 'CACTUS_KEYCHAIN_REF',
    GethKeychainPassword: 'GETH_KEYCHAIN_PASSWORD',
    PrivateKeyHex: 'PRIVATE_KEY_HEX',
    None: 'NONE'
} as const;

export type Web3SigningCredentialType = typeof Web3SigningCredentialType[keyof typeof Web3SigningCredentialType];


/**
 * 
 * @export
 * @interface Web3TransactionReceipt
 */
export interface Web3TransactionReceipt {
    [key: string]: any;

    /**
     * 
     * @type {boolean}
     * @memberof Web3TransactionReceipt
     */
    'status': boolean;
    /**
     * 
     * @type {string}
     * @memberof Web3TransactionReceipt
     */
    'transactionHash': string;
    /**
     * 
     * @type {number}
     * @memberof Web3TransactionReceipt
     */
    'transactionIndex': number;
    /**
     * 
     * @type {string}
     * @memberof Web3TransactionReceipt
     */
    'blockHash': string;
    /**
     * 
     * @type {number}
     * @memberof Web3TransactionReceipt
     */
    'blockNumber': number;
    /**
     * 
     * @type {number}
     * @memberof Web3TransactionReceipt
     */
    'gasUsed': number;
    /**
     * 
     * @type {string}
     * @memberof Web3TransactionReceipt
     */
    'contractAddress'?: string | null;
    /**
     * 
     * @type {string}
     * @memberof Web3TransactionReceipt
     */
    'from': string;
    /**
     * 
     * @type {string}
     * @memberof Web3TransactionReceipt
     */
    'to': string;
}
/**
 * 
 * @export
 * @interface WithdrawReq
 */
export interface WithdrawReq {
    /**
     * Contract locked id
     * @type {string}
     * @memberof WithdrawReq
     */
    'id': string;
    /**
     * Secret need to unlock the contract
     * @type {string}
     * @memberof WithdrawReq
     */
    'secret': string;
    /**
     * 
     * @type {Web3SigningCredential}
     * @memberof WithdrawReq
     */
    'web3SigningCredential': Web3SigningCredential;
    /**
     * connectorId for the connector besu plugin
     * @type {string}
     * @memberof WithdrawReq
     */
    'connectorId': string;
    /**
     * keychainId for the keychain plugin
     * @type {string}
     * @memberof WithdrawReq
     */
    'keychainId': string;
    /**
     * 
     * @type {NewContractObjGas}
     * @memberof WithdrawReq
     */
    'gas'?: NewContractObjGas;
}

/**
 * DefaultApi - axios parameter creator
 * @export
 */
export const DefaultApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * 
         * @param {GetSingleStatusRequest} [getSingleStatusRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getSingleStatusV1: async (getSingleStatusRequest?: GetSingleStatusRequest, options: AxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/plugins/@hyperledger/cactus-plugin-htlc-eth-besu/get-single-status`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;


    
            localVarHeaderParameter['Content-Type'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(getSingleStatusRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * 
         * @param {GetStatusRequest} [getStatusRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getStatusV1: async (getStatusRequest?: GetStatusRequest, options: AxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/plugins/@hyperledger/cactus-plugin-htlc-eth-besu/get-status`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;


    
            localVarHeaderParameter['Content-Type'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(getStatusRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * 
         * @param {InitializeRequest} [initializeRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        initializeV1: async (initializeRequest?: InitializeRequest, options: AxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/plugins/@hyperledger/cactus-plugin-htlc-eth-besu/initialize`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;


    
            localVarHeaderParameter['Content-Type'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(initializeRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * 
         * @param {NewContractObj} [newContractObj] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        newContractV1: async (newContractObj?: NewContractObj, options: AxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/plugins/@hyperledger/cactus-plugin-htlc-eth-besu/new-contract`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;


    
            localVarHeaderParameter['Content-Type'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(newContractObj, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * 
         * @param {RefundReq} [refundReq] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        refundV1: async (refundReq?: RefundReq, options: AxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/plugins/@hyperledger/cactus-plugin-htlc-eth-besu/refund`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;


    
            localVarHeaderParameter['Content-Type'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(refundReq, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * 
         * @param {WithdrawReq} [withdrawReq] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        withdrawV1: async (withdrawReq?: WithdrawReq, options: AxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/plugins/@hyperledger/cactus-plugin-htlc-eth-besu/withdraw`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;


    
            localVarHeaderParameter['Content-Type'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(withdrawReq, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * DefaultApi - functional programming interface
 * @export
 */
export const DefaultApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = DefaultApiAxiosParamCreator(configuration)
    return {
        /**
         * 
         * @param {GetSingleStatusRequest} [getSingleStatusRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getSingleStatusV1(getSingleStatusRequest?: GetSingleStatusRequest, options?: AxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<number>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getSingleStatusV1(getSingleStatusRequest, options);
            return createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration);
        },
        /**
         * 
         * @param {GetStatusRequest} [getStatusRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getStatusV1(getStatusRequest?: GetStatusRequest, options?: AxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Array<number>>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getStatusV1(getStatusRequest, options);
            return createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration);
        },
        /**
         * 
         * @param {InitializeRequest} [initializeRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async initializeV1(initializeRequest?: InitializeRequest, options?: AxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<RunTransactionResponse>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.initializeV1(initializeRequest, options);
            return createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration);
        },
        /**
         * 
         * @param {NewContractObj} [newContractObj] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async newContractV1(newContractObj?: NewContractObj, options?: AxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<InvokeContractV1Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.newContractV1(newContractObj, options);
            return createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration);
        },
        /**
         * 
         * @param {RefundReq} [refundReq] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async refundV1(refundReq?: RefundReq, options?: AxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<InvokeContractV1Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.refundV1(refundReq, options);
            return createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration);
        },
        /**
         * 
         * @param {WithdrawReq} [withdrawReq] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async withdrawV1(withdrawReq?: WithdrawReq, options?: AxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<InvokeContractV1Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.withdrawV1(withdrawReq, options);
            return createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration);
        },
    }
};

/**
 * DefaultApi - factory interface
 * @export
 */
export const DefaultApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = DefaultApiFp(configuration)
    return {
        /**
         * 
         * @param {GetSingleStatusRequest} [getSingleStatusRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getSingleStatusV1(getSingleStatusRequest?: GetSingleStatusRequest, options?: any): AxiosPromise<number> {
            return localVarFp.getSingleStatusV1(getSingleStatusRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * 
         * @param {GetStatusRequest} [getStatusRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getStatusV1(getStatusRequest?: GetStatusRequest, options?: any): AxiosPromise<Array<number>> {
            return localVarFp.getStatusV1(getStatusRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * 
         * @param {InitializeRequest} [initializeRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        initializeV1(initializeRequest?: InitializeRequest, options?: any): AxiosPromise<RunTransactionResponse> {
            return localVarFp.initializeV1(initializeRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * 
         * @param {NewContractObj} [newContractObj] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        newContractV1(newContractObj?: NewContractObj, options?: any): AxiosPromise<InvokeContractV1Response> {
            return localVarFp.newContractV1(newContractObj, options).then((request) => request(axios, basePath));
        },
        /**
         * 
         * @param {RefundReq} [refundReq] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        refundV1(refundReq?: RefundReq, options?: any): AxiosPromise<InvokeContractV1Response> {
            return localVarFp.refundV1(refundReq, options).then((request) => request(axios, basePath));
        },
        /**
         * 
         * @param {WithdrawReq} [withdrawReq] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        withdrawV1(withdrawReq?: WithdrawReq, options?: any): AxiosPromise<InvokeContractV1Response> {
            return localVarFp.withdrawV1(withdrawReq, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * DefaultApi - object-oriented interface
 * @export
 * @class DefaultApi
 * @extends {BaseAPI}
 */
export class DefaultApi extends BaseAPI {
    /**
     * 
     * @param {GetSingleStatusRequest} [getSingleStatusRequest] 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof DefaultApi
     */
    public getSingleStatusV1(getSingleStatusRequest?: GetSingleStatusRequest, options?: AxiosRequestConfig) {
        return DefaultApiFp(this.configuration).getSingleStatusV1(getSingleStatusRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * 
     * @param {GetStatusRequest} [getStatusRequest] 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof DefaultApi
     */
    public getStatusV1(getStatusRequest?: GetStatusRequest, options?: AxiosRequestConfig) {
        return DefaultApiFp(this.configuration).getStatusV1(getStatusRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * 
     * @param {InitializeRequest} [initializeRequest] 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof DefaultApi
     */
    public initializeV1(initializeRequest?: InitializeRequest, options?: AxiosRequestConfig) {
        return DefaultApiFp(this.configuration).initializeV1(initializeRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * 
     * @param {NewContractObj} [newContractObj] 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof DefaultApi
     */
    public newContractV1(newContractObj?: NewContractObj, options?: AxiosRequestConfig) {
        return DefaultApiFp(this.configuration).newContractV1(newContractObj, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * 
     * @param {RefundReq} [refundReq] 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof DefaultApi
     */
    public refundV1(refundReq?: RefundReq, options?: AxiosRequestConfig) {
        return DefaultApiFp(this.configuration).refundV1(refundReq, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * 
     * @param {WithdrawReq} [withdrawReq] 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof DefaultApi
     */
    public withdrawV1(withdrawReq?: WithdrawReq, options?: AxiosRequestConfig) {
        return DefaultApiFp(this.configuration).withdrawV1(withdrawReq, options).then((request) => request(this.axios, this.basePath));
    }
}


