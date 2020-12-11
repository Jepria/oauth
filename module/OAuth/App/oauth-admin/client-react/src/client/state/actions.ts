import { Client, ClientSearchTemplate, SearchRequest, Option } from "../types"

export const CREATE_CLIENT = 'CREATE_CLIENT'
export const CREATE_CLIENT_SUCCESS = 'CREATE_CLIENT_SUCCESS'
export const CREATE_CLIENT_FAILURE = 'CREATE_CLIENT_FAILURE'
export const UPDATE_CLIENT = 'UPDATE_CLIENT'
export const UPDATE_CLIENT_SUCCESS = 'UPDATE_CLIENT_SUCCESS'
export const UPDATE_CLIENT_FAILURE = 'UPDATE_CLIENT_FAILURE'
export const DELETE_CLIENT = 'DELETE_CLIENT'
export const DELETE_CLIENT_SUCCESS = 'DELETE_CLIENT_SUCCESS'
export const DELETE_CLIENT_FAILURE = 'DELETE_CLIENT_FAILURE'
export const POST_CLIENT_SEARCH_REQUEST = 'POST_CLIENT_SEARCH_REQUEST'
export const POST_CLIENT_SEARCH_REQUEST_SUCCESS = 'POST_CLIENT_SEARCH_REQUEST_SUCCESS'
export const POST_CLIENT_SEARCH_REQUEST_FAILURE = 'POST_CLIENT_SEARCH_REQUEST_FAILURE'
export const SEARCH_CLIENTS = 'SEARCH_CLIENTS'
export const SEARCH_CLIENTS_SUCCESS = 'SEARCH_CLIENTS_SUCCESS'
export const SEARCH_CLIENTS_FAILURE = 'SEARCH_CLIENTS_FAILURE'
export const GET_CLIENT_BY_ID = 'GET_CLIENT_BY_ID'
export const GET_CLIENT_BY_ID_SUCCESS = 'GET_CLIENT_BY_ID_SUCCESS'
export const GET_CLIENT_BY_ID_FAILURE = 'GET_CLIENT_BY_ID_FAILURE'
export const SET_CLIENT_CURRENT_RECORD = 'SET_CLIENT_CURRENT_RECORD'
export const SET_CLIENT_CURRENT_RECORD_SUCCESS = 'SET_CLIENT_CURRENT_RECORD_SUCCESS'
export const SET_CLIENT_CURRENT_RECORD_FAILURE = 'SET_CLIENT_CURRENT_RECORD_FAILURE'
export const GET_ROLES = "GET_ROLES"
export const GET_ROLES_SUCCESS = "GET_ROLES_SUCCESS"
export const GET_ROLES_FAILURE = "GET_ROLES_FAILURE"
export const SELECT_CLIENT_RECORDS = "SELECT_CLIENT_RECORDS"

export interface CreateClientAction {
  type: typeof CREATE_CLIENT
  payload: Client
  loadingMessage: string
  callback?(client: Client): any
}

export interface CreateClientSuccessAction {
  type: typeof CREATE_CLIENT_SUCCESS
  payload: Client
}

export interface CreateClientFailureAction {
  type: typeof CREATE_CLIENT_FAILURE
  error: any
}

export interface UpdateClientAction {
  type: typeof UPDATE_CLIENT
  clientId: string
  payload: Client
  loadingMessage: string
  callback?(client: Client): any
}

export interface UpdateClientSuccessAction {
  type: typeof UPDATE_CLIENT_SUCCESS
  payload: Client
}

export interface UpdateClientFailureAction {
  type: typeof UPDATE_CLIENT_FAILURE
  error: any
}

export interface DeleteClientAction {
  type: typeof DELETE_CLIENT
  clientIds: string[]
  loadingMessage: string
  callback?(): any
}

export interface DeleteClientSuccessAction {
  type: typeof DELETE_CLIENT_SUCCESS
}

