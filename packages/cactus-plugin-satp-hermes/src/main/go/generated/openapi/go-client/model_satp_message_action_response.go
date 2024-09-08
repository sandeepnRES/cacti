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

// checks if the SatpMessageActionResponse type satisfies the MappedNullable interface at compile time
var _ MappedNullable = &SatpMessageActionResponse{}

// SatpMessageActionResponse struct for SatpMessageActionResponse
type SatpMessageActionResponse struct {
	ResponseCode *string `json:"ResponseCode,omitempty"`
	Arguments []interface{} `json:"Arguments,omitempty"`
}

// NewSatpMessageActionResponse instantiates a new SatpMessageActionResponse object
// This constructor will assign default values to properties that have it defined,
// and makes sure properties required by API are set, but the set of arguments
// will change when the set of required properties is changed
func NewSatpMessageActionResponse() *SatpMessageActionResponse {
	this := SatpMessageActionResponse{}
	return &this
}

// NewSatpMessageActionResponseWithDefaults instantiates a new SatpMessageActionResponse object
// This constructor will only assign default values to properties that have it defined,
// but it doesn't guarantee that properties required by API are set
func NewSatpMessageActionResponseWithDefaults() *SatpMessageActionResponse {
	this := SatpMessageActionResponse{}
	return &this
}

// GetResponseCode returns the ResponseCode field value if set, zero value otherwise.
func (o *SatpMessageActionResponse) GetResponseCode() string {
	if o == nil || IsNil(o.ResponseCode) {
		var ret string
		return ret
	}
	return *o.ResponseCode
}

// GetResponseCodeOk returns a tuple with the ResponseCode field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *SatpMessageActionResponse) GetResponseCodeOk() (*string, bool) {
	if o == nil || IsNil(o.ResponseCode) {
		return nil, false
	}
	return o.ResponseCode, true
}

// HasResponseCode returns a boolean if a field has been set.
func (o *SatpMessageActionResponse) HasResponseCode() bool {
	if o != nil && !IsNil(o.ResponseCode) {
		return true
	}

	return false
}

// SetResponseCode gets a reference to the given string and assigns it to the ResponseCode field.
func (o *SatpMessageActionResponse) SetResponseCode(v string) {
	o.ResponseCode = &v
}

// GetArguments returns the Arguments field value if set, zero value otherwise.
func (o *SatpMessageActionResponse) GetArguments() []interface{} {
	if o == nil || IsNil(o.Arguments) {
		var ret []interface{}
		return ret
	}
	return o.Arguments
}

// GetArgumentsOk returns a tuple with the Arguments field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *SatpMessageActionResponse) GetArgumentsOk() ([]interface{}, bool) {
	if o == nil || IsNil(o.Arguments) {
		return nil, false
	}
	return o.Arguments, true
}

// HasArguments returns a boolean if a field has been set.
func (o *SatpMessageActionResponse) HasArguments() bool {
	if o != nil && !IsNil(o.Arguments) {
		return true
	}

	return false
}

// SetArguments gets a reference to the given []interface{} and assigns it to the Arguments field.
func (o *SatpMessageActionResponse) SetArguments(v []interface{}) {
	o.Arguments = v
}

func (o SatpMessageActionResponse) MarshalJSON() ([]byte, error) {
	toSerialize,err := o.ToMap()
	if err != nil {
		return []byte{}, err
	}
	return json.Marshal(toSerialize)
}

func (o SatpMessageActionResponse) ToMap() (map[string]interface{}, error) {
	toSerialize := map[string]interface{}{}
	if !IsNil(o.ResponseCode) {
		toSerialize["ResponseCode"] = o.ResponseCode
	}
	if !IsNil(o.Arguments) {
		toSerialize["Arguments"] = o.Arguments
	}
	return toSerialize, nil
}

type NullableSatpMessageActionResponse struct {
	value *SatpMessageActionResponse
	isSet bool
}

func (v NullableSatpMessageActionResponse) Get() *SatpMessageActionResponse {
	return v.value
}

func (v *NullableSatpMessageActionResponse) Set(val *SatpMessageActionResponse) {
	v.value = val
	v.isSet = true
}

func (v NullableSatpMessageActionResponse) IsSet() bool {
	return v.isSet
}

func (v *NullableSatpMessageActionResponse) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableSatpMessageActionResponse(val *SatpMessageActionResponse) *NullableSatpMessageActionResponse {
	return &NullableSatpMessageActionResponse{value: val, isSet: true}
}

func (v NullableSatpMessageActionResponse) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableSatpMessageActionResponse) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}


