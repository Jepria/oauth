import { API_PATH } from '../../../../../../config'
import {
  CreateClientUriAction, createClientUriFailure, createClientUriSuccess, 
  DeleteClientUriAction, deleteClientUriFailure, deleteClientUriSuccess, 
  GetClientUriByIdAction, getClientUriByIdFailure, getClientUriByIdSuccess, 
  SearchClientUriAction, searchClientUriFailure, searchClientUriSuccess, 
  SetCurrentRecordAction, setCurrentRecordSuccess } from '../actions';
import { put, call, all } from 'redux-saga/effects';
import ClientUriApi from '../../../api/ClientUriApi';

const api = new ClientUriApi(`${API_PATH}/client`);

export function* create(action: CreateClientUriAction) {
  try {
    const createdRecord = yield call(api.create, action.clientId, action.payload);
    yield put(createClientUriSuccess(createdRecord));
    if (action.callback) {
      yield call(action.callback, createdRecord);
    }
  } catch (error) {
    yield put(createClientUriFailure(error));
  }
}


export function* remove(action: DeleteClientUriAction) {
  try {
    yield all(action.clientUriIds.map(clientUriId => call(api.delete, action.clientId, clientUriId)));
    yield put(deleteClientUriSuccess());
    if (action.callback) {
      yield call(action.callback);
    }
  } catch (error) {
    yield put(deleteClientUriFailure(error));
  }
}

export function* getById(action: GetClientUriByIdAction) {
  try {
    const record = yield call(api.getRecordById, action.clientId, action.clientUriId);
    yield put(getClientUriByIdSuccess(record));
  } catch (error) {
    yield put(getClientUriByIdFailure(error));
  }
}

export function* search(action: SearchClientUriAction) {
  try {
    const records = yield call(api.search, action.clientId);
    yield put(searchClientUriSuccess(records));
  } catch (error) {
    yield put(searchClientUriFailure(error));
  }
}

export function* setCurrentClient(action: SetCurrentRecordAction) {
  yield put(setCurrentRecordSuccess(action.payload));
  if (action.callback) {
    yield call(action.callback);
  }
}