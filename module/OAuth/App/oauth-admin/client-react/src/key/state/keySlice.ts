import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import { KeyState } from "../types";
import * as keyActions from './keyActions';

export const initialState: KeyState = {
  isLoading: false
}

const keySlice = createSlice({
  name: "keySlice",
  initialState,
  reducers: {
    getRecordById(state, action: PayloadAction<keyActions.GetKeyAction>) {
      state.isLoading = true;
      state.message = action.payload.loadingMessage;
    },
    getRecordByIdSuccess(state, action: PayloadAction<keyActions.GetKeySuccessAction>) {
      state.isLoading = false;
      state.current = action.payload.key;
    },
    update(state, action: PayloadAction<keyActions.UpdateKeyAction>) {
      state.isLoading = true;
      state.message = action.payload.loadingMessage;
    },
    updateSuccess(state) {
      state.isLoading = false;
      state.current = undefined;
    },
    failure(state, action: PayloadAction<keyActions.FailureAction>) {
      state.isLoading = false;
      state.error = action.payload.error;
    },
  }
})

export const {
  name,
  actions,
  reducer
} = keySlice;