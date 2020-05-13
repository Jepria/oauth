import { Client } from "../client/types";

export interface Operator {
  value: number;
  name: string;
}

export interface Session {
  sessionId?: number
  authorizationCode?: string
  dateIns?: string
  operator?: {
    name: string
    value: string
  }
  operatorLogin?: string
  accessTokenId?: string
  accessTokenDateIns?: string
  accessTokenDateFinish?: string
  refreshTokenId?: string
  refreshTokenDateIns?: string
  refreshTokenDateFinish?: string
  sessionTokenId?: string
  sessionTokenDateIns?: string
  sessionTokenDateFinish?: string
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
  listSortConfiguration?: ColumnSortConfiguration;
}

export interface SessionSearchTemplate {
  sessionId?: number
  operatorId?: number
  clientId?: string
  maxRowCount: number
}

export interface SessionState {
  isLoading: boolean;
  current?: Session;
  records?: Array<Session>;
  clients?: Array<Client>;
  operators?: Array<Operator>;
  searchId?: string;
  resultSetSize?: number;
  searchRequest?: SearchRequest<SessionSearchTemplate>;
  message?: string;
  error?: Error;
}