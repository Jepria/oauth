import { all, takeEvery } from "redux-saga/effects";
import { actions as crudActons } from "./sessionCrudSlice";
import { API_PATH } from '../../config';
import {
  DeleteAllAction
} from './sessionActions';
import { actions as crudActions } from './sessionCrudSlice';
import { put, call } from 'redux-saga/effects';
import { SessionCrudApi } from '../api/SessionApi';
import { PayloadAction } from "@reduxjs/toolkit";
import axios from 'axios';

export function* sessionSaga() {
  yield all([
    yield takeEvery(crudActons.removeAll.type, removeAll),
  ]);
}

const api = new SessionCrudApi(API_PATH + '/session', true, axios);

function* removeAll(action: PayloadAction<DeleteAllAction>) {
  const { payload } = action;
  try {
    yield call(api.deleteAll, payload.operatorId);
    yield put(crudActions.removeAllSuccess({}));
    if (payload.callback) {
      yield call(payload.callback);
    }
  } catch (error) {
    yield put(crudActions.failure({ error }));
  }
}