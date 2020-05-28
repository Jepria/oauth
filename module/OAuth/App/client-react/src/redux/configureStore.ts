import createSagaMiddleware, { Saga } from 'redux-saga';
import logger from 'redux-logger';
import { Reducer } from 'react';
import { PreloadedState, createStore, applyMiddleware } from 'redux';

export function configureStore(initialState: PreloadedState<any>, saga: Saga, reducer: Reducer<any, any> ) {
  
  const sagaMiddleware = createSagaMiddleware();

  const store = createStore(
    reducer,
    initialState,
    process.env.NODE_ENV === 'development' ? applyMiddleware(logger, sagaMiddleware) : applyMiddleware(sagaMiddleware)
  );

  sagaMiddleware.run(saga);

  return store;
}