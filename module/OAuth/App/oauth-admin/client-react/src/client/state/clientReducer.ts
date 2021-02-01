import { combineReducers } from "@reduxjs/toolkit";
import { reducer as searchReducer, initialSearchState } from './clientSearchSlice'
import { reducer as crudReducer, initialEntityState } from './clientCrudSlice'
import { reducer as roleSlice, initialRoleState } from './clientRoleSlice'
import { ClientState } from "../types";

export const initialState: ClientState = {
  searchSlice: initialSearchState,
  crudSlice: initialEntityState,
  roleSlice: initialRoleState,
};

export const clientReducer = combineReducers({
  searchSlice: searchReducer,
  crudSlice: crudReducer,
  roleSlice: roleSlice
})