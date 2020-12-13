import createSagaMiddleware from 'redux-saga';
import logger from 'redux-logger';
import { configureStore as configureStoreRedux, getDefaultMiddleware } from "@reduxjs/toolkit";
import { all } from 'redux-saga/effects';
import { clientSaga} from '../../client/state/saga/watchers';
import { clientUriSaga } from '../../client/client-uri/state/saga/watchers';
import { sessionSaga } from '../../session/state/saga/watchers';
import { keySaga } from '../../key/state/saga/watchers';
import { initialState, reducer } from './reducer';

function* rootSaga() {
  yield all([
    clientSaga(),
    clientUriSaga(),
    sessionSaga(),
    keySaga()
  ]);
}

export default function configureStore() {
  
  const sagaMiddleware = createSagaMiddleware();

  const store = configureStoreRedux({
      reducer,
      middleware: [...getDefaultMiddleware(), sagaMiddleware, logger],
      preloadedState: initialState,
      devTools: process.env.NODE_ENV === 'development'
    }
  )

  sagaMiddleware.run(rootSaga);

  return store;
}