/*
Hyperledger Cacti Plugin - Connector Ethereum

Can perform basic tasks on a Ethereum ledger

API version: 2.0.0-rc.4
*/

// Code generated by OpenAPI Generator (https://openapi-generator.tech); DO NOT EDIT.

package cactus-plugin-ledger-connector-ethereum

import (
	"encoding/json"
)

// checks if the Web3SigningCredentialGethKeychainPassword type satisfies the MappedNullable interface at compile time
var _ MappedNullable = &Web3SigningCredentialGethKeychainPassword{}

// Web3SigningCredentialGethKeychainPassword struct for Web3SigningCredentialGethKeychainPassword
type Web3SigningCredentialGethKeychainPassword struct {
	Type Web3SigningCredentialType `json:"type"`
	// The ethereum account (public key) that the credential  belongs to. Basically the username in the traditional terminology of authentication.
	EthAccount string `json:"ethAccount"`
	// A geth keychain unlock password.
	Secret string `json:"secret"`
}

// NewWeb3SigningCredentialGethKeychainPassword instantiates a new Web3SigningCredentialGethKeychainPassword object
// This constructor will assign default values to properties that have it defined,
// and makes sure properties required by API are set, but the set of arguments
// will change when the set of required properties is changed
func NewWeb3SigningCredentialGethKeychainPassword(type_ Web3SigningCredentialType, ethAccount string, secret string) *Web3SigningCredentialGethKeychainPassword {
	this := Web3SigningCredentialGethKeychainPassword{}
	this.Type = type_
	this.EthAccount = ethAccount
	this.Secret = secret
	return &this
}

// NewWeb3SigningCredentialGethKeychainPasswordWithDefaults instantiates a new Web3SigningCredentialGethKeychainPassword object
// This constructor will only assign default values to properties that have it defined,
// but it doesn't guarantee that properties required by API are set
func NewWeb3SigningCredentialGethKeychainPasswordWithDefaults() *Web3SigningCredentialGethKeychainPassword {
	this := Web3SigningCredentialGethKeychainPassword{}
	return &this
}

// GetType returns the Type field value
func (o *Web3SigningCredentialGethKeychainPassword) GetType() Web3SigningCredentialType {
	if o == nil {
		var ret Web3SigningCredentialType
		return ret
	}

	return o.Type
}

// GetTypeOk returns a tuple with the Type field value
// and a boolean to check if the value has been set.
func (o *Web3SigningCredentialGethKeychainPassword) GetTypeOk() (*Web3SigningCredentialType, bool) {
	if o == nil {
		return nil, false
	}
	return &o.Type, true
}

// SetType sets field value
func (o *Web3SigningCredentialGethKeychainPassword) SetType(v Web3SigningCredentialType) {
	o.Type = v
}

// GetEthAccount returns the EthAccount field value
func (o *Web3SigningCredentialGethKeychainPassword) GetEthAccount() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.EthAccount
}

// GetEthAccountOk returns a tuple with the EthAccount field value
// and a boolean to check if the value has been set.
func (o *Web3SigningCredentialGethKeychainPassword) GetEthAccountOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.EthAccount, true
}

// SetEthAccount sets field value
func (o *Web3SigningCredentialGethKeychainPassword) SetEthAccount(v string) {
	o.EthAccount = v
}

// GetSecret returns the Secret field value
func (o *Web3SigningCredentialGethKeychainPassword) GetSecret() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.Secret
}

// GetSecretOk returns a tuple with the Secret field value
// and a boolean to check if the value has been set.
func (o *Web3SigningCredentialGethKeychainPassword) GetSecretOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.Secret, true
}

// SetSecret sets field value
func (o *Web3SigningCredentialGethKeychainPassword) SetSecret(v string) {
	o.Secret = v
}

func (o Web3SigningCredentialGethKeychainPassword) MarshalJSON() ([]byte, error) {
	toSerialize,err := o.ToMap()
	if err != nil {
		return []byte{}, err
	}
	return json.Marshal(toSerialize)
}

func (o Web3SigningCredentialGethKeychainPassword) ToMap() (map[string]interface{}, error) {
	toSerialize := map[string]interface{}{}
	toSerialize["type"] = o.Type
	toSerialize["ethAccount"] = o.EthAccount
	toSerialize["secret"] = o.Secret
	return toSerialize, nil
}

type NullableWeb3SigningCredentialGethKeychainPassword struct {
	value *Web3SigningCredentialGethKeychainPassword
	isSet bool
}

func (v NullableWeb3SigningCredentialGethKeychainPassword) Get() *Web3SigningCredentialGethKeychainPassword {
	return v.value
}

func (v *NullableWeb3SigningCredentialGethKeychainPassword) Set(val *Web3SigningCredentialGethKeychainPassword) {
	v.value = val
	v.isSet = true
}

func (v NullableWeb3SigningCredentialGethKeychainPassword) IsSet() bool {
	return v.isSet
}

func (v *NullableWeb3SigningCredentialGethKeychainPassword) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableWeb3SigningCredentialGethKeychainPassword(val *Web3SigningCredentialGethKeychainPassword) *NullableWeb3SigningCredentialGethKeychainPassword {
	return &NullableWeb3SigningCredentialGethKeychainPassword{value: val, isSet: true}
}

func (v NullableWeb3SigningCredentialGethKeychainPassword) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableWeb3SigningCredentialGethKeychainPassword) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}


