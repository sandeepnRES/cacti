/*
Hyperledger Cactus Plugin - Connector Iroha V2

Can perform basic tasks on a Iroha V2 ledger

API version: 2.0.0-rc.4
*/

// Code generated by OpenAPI Generator (https://openapi-generator.tech); DO NOT EDIT.

package cactus-plugin-ledger-connector-iroha2

import (
	"encoding/json"
)

// checks if the Iroha2KeyPair type satisfies the MappedNullable interface at compile time
var _ MappedNullable = &Iroha2KeyPair{}

// Iroha2KeyPair Pair of Iroha account private and public keys.
type Iroha2KeyPair struct {
	PrivateKey Iroha2KeyJson `json:"privateKey"`
	PublicKey string `json:"publicKey"`
}

// NewIroha2KeyPair instantiates a new Iroha2KeyPair object
// This constructor will assign default values to properties that have it defined,
// and makes sure properties required by API are set, but the set of arguments
// will change when the set of required properties is changed
func NewIroha2KeyPair(privateKey Iroha2KeyJson, publicKey string) *Iroha2KeyPair {
	this := Iroha2KeyPair{}
	this.PrivateKey = privateKey
	this.PublicKey = publicKey
	return &this
}

// NewIroha2KeyPairWithDefaults instantiates a new Iroha2KeyPair object
// This constructor will only assign default values to properties that have it defined,
// but it doesn't guarantee that properties required by API are set
func NewIroha2KeyPairWithDefaults() *Iroha2KeyPair {
	this := Iroha2KeyPair{}
	return &this
}

// GetPrivateKey returns the PrivateKey field value
func (o *Iroha2KeyPair) GetPrivateKey() Iroha2KeyJson {
	if o == nil {
		var ret Iroha2KeyJson
		return ret
	}

	return o.PrivateKey
}

// GetPrivateKeyOk returns a tuple with the PrivateKey field value
// and a boolean to check if the value has been set.
func (o *Iroha2KeyPair) GetPrivateKeyOk() (*Iroha2KeyJson, bool) {
	if o == nil {
		return nil, false
	}
	return &o.PrivateKey, true
}

// SetPrivateKey sets field value
func (o *Iroha2KeyPair) SetPrivateKey(v Iroha2KeyJson) {
	o.PrivateKey = v
}

// GetPublicKey returns the PublicKey field value
func (o *Iroha2KeyPair) GetPublicKey() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.PublicKey
}

// GetPublicKeyOk returns a tuple with the PublicKey field value
// and a boolean to check if the value has been set.
func (o *Iroha2KeyPair) GetPublicKeyOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.PublicKey, true
}

// SetPublicKey sets field value
func (o *Iroha2KeyPair) SetPublicKey(v string) {
	o.PublicKey = v
}

func (o Iroha2KeyPair) MarshalJSON() ([]byte, error) {
	toSerialize,err := o.ToMap()
	if err != nil {
		return []byte{}, err
	}
	return json.Marshal(toSerialize)
}

func (o Iroha2KeyPair) ToMap() (map[string]interface{}, error) {
	toSerialize := map[string]interface{}{}
	toSerialize["privateKey"] = o.PrivateKey
	toSerialize["publicKey"] = o.PublicKey
	return toSerialize, nil
}

type NullableIroha2KeyPair struct {
	value *Iroha2KeyPair
	isSet bool
}

func (v NullableIroha2KeyPair) Get() *Iroha2KeyPair {
	return v.value
}

func (v *NullableIroha2KeyPair) Set(val *Iroha2KeyPair) {
	v.value = val
	v.isSet = true
}

func (v NullableIroha2KeyPair) IsSet() bool {
	return v.isSet
}

func (v *NullableIroha2KeyPair) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableIroha2KeyPair(val *Iroha2KeyPair) *NullableIroha2KeyPair {
	return &NullableIroha2KeyPair{value: val, isSet: true}
}

func (v NullableIroha2KeyPair) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableIroha2KeyPair) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}


