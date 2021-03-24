import { createSearchSlice, SearchState } from "@jfront/core-redux-saga";
import { ConnectorSearch } from "@jfront/core-rest";
import { API_PATH } from "../../config";
import { Client, ClientSearchTemplate } from "../types";
import axios from 'axios';

export const initialSearchState: SearchState<ClientSearchTemplate, Client> = {
  isLoading: false,
  records: [],
  pageNumber: 1,
  pageSize: 25
}

const api = new ConnectorSearch<Client>(API_PATH + '/client', true, axios);

const slice = createSearchSlice<ClientSearchTemplate, Client>({
  name: "clientSlice",
  initialState: initialSearchState,
});

export const { name, actions, reducer } = slice;

export const clientSearchSaga = slice.createSagaMiddleware(api);