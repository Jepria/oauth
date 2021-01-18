import { createSearchSlice, SearchState } from "@jfront/core-redux-saga";
import { ConnectorSearch } from "@jfront/core-rest";
import { AppState } from "../../app/store/reducer";
import { API_PATH } from "../../config";
import { Session, SessionSearchTemplate } from "../types";
import axios from 'axios';

export const initialSearchState: SearchState<SessionSearchTemplate, Session> = {
  isLoading: false,
  records: [],
};

export const searchSelectors = {
  selectSearchId: (state: AppState) => state.session.searchSlice.searchId,
  selectSearchTemplate: (state: AppState) => state.session.searchSlice.searchTemplate,
  selectRecords: (state: AppState) => state.session.searchSlice.records,
  selectResultSetSize: (state: AppState) => state.session.searchSlice.resultSetSize,
  selectError: (state: AppState) => state.session.searchSlice.error,
  selectIsLoading: (state: AppState) => state.session.searchSlice.isLoading
}

const api = new ConnectorSearch<Session, SessionSearchTemplate>(API_PATH + '/session', true, axios);

const slice = createSearchSlice<SessionSearchTemplate, Session>({
  name: "sessionSlice",
  initialState: initialSearchState,
});

export const { name, actions, reducer } = slice;

export const sessionSearchSaga = slice.createSagaMiddleware(api);