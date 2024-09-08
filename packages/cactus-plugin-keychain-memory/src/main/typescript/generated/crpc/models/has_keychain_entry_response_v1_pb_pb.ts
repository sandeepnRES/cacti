//
//Hyperledger Cactus Plugin - Keychain Memory 
//
//Contains/describes the Hyperledger Cacti Keychain Memory plugin.
//
//The version of the OpenAPI document: 2.0.0-rc.4
//
//Generated by OpenAPI Generator: https://openapi-generator.tech

// @generated by protoc-gen-es v1.8.0 with parameter "target=ts"
// @generated from file models/has_keychain_entry_response_v1_pb.proto (package org.hyperledger.cacti.plugin.keychain.memory, syntax proto3)
/* eslint-disable */
// @ts-nocheck

import type { BinaryReadOptions, FieldList, JsonReadOptions, JsonValue, PartialMessage, PlainMessage } from "@bufbuild/protobuf";
import { Message, proto3 } from "@bufbuild/protobuf";

/**
 * @generated from message org.hyperledger.cacti.plugin.keychain.memory.HasKeychainEntryResponseV1PB
 */
export class HasKeychainEntryResponseV1PB extends Message<HasKeychainEntryResponseV1PB> {
  /**
   * The key that was used to check the presence of the value in the entry store.
   *
   * @generated from field: string key = 106079;
   */
  key = "";

  /**
   * Date and time encoded as JSON when the presence check was performed by the plugin backend.
   *
   * @generated from field: string checkedAt = 399084090;
   */
  checkedAt = "";

  /**
   * The boolean true or false indicating the presence or absence of an entry under 'key'.
   *
   * @generated from field: bool isPresent = 361185232;
   */
  isPresent = false;

  constructor(data?: PartialMessage<HasKeychainEntryResponseV1PB>) {
    super();
    proto3.util.initPartial(data, this);
  }

  static readonly runtime: typeof proto3 = proto3;
  static readonly typeName = "org.hyperledger.cacti.plugin.keychain.memory.HasKeychainEntryResponseV1PB";
  static readonly fields: FieldList = proto3.util.newFieldList(() => [
    { no: 106079, name: "key", kind: "scalar", T: 9 /* ScalarType.STRING */ },
    { no: 399084090, name: "checkedAt", kind: "scalar", T: 9 /* ScalarType.STRING */ },
    { no: 361185232, name: "isPresent", kind: "scalar", T: 8 /* ScalarType.BOOL */ },
  ]);

  static fromBinary(bytes: Uint8Array, options?: Partial<BinaryReadOptions>): HasKeychainEntryResponseV1PB {
    return new HasKeychainEntryResponseV1PB().fromBinary(bytes, options);
  }

  static fromJson(jsonValue: JsonValue, options?: Partial<JsonReadOptions>): HasKeychainEntryResponseV1PB {
    return new HasKeychainEntryResponseV1PB().fromJson(jsonValue, options);
  }

  static fromJsonString(jsonString: string, options?: Partial<JsonReadOptions>): HasKeychainEntryResponseV1PB {
    return new HasKeychainEntryResponseV1PB().fromJsonString(jsonString, options);
  }

  static equals(a: HasKeychainEntryResponseV1PB | PlainMessage<HasKeychainEntryResponseV1PB> | undefined, b: HasKeychainEntryResponseV1PB | PlainMessage<HasKeychainEntryResponseV1PB> | undefined): boolean {
    return proto3.util.equals(HasKeychainEntryResponseV1PB, a, b);
  }
}

