import { combineReducers } from "@reduxjs/toolkit";
import {  Reducer } from 'redux';
import { clientReducer, initialState as clientInitialState } from '../../client/state/clientReducer';
import { clientUriReducer, initialState as clientUriInitialState } from '../../client/client-uri/state/clientUriReducer';
import { reducer as keyReducer, initialState as keyInitialState } from '../../key/state/keySlice';
import { sessionReducer, initialState as sessionInitialState } from '../../session/state/sessionReducer';
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