import { Client } from "../client/types";

export interface Key {
  keyId: string;
  publicKey: string;
  dateIns: string;
  isActual: boolean;
}

export interface KeyState {
  isLoading: boolean;
  current?: Key;
  message?: string;
  error?: Error;
}