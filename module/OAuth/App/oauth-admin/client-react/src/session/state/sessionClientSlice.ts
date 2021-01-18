import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { ClientOptionState } from "../types";
import { GetClientsAction, GetClientsSuccessAction } from "./sessionActions";

export const initialClientState: ClientOptionState = {
  options: [],
  isLoading: false
}

const slice = createSlice({
  name: "sessionSlice/client",
  initialState: initialClientState,
  reducers: {
    getOptionsStart(state: ClientOptionState, actions: PayloadAction<GetClientsAction>) {
      state.isLoading = true;
    },
    getOptionsSuccess(state: ClientOptionState, action: PayloadAction<GetClientsSuccessAction>) {
      state.options = action.payload.clients;
      state.isLoading = false;
    },
    getOptionsFailure(state: ClientOptionState, action: PayloadAction<any>) {
      state.error = action.payload;
      state.options = [];
      state.isLoading = false;
    }
  },
});

export const { name, actions, reducer } = slice;