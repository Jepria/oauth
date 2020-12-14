import { ClientState } from "../types";
import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import * as clientActions from './clientActions';

export const initialState: ClientState = {
  records: [],
  selectedRecords: [],
  recordsLoading: false,
  rolesLoading: false,
  isLoading: false
}

const clientSlice = createSlice({
  name: "clientSlice",
  initialState,
  reducers: {
    getRecordById(state, action: PayloadAction<clientActions.GetClientByIdAction>) {
      state.isLoading = true;
      state.message = action.payload.loadingMessage;
    },
    getRecordByIdSuccess(state, action: PayloadAction<clientActions.GetClientByIdSuccessAction>) {
      state.isLoading = false;
      state.current = action.payload.client;
    },
    update(state, action: PayloadAction<clientActions.UpdateClientAction>) {
      state.isLoading = true;
      state.message = action.payload.loadingMessage;
    },
    updateSuccess(state, action: PayloadAction<clientActions.UpdateClientSuccessAction>) {
      state.isLoading = false;
      state.current = action.payload.client;
    },
    create(state, action: PayloadAction<clientActions.CreateClientAction>) {
      state.isLoading = true;
      state.message = action.payload.loadingMessage;
    },
    createSuccess(state, action: PayloadAction<clientActions.CreateClientSuccessAction>) {
      state.isLoading = false;
      state.current = action.payload.client;
    },
    remove(state, action: PayloadAction<clientActions.DeleteClientAction>) {
      state.isLoading = true;
      state.message = action.payload.loadingMessage;
    },
    removeSuccess(state) {
      state.isLoading = false;
      state.current = undefined;
      state.selectedRecords = [];
    },
    postSearchTemplate(state, action: PayloadAction<clientActions.PostSearchClientRequestAction>) {
      state.isLoading = true;
      state.message = action.payload.loadingMessage;
    },
    postSearchTemplateSuccess(state, action: PayloadAction<clientActions.PostSearchClientRequestSuccessAction>) {
      state.isLoading = false;
      state.searchId = action.payload.searchId;
      state.searchRequest = action.payload.searchRequest
    },
    search(state, action: PayloadAction<clientActions.SearchClientsAction>) {
      state.recordsLoading = true;
      state.message = action.payload.loadingMessage;
    },
    searchSuccess(state, action: PayloadAction<clientActions.SearchClientsSuccessAction>) {
      state.recordsLoading = false;
      state.records = action.payload.clients
      state.resultSetSize = action.payload.resultSetSize;
    },
    getRoles(state, action: PayloadAction<clientActions.GetRolesAction>) {
      state.rolesLoading = true;
    },
    getRolesSuccess(state, action: PayloadAction<clientActions.GetRolesSuccessAction>) {
      state.rolesLoading = false;
      state.roles = action.payload.roles;
    },
    setCurrentRecord(state, action: PayloadAction<clientActions.SetCurrentRecordAction>) {
      state.current = action.payload.currentRecord;
    },
    selectRecords(state, action: PayloadAction<clientActions.SelectRecordsAction>) {
      state.selectedRecords = action.payload.records;
    },
    failure(state, action: PayloadAction<clientActions.FailureAction>) {
      state.recordsLoading = false;
      state.isLoading = false;
      state.rolesLoading = false;
      state.error = action.payload.error;
    },
  }
});

export const {
  name,
  actions,
  reducer
} = clientSlice;