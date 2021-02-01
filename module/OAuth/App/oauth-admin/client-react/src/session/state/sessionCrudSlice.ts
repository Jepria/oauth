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

const slice = createCrudSlice<number, Session>({
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