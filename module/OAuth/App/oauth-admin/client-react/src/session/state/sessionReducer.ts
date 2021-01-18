import { combineReducers } from "@reduxjs/toolkit";
import { reducer as searchReducer, initialSearchState } from './sessionSearchSlice'
import { reducer as crudReducer, initialEntityState } from './sessionCrudSlice'
import { reducer as clientReducer, initialClientState } from './sessionClientSlice'
import { reducer as operatorReducer, initialOperatorState } from './sessionOperatorSlice'
import { SessionState } from "../types";

export const initialState: SessionState = {
  searchSlice: initialSearchState,
  crudSlice: initialEntityState,
  clientSlice: initialClientState,
  operatorSlice: initialOperatorState,
};

export const sessionReducer = combineReducers({
  searchSlice: searchReducer,
  crudSlice: crudReducer,
  clientSlice: clientReducer,
  operatorSlice: operatorReducer
})