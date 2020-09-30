import { Session, SessionSearchTemplate, SearchRequest, Operator } from "../../types";
import { Client } from "../../../client/types";

export const DELETE_SESSION = 'DELETE_SESSION';
export const DELETE_SESSION_SUCCESS = 'DELETE_SESSION_SUCCESS';
export const DELETE_SESSION_FAILURE = 'DELETE_SESSION_FAILURE';
export const POST_SESSION_SEARCH_REQUEST = 'POST_SESSION_SEARCH_REQUEST';
export const POST_SESSION_SEARCH_REQUEST_SUCCESS = 'POST_SESSION_SEARCH_REQUEST_SUCCESS';
export const POST_SESSION_SEARCH_REQUEST_FAILURE = 'POST_SESSION_SEARCH_REQUEST_FAILURE';
export const SEARCH_SESSIONS = 'SEARCH_SESSIONS';
export const SEARCH_SESSIONS_SUCCESS = 'SEARCH_SESSIONS_SUCCESS';
export const SEARCH_SESSIONS_FAILURE = 'SEARCH_SESSIONS_FAILURE';
export const GET_SESSION_BY_ID = 'GET_SESSION_BY_ID';
export const GET_SESSION_BY_ID_SUCCESS = 'GET_SESSION_BY_ID_SUCCESS';
export const GET_SESSION_BY_ID_FAILURE = 'GET_SESSION_BY_ID_FAILURE';
export const SET_SESSION_CURRENT_RECORD = 'SET_SESSION_CURRENT_RECORD';
export const SET_SESSION_CURRENT_RECORD_SUCCESS = 'SET_SESSION_CURRENT_RECORD_SUCCESS';
export const GET_CLIENTS = 'GET_CLIENTS';
export const GET_CLIENTS_SUCCESS = 'GET_CLIENTS_SUCCESS';
export const GET_CLIENTS_FAILURE = 'GET_CLIENTS_FAILURE';
export const GET_OPERATORS = 'GET_OPERATORS';
export const GET_OPERATORS_SUCCESS = 'GET_OPERATORS_SUCCESS';
export const GET_OPERATORS_FAILURE = 'GET_OPERATORS_FAILURE';

export interface DeleteSessionAction {
  type: typeof DELETE_SESSION;
  sessionId: string;
  callback?(): any;
}

export interface DeleteSessionSuccessAction {
  type: typeof DELETE_SESSION_SUCCESS
  sessionId: string
}

export interface DeleteSessionFailureAction {
  type: typeof DELETE_SESSION_FAILURE
  error: any
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

export interface PostSearchSessionRequestFailureAction {
  type: typeof POST_SESSION_SEARCH_REQUEST_FAILURE
  error: any
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

export interface SearchSessionsFailureAction {
  type: typeof SEARCH_SESSIONS_FAILURE
  error: any
}

export interface GetSessionByIdAction {
  type: typeof GET_SESSION_BY_ID
  sessionId: string
}

export interface GetSessionByIdSuccessAction {
  type: typeof GET_SESSION_BY_ID_SUCCESS
  session: Session
}

export interface GetSessionByIdFailureAction {
  type: typeof GET_SESSION_BY_ID_FAILURE
  error: any
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

export interface GetClientsFailureAction {
  type: typeof GET_CLIENTS_FAILURE
  error: any
}

export interface GetOperatorsAction {
  type: typeof GET_OPERATORS
  operatorName?: string
}

export interface GetOperatorsSuccessAction {
  type: typeof GET_OPERATORS_SUCCESS
  operators: Array<Operator>
}

export interface GetOperatorsFailureAction {
  type: typeof GET_OPERATORS_FAILURE
  error: any
}

export type SessionActionTypes = 
DeleteSessionAction | 
DeleteSessionSuccessAction | 
DeleteSessionFailureAction | 
PostSearchSessionRequestAction | 
PostSearchSessionRequestSuccessAction | 
PostSearchSessionRequestFailureAction | 
SearchSessionsAction | 
SearchSessionsSuccessAction | 
SearchSessionsFailureAction | 
GetSessionByIdAction |
GetSessionByIdSuccessAction |
GetSessionByIdFailureAction |
SetCurrentRecordAction |
SetCurrentRecordSuccessAction |
GetClientsAction |
GetClientsSuccessAction |
GetClientsFailureAction |
GetOperatorsAction |
GetOperatorsSuccessAction |
GetOperatorsFailureAction;

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

export function deleteSessionFailure(error: any): SessionActionTypes {
  return {
    type: DELETE_SESSION_FAILURE,
    error: error
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

export function postSearchSessionRequestFailure(error: any): SessionActionTypes {
  return {
    type: POST_SESSION_SEARCH_REQUEST_FAILURE,
    error: error
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

export function searchSessionsFailure(error: any): SessionActionTypes {
  return {
    type: SEARCH_SESSIONS_FAILURE,
    error: error
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

export function getSessionByIdFailure(error: any): SessionActionTypes {
  return {
    type: GET_SESSION_BY_ID_FAILURE,
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

export function getClientsFailure(error: any): SessionActionTypes {
  return {
    type: GET_CLIENTS_FAILURE,
    error: error
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

export function getOperatorsFailure(error: any): SessionActionTypes {
  return {
    type: GET_OPERATORS_FAILURE,
    error: error
  }
}
