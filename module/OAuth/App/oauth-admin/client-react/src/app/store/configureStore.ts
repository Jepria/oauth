import createSagaMiddleware from 'redux-saga';
import logger from 'redux-logger';
import { configureStore as configureStoreRedux } from "@reduxjs/toolkit";
import { all } from 'redux-saga/effects';
import { clientSaga} from '../../client/state/saga/watchers';
import { clientUriSaga } from '../../client/client-uri/state/saga/watchers';
import { sessionSaga } from '../../session/state/saga/watchers';
import { keySaga } from '../../key/state/saga/watchers';
import { initialState, reducer } from './reducer';
import { clientCrudSaga } from '../../client/state/clientCrudSlice';
import { clientSearchSaga } from '../../client/state/clientSearchSlice';
import { sessionCrudSaga } from '../../session/state/sessionCrudSlice';
import { sessionSearchSaga } from '../../session/state/sessionSearchSlice';
import { clientUriCrudSaga } from '../../client/client-uri/state/clientUriCrudSlice';

function* rootSaga() {
  yield all([
    clientSaga(),
    clientCrudSaga(),
    clientSearchSaga(),
    //---------------
    clientUriSaga(),
    clientUriCrudSaga(),
    //---------------
    sessionSaga(),
    sessionCrudSaga(),
    sessionSearchSaga(),
    //---------------
    keySaga()
  ]);
}

export default function configureStore() {
  
  const sagaMiddleware = createSagaMiddleware();

  const store = configureStoreRedux({
      reducer,
      middleware: [sagaMiddleware, logger],
      preloadedState: initialState,
      devTools: process.env.NODE_ENV === 'development'
    }
  )

  sagaMiddleware.run(rootSaga);

  return store;
}