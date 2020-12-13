import { ClientUri, ClientUriCreateDto } from "../types"

export interface CreateClientUriAction {
  clientId: string
  clientUri: ClientUriCreateDto
  loadingMessage: string
  callback?(ClientUri: ClientUri): any
}

export interface CreateClientUriSuccessAction {
  clientUri: ClientUri
}

export interface DeleteClientUriAction {
  clientId: string
  clientUriIds: string[]
  loadingMessage: string
  callback?(): any
}

export interface SearchClientUriAction {
  loadingMessage: string
  clientId: string
}

export interface SearchClientUriSuccessAction {
  clientUris: Array<ClientUri>
}

export interface GetClientUriByIdAction {
  clientId: string
  loadingMessage: string
  clientUriId: string
}

export interface GetClientUriByIdSuccessAction {
  clientUri: ClientUri
}

export interface SetCurrentRecordAction {
  currentRecord?: ClientUri
  callback?(): any
}

export interface SelectRecordsAction {
  records: Array<ClientUri>
}

export interface FailureAction {
  error: any
}