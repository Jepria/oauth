import { API_PATH } from '../../../../../config'
import { TEST_API_PATH } from '../../../../../config'
import { onLoading, onFailure, GetKeyAction, UpdateKeyAction, getKeySuccess, updateKeySuccess } from '../actions';
import { put, call } from 'redux-saga/effects';
import KeyApi from '../../../api/KeyApi';

const api = new KeyApi(TEST_API_PATH);

export function* getKey(action: GetKeyAction) {
  try {
    yield put(onLoading('Загрузка данных...'));
    const record = yield call(api.getKey);
    yield put(getKeySuccess(record));
    if (action.callback) {
      yield call(action.callback);
    }
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* updateKey(action: UpdateKeyAction) {
  try {
    yield put(onLoading('Загрузка данных...'));
    yield call(api.updateKey);
    yield put(updateKeySuccess());
    if (action.callback) {
      yield call(action.callback);
    }
  } catch (error) {
    yield put(onFailure(error));
  }
}
