import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import { ClientUriState } from "../types";
import * as clientUriActions from './clientUriActions';

export const initialState: ClientUriState = {
  records: [],
  selectedRecords: [],
  isLoading: false
}

const clientUriSlice = createSlice({
  name: "clientUriSlice",
  initialState,
  reducers: {
    create(state, action: PayloadAction<clientUriActions.CreateClientUriAction>) {
      state.isLoading = true;
      state.message = action.payload.loadingMessage;
    },
    createSuccess(state, action: PayloadAction<clientUriActions.CreateClientUriSuccessAction>) {
      state.isLoading = false;
      state.current = action.payload.clientUri;
    },
    remove(state, action: PayloadAction<clientUriActions.DeleteClientUriAction>) {
      state.isLoading = true;
      state.message = action.payload.loadingMessage;
    },
    removeSuccess(state) {
      state.isLoading = false;
      state.current = undefined;
      state.selectedRecords = [];
    },
    search(state, action: PayloadAction<clientUriActions.SearchClientUriAction>) {
      state.isLoading = true;
      state.message = action.payload.loadingMessage;
    },
    searchSuccess(state, action: PayloadAction<clientUriActions.SearchClientUriSuccessAction>) {
      state.isLoading = false;
      state.records = action.payload.clientUris;
    },
    getRecordById(state, action: PayloadAction<clientUriActions.GetClientUriByIdAction>) {
      state.isLoading = true;
      state.message = action.payload.loadingMessage;
    },
    getRecordByIdSuccess(state, action: PayloadAction<clientUriActions.GetClientUriByIdSuccessAction>) {
      state.isLoading = false;
      state.current = action.payload.clientUri;
    },
    setCurrentRecord(state, action: PayloadAction<clientUriActions.SetCurrentRecordAction>) {
      state.current = action.payload.currentRecord;
    },
    selectRecords(state, action: PayloadAction<clientUriActions.SelectRecordsAction>) {
      state.selectedRecords = action.payload.records;
    },
    failure(state, action: PayloadAction<clientUriActions.FailureAction>) {
      state.isLoading = false;
      state.error = action.payload.error;
    }
  }
})

export const {
  name,
  actions,
  reducer
} = clientUriSlice;