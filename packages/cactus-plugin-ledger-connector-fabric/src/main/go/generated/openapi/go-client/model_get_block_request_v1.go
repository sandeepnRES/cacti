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

// checks if the GetBlockRequestV1 type satisfies the MappedNullable interface at compile time
var _ MappedNullable = &GetBlockRequestV1{}

// GetBlockRequestV1 Request for GetBlock endpoint.
type GetBlockRequestV1 struct {
	// Fabric channel which we want to query.
	ChannelName string `json:"channelName"`
	// Fabric channel we want to connect to. If not provided, then one from channelName parameter will be used
	ConnectionChannelName *string `json:"connectionChannelName,omitempty"`
	GatewayOptions GatewayOptions `json:"gatewayOptions"`
	Query GetBlockRequestV1Query `json:"query"`
	ResponseType *GetBlockResponseTypeV1 `json:"responseType,omitempty"`
}

// NewGetBlockRequestV1 instantiates a new GetBlockRequestV1 object
// This constructor will assign default values to properties that have it defined,
// and makes sure properties required by API are set, but the set of arguments
// will change when the set of required properties is changed
func NewGetBlockRequestV1(channelName string, gatewayOptions GatewayOptions, query GetBlockRequestV1Query) *GetBlockRequestV1 {
	this := GetBlockRequestV1{}
	this.ChannelName = channelName
	this.GatewayOptions = gatewayOptions
	this.Query = query
	var responseType GetBlockResponseTypeV1 = Full
	this.ResponseType = &responseType
	return &this
}

// NewGetBlockRequestV1WithDefaults instantiates a new GetBlockRequestV1 object
// This constructor will only assign default values to properties that have it defined,
// but it doesn't guarantee that properties required by API are set
func NewGetBlockRequestV1WithDefaults() *GetBlockRequestV1 {
	this := GetBlockRequestV1{}
	var responseType GetBlockResponseTypeV1 = Full
	this.ResponseType = &responseType
	return &this
}

// GetChannelName returns the ChannelName field value
func (o *GetBlockRequestV1) GetChannelName() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.ChannelName
}

// GetChannelNameOk returns a tuple with the ChannelName field value
// and a boolean to check if the value has been set.
func (o *GetBlockRequestV1) GetChannelNameOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.ChannelName, true
}

// SetChannelName sets field value
func (o *GetBlockRequestV1) SetChannelName(v string) {
	o.ChannelName = v
}

// GetConnectionChannelName returns the ConnectionChannelName field value if set, zero value otherwise.
func (o *GetBlockRequestV1) GetConnectionChannelName() string {
	if o == nil || IsNil(o.ConnectionChannelName) {
		var ret string
		return ret
	}
	return *o.ConnectionChannelName
}

// GetConnectionChannelNameOk returns a tuple with the ConnectionChannelName field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *GetBlockRequestV1) GetConnectionChannelNameOk() (*string, bool) {
	if o == nil || IsNil(o.ConnectionChannelName) {
		return nil, false
	}
	return o.ConnectionChannelName, true
}

// HasConnectionChannelName returns a boolean if a field has been set.
func (o *GetBlockRequestV1) HasConnectionChannelName() bool {
	if o != nil && !IsNil(o.ConnectionChannelName) {
		return true
	}

	return false
}

// SetConnectionChannelName gets a reference to the given string and assigns it to the ConnectionChannelName field.
func (o *GetBlockRequestV1) SetConnectionChannelName(v string) {
	o.ConnectionChannelName = &v
}

// GetGatewayOptions returns the GatewayOptions field value
func (o *GetBlockRequestV1) GetGatewayOptions() GatewayOptions {
	if o == nil {
		var ret GatewayOptions
		return ret
	}

	return o.GatewayOptions
}

// GetGatewayOptionsOk returns a tuple with the GatewayOptions field value
// and a boolean to check if the value has been set.
func (o *GetBlockRequestV1) GetGatewayOptionsOk() (*GatewayOptions, bool) {
	if o == nil {
		return nil, false
	}
	return &o.GatewayOptions, true
}

// SetGatewayOptions sets field value
func (o *GetBlockRequestV1) SetGatewayOptions(v GatewayOptions) {
	o.GatewayOptions = v
}

// GetQuery returns the Query field value
func (o *GetBlockRequestV1) GetQuery() GetBlockRequestV1Query {
	if o == nil {
		var ret GetBlockRequestV1Query
		return ret
	}

	return o.Query
}

// GetQueryOk returns a tuple with the Query field value
// and a boolean to check if the value has been set.
func (o *GetBlockRequestV1) GetQueryOk() (*GetBlockRequestV1Query, bool) {
	if o == nil {
		return nil, false
	}
	return &o.Query, true
}

// SetQuery sets field value
func (o *GetBlockRequestV1) SetQuery(v GetBlockRequestV1Query) {
	o.Query = v
}

// GetResponseType returns the ResponseType field value if set, zero value otherwise.
func (o *GetBlockRequestV1) GetResponseType() GetBlockResponseTypeV1 {
	if o == nil || IsNil(o.ResponseType) {
		var ret GetBlockResponseTypeV1
		return ret
	}
	return *o.ResponseType
}

// GetResponseTypeOk returns a tuple with the ResponseType field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *GetBlockRequestV1) GetResponseTypeOk() (*GetBlockResponseTypeV1, bool) {
	if o == nil || IsNil(o.ResponseType) {
		return nil, false
	}
	return o.ResponseType, true
}

// HasResponseType returns a boolean if a field has been set.
func (o *GetBlockRequestV1) HasResponseType() bool {
	if o != nil && !IsNil(o.ResponseType) {
		return true
	}

	return false
}

// SetResponseType gets a reference to the given GetBlockResponseTypeV1 and assigns it to the ResponseType field.
func (o *GetBlockRequestV1) SetResponseType(v GetBlockResponseTypeV1) {
	o.ResponseType = &v
}

func (o GetBlockRequestV1) MarshalJSON() ([]byte, error) {
	toSerialize,err := o.ToMap()
	if err != nil {
		return []byte{}, err
	}
	return json.Marshal(toSerialize)
}

func (o GetBlockRequestV1) ToMap() (map[string]interface{}, error) {
	toSerialize := map[string]interface{}{}
	toSerialize["channelName"] = o.ChannelName
	if !IsNil(o.ConnectionChannelName) {
		toSerialize["connectionChannelName"] = o.ConnectionChannelName
	}
	toSerialize["gatewayOptions"] = o.GatewayOptions
	toSerialize["query"] = o.Query
	if !IsNil(o.ResponseType) {
		toSerialize["responseType"] = o.ResponseType
	}
	return toSerialize, nil
}

type NullableGetBlockRequestV1 struct {
	value *GetBlockRequestV1
	isSet bool
}

func (v NullableGetBlockRequestV1) Get() *GetBlockRequestV1 {
	return v.value
}

func (v *NullableGetBlockRequestV1) Set(val *GetBlockRequestV1) {
	v.value = val
	v.isSet = true
}

func (v NullableGetBlockRequestV1) IsSet() bool {
	return v.isSet
}

func (v *NullableGetBlockRequestV1) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableGetBlockRequestV1(val *GetBlockRequestV1) *NullableGetBlockRequestV1 {
	return &NullableGetBlockRequestV1{value: val, isSet: true}
}

func (v NullableGetBlockRequestV1) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableGetBlockRequestV1) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}


