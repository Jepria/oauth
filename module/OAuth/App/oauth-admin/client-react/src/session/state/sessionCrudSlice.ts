import { createCrudSlice, EntityState } from "@jfront/core-redux-saga";
import { PayloadAction } from "@reduxjs/toolkit";
import { AppState } from "../../app/store/reducer";
import { API_PATH } from "../../config";
import { SessionCrudApi } from "../api/SessionApi";
import { Session } from "../types";
import { DeleteAllAction } from './sessionActions'
import axios from 'axios';

export const initialEntityState: EntityState<Session> = {
  isLoading: false,
  selectedRecords: [],
};

const api = new SessionCrudApi(API_PATH + '/session', true, axios);

export const crudSelectors = {
  selectCurrentRecord: (state: AppState) => state.session.crudSlice.currentRecord,
  selectSelectedRecord: (state: AppState) => state.session.crudSlice.selectedRecords,
  selectError: (state: AppState) => state.session.crudSlice.error,
  selectIsLoading: (state: AppState) => state.session.crudSlice.isLoading
}


const slice = createCrudSlice<string, Session>({
  name: "sessionSlice",
  initialState: initialEntityState,
  reducers: {
    removeAll(state, action: PayloadAction<DeleteAllAction>) {
      state.isLoading = true;
    },
    removeAllSuccess(state) {
      state.isLoading = false;
      state.currentRecord = undefined;
      state.selectedRecords = [];
    },
  }
});

export const { name, actions, reducer } = slice;

export const sessionCrudSaga = slice.createSagaMiddleware(api);