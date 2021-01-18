import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { RoleOptionState } from "../types";
import { GetRolesAction, GetRolesSuccessAction } from "./clientActions";

export const initialRoleState: RoleOptionState = {
  options: [],
  isLoading: false
}

const slice = createSlice({
  name: "clientSlice/role",
  initialState: initialRoleState,
  reducers: {
    getOptionsStart(state: RoleOptionState, action: PayloadAction<GetRolesAction>) {
      state.isLoading = true;
    },
    getOptionsSuccess(state: RoleOptionState, action: PayloadAction<GetRolesSuccessAction>) {
      state.options = action.payload.roles;
      state.isLoading = false;
    },
    getOptionsFailure(state: RoleOptionState, action: PayloadAction<any>) {
      state.error = action.payload;
      state.options = [];
      state.isLoading = false;
    }
  },
});

export const { name, actions, reducer } = slice;