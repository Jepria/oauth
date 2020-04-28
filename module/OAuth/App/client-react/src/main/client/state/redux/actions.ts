import { Client, ClientSearchTemplate, SearchRequest } from "../../types";

export const CREATE_CLIENT = 'CREATE_CLIENT';
export const CREATE_CLIENT_SUCCESS = 'CREATE_CLIENT_SUCCESS';
export const UPDATE_CLIENT = 'UPDATE_CLIENT';
export const UPDATE_CLIENT_SUCCESS = 'UPDATE_CLIENT_SUCCESS';
export const DELETE_CLIENT = 'DELETE_CLIENT';
export const DELETE_CLIENT_SUCCESS = 'DELETE_CLIENT_SUCCESS';
export const POST_CLIENT_SEARCH_REQUEST = 'POST_CLIENT_SEARCH_REQUEST';
export const POST_CLIENT_SEARCH_REQUEST_SUCCESS = 'POST_CLIENT_SEARCH_REQUEST_SUCCESS';
export const SEARCH_CLIENTS = 'SEARCH_CLIENTS';
export const SEARCH_CLIENTS_SUCCESS = 'SEARCH_CLIENTS_SUCCESS';
export const GET_CLIENT_BY_ID = 'GET_CLIENT_BY_ID';
export const GET_CLIENT_BY_ID_SUCCESS = 'GET_CLIENT_BY_ID_SUCCESS';
export const SET_CURRENT_RECORD = 'SET_CURRENT_RECORD';
export const SET_CURRENT_RECORD_SUCCESS = 'SET_CURRENT_RECORD_SUCCESS';
export const LOADING = 'LOADING';
export const FAILURE = 'FAILURE';

export interface CreateClientAction {
  type: typeof CREATE_CLIENT;
  payload: Client;
  callback?(client: Client): any;
}

export interface CreateClientSuccessAction {
  type: typeof CREATE_CLIENT_SUCCESS
  payload: Client
}

export interface UpdateClientAction {
  type: typeof UPDATE_CLIENT;
  clientId: string;
  payload: Client;
  callback?(client: Client): any;
}

export interface UpdateClientSuccessAction {
  type: typeof UPDATE_CLIENT_SUCCESS
  payload: Client
}

export interface DeleteClientAction {
  type: typeof DELETE_CLIENT;
  clientId: string;
  callback?(): any;
}

export interface DeleteClientSuccessAction {
  type: typeof DELETE_CLIENT_SUCCESS
  clientId: string
}

export interface PostSearchClientRequestAction {
  type: typeof POST_CLIENT_SEARCH_REQUEST
  searchRequest: SearchRequest<ClientSearchTemplate>
}

export interface PostSearchClientRequestSuccessAction {
  type: typeof POST_CLIENT_SEARCH_REQUEST_SUCCESS
  searchRequest: SearchRequest<ClientSearchTemplate>
  searchId: string;
}

export interface SearchClientsAction {
  type: typeof SEARCH_CLIENTS
  searchId: string;
  pageSize: number;
  page: number;
}

export interface SearchClientsSuccessAction {
  type: typeof SEARCH_CLIENTS_SUCCESS
  clients: Array<Client>;
  resultSetSize: number;
}

export interface GetClientByIdAction {
  type: typeof GET_CLIENT_BY_ID
  clientId: string
}

export interface GetClientByIdSuccessAction {
  type: typeof GET_CLIENT_BY_ID_SUCCESS
  client: Client
}

export interface LoadingAction {
  type: typeof LOADING
  message: string
}

export interface FailureAction {
  type: typeof FAILURE
  error: Error
}

export interface SetCurrentRecordAction {
  type: typeof SET_CURRENT_RECORD
  payload?: Client
  callback?(): any;
}

export interface SetCurrentRecordSuccessAction {
  type: typeof SET_CURRENT_RECORD_SUCCESS
  payload?: Client
}

export type ClientActionTypes = 
CreateClientAction | 
CreateClientSuccessAction | 
UpdateClientAction | 
UpdateClientSuccessAction | 
DeleteClientAction | 
DeleteClientSuccessAction | 
PostSearchClientRequestAction | 
PostSearchClientRequestSuccessAction | 
SearchClientsAction | 
SearchClientsSuccessAction | 
GetClientByIdAction |
GetClientByIdSuccessAction |
LoadingAction |
FailureAction |
SetCurrentRecordAction |
SetCurrentRecordSuccessAction;

export function createClient(client: Client, callback?: (client: Client) => any): ClientActionTypes {
  return {
    type: CREATE_CLIENT,
    payload: client,
    callback: callback
  }
}

export function createClientSuccess(client: Client): ClientActionTypes {
  return {
    type: CREATE_CLIENT_SUCCESS,
    payload: client
  }
}

export function updateClient(clientId: string, client: Client, callback?: (client: Client) => any): ClientActionTypes {
  return {
    type: UPDATE_CLIENT,
    clientId: clientId,
    payload: client,
    callback: callback
  }
}

export function updateClientSuccess(client: Client): ClientActionTypes {
  return {
    type: UPDATE_CLIENT_SUCCESS,
    payload: client
  }
}

export function deleteClient(clientId: string, callback?: () => any): ClientActionTypes {
  return {
    type: DELETE_CLIENT,
    clientId: clientId,
    callback: callback
  }
}

export function deleteClientSuccess(clientId: string): ClientActionTypes {
  return {
    type: DELETE_CLIENT_SUCCESS,
    clientId: clientId
  }
}

export function postSearchClientRequest(searchRequest: SearchRequest<ClientSearchTemplate>): ClientActionTypes {
  return {
    type: POST_CLIENT_SEARCH_REQUEST,
    searchRequest: searchRequest
  }
}

export function postSearchClientRequestSuccess(searchId: string, searchRequest: SearchRequest<ClientSearchTemplate>): ClientActionTypes {
  return {
    type: POST_CLIENT_SEARCH_REQUEST_SUCCESS,
    searchRequest: searchRequest,
    searchId: searchId
  }
}

export function searchClients(searchId: string, pageSize: number, pageNumber: number): ClientActionTypes {
  return {  
    type: SEARCH_CLIENTS,
    searchId: searchId,
    pageSize: pageSize,
    page: pageNumber
  }
}

export function searchClientsSuccess(clients: Array<Client>, resultSetSize: number): ClientActionTypes {
  return {  
    type: SEARCH_CLIENTS_SUCCESS,
    clients: clients,
    resultSetSize: resultSetSize
  }
}

export function getClientById(clientId: string): ClientActionTypes {
  return {
    type: GET_CLIENT_BY_ID,
    clientId: clientId
  }
}

export function getClientByIdSuccess(client: Client): ClientActionTypes {
  return {
    type: GET_CLIENT_BY_ID_SUCCESS,
    client: client
  }
}

export function onLoading(message: string): ClientActionTypes {
  return {
    type: LOADING,
    message: message
  }
}

export function onFailure(error: Error): ClientActionTypes {
  return {
    type: FAILURE,
    error: error
  }
}

export function setCurrentRecord(current?: Client, callback?: () => any): ClientActionTypes {
  return {
    type: SET_CURRENT_RECORD,
    payload: current,
    callback: callback
  }
}

export function setCurrentRecordSuccess(current?: Client): ClientActionTypes {
  return {
    type: SET_CURRENT_RECORD_SUCCESS,
    payload: current
  }
}
