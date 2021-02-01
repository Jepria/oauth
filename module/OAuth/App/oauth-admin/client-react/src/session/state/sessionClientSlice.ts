import { ClientOptionState } from "../types";
import { createOptionsSlice, OptionState } from '@jfront/core-redux-saga'
import axios from 'axios';
import { API_PATH } from '../../config';
import { ClientOptionsApi } from "../../client/api/ClientApi";
import { Client } from "../../client/types";

const api = new ClientOptionsApi(API_PATH + '/client', true, axios);

export const initialClientState: OptionState<Client> = {
  options: [],
  isLoading: false
}

const slice = createOptionsSlice({
  name: "sessionSlice/client",
  initialState: initialClientState,
  reducers: {}
})

export const sessionClientSaga = slice.createSagaMiddleware((clientName: string) => api.getClients(clientName))

export const {name, actions, reducer} = slice;
