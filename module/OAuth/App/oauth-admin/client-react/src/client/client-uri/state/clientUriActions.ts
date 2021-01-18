import { ClientUri } from "../types"

export interface SearchClientUriAction {
  clientId: string
}

export interface SearchClientUriSuccessAction {
  clientUris: Array<ClientUri>
}

export interface FailureAction {
  error: any
}