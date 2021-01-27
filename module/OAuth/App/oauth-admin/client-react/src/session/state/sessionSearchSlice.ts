import { createSearchSlice, SearchState } from "@jfront/core-redux-saga";
import { ConnectorSearch } from "@jfront/core-rest";
import { API_PATH } from "../../config";
import { Session, SessionSearchTemplate } from "../types";
import axios from 'axios';

export const initialSearchState: SearchState<SessionSearchTemplate, Session> = {
  isLoading: false,
  records: [],
  pageNumber: 1,
  pageSize: 25,
};

const api = new ConnectorSearch<Session, SessionSearchTemplate>(API_PATH + '/session', true, axios);

const slice = createSearchSlice<SessionSearchTemplate, Session>({
  name: "sessionSlice",
  initialState: initialSearchState,
});

export const { name, actions, reducer } = slice;

export const sessionSearchSaga = slice.createSagaMiddleware(api);