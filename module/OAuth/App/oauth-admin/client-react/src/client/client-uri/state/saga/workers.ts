import { API_PATH } from '../../../../config'
import {
  SearchClientUriAction
} from '../clientUriActions';
import { actions } from '../clientUriSearchSlice';
import { put, call } from 'redux-saga/effects';
import ClientUriSearchApi from '../../api/ClientUriApi';
import { PayloadAction } from '@reduxjs/toolkit';

const api = new ClientUriSearchApi(`${API_PATH}/client`);

export function* search(action: PayloadAction<SearchClientUriAction>) {
  const { payload } = action;
  try {
    const records = yield call(api.search, payload.clientId);
    yield put(actions.searchSuccess({ clientUris: records }));
  } catch (error) {
    yield put(actions.failure({ error }));
  }
}