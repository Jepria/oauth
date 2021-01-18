import { API_PATH } from '../../../config';
import {
  GetClientsAction,
  GetOperatorsAction,
  DeleteAllAction
} from '../sessionActions';
import { actions as crudActions } from '../sessionCrudSlice';
import { actions as operatorOptionActions } from '../sessionOperatorSlice';
import { actions as clientOptionActions } from '../sessionClientSlice';
import { put, call } from 'redux-saga/effects';
import { ClientOptionsApi } from '../../../client/api/ClientApi';
import OperatorApi from '../../api/OperatorApi';
import { SessionCrudApi } from '../../api/SessionApi';
import { PayloadAction } from "@reduxjs/toolkit";
import axios from 'axios';

const api = new SessionCrudApi(API_PATH + '/session', true, axios);
const clientApi = new ClientOptionsApi(API_PATH + "/client", true, axios);
const operatorApi = new OperatorApi(API_PATH);

export function* getClients(action: PayloadAction<GetClientsAction>) {
  const { payload } = action;
  try {
    const clients = yield call(clientApi.getClients, payload.clientName);
    yield put(clientOptionActions.getOptionsSuccess({ clients }));
  } catch (error) {
    yield put(clientOptionActions.getOptionsFailure(error));
  }
}

export function* getOperators(action: PayloadAction<GetOperatorsAction>) {
  const { payload } = action;
  try {
    const operators = yield call(operatorApi.getOperators, payload.operatorName);
    yield put(operatorOptionActions.getOptionsSuccess({ operators }));
  } catch (error) {
    yield put(operatorOptionActions.getOptionsFailure({ error }));
  }
}

export function* removeAll(action: PayloadAction<DeleteAllAction>) {
  const { payload } = action;
  try {
    yield call(api.deleteAll, payload.operatorId);
    yield put(crudActions.removeAllSuccess({}));
    if (payload.callback) {
      yield call(payload.callback);
    }
  } catch (error) {
    // yield put(crudActions.failure({ error }));
  }
}