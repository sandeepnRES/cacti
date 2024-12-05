/*
Hyperledger Cactus Plugin - Connector Iroha V2

Can perform basic tasks on a Iroha V2 ledger

API version: 2.1.0
*/

// Code generated by OpenAPI Generator (https://openapi-generator.tech); DO NOT EDIT.

package cactus-plugin-ledger-connector-iroha2

import (
	"encoding/json"
)

// checks if the IrohaQueryDefinitionV1 type satisfies the MappedNullable interface at compile time
var _ MappedNullable = &IrohaQueryDefinitionV1{}

// IrohaQueryDefinitionV1 Iroha V2 query definition.
type IrohaQueryDefinitionV1 struct {
	// Name of the query to be executed.
	Query IrohaQuery `json:"query"`
	// The list of arguments to pass with the query.
	Params []interface{} `json:"params,omitempty"`
}

// NewIrohaQueryDefinitionV1 instantiates a new IrohaQueryDefinitionV1 object
// This constructor will assign default values to properties that have it defined,
// and makes sure properties required by API are set, but the set of arguments
// will change when the set of required properties is changed
func NewIrohaQueryDefinitionV1(query IrohaQuery) *IrohaQueryDefinitionV1 {
	this := IrohaQueryDefinitionV1{}
	this.Query = query
	return &this
}

// NewIrohaQueryDefinitionV1WithDefaults instantiates a new IrohaQueryDefinitionV1 object
// This constructor will only assign default values to properties that have it defined,
// but it doesn't guarantee that properties required by API are set
func NewIrohaQueryDefinitionV1WithDefaults() *IrohaQueryDefinitionV1 {
	this := IrohaQueryDefinitionV1{}
	return &this
}

// GetQuery returns the Query field value
func (o *IrohaQueryDefinitionV1) GetQuery() IrohaQuery {
	if o == nil {
		var ret IrohaQuery
		return ret
	}

	return o.Query
}

// GetQueryOk returns a tuple with the Query field value
// and a boolean to check if the value has been set.
func (o *IrohaQueryDefinitionV1) GetQueryOk() (*IrohaQuery, bool) {
	if o == nil {
		return nil, false
	}
	return &o.Query, true
}

// SetQuery sets field value
func (o *IrohaQueryDefinitionV1) SetQuery(v IrohaQuery) {
	o.Query = v
}

// GetParams returns the Params field value if set, zero value otherwise.
func (o *IrohaQueryDefinitionV1) GetParams() []interface{} {
	if o == nil || IsNil(o.Params) {
		var ret []interface{}
		return ret
	}
	return o.Params
}

// GetParamsOk returns a tuple with the Params field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *IrohaQueryDefinitionV1) GetParamsOk() ([]interface{}, bool) {
	if o == nil || IsNil(o.Params) {
		return nil, false
	}
	return o.Params, true
}

// HasParams returns a boolean if a field has been set.
func (o *IrohaQueryDefinitionV1) HasParams() bool {
	if o != nil && !IsNil(o.Params) {
		return true
	}

	return false
}

// SetParams gets a reference to the given []interface{} and assigns it to the Params field.
func (o *IrohaQueryDefinitionV1) SetParams(v []interface{}) {
	o.Params = v
}

func (o IrohaQueryDefinitionV1) MarshalJSON() ([]byte, error) {
	toSerialize,err := o.ToMap()
	if err != nil {
		return []byte{}, err
	}
	return json.Marshal(toSerialize)
}

func (o IrohaQueryDefinitionV1) ToMap() (map[string]interface{}, error) {
	toSerialize := map[string]interface{}{}
	toSerialize["query"] = o.Query
	if !IsNil(o.Params) {
		toSerialize["params"] = o.Params
	}
	return toSerialize, nil
}

type NullableIrohaQueryDefinitionV1 struct {
	value *IrohaQueryDefinitionV1
	isSet bool
}

func (v NullableIrohaQueryDefinitionV1) Get() *IrohaQueryDefinitionV1 {
	return v.value
}

func (v *NullableIrohaQueryDefinitionV1) Set(val *IrohaQueryDefinitionV1) {
	v.value = val
	v.isSet = true
}

func (v NullableIrohaQueryDefinitionV1) IsSet() bool {
	return v.isSet
}

func (v *NullableIrohaQueryDefinitionV1) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableIrohaQueryDefinitionV1(val *IrohaQueryDefinitionV1) *NullableIrohaQueryDefinitionV1 {
	return &NullableIrohaQueryDefinitionV1{value: val, isSet: true}
}

func (v NullableIrohaQueryDefinitionV1) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableIrohaQueryDefinitionV1) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}

