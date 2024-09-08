/*
Hyperledger Cactus Plugin - Odap Hermes

Implementation for Odap and Hermes

API version: 2.0.0-rc.4
*/

// Code generated by OpenAPI Generator (https://openapi-generator.tech); DO NOT EDIT.

package cactus-plugin-satp-hermes

import (
	"encoding/json"
)

// checks if the RecoverUpdateV1Message type satisfies the MappedNullable interface at compile time
var _ MappedNullable = &RecoverUpdateV1Message{}

// RecoverUpdateV1Message struct for RecoverUpdateV1Message
type RecoverUpdateV1Message struct {
	SessionID string `json:"sessionID"`
	RecoveredLogs []LocalLog `json:"recoveredLogs"`
	Signature string `json:"signature"`
}

// NewRecoverUpdateV1Message instantiates a new RecoverUpdateV1Message object
// This constructor will assign default values to properties that have it defined,
// and makes sure properties required by API are set, but the set of arguments
// will change when the set of required properties is changed
func NewRecoverUpdateV1Message(sessionID string, recoveredLogs []LocalLog, signature string) *RecoverUpdateV1Message {
	this := RecoverUpdateV1Message{}
	this.SessionID = sessionID
	this.RecoveredLogs = recoveredLogs
	this.Signature = signature
	return &this
}

// NewRecoverUpdateV1MessageWithDefaults instantiates a new RecoverUpdateV1Message object
// This constructor will only assign default values to properties that have it defined,
// but it doesn't guarantee that properties required by API are set
func NewRecoverUpdateV1MessageWithDefaults() *RecoverUpdateV1Message {
	this := RecoverUpdateV1Message{}
	return &this
}

// GetSessionID returns the SessionID field value
func (o *RecoverUpdateV1Message) GetSessionID() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.SessionID
}

// GetSessionIDOk returns a tuple with the SessionID field value
// and a boolean to check if the value has been set.
func (o *RecoverUpdateV1Message) GetSessionIDOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.SessionID, true
}

// SetSessionID sets field value
func (o *RecoverUpdateV1Message) SetSessionID(v string) {
	o.SessionID = v
}

// GetRecoveredLogs returns the RecoveredLogs field value
func (o *RecoverUpdateV1Message) GetRecoveredLogs() []LocalLog {
	if o == nil {
		var ret []LocalLog
		return ret
	}

	return o.RecoveredLogs
}

// GetRecoveredLogsOk returns a tuple with the RecoveredLogs field value
// and a boolean to check if the value has been set.
func (o *RecoverUpdateV1Message) GetRecoveredLogsOk() ([]LocalLog, bool) {
	if o == nil {
		return nil, false
	}
	return o.RecoveredLogs, true
}

// SetRecoveredLogs sets field value
func (o *RecoverUpdateV1Message) SetRecoveredLogs(v []LocalLog) {
	o.RecoveredLogs = v
}

// GetSignature returns the Signature field value
func (o *RecoverUpdateV1Message) GetSignature() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.Signature
}

// GetSignatureOk returns a tuple with the Signature field value
// and a boolean to check if the value has been set.
func (o *RecoverUpdateV1Message) GetSignatureOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.Signature, true
}

// SetSignature sets field value
func (o *RecoverUpdateV1Message) SetSignature(v string) {
	o.Signature = v
}

func (o RecoverUpdateV1Message) MarshalJSON() ([]byte, error) {
	toSerialize,err := o.ToMap()
	if err != nil {
		return []byte{}, err
	}
	return json.Marshal(toSerialize)
}

func (o RecoverUpdateV1Message) ToMap() (map[string]interface{}, error) {
	toSerialize := map[string]interface{}{}
	toSerialize["sessionID"] = o.SessionID
	toSerialize["recoveredLogs"] = o.RecoveredLogs
	toSerialize["signature"] = o.Signature
	return toSerialize, nil
}

type NullableRecoverUpdateV1Message struct {
	value *RecoverUpdateV1Message
	isSet bool
}

func (v NullableRecoverUpdateV1Message) Get() *RecoverUpdateV1Message {
	return v.value
}

func (v *NullableRecoverUpdateV1Message) Set(val *RecoverUpdateV1Message) {
	v.value = val
	v.isSet = true
}

func (v NullableRecoverUpdateV1Message) IsSet() bool {
	return v.isSet
}

func (v *NullableRecoverUpdateV1Message) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableRecoverUpdateV1Message(val *RecoverUpdateV1Message) *NullableRecoverUpdateV1Message {
	return &NullableRecoverUpdateV1Message{value: val, isSet: true}
}

func (v NullableRecoverUpdateV1Message) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableRecoverUpdateV1Message) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}


