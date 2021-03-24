import { createSessionSearchSlice, SessionSearchState } from "@jfront/core-redux-saga";
import { ConnectorSessionSearch } from "@jfront/core-rest";
import { API_PATH } from "../../config";
import { Session, SessionSearchTemplate } from "../types";
import axios from 'axios';

export const initialSearchState: SessionSearchState<SessionSearchTemplate, Session> = {
  isLoading: false,
  records: [],
  pageNumber: 1,
  pageSize: 25,
};

const api = new ConnectorSessionSearch<Session, SessionSearchTemplate>(API_PATH + '/session', true, axios);

const slice = createSessionSearchSlice<SessionSearchTemplate, Session>({
  name: "sessionSlice",
  initialState: initialSearchState,
});

export const { name, actions, reducer } = slice;

export const sessionSearchSaga = slice.createSagaMiddleware(api);