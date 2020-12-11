import { ClientUri, ClientUriCreateDto } from "../types"

export const CREATE_CLIENT_URI = 'CREATE_CLIENT_URI'
export const CREATE_CLIENT_URI_SUCCESS = 'CREATE_CLIENT_URI_SUCCESS'
export const CREATE_CLIENT_URI_FAILURE = 'CREATE_CLIENT_URI_FAILURE'
export const DELETE_CLIENT_URI = 'DELETE_CLIENT_URI'
export const DELETE_CLIENT_URI_SUCCESS = 'DELETE_CLIENT_URI_SUCCESS'
export const DELETE_CLIENT_URI_FAILURE = 'DELETE_CLIENT_URI_FAILURE'
export const SEARCH_CLIENT_URI = 'SEARCH_CLIENT_URI'
export const SEARCH_CLIENT_URI_SUCCESS = 'SEARCH_CLIENT_URI_SUCCESS'
export const SEARCH_CLIENT_URI_FAILURE = 'SEARCH_CLIENT_URI_FAILURE'
export const GET_CLIENT_URI_BY_ID = 'GET_CLIENT_URI_BY_ID'
export const GET_CLIENT_URI_BY_ID_SUCCESS = 'GET_CLIENT_URI_BY_ID_SUCCESS'
export const GET_CLIENT_URI_BY_ID_FAILURE = 'GET_CLIENT_URI_BY_ID_FAILURE'
export const SET_CLIENT_URI_CURRENT_RECORD = 'SET_CLIENT_URI_CURRENT_RECORD'
export const SET_CLIENT_URI_CURRENT_RECORD_SUCCESS = 'SET_CLIENT_URI_CURRENT_RECORD_SUCCESS'
export const SET_CLIENT_URI_CURRENT_RECORD_FAILURE = 'SET_CLIENT_URI_CURRENT_RECORD_FAILURE'
export const SELECT_CLIENT_URI_RECORDS = 'SELECT_CLIENT_URI_RECORDS'

export interface CreateClientUriAction {
  type: typeof CREATE_CLIENT_URI
  clientId: string
  payload: ClientUriCreateDto
  loadingMessage: string
  callback?(ClientUri: ClientUri): any
}

export interface CreateClientUriSuccessAction {
  type: typeof CREATE_CLIENT_URI_SUCCESS
  payload: ClientUri
}

export interface CreateClientUriFailureAction {
  type: typeof CREATE_CLIENT_URI_FAILURE
  error: any
}

export interface DeleteClientUriAction {
  type: typeof DELETE_CLIENT_URI
  clientId: string
  clientUriIds: string[]
  loadingMessage: string
  callback?(): any
}

export interface DeleteClientUriSuccessAction {
  type: typeof DELETE_CLIENT_URI_SUCCESS
}

export interface DeleteClientUriFailureAction {
  type: typeof DELETE_CLIENT_URI_FAILURE
  error: any
}

export interface SearchClientUriAction {
  type: typeof SEARCH_CLIENT_URI
  loadingMessage: string
  clientId: string
}

export interface SearchClientUriSuccessAction {
  type: typeof SEARCH_CLIENT_URI_SUCCESS
  clientUris: Array<ClientUri>
}

export interface SearchClientUriFailureAction {
  type: typeof SEARCH_CLIENT_URI_FAILURE
  error: any
}

export interface GetClientUriByIdAction {
  type: typeof GET_CLIENT_URI_BY_ID
  clientId: string
  loadingMessage: string
  clientUriId: string
}

export interface GetClientUriByIdSuccessAction {
  type: typeof GET_CLIENT_URI_BY_ID_SUCCESS
  clientUri: ClientUri
}

export interface GetClientUriByIdFailureAction {
  type: typeof GET_CLIENT_URI_BY_ID_FAILURE
  error: any
}

export interface SetCurrentRecordAction {
  type: typeof SET_CLIENT_URI_CURRENT_RECORD
  payload?: ClientUri
  callback?(): any
}

export interface SetCurrentRecordSuccessAction {
  type: typeof SET_CLIENT_URI_CURRENT_RECORD_SUCCESS
  payload?: ClientUri
}

export interface SelectRecordsAction {
  type: typeof SELECT_CLIENT_URI_RECORDS
  records: Array<ClientUri>
}

export type ClientUriActionTypes = 
CreateClientUriAction | 
CreateClientUriSuccessAction | 
CreateClientUriFailureAction | 
DeleteClientUriAction | 
DeleteClientUriSuccessAction | 
DeleteClientUriFailureAction | 
SearchClientUriAction | 
SearchClientUriSuccessAction | 
SearchClientUriFailureAction | 
GetClientUriByIdAction |
GetClientUriByIdSuccessAction |
GetClientUriByIdFailureAction |
SetCurrentRecordAction |
SetCurrentRecordSuccessAction |
SelectRecordsAction

export function createClientUri(clientId: string, ClientUri: ClientUriCreateDto, loadingMessage: string, callback?: (ClientUri: ClientUri) => any): ClientUriActionTypes {
  return {
    type: CREATE_CLIENT_URI,
    clientId: clientId,
    payload: ClientUri,
    loadingMessage,
    callback: callback
  }
}

export function createClientUriSuccess(ClientUri: ClientUri): ClientUriActionTypes {
  return {
    type: CREATE_CLIENT_URI_SUCCESS,
    payload: ClientUri
  }
}

export function createClientUriFailure(error: any): ClientUriActionTypes {
  return {
    type: CREATE_CLIENT_URI_FAILURE,
    error: error
  }
}

export function deleteClientUri(clientId: string, clientUriIds: string[], loadingMessage: string, callback?: () => any): ClientUriActionTypes {
  return {
    type: DELETE_CLIENT_URI,
    clientId: clientId,
    clientUriIds,
    loadingMessage,
    callback: callback
  }
}

export function deleteClientUriSuccess(): ClientUriActionTypes {
  return {
    type: DELETE_CLIENT_URI_SUCCESS
  }
}

export function deleteClientUriFailure(error: any): ClientUriActionTypes {
  return {
    type: DELETE_CLIENT_URI_FAILURE,
    error: error
  }
}

export function searchClientUri(clientId: string, loadingMessage: string): ClientUriActionTypes {
  return {  
    type: SEARCH_CLIENT_URI,
    loadingMessage,
    clientId: clientId
  }
}

export function searchClientUriSuccess(clientUris: Array<ClientUri>): ClientUriActionTypes {
  return {  
    type: SEARCH_CLIENT_URI_SUCCESS,
    clientUris: clientUris
  }
}

export function searchClientUriFailure(error: any): ClientUriActionTypes {
  return {
    type: SEARCH_CLIENT_URI_FAILURE,
    error: error
  }
}

export function getClientUriById(clientId: string, clientUriId: string, loadingMessage: string): ClientUriActionTypes {
  return {
    type: GET_CLIENT_URI_BY_ID,
    clientId: clientId,
    loadingMessage,
    clientUriId: clientUriId
  }
}

export function getClientUriByIdSuccess(ClientUri: ClientUri): ClientUriActionTypes {
  return {
    type: GET_CLIENT_URI_BY_ID_SUCCESS,
    clientUri: ClientUri
  }
}

export function getClientUriByIdFailure(error: any): ClientUriActionTypes {
  return {
    type: GET_CLIENT_URI_BY_ID_FAILURE,
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

export function selectRecords(records: Array<ClientUri>): ClientUriActionTypes {
  return {
    type: SELECT_CLIENT_URI_RECORDS,
    records: records
  }
}
