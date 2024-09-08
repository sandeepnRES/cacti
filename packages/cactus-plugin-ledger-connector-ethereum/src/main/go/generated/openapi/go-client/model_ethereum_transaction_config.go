/*
Hyperledger Cacti Plugin - Connector Ethereum

Can perform basic tasks on a Ethereum ledger

API version: 2.0.0-rc.4
*/

// Code generated by OpenAPI Generator (https://openapi-generator.tech); DO NOT EDIT.

package cactus-plugin-ledger-connector-ethereum

import (
	"encoding/json"
)

// checks if the EthereumTransactionConfig type satisfies the MappedNullable interface at compile time
var _ MappedNullable = &EthereumTransactionConfig{}

// EthereumTransactionConfig struct for EthereumTransactionConfig
type EthereumTransactionConfig struct {
	RawTransaction *string `json:"rawTransaction,omitempty"`
	From *string `json:"from,omitempty"`
	To *string `json:"to,omitempty"`
	Value *string `json:"value,omitempty"`
	Nonce *string `json:"nonce,omitempty"`
	Data *string `json:"data,omitempty"`
	GasConfig *GasTransactionConfig `json:"gasConfig,omitempty"`
}

// NewEthereumTransactionConfig instantiates a new EthereumTransactionConfig object
// This constructor will assign default values to properties that have it defined,
// and makes sure properties required by API are set, but the set of arguments
// will change when the set of required properties is changed
func NewEthereumTransactionConfig() *EthereumTransactionConfig {
	this := EthereumTransactionConfig{}
	return &this
}

// NewEthereumTransactionConfigWithDefaults instantiates a new EthereumTransactionConfig object
// This constructor will only assign default values to properties that have it defined,
// but it doesn't guarantee that properties required by API are set
func NewEthereumTransactionConfigWithDefaults() *EthereumTransactionConfig {
	this := EthereumTransactionConfig{}
	return &this
}

// GetRawTransaction returns the RawTransaction field value if set, zero value otherwise.
func (o *EthereumTransactionConfig) GetRawTransaction() string {
	if o == nil || IsNil(o.RawTransaction) {
		var ret string
		return ret
	}
	return *o.RawTransaction
}

// GetRawTransactionOk returns a tuple with the RawTransaction field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *EthereumTransactionConfig) GetRawTransactionOk() (*string, bool) {
	if o == nil || IsNil(o.RawTransaction) {
		return nil, false
	}
	return o.RawTransaction, true
}

// HasRawTransaction returns a boolean if a field has been set.
func (o *EthereumTransactionConfig) HasRawTransaction() bool {
	if o != nil && !IsNil(o.RawTransaction) {
		return true
	}

	return false
}

// SetRawTransaction gets a reference to the given string and assigns it to the RawTransaction field.
func (o *EthereumTransactionConfig) SetRawTransaction(v string) {
	o.RawTransaction = &v
}

// GetFrom returns the From field value if set, zero value otherwise.
func (o *EthereumTransactionConfig) GetFrom() string {
	if o == nil || IsNil(o.From) {
		var ret string
		return ret
	}
	return *o.From
}

// GetFromOk returns a tuple with the From field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *EthereumTransactionConfig) GetFromOk() (*string, bool) {
	if o == nil || IsNil(o.From) {
		return nil, false
	}
	return o.From, true
}

// HasFrom returns a boolean if a field has been set.
func (o *EthereumTransactionConfig) HasFrom() bool {
	if o != nil && !IsNil(o.From) {
		return true
	}

	return false
}

// SetFrom gets a reference to the given string and assigns it to the From field.
func (o *EthereumTransactionConfig) SetFrom(v string) {
	o.From = &v
}

// GetTo returns the To field value if set, zero value otherwise.
func (o *EthereumTransactionConfig) GetTo() string {
	if o == nil || IsNil(o.To) {
		var ret string
		return ret
	}
	return *o.To
}

// GetToOk returns a tuple with the To field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *EthereumTransactionConfig) GetToOk() (*string, bool) {
	if o == nil || IsNil(o.To) {
		return nil, false
	}
	return o.To, true
}

// HasTo returns a boolean if a field has been set.
func (o *EthereumTransactionConfig) HasTo() bool {
	if o != nil && !IsNil(o.To) {
		return true
	}

	return false
}

// SetTo gets a reference to the given string and assigns it to the To field.
func (o *EthereumTransactionConfig) SetTo(v string) {
	o.To = &v
}

// GetValue returns the Value field value if set, zero value otherwise.
func (o *EthereumTransactionConfig) GetValue() string {
	if o == nil || IsNil(o.Value) {
		var ret string
		return ret
	}
	return *o.Value
}

// GetValueOk returns a tuple with the Value field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *EthereumTransactionConfig) GetValueOk() (*string, bool) {
	if o == nil || IsNil(o.Value) {
		return nil, false
	}
	return o.Value, true
}

// HasValue returns a boolean if a field has been set.
func (o *EthereumTransactionConfig) HasValue() bool {
	if o != nil && !IsNil(o.Value) {
		return true
	}

	return false
}

