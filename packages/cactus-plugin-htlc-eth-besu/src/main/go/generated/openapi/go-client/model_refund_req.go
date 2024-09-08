/*
Hyperledger Cactus Plugin - HTLC-ETH Besu

No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)

API version: 2.0.0-rc.4
*/

// Code generated by OpenAPI Generator (https://openapi-generator.tech); DO NOT EDIT.

package cactus-plugin-htlc-eth-besu

import (
	"encoding/json"
)

// checks if the RefundReq type satisfies the MappedNullable interface at compile time
var _ MappedNullable = &RefundReq{}

// RefundReq struct for RefundReq
type RefundReq struct {
	// Contract htlc id for refund
	Id string `json:"id"`
	Web3SigningCredential Web3SigningCredential `json:"web3SigningCredential"`
	// connectorId for the connector besu plugin
	ConnectorId string `json:"connectorId"`
	// keychainId for the keychain plugin
	KeychainId string `json:"keychainId"`
	Gas *NewContractObjGas `json:"gas,omitempty"`
}

// NewRefundReq instantiates a new RefundReq object
// This constructor will assign default values to properties that have it defined,
// and makes sure properties required by API are set, but the set of arguments
// will change when the set of required properties is changed
func NewRefundReq(id string, web3SigningCredential Web3SigningCredential, connectorId string, keychainId string) *RefundReq {
	this := RefundReq{}
	this.Id = id
	this.Web3SigningCredential = web3SigningCredential
	this.ConnectorId = connectorId
	this.KeychainId = keychainId
	return &this
}

// NewRefundReqWithDefaults instantiates a new RefundReq object
// This constructor will only assign default values to properties that have it defined,
// but it doesn't guarantee that properties required by API are set
func NewRefundReqWithDefaults() *RefundReq {
	this := RefundReq{}
	return &this
}

// GetId returns the Id field value
func (o *RefundReq) GetId() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.Id
}

// GetIdOk returns a tuple with the Id field value
// and a boolean to check if the value has been set.
func (o *RefundReq) GetIdOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.Id, true
}

// SetId sets field value
func (o *RefundReq) SetId(v string) {
	o.Id = v
}

// GetWeb3SigningCredential returns the Web3SigningCredential field value
func (o *RefundReq) GetWeb3SigningCredential() Web3SigningCredential {
	if o == nil {
		var ret Web3SigningCredential
		return ret
	}

	return o.Web3SigningCredential
}

// GetWeb3SigningCredentialOk returns a tuple with the Web3SigningCredential field value
// and a boolean to check if the value has been set.
func (o *RefundReq) GetWeb3SigningCredentialOk() (*Web3SigningCredential, bool) {
	if o == nil {
		return nil, false
	}
	return &o.Web3SigningCredential, true
}

// SetWeb3SigningCredential sets field value
func (o *RefundReq) SetWeb3SigningCredential(v Web3SigningCredential) {
	o.Web3SigningCredential = v
}

// GetConnectorId returns the ConnectorId field value
func (o *RefundReq) GetConnectorId() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.ConnectorId
}

// GetConnectorIdOk returns a tuple with the ConnectorId field value
// and a boolean to check if the value has been set.
func (o *RefundReq) GetConnectorIdOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.ConnectorId, true
}

// SetConnectorId sets field value
func (o *RefundReq) SetConnectorId(v string) {
	o.ConnectorId = v
}

// GetKeychainId returns the KeychainId field value
func (o *RefundReq) GetKeychainId() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.KeychainId
}

// GetKeychainIdOk returns a tuple with the KeychainId field value
// and a boolean to check if the value has been set.
func (o *RefundReq) GetKeychainIdOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.KeychainId, true
}

// SetKeychainId sets field value
func (o *RefundReq) SetKeychainId(v string) {
	o.KeychainId = v
}

// GetGas returns the Gas field value if set, zero value otherwise.
func (o *RefundReq) GetGas() NewContractObjGas {
	if o == nil || IsNil(o.Gas) {
		var ret NewContractObjGas
		return ret
	}
	return *o.Gas
}

// GetGasOk returns a tuple with the Gas field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *RefundReq) GetGasOk() (*NewContractObjGas, bool) {
	if o == nil || IsNil(o.Gas) {
		return nil, false
	}
	return o.Gas, true
}

// HasGas returns a boolean if a field has been set.
func (o *RefundReq) HasGas() bool {
	if o != nil && !IsNil(o.Gas) {
		return true
	}

	return false
}

// SetGas gets a reference to the given NewContractObjGas and assigns it to the Gas field.
func (o *RefundReq) SetGas(v NewContractObjGas) {
	o.Gas = &v
}

func (o RefundReq) MarshalJSON() ([]byte, error) {
	toSerialize,err := o.ToMap()
	if err != nil {
		return []byte{}, err
	}
	return json.Marshal(toSerialize)
}

func (o RefundReq) ToMap() (map[string]interface{}, error) {
	toSerialize := map[string]interface{}{}
	toSerialize["id"] = o.Id
	toSerialize["web3SigningCredential"] = o.Web3SigningCredential
	toSerialize["connectorId"] = o.ConnectorId
	toSerialize["keychainId"] = o.KeychainId
	if !IsNil(o.Gas) {
		toSerialize["gas"] = o.Gas
	}
	return toSerialize, nil
}

type NullableRefundReq struct {
	value *RefundReq
	isSet bool
}

func (v NullableRefundReq) Get() *RefundReq {
	return v.value
}

func (v *NullableRefundReq) Set(val *RefundReq) {
	v.value = val
	v.isSet = true
}

func (v NullableRefundReq) IsSet() bool {
	return v.isSet
}

func (v *NullableRefundReq) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableRefundReq(val *RefundReq) *NullableRefundReq {
	return &NullableRefundReq{value: val, isSet: true}
}

func (v NullableRefundReq) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableRefundReq) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}