export interface DeleteClientFailureAction {
  type: typeof DELETE_CLIENT_FAILURE
  error: any
}

export interface PostSearchClientRequestAction {
  type: typeof POST_CLIENT_SEARCH_REQUEST
  searchRequest: SearchRequest<ClientSearchTemplate>
  callback?(): any
  loadingMessage: string
}

export interface PostSearchClientRequestSuccessAction {
  type: typeof POST_CLIENT_SEARCH_REQUEST_SUCCESS
  searchRequest: SearchRequest<ClientSearchTemplate>
  searchId: string
}

export interface PostSearchClientRequestFailureAction {
  type: typeof POST_CLIENT_SEARCH_REQUEST_FAILURE
  error: any
}

export interface SearchClientsAction {
  type: typeof SEARCH_CLIENTS
  searchId: string
  pageSize: number
  page: number
  loadingMessage: string
}

export interface SearchClientsSuccessAction {
  type: typeof SEARCH_CLIENTS_SUCCESS
  clients: Array<Client>
  resultSetSize: number
}

export interface SearchClientsFailureAction {
  type: typeof SEARCH_CLIENTS_FAILURE
  error: any
}

export interface GetClientByIdAction {
  type: typeof GET_CLIENT_BY_ID
  clientId: string
  loadingMessage: string
}

export interface GetClientByIdSuccessAction {
  type: typeof GET_CLIENT_BY_ID_SUCCESS
  client: Client
}

export interface GetClientByIdFailureAction {
  type: typeof GET_CLIENT_BY_ID_FAILURE
  error: any
}

export interface SetCurrentRecordAction {
  type: typeof SET_CLIENT_CURRENT_RECORD
  payload?: Client
  callback?(): any
}

export interface SetCurrentRecordSuccessAction {
  type: typeof SET_CLIENT_CURRENT_RECORD_SUCCESS
  payload?: Client
}

export interface GetRolesAction {
  type: typeof GET_ROLES
  roleName?: string
}

export interface GetRolesSuccessAction {
  type: typeof GET_ROLES_SUCCESS
  roles: Array<Option>
}

export interface GetRolesFailureAction {
  type: typeof GET_ROLES_FAILURE
  error: any
}

export interface SelectRecordsAction {
  type: typeof SELECT_CLIENT_RECORDS
  records: Array<Client>
}

export type ClientActionTypes = 
CreateClientAction | 
CreateClientSuccessAction | 
CreateClientFailureAction | 
UpdateClientAction | 
UpdateClientSuccessAction | 
UpdateClientFailureAction | 
DeleteClientAction | 
DeleteClientSuccessAction | 
DeleteClientFailureAction | 
PostSearchClientRequestAction | 
PostSearchClientRequestSuccessAction | 
PostSearchClientRequestFailureAction | 
SearchClientsAction | 
SearchClientsSuccessAction | 
SearchClientsFailureAction | 
GetClientByIdAction |
GetClientByIdSuccessAction |
GetClientByIdFailureAction |
SetCurrentRecordAction |
SetCurrentRecordSuccessAction |
GetRolesAction |
GetRolesSuccessAction |
GetRolesFailureAction |
SelectRecordsAction;

export function createClient(client: Client, loadingMessage: string, callback?: (client: Client) => any): ClientActionTypes {
  return {
    type: CREATE_CLIENT,
    payload: client,
    loadingMessage,
    callback: callback
  }
}

export function createClientSuccess(client: Client): ClientActionTypes {
  return {
    type: CREATE_CLIENT_SUCCESS,
    payload: client
  }
}

export function createClientFailure(error: any): ClientActionTypes {
  return {
    type: CREATE_CLIENT_FAILURE,
    error: error
  }
}

export function updateClient(clientId: string, client: Client, loadingMessage: string, callback?: (client: Client) => any): ClientActionTypes {
  return {
    type: UPDATE_CLIENT,
    clientId: clientId,
    payload: client,
    loadingMessage,
    callback: callback
  }
}

