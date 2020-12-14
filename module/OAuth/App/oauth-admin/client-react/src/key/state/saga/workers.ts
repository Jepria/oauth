import { API_PATH } from '../../../config';
import { GetKeyAction, UpdateKeyAction } from '../keyActions';
import { actions } from '../keySlice';
import { put, call } from 'redux-saga/effects';
import KeyApi from '../../api/KeyApi';
import { PayloadAction } from '@reduxjs/toolkit';

const api = new KeyApi(API_PATH);

export function* getKey(action: PayloadAction<GetKeyAction>) {
  const { payload } = action;
  try {
    const record = yield call(api.getKey);
    yield put(actions.getRecordByIdSuccess({key: record}));
    if (payload.callback) {
      yield call(payload.callback);
    }
  } catch (error) {
    yield put(actions.failure({error}));
  }
}

export function* updateKey(action: PayloadAction<UpdateKeyAction>) {
  try {
    yield call(api.updateKey);
    yield put(actions.updateSuccess());
    if (action.payload.callback) {
      yield call(action.payload.callback);
    }
  } catch (error) {
    yield put(actions.failure({error}));
  }
}
