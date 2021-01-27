import { createOptionsSlice, OptionState } from '@jfront/core-redux-saga'
import { ClientOptionsApi } from '../api/ClientApi';
import { Option } from '../types';
import axios from 'axios';
import { API_PATH } from '../../config';

const api = new ClientOptionsApi(API_PATH + '/client', true, axios);

export const initialRoleState: OptionState<Option> = {
  options: [],
  isLoading: false
}

const slice = createOptionsSlice({
  name: "clientSlice/role",
  initialState: initialRoleState,
  reducers: {}
})

export const clientRoleSaga = slice.createSagaMiddleware((roleName: string) => api.getRoles(roleName))

export const {name, actions, reducer} = slice;

