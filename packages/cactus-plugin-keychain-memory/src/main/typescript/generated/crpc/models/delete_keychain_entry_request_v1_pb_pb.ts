//
//Hyperledger Cactus Plugin - Keychain Memory 
//
//Contains/describes the Hyperledger Cacti Keychain Memory plugin.
//
//The version of the OpenAPI document: 2.0.0-rc.4
//
//Generated by OpenAPI Generator: https://openapi-generator.tech

// @generated by protoc-gen-es v1.8.0 with parameter "target=ts"
// @generated from file models/delete_keychain_entry_request_v1_pb.proto (package org.hyperledger.cacti.plugin.keychain.memory, syntax proto3)
/* eslint-disable */
// @ts-nocheck

import type { BinaryReadOptions, FieldList, JsonReadOptions, JsonValue, PartialMessage, PlainMessage } from "@bufbuild/protobuf";
import { Message, proto3 } from "@bufbuild/protobuf";

/**
 * @generated from message org.hyperledger.cacti.plugin.keychain.memory.DeleteKeychainEntryRequestV1PB
 */
export class DeleteKeychainEntryRequestV1PB extends Message<DeleteKeychainEntryRequestV1PB> {
  /**
   * The key for the entry to check the presence of on the keychain.
   *
   * @generated from field: string key = 106079;
   */
  key = "";

  constructor(data?: PartialMessage<DeleteKeychainEntryRequestV1PB>) {
    super();
    proto3.util.initPartial(data, this);
  }

  static readonly runtime: typeof proto3 = proto3;
  static readonly typeName = "org.hyperledger.cacti.plugin.keychain.memory.DeleteKeychainEntryRequestV1PB";
  static readonly fields: FieldList = proto3.util.newFieldList(() => [
    { no: 106079, name: "key", kind: "scalar", T: 9 /* ScalarType.STRING */ },
  ]);

  static fromBinary(bytes: Uint8Array, options?: Partial<BinaryReadOptions>): DeleteKeychainEntryRequestV1PB {
    return new DeleteKeychainEntryRequestV1PB().fromBinary(bytes, options);
  }

  static fromJson(jsonValue: JsonValue, options?: Partial<JsonReadOptions>): DeleteKeychainEntryRequestV1PB {
    return new DeleteKeychainEntryRequestV1PB().fromJson(jsonValue, options);
  }

  static fromJsonString(jsonString: string, options?: Partial<JsonReadOptions>): DeleteKeychainEntryRequestV1PB {
    return new DeleteKeychainEntryRequestV1PB().fromJsonString(jsonString, options);
  }

  static equals(a: DeleteKeychainEntryRequestV1PB | PlainMessage<DeleteKeychainEntryRequestV1PB> | undefined, b: DeleteKeychainEntryRequestV1PB | PlainMessage<DeleteKeychainEntryRequestV1PB> | undefined): boolean {
    return proto3.util.equals(DeleteKeychainEntryRequestV1PB, a, b);
  }
}

