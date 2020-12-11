import { Key } from "../types";

export const GET_KEY = 'GET_KEY';
export const GET_KEY_SUCCESS = 'GET_KEY_SUCCESS';
export const GET_KEY_FAILURE = 'GET_KEY_FAILURE';
export const UPDATE_KEY = 'UPDATE_KEY';
export const UPDATE_KEY_SUCCESS = 'UPDATE_KEY_SUCCESS';
export const UPDATE_KEY_FAILURE = 'UPDATE_KEY_FAILURE';

export interface GetKeyAction {
  type: typeof GET_KEY
  loadingMessage: string
  callback?(): any
}

export interface GetKeySuccessAction {
  type: typeof GET_KEY_SUCCESS
  key: Key
}

export interface GetKeyFailureAction {
  type: typeof GET_KEY_FAILURE
  error: any
}

export interface UpdateKeyAction {
  type: typeof UPDATE_KEY
  loadingMessage: string
  callback?(): any
}

export interface UpdateKeySuccessAction {
  type: typeof UPDATE_KEY_SUCCESS
}

export interface UpdateKeyFailureAction {
  type: typeof UPDATE_KEY_FAILURE
  error: any
}

export type KeyActionTypes = 
GetKeyAction |
GetKeySuccessAction |
GetKeyFailureAction |
UpdateKeyAction |
UpdateKeySuccessAction |
UpdateKeyFailureAction;


export function getKey(loadingMessage: string, callback?: () => any): KeyActionTypes {
  return {
    type: GET_KEY,
    loadingMessage,
    callback: callback
  }
}

export function getKeySuccess(key: Key): KeyActionTypes {
  return {
    type: GET_KEY_SUCCESS,
    key: key
  }
}


export function getKeyFailure(error: any): KeyActionTypes {
  return {
    type: GET_KEY_FAILURE,
    error: error
  }
}
export function updateKey(loadingMessage: string, callback?: () => any): KeyActionTypes {
  return {
    type: UPDATE_KEY,
    loadingMessage,
    callback: callback
  }
}

export function updateKeySuccess(): KeyActionTypes {
  return {
    type: UPDATE_KEY_SUCCESS
  }
}

export function updateKeyFailure(error: any): KeyActionTypes {
  return {
    type: UPDATE_KEY_FAILURE,
    error: error
  }
}