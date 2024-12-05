/**
 * Generated by the protoc-gen-ts.  DO NOT EDIT!
 * compiler version: 3.19.1
 * source: models/sign_transaction_request_pb.proto
 * git: https://github.com/thesayyn/protoc-gen-ts */
import * as dependency_1 from "./../google/protobuf/any";
import * as pb_1 from "google-protobuf";
export namespace org.hyperledger.cacti.plugin.ledger.connector.besu {
    export class SignTransactionRequestPB extends pb_1.Message {
        #one_of_decls: number[][] = [];
        constructor(data?: any[] | {
            keychainId?: string;
            keychainRef?: string;
            transactionHash?: string;
        }) {
            super();
            pb_1.Message.initialize(this, Array.isArray(data) ? data : [], 0, -1, [], this.#one_of_decls);
            if (!Array.isArray(data) && typeof data == "object") {
                if ("keychainId" in data && data.keychainId != undefined) {
                    this.keychainId = data.keychainId;
                }
                if ("keychainRef" in data && data.keychainRef != undefined) {
                    this.keychainRef = data.keychainRef;
                }
                if ("transactionHash" in data && data.transactionHash != undefined) {
                    this.transactionHash = data.transactionHash;
                }
            }
        }
        get keychainId() {
            return pb_1.Message.getFieldWithDefault(this, 14058372, "") as string;
        }
        set keychainId(value: string) {
            pb_1.Message.setField(this, 14058372, value);
        }
        get keychainRef() {
            return pb_1.Message.getFieldWithDefault(this, 101070193, "") as string;
        }
        set keychainRef(value: string) {
            pb_1.Message.setField(this, 101070193, value);
        }
        get transactionHash() {
            return pb_1.Message.getFieldWithDefault(this, 188901646, "") as string;
        }
        set transactionHash(value: string) {
            pb_1.Message.setField(this, 188901646, value);
        }
        static fromObject(data: {
            keychainId?: string;
            keychainRef?: string;
            transactionHash?: string;
        }): SignTransactionRequestPB {
            const message = new SignTransactionRequestPB({});
            if (data.keychainId != null) {
                message.keychainId = data.keychainId;
            }
            if (data.keychainRef != null) {
                message.keychainRef = data.keychainRef;
            }
            if (data.transactionHash != null) {
                message.transactionHash = data.transactionHash;
            }
            return message;
        }
        toObject() {
            const data: {
                keychainId?: string;
                keychainRef?: string;
                transactionHash?: string;
            } = {};
            if (this.keychainId != null) {
                data.keychainId = this.keychainId;
            }
            if (this.keychainRef != null) {
                data.keychainRef = this.keychainRef;
            }
            if (this.transactionHash != null) {
                data.transactionHash = this.transactionHash;
            }
            return data;
        }
        serialize(): Uint8Array;
        serialize(w: pb_1.BinaryWriter): void;
        serialize(w?: pb_1.BinaryWriter): Uint8Array | void {
            const writer = w || new pb_1.BinaryWriter();
            if (this.keychainId.length)
                writer.writeString(14058372, this.keychainId);
            if (this.keychainRef.length)
                writer.writeString(101070193, this.keychainRef);
            if (this.transactionHash.length)
                writer.writeString(188901646, this.transactionHash);
            if (!w)
                return writer.getResultBuffer();
        }
        static deserialize(bytes: Uint8Array | pb_1.BinaryReader): SignTransactionRequestPB {
            const reader = bytes instanceof pb_1.BinaryReader ? bytes : new pb_1.BinaryReader(bytes), message = new SignTransactionRequestPB();
            while (reader.nextField()) {
                if (reader.isEndGroup())
                    break;
                switch (reader.getFieldNumber()) {
                    case 14058372:
                        message.keychainId = reader.readString();
                        break;
                    case 101070193:
                        message.keychainRef = reader.readString();
                        break;
                    case 188901646:
                        message.transactionHash = reader.readString();
                        break;
                    default: reader.skipField();
                }
            }
            return message;
        }
        serializeBinary(): Uint8Array {
            return this.serialize();
        }
        static deserializeBinary(bytes: Uint8Array): SignTransactionRequestPB {
            return SignTransactionRequestPB.deserialize(bytes);
        }
    }
}