export function updateClientSuccess(client: Client): ClientActionTypes {
  return {
    type: UPDATE_CLIENT_SUCCESS,
    payload: client
  }
}

export function updateClientFailure(error: any): ClientActionTypes {
  return {
    type: UPDATE_CLIENT_FAILURE,
    error: error
  }
}

export function deleteClient(clientIds: string[], loadingMessage: string, callback?: () => any): ClientActionTypes {
  return {
    type: DELETE_CLIENT,
    clientIds,
    loadingMessage,
    callback: callback
  }
}

export function deleteClientSuccess(): ClientActionTypes {
  return {
    type: DELETE_CLIENT_SUCCESS,
  }
}

export function deleteClientFailure(error: any): ClientActionTypes {
  return {
    type: DELETE_CLIENT_FAILURE,
    error: error
  }
}

export function postSearchClientRequest(searchRequest: SearchRequest<ClientSearchTemplate>, loadingMessage: string, callback?: () => any): ClientActionTypes {
  return {
    type: POST_CLIENT_SEARCH_REQUEST,
    searchRequest: searchRequest,
    loadingMessage,
    callback
  }
}

export function postSearchClientRequestSuccess(searchId: string, searchRequest: SearchRequest<ClientSearchTemplate>): ClientActionTypes {
  return {
    type: POST_CLIENT_SEARCH_REQUEST_SUCCESS,
    searchRequest: searchRequest,
    searchId: searchId
  }
}

export function postSearchClientRequestFailure(error: any): ClientActionTypes {
  return {
    type: POST_CLIENT_SEARCH_REQUEST_FAILURE,
    error: error
  }
}

export function searchClients(searchId: string, pageSize: number, pageNumber: number, loadingMessage: string): ClientActionTypes {
  return {  
    type: SEARCH_CLIENTS,
    searchId: searchId,
    pageSize: pageSize,
    page: pageNumber,
    loadingMessage
  }
}

export function searchClientsSuccess(clients: Array<Client>, resultSetSize: number): ClientActionTypes {
  return {  
    type: SEARCH_CLIENTS_SUCCESS,
    clients: clients,
    resultSetSize: resultSetSize
  }
}

export function searchClientsFailure(error: any): ClientActionTypes {
  return {
    type: SEARCH_CLIENTS_FAILURE,
    error: error
  }
}

export function getClientById(clientId: string, loadingMessage: string): ClientActionTypes {
  return {
    type: GET_CLIENT_BY_ID,
    clientId: clientId,
    loadingMessage
  }
}

export function getClientByIdSuccess(client: Client): ClientActionTypes {
  return {
    type: GET_CLIENT_BY_ID_SUCCESS,
    client: client
  }
}

export function getClientByIdFailure(error: any): ClientActionTypes {
  return {
    type: GET_CLIENT_BY_ID_FAILURE,
    error: error
  }
}

export function setCurrentRecord(current?: Client, callback?: () => any): ClientActionTypes {
  return {
    type: SET_CLIENT_CURRENT_RECORD,
    payload: current,
    callback: callback
  }
}

export function setCurrentRecordSuccess(current?: Client): ClientActionTypes {
  return {
    type: SET_CLIENT_CURRENT_RECORD_SUCCESS,
    payload: current
  }
}

export function getRoles(roleName?: string): ClientActionTypes {
  return {
    type: GET_ROLES,
    roleName: roleName
  }
}

export function getRolesSuccess(roles: Array<Option>): ClientActionTypes {
  return {
    type: GET_ROLES_SUCCESS,
    roles: roles
  }
}

export function getRolesFailure(error: any): ClientActionTypes {
  return {
    type: GET_ROLES_FAILURE,
    error: error
  }
}

export function selectRecords(records: Array<Client>): ClientActionTypes {
  return {
    type: SELECT_CLIENT_RECORDS,
    records: records
  }
}
