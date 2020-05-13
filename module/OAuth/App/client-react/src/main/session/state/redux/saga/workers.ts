import SessionApi from '../../../api/SessionApi'
import { API_PATH} from '../../../../../config'
import { TEST_API_PATH } from '../../../../../config'
import { onLoading, onFailure, 
  DeleteSessionAction, deleteSessionSuccess, 
  GetSessionByIdAction, getSessionByIdSuccess, 
  PostSearchSessionRequestAction, postSearchSessionRequestSuccess, 
  SearchSessionsAction, searchSessionsSuccess, 
  SetCurrentRecordAction, setCurrentRecordSuccess, GetClientsAction, getClientsSuccess, GetOperatorsAction, getOperatorsSuccess } from '../actions';
import { put, call } from 'redux-saga/effects';
import ClientApi from '../../../../client/api/ClientApi';
import OperatorApi from '../../../api/OperatorApi';

const api = new SessionApi(TEST_API_PATH);
const clientApi = new ClientApi(TEST_API_PATH);
const operatorApi = new OperatorApi(TEST_API_PATH);

export function* remove(action: DeleteSessionAction) {
  try {
    yield put(onLoading('Удаление...'));
    yield call(api.delete, action.sessionId);
    yield put(deleteSessionSuccess(action.sessionId));
    if (action.callback) {
      yield call(action.callback);
    }
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* getById(action: GetSessionByIdAction) {
  try {
    yield put(onLoading('Загрузка данных...'));
    const record = yield call(api.getRecordById, action.sessionId);
    yield put(getSessionByIdSuccess(record));
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* postSearchRequest(action: PostSearchSessionRequestAction) {
  try {
    yield put(onLoading('Загрузка данных...'));
    const searchId = yield call(api.postSearchRequest, action.searchRequest);
    yield put(postSearchSessionRequestSuccess(searchId, action.searchRequest));
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* search(action: SearchSessionsAction) {
  try {
    yield put(onLoading('Загрузка данных...'));
    const resultSetSize = yield call(api.getResultSetSize, action.searchId);
    const records = yield call(api.search, action.searchId, action.pageSize, action.page);
    yield put(searchSessionsSuccess(records, resultSetSize));
  } catch (error) {
    yield put(onFailure(error));
  }
}

export function* setCurrentSession(action: SetCurrentRecordAction) {
  yield put(setCurrentRecordSuccess(action.payload));
  if (action.callback) {
    yield call(action.callback);
  }
}

export function* getClients(action: GetClientsAction) {
  const clients = yield call(clientApi.getClients, action.clientName);
  yield put(getClientsSuccess(clients));
}

export function* getOperators(action: GetOperatorsAction) {
  const operators = yield call(operatorApi.getOperators, action.operatorName);
  yield put(getOperatorsSuccess(operators));
}