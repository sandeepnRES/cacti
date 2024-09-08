/*
Hyperledger Cactus Plugin - Connector Polkadot

Can perform basic tasks on a Polkadot parachain

API version: 2.0.0-rc.4
*/

// Code generated by OpenAPI Generator (https://openapi-generator.tech); DO NOT EDIT.

package cactus-plugin-ledger-connector-polkadot

import (
	"encoding/json"
)

// checks if the SignRawTransactionResponse type satisfies the MappedNullable interface at compile time
var _ MappedNullable = &SignRawTransactionResponse{}

// SignRawTransactionResponse struct for SignRawTransactionResponse
type SignRawTransactionResponse struct {
	Success bool `json:"success"`
	SignedTransaction string `json:"signedTransaction"`
}

// NewSignRawTransactionResponse instantiates a new SignRawTransactionResponse object
// This constructor will assign default values to properties that have it defined,
// and makes sure properties required by API are set, but the set of arguments
// will change when the set of required properties is changed
func NewSignRawTransactionResponse(success bool, signedTransaction string) *SignRawTransactionResponse {
	this := SignRawTransactionResponse{}
	this.Success = success
	this.SignedTransaction = signedTransaction
	return &this
}

// NewSignRawTransactionResponseWithDefaults instantiates a new SignRawTransactionResponse object
// This constructor will only assign default values to properties that have it defined,
// but it doesn't guarantee that properties required by API are set
func NewSignRawTransactionResponseWithDefaults() *SignRawTransactionResponse {
	this := SignRawTransactionResponse{}
	return &this
}

// GetSuccess returns the Success field value
func (o *SignRawTransactionResponse) GetSuccess() bool {
	if o == nil {
		var ret bool
		return ret
	}

	return o.Success
}

// GetSuccessOk returns a tuple with the Success field value
// and a boolean to check if the value has been set.
func (o *SignRawTransactionResponse) GetSuccessOk() (*bool, bool) {
	if o == nil {
		return nil, false
	}
	return &o.Success, true
}

// SetSuccess sets field value
func (o *SignRawTransactionResponse) SetSuccess(v bool) {
	o.Success = v
}

// GetSignedTransaction returns the SignedTransaction field value
func (o *SignRawTransactionResponse) GetSignedTransaction() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.SignedTransaction
}

// GetSignedTransactionOk returns a tuple with the SignedTransaction field value
// and a boolean to check if the value has been set.
func (o *SignRawTransactionResponse) GetSignedTransactionOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.SignedTransaction, true
}

// SetSignedTransaction sets field value
func (o *SignRawTransactionResponse) SetSignedTransaction(v string) {
	o.SignedTransaction = v
}

func (o SignRawTransactionResponse) MarshalJSON() ([]byte, error) {
	toSerialize,err := o.ToMap()
	if err != nil {
		return []byte{}, err
	}
	return json.Marshal(toSerialize)
}

func (o SignRawTransactionResponse) ToMap() (map[string]interface{}, error) {
	toSerialize := map[string]interface{}{}
	toSerialize["success"] = o.Success
	toSerialize["signedTransaction"] = o.SignedTransaction
	return toSerialize, nil
}

type NullableSignRawTransactionResponse struct {
	value *SignRawTransactionResponse
	isSet bool
}

func (v NullableSignRawTransactionResponse) Get() *SignRawTransactionResponse {
	return v.value
}

func (v *NullableSignRawTransactionResponse) Set(val *SignRawTransactionResponse) {
	v.value = val
	v.isSet = true
}

func (v NullableSignRawTransactionResponse) IsSet() bool {
	return v.isSet
}

func (v *NullableSignRawTransactionResponse) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableSignRawTransactionResponse(val *SignRawTransactionResponse) *NullableSignRawTransactionResponse {
	return &NullableSignRawTransactionResponse{value: val, isSet: true}
}

func (v NullableSignRawTransactionResponse) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableSignRawTransactionResponse) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}


