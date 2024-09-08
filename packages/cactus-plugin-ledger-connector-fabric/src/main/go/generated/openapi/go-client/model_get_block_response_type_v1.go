/*
Hyperledger Cactus Plugin - Connector Fabric

Can perform basic tasks on a fabric ledger

API version: 2.0.0-rc.4
*/

// Code generated by OpenAPI Generator (https://openapi-generator.tech); DO NOT EDIT.

package cactus-plugin-ledger-connector-fabric

import (
	"encoding/json"
	"fmt"
)

// GetBlockResponseTypeV1 Response type from GetBlock.
type GetBlockResponseTypeV1 string

// List of GetBlockResponseTypeV1
const (
	Full GetBlockResponseTypeV1 = "full"
	Encoded GetBlockResponseTypeV1 = "encoded"
	CactiTransactions GetBlockResponseTypeV1 = "cacti:transactions"
	CactiFullBlock GetBlockResponseTypeV1 = "cacti:full-block"
)

// All allowed values of GetBlockResponseTypeV1 enum
var AllowedGetBlockResponseTypeV1EnumValues = []GetBlockResponseTypeV1{
	"full",
	"encoded",
	"cacti:transactions",
	"cacti:full-block",
}

func (v *GetBlockResponseTypeV1) UnmarshalJSON(src []byte) error {
	var value string
	err := json.Unmarshal(src, &value)
	if err != nil {
		return err
	}
	enumTypeValue := GetBlockResponseTypeV1(value)
	for _, existing := range AllowedGetBlockResponseTypeV1EnumValues {
		if existing == enumTypeValue {
			*v = enumTypeValue
			return nil
		}
	}

	return fmt.Errorf("%+v is not a valid GetBlockResponseTypeV1", value)
}

// NewGetBlockResponseTypeV1FromValue returns a pointer to a valid GetBlockResponseTypeV1
// for the value passed as argument, or an error if the value passed is not allowed by the enum
func NewGetBlockResponseTypeV1FromValue(v string) (*GetBlockResponseTypeV1, error) {
	ev := GetBlockResponseTypeV1(v)
	if ev.IsValid() {
		return &ev, nil
	} else {
		return nil, fmt.Errorf("invalid value '%v' for GetBlockResponseTypeV1: valid values are %v", v, AllowedGetBlockResponseTypeV1EnumValues)
	}
}

// IsValid return true if the value is valid for the enum, false otherwise
func (v GetBlockResponseTypeV1) IsValid() bool {
	for _, existing := range AllowedGetBlockResponseTypeV1EnumValues {
		if existing == v {
			return true
		}
	}
	return false
}

// Ptr returns reference to GetBlockResponseTypeV1 value
func (v GetBlockResponseTypeV1) Ptr() *GetBlockResponseTypeV1 {
	return &v
}

type NullableGetBlockResponseTypeV1 struct {
	value *GetBlockResponseTypeV1
	isSet bool
}

func (v NullableGetBlockResponseTypeV1) Get() *GetBlockResponseTypeV1 {
	return v.value
}

func (v *NullableGetBlockResponseTypeV1) Set(val *GetBlockResponseTypeV1) {
	v.value = val
	v.isSet = true
}

func (v NullableGetBlockResponseTypeV1) IsSet() bool {
	return v.isSet
}

func (v *NullableGetBlockResponseTypeV1) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableGetBlockResponseTypeV1(val *GetBlockResponseTypeV1) *NullableGetBlockResponseTypeV1 {
	return &NullableGetBlockResponseTypeV1{value: val, isSet: true}
}

func (v NullableGetBlockResponseTypeV1) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableGetBlockResponseTypeV1) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}

