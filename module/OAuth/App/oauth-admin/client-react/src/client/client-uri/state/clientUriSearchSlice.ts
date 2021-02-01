import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import { ClientUriSearchState } from "../types";
import * as clientUriActions from './clientUriActions';

export const initialSearchState: ClientUriSearchState = {
  records: [],
  isLoading: false
}

const clientUriSlice = createSlice({
  name: "clientUriSlice",
  initialState: initialSearchState,
  reducers: {
    search(state, action: PayloadAction<clientUriActions.SearchClientUriAction>) {
      state.isLoading = true;
    },
    searchSuccess(state, action: PayloadAction<clientUriActions.SearchClientUriSuccessAction>) {
      state.isLoading = false;
      state.records = action.payload.clientUris;
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