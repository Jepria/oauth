import ClientApi from '../../../api/ClientApi'
import { API_PATH } from '../../../../../config'
import {
  CreateClientAction, createClientSuccess, 
  UpdateClientAction, updateClientSuccess, 
  DeleteClientAction, deleteClientSuccess, 
  GetClientByIdAction, getClientByIdSuccess, 
  PostSearchClientRequestAction, postSearchClientRequestSuccess, 
  SearchClientsAction, searchClientsSuccess, 
  SetCurrentRecordAction, setCurrentRecordSuccess, GetRolesAction, 
  getRolesSuccess, updateClientFailure, createClientFailure, deleteClientFailure, 
  getClientByIdFailure, postSearchClientRequestFailure, searchClientsFailure, getRolesFailure} from '../actions';
import { put, call } from 'redux-saga/effects';

const api = new ClientApi(API_PATH + '/client');

export function* create(action: CreateClientAction) {
  try {
    const createdRecord = yield call(api.create, action.payload);
    yield put(createClientSuccess(createdRecord));
    if (action.callback) {
      yield call(action.callback, createdRecord);
    }
  } catch (error) {
    yield put(createClientFailure(error));
  }
}

export function* update(action: UpdateClientAction) {
  try {
    const updatedRecord = yield call(api.update,action.clientId, action.payload);
    yield put(updateClientSuccess(updatedRecord));
    if (action.callback) {
      yield call(action.callback, updatedRecord);
    }
  } catch (error) {
    yield put(updateClientFailure(error));
  }
}

export function* remove(action: DeleteClientAction) {
  try {
    yield call(api.delete, action.clientId);
    yield put(deleteClientSuccess(action.clientId));
    if (action.callback) {
      yield call(action.callback);
    }
  } catch (error) {
    yield put(deleteClientFailure(error));
  }
}

export function* getById(action: GetClientByIdAction) {
  try {
    const record = yield call(api.getRecordById, action.clientId);
    yield put(getClientByIdSuccess(record));
  } catch (error) {
    yield put(getClientByIdFailure(error));
  }
}

export function* postSearchRequest(action: PostSearchClientRequestAction) {
  try {
    const searchId = yield call(api.postSearchRequest, action.searchRequest);
    yield put(postSearchClientRequestSuccess(searchId, action.searchRequest));
    if (action.callback) {
      yield call(action.callback);
    }
  } catch (error) {
    yield put(postSearchClientRequestFailure(error));
  }
}

export function* search(action: SearchClientsAction) {
  try {
    const resultSetSize = yield call(api.getResultSetSize, action.searchId);
    const records = yield call(api.search, action.searchId, action.pageSize, action.page);
    yield put(searchClientsSuccess(records, resultSetSize));
  } catch (error) {
    yield put(searchClientsFailure(error));
  }
}

export function* setCurrentClient(action: SetCurrentRecordAction) {
  yield put(setCurrentRecordSuccess(action.payload));
  if (action.callback) {
    yield call(action.callback);
  }
}

export function* getRoles(action: GetRolesAction) {
  const roles = yield call(api.getRoles, action.roleName);
  try {
    yield put(getRolesSuccess(roles));
  } catch(error) {
    yield put(getRolesFailure(error));
  }
}