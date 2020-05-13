import { Key } from "../../types";

export const GET_KEY = 'GET_KEY';
export const GET_KEY_SUCCESS = 'GET_KEY_SUCCESS';
export const UPDATE_KEY = 'UPDATE_KEY';
export const UPDATE_KEY_SUCCESS = 'UPDATE_KEY_SUCCESS';
export const LOADING = 'LOADING';
export const FAILURE = 'FAILURE';


export interface GetKeyAction {
  type: typeof GET_KEY
  callback?(): any;
}

export interface GetKeySuccessAction {
  type: typeof GET_KEY_SUCCESS
  key: Key
}

export interface UpdateKeyAction {
  type: typeof UPDATE_KEY
  callback?(): any;
}

export interface UpdateKeySuccessAction {
  type: typeof UPDATE_KEY_SUCCESS
}

export interface LoadingAction {
  type: typeof LOADING
  message: string
}

export interface FailureAction {
  type: typeof FAILURE
  error: Error
}

export type KeyActionTypes = 
GetKeyAction |
GetKeySuccessAction |
UpdateKeyAction |
UpdateKeySuccessAction |
LoadingAction |
FailureAction;


export function getKey(callback?: () => any): KeyActionTypes {
  return {
    type: GET_KEY,
    callback: callback
  }
}

export function getKeySuccess(key: Key): KeyActionTypes {
  return {
    type: GET_KEY_SUCCESS,
    key: key
  }
}

export function updateKey(callback?: () => any): KeyActionTypes {
  return {
    type: UPDATE_KEY,
    callback: callback
  }
}

export function updateKeySuccess(): KeyActionTypes {
  return {
    type: UPDATE_KEY_SUCCESS
  }
}

export function onLoading(message: string): KeyActionTypes {
  return {
    type: LOADING,
    message: message
  }
}

export function onFailure(error: Error): KeyActionTypes {
  return {
    type: FAILURE,
    error: error
  }
}
