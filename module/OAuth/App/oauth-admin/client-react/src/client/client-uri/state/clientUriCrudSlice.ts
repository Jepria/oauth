import { createCrudSlice, EntityState } from "@jfront/core-redux-saga";
import { API_PATH } from "../../../config";
import { ClientUri, ClientUriCreateDto, ClientUriPrimaryKey } from "../types";
import axios from 'axios';
import ClientUriCrudApi from "../api/ClientUriCrudApi";

export const initialEntityState: EntityState<ClientUri> = {
  isLoading: false,
  selectedRecords: [],
};

const api = new ClientUriCrudApi(API_PATH + '/client', true, axios);

const slice = createCrudSlice<ClientUriPrimaryKey, ClientUri, ClientUriCreateDto>({
  name: "clientUriSlice",
  initialState: initialEntityState,
});

export const { name, actions, reducer } = slice;

export const clientUriCrudSaga = slice.createSagaMiddleware(api);