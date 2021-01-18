import { Operator } from "../types"
import { Client } from "../../client/types"

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

export interface DeleteAllAction {
  operatorId: number
  loadingMessage: string
  callback?(): any
}
