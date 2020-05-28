import { API_PATH } from '../../../../../../config'
import { onLoading, onFailure, 
  CreateClientUriAction, createClientUriSuccess, 
  DeleteClientUriAction, deleteClientUriSuccess, 
  GetClientUriByIdAction, getClientUriByIdSuccess, 
  SearchClientUriAction, searchClientUriSuccess, 
  SetCurrentRecordAction, setCurrentRecordSuccess } from '../actions';
import { put, call } from 'redux-saga/effects';
import ClientUriApi from '../../../api/ClientUriApi';

const api = new ClientUriApi(`${API_PATH}/client`);

export function* create(action: CreateClientUriAction) {
  try {
    yield put(onLoading('Сохранение...'));
    const createdRecord = yield call(api.create, action.clientId, action.payload);
    yield put(createClientUriSuccess(createdRecord));
    if (action.callback) {
      yield call(action.callback, createdRecord);
    }
  } catch (error) {
    yield put(onFailure(error));
  }
}


export function* remove(action: DeleteClientUriAction) {
  try {
    yield put(onLoading('Удаление...'));
    yield call(api.delete, action.clientId, action.clientUriId);
    yield put(deleteClientUriSuccess(action.clientId, action.clientUriId));
    if (action.callback) {
      yield call(action.callback);
    }
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* getById(action: GetClientUriByIdAction) {
  try {
    yield put(onLoading('Загрузка данных...'));
    const record = yield call(api.getRecordById, action.clientId, action.clientUriId);
    yield put(getClientUriByIdSuccess(record));
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* search(action: SearchClientUriAction) {
  try {
    yield put(onLoading('Загрузка данных...'));
    const records = yield call(api.search, action.clientId);
    yield put(searchClientUriSuccess(records));
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* setCurrentClient(action: SetCurrentRecordAction) {
  yield put(setCurrentRecordSuccess(action.payload));
  if (action.callback) {
    yield call(action.callback);
  }
}