import { Client } from "../client/types";

export interface Operator {
  value: number;
  name: string;
}

export interface Session {
  sessionId?: number
  authorizationCode?: string
  dateIns?: Date
  operator?: {
    name: string
    value: string
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
  client?: {
    name: string
    value: string
  }
  isBlocked?: boolean
}

export interface ColumnSortConfiguration {
  columnName: string;
  sortOrder: string;
}

export interface SearchRequest<Type> {
  template: Type;
  listSortConfiguration?: Array<ColumnSortConfiguration>;
}

export interface SessionSearchTemplate {
  sessionId?: number
  operatorId?: number
  clientId?: string
  maxRowCount: number
}

export interface SessionState {
  isLoading: boolean;
  recordsLoading: boolean;
  clientsLoading: boolean;
  operatorsLoading: boolean;
  current?: Session;
  records: Array<Session>;
  clients?: Array<Client>;
  operators?: Array<Operator>;
  searchId?: string;
  resultSetSize?: number;
  searchRequest?: SearchRequest<SessionSearchTemplate>;
  message?: string;
  error?: Error;
}