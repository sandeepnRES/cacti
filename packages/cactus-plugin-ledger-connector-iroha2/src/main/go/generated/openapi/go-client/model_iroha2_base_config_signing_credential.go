/*
Hyperledger Cactus Plugin - Connector Iroha V2

Can perform basic tasks on a Iroha V2 ledger

API version: 2.0.0-rc.4
*/

// Code generated by OpenAPI Generator (https://openapi-generator.tech); DO NOT EDIT.

package cactus-plugin-ledger-connector-iroha2

import (
	"encoding/json"
	"fmt"
)

// Iroha2BaseConfigSigningCredential - struct for Iroha2BaseConfigSigningCredential
type Iroha2BaseConfigSigningCredential struct {
	Iroha2KeyPair *Iroha2KeyPair
	KeychainReference *KeychainReference
}

// Iroha2KeyPairAsIroha2BaseConfigSigningCredential is a convenience function that returns Iroha2KeyPair wrapped in Iroha2BaseConfigSigningCredential
func Iroha2KeyPairAsIroha2BaseConfigSigningCredential(v *Iroha2KeyPair) Iroha2BaseConfigSigningCredential {
	return Iroha2BaseConfigSigningCredential{
		Iroha2KeyPair: v,
	}
}

// KeychainReferenceAsIroha2BaseConfigSigningCredential is a convenience function that returns KeychainReference wrapped in Iroha2BaseConfigSigningCredential
func KeychainReferenceAsIroha2BaseConfigSigningCredential(v *KeychainReference) Iroha2BaseConfigSigningCredential {
	return Iroha2BaseConfigSigningCredential{
		KeychainReference: v,
	}
}


// Unmarshal JSON data into one of the pointers in the struct
func (dst *Iroha2BaseConfigSigningCredential) UnmarshalJSON(data []byte) error {
	var err error
	match := 0
	// try to unmarshal data into Iroha2KeyPair
	err = newStrictDecoder(data).Decode(&dst.Iroha2KeyPair)
	if err == nil {
		jsonIroha2KeyPair, _ := json.Marshal(dst.Iroha2KeyPair)
		if string(jsonIroha2KeyPair) == "{}" { // empty struct
			dst.Iroha2KeyPair = nil
		} else {
			match++
		}
	} else {
		dst.Iroha2KeyPair = nil
	}

	// try to unmarshal data into KeychainReference
	err = newStrictDecoder(data).Decode(&dst.KeychainReference)
	if err == nil {
		jsonKeychainReference, _ := json.Marshal(dst.KeychainReference)
		if string(jsonKeychainReference) == "{}" { // empty struct
			dst.KeychainReference = nil
		} else {
			match++
		}
	} else {
		dst.KeychainReference = nil
	}

	if match > 1 { // more than 1 match
		// reset to nil
		dst.Iroha2KeyPair = nil
		dst.KeychainReference = nil

		return fmt.Errorf("data matches more than one schema in oneOf(Iroha2BaseConfigSigningCredential)")
	} else if match == 1 {
		return nil // exactly one match
	} else { // no match
		return fmt.Errorf("data failed to match schemas in oneOf(Iroha2BaseConfigSigningCredential)")
	}
}

// Marshal data from the first non-nil pointers in the struct to JSON
func (src Iroha2BaseConfigSigningCredential) MarshalJSON() ([]byte, error) {
	if src.Iroha2KeyPair != nil {
		return json.Marshal(&src.Iroha2KeyPair)
	}

	if src.KeychainReference != nil {
		return json.Marshal(&src.KeychainReference)
	}

	return nil, nil // no data in oneOf schemas
}

// Get the actual instance
func (obj *Iroha2BaseConfigSigningCredential) GetActualInstance() (interface{}) {
	if obj == nil {
		return nil
	}
	if obj.Iroha2KeyPair != nil {
		return obj.Iroha2KeyPair
	}

	if obj.KeychainReference != nil {
		return obj.KeychainReference
	}

	// all schemas are nil
	return nil
}

type NullableIroha2BaseConfigSigningCredential struct {
	value *Iroha2BaseConfigSigningCredential
	isSet bool
}

func (v NullableIroha2BaseConfigSigningCredential) Get() *Iroha2BaseConfigSigningCredential {
	return v.value
}

func (v *NullableIroha2BaseConfigSigningCredential) Set(val *Iroha2BaseConfigSigningCredential) {
	v.value = val
	v.isSet = true
}

func (v NullableIroha2BaseConfigSigningCredential) IsSet() bool {
	return v.isSet
}

func (v *NullableIroha2BaseConfigSigningCredential) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableIroha2BaseConfigSigningCredential(val *Iroha2BaseConfigSigningCredential) *NullableIroha2BaseConfigSigningCredential {
	return &NullableIroha2BaseConfigSigningCredential{value: val, isSet: true}
}

func (v NullableIroha2BaseConfigSigningCredential) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableIroha2BaseConfigSigningCredential) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}


