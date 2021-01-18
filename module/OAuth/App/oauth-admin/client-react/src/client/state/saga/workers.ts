import { ClientOptionsApi } from '../../api/ClientApi'
import { API_PATH } from '../../../config'
import {
  GetRolesAction
} from '../clientActions';
import { actions } from '../clientRoleSlice'
import { put, call } from 'redux-saga/effects';
import { PayloadAction } from '@reduxjs/toolkit';
import axios from 'axios';

const api = new ClientOptionsApi(API_PATH + '/client', true, axios);

export function* getRoles(action: PayloadAction<GetRolesAction>) {
  const roles = yield call(api.getRoles, action.payload.roleName);
  try {
    yield put(actions.getOptionsSuccess({ roles }));
  } catch (error) {
    yield put(actions.getOptionsFailure(error));
  }
}