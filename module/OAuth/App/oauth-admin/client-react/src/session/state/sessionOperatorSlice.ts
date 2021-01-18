import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { OperatorOptionState } from "../types";
import { GetOperatorsAction, GetOperatorsSuccessAction } from "./sessionActions";

export const initialOperatorState: OperatorOptionState = {
  options: [],
  isLoading: false
}

const slice = createSlice({
  name: "sessionSlice/operator",
  initialState: initialOperatorState,
  reducers: {
    getOptionsStart(state: OperatorOptionState, action: PayloadAction<GetOperatorsAction>) {
      state.isLoading = true;
    },
    getOptionsSuccess(state: OperatorOptionState, action: PayloadAction<GetOperatorsSuccessAction>) {
      state.options = action.payload.operators;
      state.isLoading = false;
    },
    getOptionsFailure(state: OperatorOptionState, action: PayloadAction<any>) {
      state.error = action.payload;
      state.options = [];
      state.isLoading = false;
    }
  },
});

export const { name, actions, reducer } = slice;