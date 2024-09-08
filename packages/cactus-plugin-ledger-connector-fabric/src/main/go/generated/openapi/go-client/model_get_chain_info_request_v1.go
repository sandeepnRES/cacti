/*
Hyperledger Cactus Plugin - Connector Fabric

Can perform basic tasks on a fabric ledger

API version: 2.0.0-rc.4
*/

// Code generated by OpenAPI Generator (https://openapi-generator.tech); DO NOT EDIT.

package cactus-plugin-ledger-connector-fabric

import (
	"encoding/json"
)

// checks if the GetChainInfoRequestV1 type satisfies the MappedNullable interface at compile time
var _ MappedNullable = &GetChainInfoRequestV1{}

// GetChainInfoRequestV1 Request for GetChainInfo endpoint.
type GetChainInfoRequestV1 struct {
	// Fabric channel which we want to query.
	ChannelName string `json:"channelName"`
	// Fabric channel we want to connect to. If not provided, then one from channelName parameter will be used
	ConnectionChannelName *string `json:"connectionChannelName,omitempty"`
	GatewayOptions GatewayOptions `json:"gatewayOptions"`
}

// NewGetChainInfoRequestV1 instantiates a new GetChainInfoRequestV1 object
// This constructor will assign default values to properties that have it defined,
// and makes sure properties required by API are set, but the set of arguments
// will change when the set of required properties is changed
func NewGetChainInfoRequestV1(channelName string, gatewayOptions GatewayOptions) *GetChainInfoRequestV1 {
	this := GetChainInfoRequestV1{}
	this.ChannelName = channelName
	this.GatewayOptions = gatewayOptions
	return &this
}

// NewGetChainInfoRequestV1WithDefaults instantiates a new GetChainInfoRequestV1 object
// This constructor will only assign default values to properties that have it defined,
// but it doesn't guarantee that properties required by API are set
func NewGetChainInfoRequestV1WithDefaults() *GetChainInfoRequestV1 {
	this := GetChainInfoRequestV1{}
	return &this
}

// GetChannelName returns the ChannelName field value
func (o *GetChainInfoRequestV1) GetChannelName() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.ChannelName
}

// GetChannelNameOk returns a tuple with the ChannelName field value
// and a boolean to check if the value has been set.
func (o *GetChainInfoRequestV1) GetChannelNameOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.ChannelName, true
}

// SetChannelName sets field value
func (o *GetChainInfoRequestV1) SetChannelName(v string) {
	o.ChannelName = v
}

// GetConnectionChannelName returns the ConnectionChannelName field value if set, zero value otherwise.
func (o *GetChainInfoRequestV1) GetConnectionChannelName() string {
	if o == nil || IsNil(o.ConnectionChannelName) {
		var ret string
		return ret
	}
	return *o.ConnectionChannelName
}

// GetConnectionChannelNameOk returns a tuple with the ConnectionChannelName field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *GetChainInfoRequestV1) GetConnectionChannelNameOk() (*string, bool) {
	if o == nil || IsNil(o.ConnectionChannelName) {
		return nil, false
	}
	return o.ConnectionChannelName, true
}

// HasConnectionChannelName returns a boolean if a field has been set.
func (o *GetChainInfoRequestV1) HasConnectionChannelName() bool {
	if o != nil && !IsNil(o.ConnectionChannelName) {
		return true
	}

	return false
}

// SetConnectionChannelName gets a reference to the given string and assigns it to the ConnectionChannelName field.
func (o *GetChainInfoRequestV1) SetConnectionChannelName(v string) {
	o.ConnectionChannelName = &v
}

// GetGatewayOptions returns the GatewayOptions field value
func (o *GetChainInfoRequestV1) GetGatewayOptions() GatewayOptions {
	if o == nil {
		var ret GatewayOptions
		return ret
	}

	return o.GatewayOptions
}

// GetGatewayOptionsOk returns a tuple with the GatewayOptions field value
// and a boolean to check if the value has been set.
func (o *GetChainInfoRequestV1) GetGatewayOptionsOk() (*GatewayOptions, bool) {
	if o == nil {
		return nil, false
	}
	return &o.GatewayOptions, true
}

// SetGatewayOptions sets field value
func (o *GetChainInfoRequestV1) SetGatewayOptions(v GatewayOptions) {
	o.GatewayOptions = v
}

func (o GetChainInfoRequestV1) MarshalJSON() ([]byte, error) {
	toSerialize,err := o.ToMap()
	if err != nil {
		return []byte{}, err
	}
	return json.Marshal(toSerialize)
}

func (o GetChainInfoRequestV1) ToMap() (map[string]interface{}, error) {
	toSerialize := map[string]interface{}{}
	toSerialize["channelName"] = o.ChannelName
	if !IsNil(o.ConnectionChannelName) {
		toSerialize["connectionChannelName"] = o.ConnectionChannelName
	}
	toSerialize["gatewayOptions"] = o.GatewayOptions
	return toSerialize, nil
}

type NullableGetChainInfoRequestV1 struct {
	value *GetChainInfoRequestV1
	isSet bool
}

func (v NullableGetChainInfoRequestV1) Get() *GetChainInfoRequestV1 {
	return v.value
}

func (v *NullableGetChainInfoRequestV1) Set(val *GetChainInfoRequestV1) {
	v.value = val
	v.isSet = true
}

func (v NullableGetChainInfoRequestV1) IsSet() bool {
	return v.isSet
}

func (v *NullableGetChainInfoRequestV1) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableGetChainInfoRequestV1(val *GetChainInfoRequestV1) *NullableGetChainInfoRequestV1 {
	return &NullableGetChainInfoRequestV1{value: val, isSet: true}
}

func (v NullableGetChainInfoRequestV1) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableGetChainInfoRequestV1) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}


