import { Session, SessionSearchTemplate, SearchRequest, Operator } from "../types"
import { Client } from "../../client/types"

export interface DeleteSessionAction {
  sessionIds: string[]
  loadingMessage: string
  callback?(): any
}

export interface PostSearchSessionRequestAction {
  searchRequest: SearchRequest<SessionSearchTemplate>
  loadingMessage: string
  callback?(): any
}

export interface PostSearchSessionRequestSuccessAction {
  searchRequest: SearchRequest<SessionSearchTemplate>
  searchId: string
}

export interface SearchSessionsAction {
  searchId: string
  pageSize: number
  page: number
  loadingMessage: string
}

export interface SearchSessionsSuccessAction {
  records: Array<Session>
  resultSetSize: number
}

export interface GetSessionByIdAction {
  sessionId: string
  loadingMessage: string
}

export interface GetSessionByIdSuccessAction {
  session: Session
}

export interface SetCurrentRecordAction {
  currentRecord?: Session
  callback?(): any
}

export interface GetClientsAction {
  clientName?: string
}

export interface GetClientsSuccessAction {
  clients: Array<Client>
}

export interface GetOperatorsAction {
  operatorName?: string
}

export interface GetOperatorsSuccessAction {
  operators: Array<Operator>
}

export interface SelectRecordsAction {
  records: Array<Session>
}

export interface DeleteAllAction {
  operatorId: number
  loadingMessage: string
  callback?(): any
}

export interface FailureAction {
  error?: any
}

