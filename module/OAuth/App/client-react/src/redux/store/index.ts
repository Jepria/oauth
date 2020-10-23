import { all } from 'redux-saga/effects';
import { clientSaga} from '../../main/client/state/redux/saga/watchers';
import { combineReducers, Reducer } from 'redux';
import { clientReducer, initialState as clientInitialState } from '../../main/client/state/redux/reducer';
import { clientUriReducer, initialState as clientUriInitialState } from '../../main/client/client-uri/state/redux/reducer';
import { keyReducer, initialState as keyInitialState } from '../../main/key/state/redux/reducer';
import { sessionReducer, initialState as sessionInitialState } from '../../main/session/state/redux/reducer';
import { ClientState } from '../../main/client/types';
import { ClientUriState } from '../../main/client/client-uri/types';
import { clientUriSaga } from '../../main/client/client-uri/state/redux/saga/watchers';
import { sessionSaga } from '../../main/session/state/redux/saga/watchers';
import { SessionState } from '../../main/session/types';
import { KeyState } from '../../main/key/types';
import { keySaga } from '../../main/key/state/redux/saga/watchers';

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