import { combineReducers } from "@reduxjs/toolkit";
import { reducer as crudReducer, initialEntityState } from './clientUriCrudSlice'
import { reducer as searchReducer, initialSearchState } from './clientUriSearchSlice'
import { ClientUriState } from "../types";

export const initialState: ClientUriState = {
  searchSlice: initialSearchState,
  crudSlice: initialEntityState,
};

export const clientUriReducer = combineReducers({
  searchSlice: searchReducer,
  crudSlice: crudReducer
})