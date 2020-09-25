import { all, takeLatest, takeEvery  } from "redux-saga/effects";
import { DELETE_SESSION, GET_SESSION_BY_ID, POST_SESSION_SEARCH_REQUEST, SEARCH_SESSIONS, SET_SESSION_CURRENT_RECORD, GET_CLIENTS, GET_OPERATORS } from "../actions";
import { remove, getById, postSearchRequest, search, setCurrentSession, getClients, getOperators } from "./workers";


function* entityWatcher() {
  yield takeEvery(DELETE_SESSION, remove);
  yield takeEvery(GET_SESSION_BY_ID, getById);
  yield takeEvery(SET_SESSION_CURRENT_RECORD, setCurrentSession);
}

function* searchWatcher() {
  yield takeLatest(POST_SESSION_SEARCH_REQUEST, postSearchRequest);
  yield takeLatest(SEARCH_SESSIONS, search);
  yield takeLatest(GET_CLIENTS, getClients);
  yield takeLatest(GET_OPERATORS, getOperators);
}

export function* sessionSaga() {
  yield all([
    entityWatcher(),
    searchWatcher()
  ]);
}