import { API_PATH} from '../../../../../config';
import { 
  DeleteSessionAction, deleteSessionSuccess, 
  GetSessionByIdAction, getSessionByIdSuccess, 
  PostSearchSessionRequestAction, postSearchSessionRequestSuccess, 
  SearchSessionsAction, searchSessionsSuccess, 
  SetCurrentRecordAction, setCurrentRecordSuccess, GetClientsAction, getClientsSuccess, GetOperatorsAction, getOperatorsSuccess, deleteSessionFailure, getSessionByIdFailure, postSearchSessionRequestFailure, searchSessionsFailure, getClientsFailure, getOperatorsFailure } from '../actions';
import { put, call } from 'redux-saga/effects';
import ClientApi from '../../../../client/api/ClientApi';
import OperatorApi from '../../../api/OperatorApi';
import { ConnectorCrud } from '../../../../../rest/connector/ConnectorCrud';

const api = new ConnectorCrud(API_PATH + '/session');
const clientApi = new ClientApi(API_PATH + "/client");
const operatorApi = new OperatorApi(API_PATH);

export function* remove(action: DeleteSessionAction) {
  try {
    yield call(api.delete, action.sessionId);
    yield put(deleteSessionSuccess(action.sessionId));
    if (action.callback) {
      yield call(action.callback);
    }
  } catch (error) {
    yield put(deleteSessionFailure(error));
  }
}

export function* getById(action: GetSessionByIdAction) {
  try {
    const record = yield call(api.getRecordById, action.sessionId);
    yield put(getSessionByIdSuccess(record));
  } catch (error) {
    yield put(getSessionByIdFailure(error));
  }
}

export function* postSearchRequest(action: PostSearchSessionRequestAction) {
  try {
    const searchId = yield call(api.postSearchRequest, action.searchRequest);
    yield put(postSearchSessionRequestSuccess(searchId, action.searchRequest));
    if (action.callback) {
      yield call(action.callback);
    }
  } catch (error) {
    yield put(postSearchSessionRequestFailure(error));
  }
}

export function* search(action: SearchSessionsAction) {
  try {
    const resultSetSize = yield call(api.getResultSetSize, action.searchId);
    const records = yield call(api.search, action.searchId, action.pageSize, action.page);
    yield put(searchSessionsSuccess(records, resultSetSize));
  } catch (error) {
    yield put(searchSessionsFailure(error));
  }
}

export function* setCurrentSession(action: SetCurrentRecordAction) {
  yield put(setCurrentRecordSuccess(action.payload));
  if (action.callback) {
    yield call(action.callback);
  }
}

export function* getClients(action: GetClientsAction) {
  try {
    const clients = yield call(clientApi.getClients, action.clientName);
    yield put(getClientsSuccess(clients));
  } catch (error) {
    yield put(getClientsFailure(error));
  }
}

export function* getOperators(action: GetOperatorsAction) {
  try {
    const operators = yield call(operatorApi.getOperators, action.operatorName);
    yield put(getOperatorsSuccess(operators));
  } catch(error) {
    yield put(getOperatorsFailure(error));
  }
}