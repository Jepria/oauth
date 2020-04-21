import { all } from 'redux-saga/effects';
import clientSaga from '../client/state/saga/watchers';
import { combineReducers, Reducer } from 'redux';
import { clientReducer, initialState as clientInitialState} from '../client/state/reducer';
import { ClientState } from '../client/types';

export default function* sagas() {
  yield all([clientSaga()]);
}

export interface AppState {
  client: ClientState
} 

export const initialState: AppState = {
  client: clientInitialState
}

export const reducers: Reducer<AppState> = combineReducers<AppState>({
  client: clientReducer
});