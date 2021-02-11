import { EntityState } from "@jfront/core-redux-saga";

export interface ClientUri {
  clientUriId: number;
  clientUri: string;
}

export interface ClientUriPrimaryKey {
  clientId: string;
  clientUriId: number;
}

export interface ClientUriCreateDto {
  clientId?: string;
  clientUri: string;
}

export interface ClientUriState {
  searchSlice: ClientUriSearchState
  crudSlice: EntityState<ClientUri>
}

export interface ClientUriSearchState {
  isLoading?: boolean;
  records: Array<ClientUri>;
  error?: Error;
}