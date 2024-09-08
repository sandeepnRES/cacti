/*
Hyperledger Cactus Plugin - Connector Besu

Can perform basic tasks on a Besu ledger

API version: 2.0.0-rc.4
*/

// Code generated by OpenAPI Generator (https://openapi-generator.tech); DO NOT EDIT.

package cactus-plugin-ledger-connector-besu

import (
	"encoding/json"
)

// checks if the Web3SigningCredentialCactusKeychainRef type satisfies the MappedNullable interface at compile time
var _ MappedNullable = &Web3SigningCredentialCactusKeychainRef{}

// Web3SigningCredentialCactusKeychainRef struct for Web3SigningCredentialCactusKeychainRef
type Web3SigningCredentialCactusKeychainRef struct {
	Type Web3SigningCredentialType `json:"type"`
	// The ethereum account (public key) that the credential  belongs to. Basically the username in the traditional  terminology of authentication.
	EthAccount string `json:"ethAccount"`
	// The key to use when looking up the the keychain entry holding the secret pointed to by the  keychainEntryKey parameter.
	KeychainEntryKey string `json:"keychainEntryKey"`
	// The keychain ID to use when looking up the the keychain plugin instance that will be used to retrieve the secret pointed to by the keychainEntryKey parameter.
	KeychainId string `json:"keychainId"`
}

// NewWeb3SigningCredentialCactusKeychainRef instantiates a new Web3SigningCredentialCactusKeychainRef object
// This constructor will assign default values to properties that have it defined,
// and makes sure properties required by API are set, but the set of arguments
// will change when the set of required properties is changed
func NewWeb3SigningCredentialCactusKeychainRef(type_ Web3SigningCredentialType, ethAccount string, keychainEntryKey string, keychainId string) *Web3SigningCredentialCactusKeychainRef {
	this := Web3SigningCredentialCactusKeychainRef{}
	this.Type = type_
	this.EthAccount = ethAccount
	this.KeychainEntryKey = keychainEntryKey
	this.KeychainId = keychainId
	return &this
}

// NewWeb3SigningCredentialCactusKeychainRefWithDefaults instantiates a new Web3SigningCredentialCactusKeychainRef object
// This constructor will only assign default values to properties that have it defined,
// but it doesn't guarantee that properties required by API are set
func NewWeb3SigningCredentialCactusKeychainRefWithDefaults() *Web3SigningCredentialCactusKeychainRef {
	this := Web3SigningCredentialCactusKeychainRef{}
	return &this
}

// GetType returns the Type field value
func (o *Web3SigningCredentialCactusKeychainRef) GetType() Web3SigningCredentialType {
	if o == nil {
		var ret Web3SigningCredentialType
		return ret
	}

	return o.Type
}

// GetTypeOk returns a tuple with the Type field value
// and a boolean to check if the value has been set.
func (o *Web3SigningCredentialCactusKeychainRef) GetTypeOk() (*Web3SigningCredentialType, bool) {
	if o == nil {
		return nil, false
	}
	return &o.Type, true
}

// SetType sets field value
func (o *Web3SigningCredentialCactusKeychainRef) SetType(v Web3SigningCredentialType) {
	o.Type = v
}

// GetEthAccount returns the EthAccount field value
func (o *Web3SigningCredentialCactusKeychainRef) GetEthAccount() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.EthAccount
}

// GetEthAccountOk returns a tuple with the EthAccount field value
// and a boolean to check if the value has been set.
func (o *Web3SigningCredentialCactusKeychainRef) GetEthAccountOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.EthAccount, true
}

// SetEthAccount sets field value
func (o *Web3SigningCredentialCactusKeychainRef) SetEthAccount(v string) {
	o.EthAccount = v
}

// GetKeychainEntryKey returns the KeychainEntryKey field value
func (o *Web3SigningCredentialCactusKeychainRef) GetKeychainEntryKey() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.KeychainEntryKey
}

// GetKeychainEntryKeyOk returns a tuple with the KeychainEntryKey field value
// and a boolean to check if the value has been set.
func (o *Web3SigningCredentialCactusKeychainRef) GetKeychainEntryKeyOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.KeychainEntryKey, true
}

// SetKeychainEntryKey sets field value
func (o *Web3SigningCredentialCactusKeychainRef) SetKeychainEntryKey(v string) {
	o.KeychainEntryKey = v
}

// GetKeychainId returns the KeychainId field value
func (o *Web3SigningCredentialCactusKeychainRef) GetKeychainId() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.KeychainId
}

// GetKeychainIdOk returns a tuple with the KeychainId field value
// and a boolean to check if the value has been set.
func (o *Web3SigningCredentialCactusKeychainRef) GetKeychainIdOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.KeychainId, true
}

// SetKeychainId sets field value
func (o *Web3SigningCredentialCactusKeychainRef) SetKeychainId(v string) {
	o.KeychainId = v
}

func (o Web3SigningCredentialCactusKeychainRef) MarshalJSON() ([]byte, error) {
	toSerialize,err := o.ToMap()
	if err != nil {
		return []byte{}, err
	}
	return json.Marshal(toSerialize)
}

func (o Web3SigningCredentialCactusKeychainRef) ToMap() (map[string]interface{}, error) {
	toSerialize := map[string]interface{}{}
	toSerialize["type"] = o.Type
	toSerialize["ethAccount"] = o.EthAccount
	toSerialize["keychainEntryKey"] = o.KeychainEntryKey
	toSerialize["keychainId"] = o.KeychainId
	return toSerialize, nil
}

type NullableWeb3SigningCredentialCactusKeychainRef struct {
	value *Web3SigningCredentialCactusKeychainRef
	isSet bool
}

func (v NullableWeb3SigningCredentialCactusKeychainRef) Get() *Web3SigningCredentialCactusKeychainRef {
	return v.value
}

func (v *NullableWeb3SigningCredentialCactusKeychainRef) Set(val *Web3SigningCredentialCactusKeychainRef) {
	v.value = val
	v.isSet = true
}

func (v NullableWeb3SigningCredentialCactusKeychainRef) IsSet() bool {
	return v.isSet
}

func (v *NullableWeb3SigningCredentialCactusKeychainRef) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableWeb3SigningCredentialCactusKeychainRef(val *Web3SigningCredentialCactusKeychainRef) *NullableWeb3SigningCredentialCactusKeychainRef {
	return &NullableWeb3SigningCredentialCactusKeychainRef{value: val, isSet: true}
}

func (v NullableWeb3SigningCredentialCactusKeychainRef) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableWeb3SigningCredentialCactusKeychainRef) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}


