/*
Hyperledger Core API

Contains/describes the core API types for Cactus. Does not describe actual endpoints on its own as this is left to the implementing plugins who can import and re-use commonly needed type definitions from this specification. One example of said commonly used type definitions would be the types related to consortium management, cactus nodes, ledgers, etc..

API version: 2.0.0-rc.4
*/

// Code generated by OpenAPI Generator (https://openapi-generator.tech); DO NOT EDIT.

package cactus-core-api

import (
	"encoding/json"
)

// checks if the ConsortiumMember type satisfies the MappedNullable interface at compile time
var _ MappedNullable = &ConsortiumMember{}

// ConsortiumMember struct for ConsortiumMember
type ConsortiumMember struct {
	Id string `json:"id"`
	// The human readable name a Consortium member can be referred to while making it easy for humans to distinguish this particular consortium member entity from any other ones.
	Name string `json:"name"`
	NodeIds []string `json:"nodeIds"`
}

// NewConsortiumMember instantiates a new ConsortiumMember object
// This constructor will assign default values to properties that have it defined,
// and makes sure properties required by API are set, but the set of arguments
// will change when the set of required properties is changed
func NewConsortiumMember(id string, name string, nodeIds []string) *ConsortiumMember {
	this := ConsortiumMember{}
	this.Id = id
	this.Name = name
	this.NodeIds = nodeIds
	return &this
}

// NewConsortiumMemberWithDefaults instantiates a new ConsortiumMember object
// This constructor will only assign default values to properties that have it defined,
// but it doesn't guarantee that properties required by API are set
func NewConsortiumMemberWithDefaults() *ConsortiumMember {
	this := ConsortiumMember{}
	return &this
}

// GetId returns the Id field value
func (o *ConsortiumMember) GetId() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.Id
}

// GetIdOk returns a tuple with the Id field value
// and a boolean to check if the value has been set.
func (o *ConsortiumMember) GetIdOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.Id, true
}

// SetId sets field value
func (o *ConsortiumMember) SetId(v string) {
	o.Id = v
}

// GetName returns the Name field value
func (o *ConsortiumMember) GetName() string {
	if o == nil {
		var ret string
		return ret
	}

	return o.Name
}

// GetNameOk returns a tuple with the Name field value
// and a boolean to check if the value has been set.
func (o *ConsortiumMember) GetNameOk() (*string, bool) {
	if o == nil {
		return nil, false
	}
	return &o.Name, true
}

// SetName sets field value
func (o *ConsortiumMember) SetName(v string) {
	o.Name = v
}

// GetNodeIds returns the NodeIds field value
func (o *ConsortiumMember) GetNodeIds() []string {
	if o == nil {
		var ret []string
		return ret
	}

	return o.NodeIds
}

// GetNodeIdsOk returns a tuple with the NodeIds field value
// and a boolean to check if the value has been set.
func (o *ConsortiumMember) GetNodeIdsOk() ([]string, bool) {
	if o == nil {
		return nil, false
	}
	return o.NodeIds, true
}

// SetNodeIds sets field value
func (o *ConsortiumMember) SetNodeIds(v []string) {
	o.NodeIds = v
}

func (o ConsortiumMember) MarshalJSON() ([]byte, error) {
	toSerialize,err := o.ToMap()
	if err != nil {
		return []byte{}, err
	}
	return json.Marshal(toSerialize)
}

func (o ConsortiumMember) ToMap() (map[string]interface{}, error) {
	toSerialize := map[string]interface{}{}
	toSerialize["id"] = o.Id
	toSerialize["name"] = o.Name
	toSerialize["nodeIds"] = o.NodeIds
	return toSerialize, nil
}

type NullableConsortiumMember struct {
	value *ConsortiumMember
	isSet bool
}

func (v NullableConsortiumMember) Get() *ConsortiumMember {
	return v.value
}

func (v *NullableConsortiumMember) Set(val *ConsortiumMember) {
	v.value = val
	v.isSet = true
}

func (v NullableConsortiumMember) IsSet() bool {
	return v.isSet
}

func (v *NullableConsortiumMember) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableConsortiumMember(val *ConsortiumMember) *NullableConsortiumMember {
	return &NullableConsortiumMember{value: val, isSet: true}
}

func (v NullableConsortiumMember) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableConsortiumMember) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}


