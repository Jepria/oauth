import { combineReducers } from "@reduxjs/toolkit";
import {  Reducer } from 'redux';
import { reducer as clientReducer, initialState as clientInitialState } from '../../client/state/clientSlice';
import { reducer as clientUriReducer, initialState as clientUriInitialState } from '../../client/client-uri/state/clientUriSlice';
import { reducer as keyReducer, initialState as keyInitialState } from '../../key/state/keySlice';
import { reducer as sessionReducer, initialState as sessionInitialState } from '../../session/state/sessionSlice';
import { ClientState } from '../../client/types';
import { ClientUriState } from '../../client/client-uri/types';
import { SessionState } from '../../session/types';
import { KeyState } from '../../key/types';

export interface AppState {
  client: ClientState;
  clientUri: ClientUriState;
  session: SessionState;
  key: KeyState;
}

export const initialState: AppState = {
  client: clientInitialState,
  clientUri: clientUriInitialState,
  session: sessionInitialState,
  key: keyInitialState
}

export const reducer: Reducer<AppState> = combineReducers<AppState>({
  client: clientReducer,
  clientUri: clientUriReducer,
  session: sessionReducer,
  key: keyReducer
});