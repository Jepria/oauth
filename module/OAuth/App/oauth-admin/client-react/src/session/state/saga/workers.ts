import { API_PATH} from '../../../config';
import { 
  DeleteSessionAction,
  GetSessionByIdAction,
  PostSearchSessionRequestAction,
  SearchSessionsAction,
  SetCurrentRecordAction,
  GetClientsAction,
  GetOperatorsAction,
  DeleteAllAction  } from '../sessionActions';
import { actions } from '../sessionSlice';
import { put, call, all } from 'redux-saga/effects';
import ClientApi from '../../../client/api/ClientApi';
import OperatorApi from '../../api/OperatorApi';
import SessionApi from '../../api/SessionApi';
import {PayloadAction} from "@reduxjs/toolkit";

const api = new SessionApi(API_PATH + '/session');
const clientApi = new ClientApi(API_PATH + "/client");
const operatorApi = new OperatorApi(API_PATH);

export function* remove(action: PayloadAction<DeleteSessionAction>) {
  const { payload } = action;
  try {
    yield all(payload.sessionIds.map(sessionId => call(api.delete, sessionId)));
    yield put(actions.removeSuccess());
    if (payload.callback) {
      yield call(payload.callback);
    }
  } catch (error) {
    yield put(actions.failure(error));
  }
}

export function* getById(action: PayloadAction<GetSessionByIdAction>) {
  const { payload } = action;
  try {
    const record = yield call(api.getRecordById, payload.sessionId);
    yield put(actions.getRecordByIdSuccess(record));
  } catch (error) {
    yield put(actions.failure(error));
  }
}

export function* postSearchRequest(action: PayloadAction<PostSearchSessionRequestAction>) {
  const { payload } = action;
  try {
    const searchId = yield call(api.postSearchRequest, payload.searchRequest);
    yield put(actions.postSearchTemplateSuccess({searchId, searchRequest: payload.searchRequest}));
    if (payload.callback) {
      yield call(payload.callback);
    }
  } catch (error) {
    yield put(actions.failure(error));
  }
}

export function* search(action: PayloadAction<SearchSessionsAction>) {
  const { payload } = action;
  try {
    const records = yield call(api.search, payload.searchId, payload.pageSize, payload.page);
    const resultSetSize = yield call(api.getResultSetSize, payload.searchId, '');
    yield put(actions.searchSuccess({records, resultSetSize}));
  } catch (error) {
    yield put(actions.failure(error));
  }
}

export function* setCurrentSession(action: PayloadAction<SetCurrentRecordAction>) {
  const { payload } = action;
  if (payload.callback) {
    yield call(payload.callback);
  }
}

export function* getClients(action: PayloadAction<GetClientsAction>) {
  const { payload } = action;
  try {
    const clients = yield call(clientApi.getClients, payload.clientName);
    yield put(actions.getClientsSuccess({clients}));
  } catch (error) {
    yield put(actions.failure(error));
  }
}

export function* getOperators(action: PayloadAction<GetOperatorsAction>) {
  const { payload } = action;
  try {
    const operators = yield call(operatorApi.getOperators, payload.operatorName);
    yield put(actions.getOperatorsSuccess({operators}));
  } catch(error) {
    yield put(actions.failure(error));
  }
}

export function* removeAll(action: PayloadAction<DeleteAllAction>) {
  const { payload } = action;
  try {
    yield call(api.deleteAll, payload.operatorId);
    yield put(actions.removeAllSuccess());
    if (payload.callback) {
      yield call(payload.callback);
    }
  } catch (error) {
    yield put(actions.failure(error));
  }
}