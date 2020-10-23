import { API_PATH } from '../../../../../config';
import { GetKeyAction, UpdateKeyAction, getKeySuccess, updateKeySuccess, getKeyFailure, updateKeyFailure } from '../actions';
import { put, call } from 'redux-saga/effects';
import KeyApi from '../../../api/KeyApi';

const api = new KeyApi(API_PATH);

export function* getKey(action: GetKeyAction) {
  try {
    const record = yield call(api.getKey);
    yield put(getKeySuccess(record));
    if (action.callback) {
      yield call(action.callback);
    }
  } catch (error) {
    yield put(getKeyFailure(error));
  }
}

export function* updateKey(action: UpdateKeyAction) {
  try {
    yield call(api.updateKey);
    yield put(updateKeySuccess());
    if (action.callback) {
      yield call(action.callback);
    }
  } catch (error) {
    yield put(updateKeyFailure(error));
  }
}
