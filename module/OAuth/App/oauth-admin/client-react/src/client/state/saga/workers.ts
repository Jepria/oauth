import ClientApi from '../../api/ClientApi'
import { API_PATH } from '../../../config'
import {
  CreateClientAction,
  UpdateClientAction,
  DeleteClientAction,
  GetClientByIdAction,
  PostSearchClientRequestAction,
  SearchClientsAction,
  SetCurrentRecordAction,
  GetRolesAction
} from '../clientActions';
import { actions } from '../clientSlice'
import { put, call, all } from 'redux-saga/effects';
import { PayloadAction } from '@reduxjs/toolkit';

const api = new ClientApi(API_PATH + '/client');

export function* create(action: PayloadAction<CreateClientAction>) {
  try {
    const createdRecord = yield call(api.create, action.payload.client);
    yield put(actions.createSuccess({ client: createdRecord }));
    if (action.payload.callback) {
      yield call(action.payload.callback, createdRecord);
    }
  } catch (error) {
    yield put(actions.failure(error));
  }
}

export function* update(action: PayloadAction<UpdateClientAction>) {
  try {
    const updatedRecord = yield call(api.update, action.payload.clientId, action.payload.client);
    yield put(actions.updateSuccess({ client: updatedRecord }));
    if (action.payload.callback) {
      yield call(action.payload.callback, updatedRecord);
    }
  } catch (error) {
    yield put(actions.failure(error));
  }
}

export function* remove(action: PayloadAction<DeleteClientAction>) {
  try {
    yield all(action.payload.clientIds.map(clientId => call(api.delete, clientId)));
    yield put(actions.removeSuccess());
    if (action.payload.callback) {
      yield call(action.payload.callback);
    }
  } catch (error) {
    yield put(actions.failure(error));
  }
}

export function* getById(action: PayloadAction<GetClientByIdAction>) {
  try {
    const record = yield call(api.getRecordById, action.payload.clientId);
    yield put(actions.getRecordByIdSuccess({ client: record }));
  } catch (error) {
    yield put(actions.failure(error));
  }
}

export function* postSearchRequest(action: PayloadAction<PostSearchClientRequestAction>) {
  try {
    const searchId = yield call(api.postSearchRequest, action.payload.searchRequest);
    yield put(actions.postSearchTemplateSuccess({ searchId, searchRequest: action.payload.searchRequest }));
    if (action.payload.callback) {
      yield call(action.payload.callback);
    }
  } catch (error) {
    yield put(actions.failure(error));
  }
}

export function* search(action: PayloadAction<SearchClientsAction>) {
  try {
    const records = yield call(api.search, action.payload.searchId, action.payload.pageSize, action.payload.page);
    const resultSetSize = yield call(api.getResultSetSize, action.payload.searchId, '');
    yield put(actions.searchSuccess({ clients: records, resultSetSize }));
  } catch (error) {
    yield put(actions.failure(error));
  }
}

export function* setCurrentClient(action: PayloadAction<SetCurrentRecordAction>) {
  if (action.payload.callback) {
    yield call(action.payload.callback);
  }
}

export function* getRoles(action: PayloadAction<GetRolesAction>) {
  const roles = yield call(api.getRoles, action.payload.roleName);
  try {
    yield put(actions.getRolesSuccess({ roles }));
  } catch (error) {
    yield put(actions.failure(error));
  }
}