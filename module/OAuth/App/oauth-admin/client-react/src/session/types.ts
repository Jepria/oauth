import { EntityState, SearchState } from "@jfront/core-redux-saga";
import { Client } from "../client/types";

export interface Operator {
  value: number;
  name: string;
}

export interface Session {
  sessionId: number
  authorizationCode?: string
  dateIns?: Date
  operator?: {
    name: string
    value: number
  }
  operatorLogin?: string
  accessTokenId?: string
  accessTokenDateIns?: Date
  accessTokenDateFinish?: Date
  refreshTokenId?: string
  refreshTokenDateIns?: Date
  refreshTokenDateFinish?: Date
  sessionTokenId?: string
  sessionTokenDateIns?: Date
  sessionTokenDateFinish?: Date
  codeChallenge?: string
  redirectUri?: string
  clientId?: string
  clientName?: string
  clientNameEn?: string
  isBlocked?: boolean
}

export interface SessionSearchTemplate {
  sessionId?: number
  operatorId?: number
  clientId?: string
  maxRowCount: number
}

export interface ClientOptionState {
  options: Client[];
  isLoading: boolean;
  error?: any;
}

export interface OperatorOptionState {
  options: Operator[];
  isLoading: boolean;
  error?: any;
}

export interface SessionState {
  searchSlice: SearchState<SessionSearchTemplate, Session>
  crudSlice: EntityState<Session>
  clientSlice: ClientOptionState
  operatorSlice: OperatorOptionState
}