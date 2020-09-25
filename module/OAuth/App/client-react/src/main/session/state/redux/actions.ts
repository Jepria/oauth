import { Session, SessionSearchTemplate, SearchRequest, Operator } from "../../types";
import { Client } from "../../../client/types";

export const DELETE_SESSION = 'DELETE_SESSION';
export const DELETE_SESSION_SUCCESS = 'DELETE_SESSION_SUCCESS';
export const POST_SESSION_SEARCH_REQUEST = 'POST_SESSION_SEARCH_REQUEST';
export const POST_SESSION_SEARCH_REQUEST_SUCCESS = 'POST_SESSION_SEARCH_REQUEST_SUCCESS';
export const SEARCH_SESSIONS = 'SEARCH_SESSIONS';
export const SEARCH_SESSIONS_SUCCESS = 'SEARCH_SESSIONS_SUCCESS';
export const GET_SESSION_BY_ID = 'GET_SESSION_BY_ID';
export const GET_SESSION_BY_ID_SUCCESS = 'GET_SESSION_BY_ID_SUCCESS';
export const SET_SESSION_CURRENT_RECORD = 'SET_SESSION_CURRENT_RECORD';
export const SET_SESSION_CURRENT_RECORD_SUCCESS = 'SET_SESSION_CURRENT_RECORD_SUCCESS';
export const GET_CLIENTS = 'GET_CLIENTS';
export const GET_CLIENTS_SUCCESS = 'GET_CLIENTS_SUCCESS';
export const GET_OPERATORS = 'GET_OPERATORS';
export const GET_OPERATORS_SUCCESS = 'GET_OPERATORS_SUCCESS';
export const SESSION_LOADING = 'SESSION_LOADING';
export const SESSION_FAILURE = 'SESSION_FAILURE';

export interface DeleteSessionAction {
  type: typeof DELETE_SESSION;
  sessionId: string;
  callback?(): any;
}

export interface DeleteSessionSuccessAction {
  type: typeof DELETE_SESSION_SUCCESS
  sessionId: string
}

export interface PostSearchSessionRequestAction {
  type: typeof POST_SESSION_SEARCH_REQUEST
  searchRequest: SearchRequest<SessionSearchTemplate>
  callback?(): any;
}

export interface PostSearchSessionRequestSuccessAction {
  type: typeof POST_SESSION_SEARCH_REQUEST_SUCCESS
  searchRequest: SearchRequest<SessionSearchTemplate>
  searchId: string;
}

export interface SearchSessionsAction {
  type: typeof SEARCH_SESSIONS
  searchId: string;
  pageSize: number;
  page: number;
}

export interface SearchSessionsSuccessAction {
  type: typeof SEARCH_SESSIONS_SUCCESS
  sessions: Array<Session>;
  resultSetSize: number;
}

export interface GetSessionByIdAction {
  type: typeof GET_SESSION_BY_ID
  sessionId: string
}

export interface GetSessionByIdSuccessAction {
  type: typeof GET_SESSION_BY_ID_SUCCESS
  session: Session
}

export interface LoadingAction {
  type: typeof SESSION_LOADING
  message: string
}

export interface FailureAction {
  type: typeof SESSION_FAILURE
  error: Error
}

export interface SetCurrentRecordAction {
  type: typeof SET_SESSION_CURRENT_RECORD
  payload?: Session
  callback?(): any;
}

export interface SetCurrentRecordSuccessAction {
  type: typeof SET_SESSION_CURRENT_RECORD_SUCCESS
  payload?: Session
}

export interface GetClientsAction {
  type: typeof GET_CLIENTS
  clientName?: string
}

export interface GetClientsSuccessAction {
  type: typeof GET_CLIENTS_SUCCESS
  clients: Array<Client>
}

export interface GetOperatorsAction {
  type: typeof GET_OPERATORS
  operatorName?: string
}

export interface GetOperatorsSuccessAction {
  type: typeof GET_OPERATORS_SUCCESS
  operators: Array<Operator>
}
export type SessionActionTypes = 
DeleteSessionAction | 
DeleteSessionSuccessAction | 
PostSearchSessionRequestAction | 
PostSearchSessionRequestSuccessAction | 
SearchSessionsAction | 
SearchSessionsSuccessAction | 
GetSessionByIdAction |
GetSessionByIdSuccessAction |
LoadingAction |
FailureAction |
SetCurrentRecordAction |
SetCurrentRecordSuccessAction |
GetClientsAction |
GetClientsSuccessAction |
GetOperatorsAction |
GetOperatorsSuccessAction;

export function deleteSession(sessionId: string, callback?: () => any): SessionActionTypes {
  return {
    type: DELETE_SESSION,
    sessionId: sessionId,
    callback: callback
  }
}

export function deleteSessionSuccess(sessionId: string): SessionActionTypes {
  return {
    type: DELETE_SESSION_SUCCESS,
    sessionId: sessionId
  }
}

export function postSearchSessionRequest(searchRequest: SearchRequest<SessionSearchTemplate>, callback?: () => any): SessionActionTypes {
  return {
    type: POST_SESSION_SEARCH_REQUEST,
    searchRequest: searchRequest,
    callback
  }
}

export function postSearchSessionRequestSuccess(searchId: string, searchRequest: SearchRequest<SessionSearchTemplate>): SessionActionTypes {
  return {
    type: POST_SESSION_SEARCH_REQUEST_SUCCESS,
    searchRequest: searchRequest,
    searchId: searchId
  }
}

export function searchSessions(searchId: string, pageSize: number, pageNumber: number): SessionActionTypes {
  return {  
    type: SEARCH_SESSIONS,
    searchId: searchId,
    pageSize: pageSize,
    page: pageNumber
  }
}

export function searchSessionsSuccess(sessions: Array<Session>, resultSetSize: number): SessionActionTypes {
  return {  
    type: SEARCH_SESSIONS_SUCCESS,
    sessions: sessions,
    resultSetSize: resultSetSize
  }
}

export function getSessionById(sessionId: string): SessionActionTypes {
  return {
    type: GET_SESSION_BY_ID,
    sessionId: sessionId
  }
}

export function getSessionByIdSuccess(session: Session): SessionActionTypes {
  return {
    type: GET_SESSION_BY_ID_SUCCESS,
    session: session
  }
}

export function onLoading(message: string): SessionActionTypes {
  return {
    type: SESSION_LOADING,
    message: message
  }
}

export function onFailure(error: Error): SessionActionTypes {
  return {
    type: SESSION_FAILURE,
    error: error
  }
}

export function setCurrentRecord(current?: Session, callback?: () => any): SessionActionTypes {
  return {
    type: SET_SESSION_CURRENT_RECORD,
    payload: current,
    callback: callback
  }
}

export function setCurrentRecordSuccess(current?: Session): SessionActionTypes {
  return {
    type: SET_SESSION_CURRENT_RECORD_SUCCESS,
    payload: current
  }
}

export function getClients(clientName?: string): SessionActionTypes {
  return {
    type: GET_CLIENTS,
    clientName: clientName
  }
}

export function getClientsSuccess(clients: Array<Client>): SessionActionTypes {
  return {
    type: GET_CLIENTS_SUCCESS,
    clients: clients
  }
}

export function getOperators(operatorName?: string): SessionActionTypes {
  return {
    type: GET_OPERATORS,
    operatorName: operatorName
  }
}

export function getOperatorsSuccess(operators: Array<Operator>): SessionActionTypes {
  return {
    type: GET_OPERATORS_SUCCESS,
    operators: operators
  }
}
