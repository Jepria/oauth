import { createStore, applyMiddleware, combineReducers, PreloadedState, AnyAction } from 'redux';
import createSagaMiddleware, { Saga } from 'redux-saga';
import logger from 'redux-logger';
import { Reducer } from 'react';

export function configureStore(initialState: PreloadedState<any>, saga: Saga, reducer: Reducer<any, any> ) {
  
  const sagaMiddleware = createSagaMiddleware();

  const store = createStore(
    reducer,
    initialState,
    applyMiddleware(logger, sagaMiddleware)
  );

  
  sagaMiddleware.run(saga);

  return store;
}