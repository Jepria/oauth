import ClientApi from '../../api/ClientApi'
import { API_PATH} from '../../../../config'
import { TEST_API_PATH } from '../../../../config'
import { onLoading, onFailure, CreateClientAction, createClientSuccess, UpdateClientAction, updateClientSuccess, DeleteClientAction, deleteClientSuccess, GetClientByIdAction, getClientByIdSuccess, PostSearchClientRequestSuccessAction, PostSearchClientRequestAction, postSearchClientRequestSuccess, SearchClientsAction, searchClientsSuccess, SetCurrentRecordAction, setCurrentRecord } from '../actions';
import { put, call } from 'redux-saga/effects';

const api = new ClientApi(`${TEST_API_PATH}/client`);

export function* create(action: CreateClientAction) {
  try {
    yield put(onLoading('Сохранение...'));
    const createdRecord = yield call(api.create, action.payload);
    yield put(createClientSuccess(createdRecord));
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* update(action: UpdateClientAction) {
  try {
    yield put(onLoading('Сохранение...'));
    const updatedRecord = yield call(api.update,action.clientId, action.payload);
    yield put(updateClientSuccess(updatedRecord));
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* remove(action: DeleteClientAction) {
  try {
    yield put(onLoading('Удаление...'));
    yield call(api.delete, action.clientId);
    yield put(deleteClientSuccess(action.clientId));
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* getById(action: GetClientByIdAction) {
  try {
    yield put(onLoading('Загрузка данных...'));
    const record = yield call(api.getRecordById, action.clientId);
    yield put(getClientByIdSuccess(record));
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* postSearchRequest(action: PostSearchClientRequestAction) {
  try {
    yield put(onLoading('Загрузка данных...'));
    const searchId = yield call(api.postSearchRequest, action.searchRequest);
    yield put(postSearchClientRequestSuccess(searchId, action.searchRequest));
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* search(action: SearchClientsAction) {
  try {
    yield put(onLoading('Загрузка данных...'));
    const records = yield call(api.search, action.searchId);
    yield put(searchClientsSuccess(records));
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* setCurrentClient(action: SetCurrentRecordAction) {
  yield put(setCurrentRecord(action.payload));
}