import { ClientUri } from "../../types";

export const CREATE_CLIENT_URI = 'CREATE_CLIENT_URI';
export const CREATE_CLIENT_URI_SUCCESS = 'CREATE_CLIENT_URI_SUCCESS';
export const DELETE_CLIENT_URI = 'DELETE_CLIENT_URI';
export const DELETE_CLIENT_URI_SUCCESS = 'DELETE_CLIENT_URI_SUCCESS';
export const SEARCH_CLIENT_URI = 'SEARCH_CLIENT_URI';
export const SEARCH_CLIENT_URI_SUCCESS = 'SEARCH_CLIENT_URI_SUCCESS';
export const GET_CLIENT_URI_BY_ID = 'GET_CLIENT_URI_BY_ID';
export const GET_CLIENT_URI_BY_ID_SUCCESS = 'GET_CLIENT_URI_BY_ID_SUCCESS';
export const SET_CLIENT_URI_CURRENT_RECORD = 'SET_CLIENT_URI_CURRENT_RECORD';
export const SET_CLIENT_URI_CURRENT_RECORD_SUCCESS = 'SET_CLIENT_URI_CURRENT_RECORD_SUCCESS';
export const CLIENT_URI_LOADING = 'CLIENT_URI_LOADING';
export const CLIENT_URI_FAILURE = 'CLIENT_URI_FAILURE';

export interface CreateClientUriAction {
  type: typeof CREATE_CLIENT_URI;
  clientId: string;
  payload: ClientUri;
  callback?(ClientUri: ClientUri): any;
}

export interface CreateClientUriSuccessAction {
  type: typeof CREATE_CLIENT_URI_SUCCESS
  payload: ClientUri
}

export interface DeleteClientUriAction {
  type: typeof DELETE_CLIENT_URI;
  clientId: string;
  clientUriId: string;
  callback?(): any;
}

export interface DeleteClientUriSuccessAction {
  type: typeof DELETE_CLIENT_URI_SUCCESS;
  clientId: string;
  clientUriId: string;
}

export interface SearchClientUriAction {
  type: typeof SEARCH_CLIENT_URI;
  clientId: string;
}

export interface SearchClientUriSuccessAction {
  type: typeof SEARCH_CLIENT_URI_SUCCESS
  clientUris: Array<ClientUri>;
}

export interface GetClientUriByIdAction {
  type: typeof GET_CLIENT_URI_BY_ID;
  clientId: string;
  clientUriId: string;
}

export interface GetClientUriByIdSuccessAction {
  type: typeof GET_CLIENT_URI_BY_ID_SUCCESS
  clientUri: ClientUri
}

export interface LoadingAction {
  type: typeof CLIENT_URI_LOADING
  message: string
}

export interface FailureAction {
  type: typeof CLIENT_URI_FAILURE
  error: Error
}

export interface SetCurrentRecordAction {
  type: typeof SET_CLIENT_URI_CURRENT_RECORD
  payload?: ClientUri
  callback?(): any;
}

export interface SetCurrentRecordSuccessAction {
  type: typeof SET_CLIENT_URI_CURRENT_RECORD_SUCCESS
  payload?: ClientUri
}

export type ClientUriActionTypes = 
CreateClientUriAction | 
CreateClientUriSuccessAction | 
DeleteClientUriAction | 
DeleteClientUriSuccessAction | 
SearchClientUriAction | 
SearchClientUriSuccessAction | 
GetClientUriByIdAction |
GetClientUriByIdSuccessAction |
LoadingAction |
FailureAction |
SetCurrentRecordAction |
SetCurrentRecordSuccessAction;

export function createClientUri(clientId: string, ClientUri: ClientUri, callback?: (ClientUri: ClientUri) => any): ClientUriActionTypes {
  return {
    type: CREATE_CLIENT_URI,
    clientId: clientId,
    payload: ClientUri,
    callback: callback
  }
}

export function createClientUriSuccess(ClientUri: ClientUri): ClientUriActionTypes {
  return {
    type: CREATE_CLIENT_URI_SUCCESS,
    payload: ClientUri
  }
}

export function deleteClientUri(clientId: string, clientUriId: string, callback?: () => any): ClientUriActionTypes {
  return {
    type: DELETE_CLIENT_URI,
    clientId: clientId,
    clientUriId: clientUriId,
    callback: callback
  }
}

export function deleteClientUriSuccess(clientId: string, clientUriId: string): ClientUriActionTypes {
  return {
    type: DELETE_CLIENT_URI_SUCCESS,
    clientId: clientId,
    clientUriId: clientUriId
  }
}

export function searchClientUri(clientId: string): ClientUriActionTypes {
  return {  
    type: SEARCH_CLIENT_URI,
    clientId: clientId
  }
}

export function searchClientUriSuccess(clientUris: Array<ClientUri>): ClientUriActionTypes {
  return {  
    type: SEARCH_CLIENT_URI_SUCCESS,
    clientUris: clientUris
  }
}

export function getClientUriById(clientId: string, clientUriId: string): ClientUriActionTypes {
  return {
    type: GET_CLIENT_URI_BY_ID,
    clientId: clientId,
    clientUriId: clientUriId
  }
}

export function getClientUriByIdSuccess(ClientUri: ClientUri): ClientUriActionTypes {
  return {
    type: GET_CLIENT_URI_BY_ID_SUCCESS,
    clientUri: ClientUri
  }
}

export function onLoading(message: string): ClientUriActionTypes {
  return {
    type: CLIENT_URI_LOADING,
    message: message
  }
}

export function onFailure(error: Error): ClientUriActionTypes {
  return {
    type: CLIENT_URI_FAILURE,
    error: error
  }
}

export function setCurrentRecord(current?: ClientUri, callback?: () => any): ClientUriActionTypes {
  return {
    type: SET_CLIENT_URI_CURRENT_RECORD,
    payload: current,
    callback: callback
  }
}

export function setCurrentRecordSuccess(current?: ClientUri): ClientUriActionTypes {
  return {
    type: SET_CLIENT_URI_CURRENT_RECORD_SUCCESS,
    payload: current
  }
}
