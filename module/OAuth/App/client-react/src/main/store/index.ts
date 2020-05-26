import { all } from 'redux-saga/effects';
import { clientSaga} from '../client/state/redux/saga/watchers';
import { combineReducers, Reducer } from 'redux';
import { clientReducer, initialState as clientInitialState } from '../client/state/redux/reducer';
import { clientUriReducer, initialState as clientUriInitialState } from '../client/client-uri/state/redux/reducer';
import { keyReducer, initialState as keyInitialState } from '../key/state/redux/reducer';
import { sessionReducer, initialState as sessionInitialState } from '../session/state/redux/reducer';
import { ClientState } from '../client/types';
import { ClientUriState } from '../client/client-uri/types';
import { clientUriSaga } from '../client/client-uri/state/redux/saga/watchers';
import { sessionSaga } from '../session/state/redux/saga/watchers';
import { SessionState } from '../session/types';
import { KeyState } from '../key/types';
import { keySaga } from '../key/state/redux/saga/watchers';

export function* sagas() {
  yield all([
    clientSaga(),
    clientUriSaga(),
    sessionSaga(),
    keySaga()
  ]);
}

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

export const reducers: Reducer<AppState> = combineReducers<AppState>({
  client: clientReducer,
  clientUri: clientUriReducer,
  session: sessionReducer,
  key: keyReducer
});