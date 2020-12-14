import { SessionState } from "../types";
import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import * as sessionActions from './sessionActions';

export const initialState: SessionState = {
  records: [],
  selectedRecords: [],
  isLoading: false,
  recordsLoading: false,
  clientsLoading: false,
  operatorsLoading: false
}

const sessionSlice = createSlice(
  {
    name: "sessionSlice",
    initialState,
    reducers: {
      postSearchTemplate(state, action: PayloadAction<sessionActions.PostSearchSessionRequestAction>) {
        state.isLoading = true;
        state.message = action.payload.loadingMessage;
      },
      postSearchTemplateSuccess(state, action: PayloadAction<sessionActions.PostSearchSessionRequestSuccessAction>) {
        state.searchId = action.payload.searchId;
        state.searchRequest = action.payload.searchRequest;
        state.isLoading = false;
      },
      search(state, action: PayloadAction<sessionActions.SearchSessionsAction>) {
        state.recordsLoading = true;
        state.message = action.payload.loadingMessage;
      },
      searchSuccess(state, action: PayloadAction<sessionActions.SearchSessionsSuccessAction>) {
        state.recordsLoading = false;
        state.records = action.payload.records;
        state.resultSetSize = action.payload.resultSetSize;
      },
      setCurrentRecord(state, action: PayloadAction<sessionActions.SetCurrentRecordAction>) {
        state.current = action.payload.currentRecord;
      },
      getRecordById(state, action: PayloadAction<sessionActions.GetSessionByIdAction>) {
        state.isLoading = true;
        state.message = action.payload.loadingMessage;
      },
      getRecordByIdSuccess(state, action: PayloadAction<sessionActions.GetSessionByIdSuccessAction>) {
        state.isLoading = false;
        state.current = action.payload.session;
      },
      remove(state, action: PayloadAction<sessionActions.DeleteSessionAction>) {
        state.isLoading = true;
        state.message = action.payload.loadingMessage;
      },
      removeSuccess(state) {
        state.isLoading = false;
        state.current = undefined;
        state.records = [];
      },
      removeAll(state, action: PayloadAction<sessionActions.DeleteAllAction>) {
        state.isLoading = true;
        state.message = action.payload.loadingMessage;
      },
      removeAllSuccess(state) {
        state.isLoading = false;
        state.current = undefined;
        state.selectedRecords = [];
      },
      getClients(state, action: PayloadAction<sessionActions.GetClientsAction>) {
        state.clientsLoading = true;
      },
      getClientsSuccess(state, action: PayloadAction<sessionActions.GetClientsSuccessAction>) {
        state.clientsLoading = false;
        state.clients = action.payload.clients;
      },
      getOperators(state, action: PayloadAction<sessionActions.GetOperatorsAction>) {
        state.operatorsLoading = true;
      },
      getOperatorsSuccess(state, action: PayloadAction<sessionActions.GetOperatorsSuccessAction>) {
        state.operatorsLoading = false;
        state.operators = action.payload.operators;
      },
      selectRecords(state, action: PayloadAction<sessionActions.SelectRecordsAction>) {
        state.selectedRecords = action.payload.records
      },
      failure(state, action: PayloadAction<sessionActions.FailureAction>) {
        state.operatorsLoading = false;
        state.clientsLoading = false
        state.recordsLoading = false;
        state.isLoading = false;
        state.error = action.payload.error;
      }
    }
  }
)

export const {
  name,
  actions,
  reducer
} = sessionSlice;