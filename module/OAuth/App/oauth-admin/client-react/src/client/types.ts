import { EntityState, OptionState, SearchState } from "@jfront/core-redux-saga";

export interface Client {
  clientId: string;
  clientName?: string;
  clientNameEn?: string;
  clientSecret?: string;
  applicationType?: string;
  grantTypes?: Array<string>;
  scope?: Array<Option>;
}

export interface ClientSearchTemplate {
  clientId?: string;
  clientName?: string;
  clientNameEn?: string;
  maxRowCount: number;
}

export interface Option {
  name: string;
  value: string;
}

export interface ClientState {
  searchSlice: SearchState<ClientSearchTemplate, Client>
  crudSlice: EntityState<Client>
  roleSlice: OptionState<Option>
}