// SetValue gets a reference to the given string and assigns it to the Value field.
func (o *EthereumTransactionConfig) SetValue(v string) {
	o.Value = &v
}

// GetNonce returns the Nonce field value if set, zero value otherwise.
func (o *EthereumTransactionConfig) GetNonce() string {
	if o == nil || IsNil(o.Nonce) {
		var ret string
		return ret
	}
	return *o.Nonce
}

// GetNonceOk returns a tuple with the Nonce field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *EthereumTransactionConfig) GetNonceOk() (*string, bool) {
	if o == nil || IsNil(o.Nonce) {
		return nil, false
	}
	return o.Nonce, true
}

// HasNonce returns a boolean if a field has been set.
func (o *EthereumTransactionConfig) HasNonce() bool {
	if o != nil && !IsNil(o.Nonce) {
		return true
	}

	return false
}

// SetNonce gets a reference to the given string and assigns it to the Nonce field.
func (o *EthereumTransactionConfig) SetNonce(v string) {
	o.Nonce = &v
}

// GetData returns the Data field value if set, zero value otherwise.
func (o *EthereumTransactionConfig) GetData() string {
	if o == nil || IsNil(o.Data) {
		var ret string
		return ret
	}
	return *o.Data
}

// GetDataOk returns a tuple with the Data field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *EthereumTransactionConfig) GetDataOk() (*string, bool) {
	if o == nil || IsNil(o.Data) {
		return nil, false
	}
	return o.Data, true
}

// HasData returns a boolean if a field has been set.
func (o *EthereumTransactionConfig) HasData() bool {
	if o != nil && !IsNil(o.Data) {
		return true
	}

	return false
}

// SetData gets a reference to the given string and assigns it to the Data field.
func (o *EthereumTransactionConfig) SetData(v string) {
	o.Data = &v
}

// GetGasConfig returns the GasConfig field value if set, zero value otherwise.
func (o *EthereumTransactionConfig) GetGasConfig() GasTransactionConfig {
	if o == nil || IsNil(o.GasConfig) {
		var ret GasTransactionConfig
		return ret
	}
	return *o.GasConfig
}

// GetGasConfigOk returns a tuple with the GasConfig field value if set, nil otherwise
// and a boolean to check if the value has been set.
func (o *EthereumTransactionConfig) GetGasConfigOk() (*GasTransactionConfig, bool) {
	if o == nil || IsNil(o.GasConfig) {
		return nil, false
	}
	return o.GasConfig, true
}

// HasGasConfig returns a boolean if a field has been set.
func (o *EthereumTransactionConfig) HasGasConfig() bool {
	if o != nil && !IsNil(o.GasConfig) {
		return true
	}

	return false
}

// SetGasConfig gets a reference to the given GasTransactionConfig and assigns it to the GasConfig field.
func (o *EthereumTransactionConfig) SetGasConfig(v GasTransactionConfig) {
	o.GasConfig = &v
}

func (o EthereumTransactionConfig) MarshalJSON() ([]byte, error) {
	toSerialize,err := o.ToMap()
	if err != nil {
		return []byte{}, err
	}
	return json.Marshal(toSerialize)
}

func (o EthereumTransactionConfig) ToMap() (map[string]interface{}, error) {
	toSerialize := map[string]interface{}{}
	if !IsNil(o.RawTransaction) {
		toSerialize["rawTransaction"] = o.RawTransaction
	}
	if !IsNil(o.From) {
		toSerialize["from"] = o.From
	}
	if !IsNil(o.To) {
		toSerialize["to"] = o.To
	}
	if !IsNil(o.Value) {
		toSerialize["value"] = o.Value
	}
	if !IsNil(o.Nonce) {
		toSerialize["nonce"] = o.Nonce
	}
	if !IsNil(o.Data) {
		toSerialize["data"] = o.Data
	}
	if !IsNil(o.GasConfig) {
		toSerialize["gasConfig"] = o.GasConfig
	}
	return toSerialize, nil
}

type NullableEthereumTransactionConfig struct {
	value *EthereumTransactionConfig
	isSet bool
}

func (v NullableEthereumTransactionConfig) Get() *EthereumTransactionConfig {
	return v.value
}

func (v *NullableEthereumTransactionConfig) Set(val *EthereumTransactionConfig) {
	v.value = val
	v.isSet = true
}

func (v NullableEthereumTransactionConfig) IsSet() bool {
	return v.isSet
}

func (v *NullableEthereumTransactionConfig) Unset() {
	v.value = nil
	v.isSet = false
}

func NewNullableEthereumTransactionConfig(val *EthereumTransactionConfig) *NullableEthereumTransactionConfig {
	return &NullableEthereumTransactionConfig{value: val, isSet: true}
}

func (v NullableEthereumTransactionConfig) MarshalJSON() ([]byte, error) {
	return json.Marshal(v.value)
}

func (v *NullableEthereumTransactionConfig) UnmarshalJSON(src []byte) error {
	v.isSet = true
	return json.Unmarshal(src, &v.value)
}


