import { API_PATH } from '../../../../config'
import {
  CreateClientUriAction, 
  DeleteClientUriAction,
  GetClientUriByIdAction,
  SearchClientUriAction, 
  SetCurrentRecordAction } from '../clientUriActions';
import { actions } from '../clientUriSlice';
import { put, call, all } from 'redux-saga/effects';
import ClientUriApi from '../../api/ClientUriApi';
import { PayloadAction } from '@reduxjs/toolkit';

const api = new ClientUriApi(`${API_PATH}/client`);

export function* create(action: PayloadAction<CreateClientUriAction>) {
  const { payload } = action;
  try {
    const createdRecord = yield call(api.create, payload.clientId, payload.clientUri);
    yield put(actions.createSuccess({clientUri: createdRecord}));
    if (payload.callback) {
      yield call(payload.callback, createdRecord);
    }
  } catch (error) {
    yield put(actions.failure({error}));
  }
}


export function* remove(action: PayloadAction<DeleteClientUriAction>) {
  const { payload } = action;
  try {
    yield all(payload.clientUriIds.map(clientUriId => call(api.delete, payload.clientId, clientUriId)));
    yield put(actions.removeSuccess());
    if (payload.callback) {
      yield call(payload.callback);
    }
  } catch (error) {
    yield put(actions.failure({error}));
  }
}

export function* getById(action: PayloadAction<GetClientUriByIdAction>) {
  const { payload } = action;
  try {
    const record = yield call(api.getRecordById, payload.clientId, payload.clientUriId);
    yield put(actions.getRecordByIdSuccess({clientUri: record}));
  } catch (error) {
    yield put(actions.failure({error}));
  }
}

export function* search(action: PayloadAction<SearchClientUriAction>) {
  const { payload } = action;
  try {
    const records = yield call(api.search, payload.clientId);
    yield put(actions.searchSuccess({clientUris: records}));
  } catch (error) {
    yield put(actions.failure({error}));
  }
}

export function* setCurrentClient(action: PayloadAction<SetCurrentRecordAction>) {
  const { payload } = action;
  if (payload.callback) {
    yield call(payload.callback);
  }
}