import { createCrudSlice, EntityState } from "@jfront/core-redux-saga";
import { API_PATH } from "../../config";
import { Client } from "../types";
import axios from 'axios';
import { ConnectorCrud } from "@jfront/core-rest";

export const initialEntityState: EntityState<Client> = {
  isLoading: false,
  selectedRecords: [],
};

const api = new ConnectorCrud<Client>(API_PATH + '/client', true, axios);

const slice = createCrudSlice<string, Client>({
  name: "clientSlice",
  initialState: initialEntityState,
});

export const { name, actions, reducer } = slice;

export const clientCrudSaga = slice.createSagaMiddleware(api);