import { Key } from "../types";

export interface GetKeyAction {
  loadingMessage: string
  callback?(): any
}

export interface GetKeySuccessAction {
  key: Key
}

export interface UpdateKeyAction {
  loadingMessage: string
  callback?(): any
}

export interface UpdateKeySuccessAction {
}

export interface FailureAction {
  error: any
}