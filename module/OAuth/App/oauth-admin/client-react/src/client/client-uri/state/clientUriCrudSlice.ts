import { createCrudSlice, EntityState } from "@jfront/core-redux-saga";
import { ClientUri, ClientUriCreateDto, ClientUriPrimaryKey } from "../types";
import { clientUriCrudApi } from '../api/ClientUriCrudApi'

export const initialEntityState: EntityState<ClientUri> = {
  isLoading: false,
  selectedRecords: [],
};

const slice = createCrudSlice<ClientUriPrimaryKey, ClientUri, ClientUriCreateDto>({
  name: "clientUriSlice",
  initialState: initialEntityState,
});

export const { name, actions, reducer } = slice;

export const clientUriCrudSaga = slice.createSagaMiddleware(clientUriCrudApi);