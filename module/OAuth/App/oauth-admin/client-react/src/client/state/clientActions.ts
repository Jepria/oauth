import { Client, ClientSearchTemplate, SearchRequest, Option } from "../types"

export interface CreateClientAction {
  client: Client
  loadingMessage: string
  callback?(client: Client): any
}

export interface CreateClientSuccessAction {
  client: Client
}

export interface UpdateClientAction {
  clientId: string
  client: Client
  loadingMessage: string
  callback?(client: Client): any
}

export interface UpdateClientSuccessAction {
  client: Client
}

export interface DeleteClientAction {
  clientIds: string[]
  loadingMessage: string
  callback?(): any
}

export interface PostSearchClientRequestAction {
  searchRequest: SearchRequest<ClientSearchTemplate>
  callback?(): any
  loadingMessage: string
}

export interface PostSearchClientRequestSuccessAction {
  searchRequest: SearchRequest<ClientSearchTemplate>
  searchId: string
}

export interface SearchClientsAction {
  searchId: string
  pageSize: number
  page: number
  loadingMessage: string
}

export interface SearchClientsSuccessAction {
  clients: Array<Client>
  resultSetSize: number
}

export interface GetClientByIdAction {
  clientId: string
  loadingMessage: string
}

export interface GetClientByIdSuccessAction {
  client: Client
}

export interface SetCurrentRecordAction {
  currentRecord?: Client
  callback?(): any
}

export interface SetCurrentRecordSuccessAction {
  payload?: Client
}

export interface GetRolesAction {
  roleName?: string
}

export interface GetRolesSuccessAction {
  roles: Array<Option>
}

export interface SelectRecordsAction {
  records: Array<Client>
}

export interface FailureAction {
  error: any
}
