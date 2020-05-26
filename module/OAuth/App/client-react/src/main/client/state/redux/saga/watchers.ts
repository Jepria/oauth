import { all, takeLatest, takeEvery  } from "redux-saga/effects";
import { CREATE_CLIENT, UPDATE_CLIENT, DELETE_CLIENT, GET_CLIENT_BY_ID, POST_CLIENT_SEARCH_REQUEST, SEARCH_CLIENTS, SET_CURRENT_RECORD } from "../actions";
import { create, update, remove, getById, postSearchRequest, search, setCurrentClient } from "./workers";


function* entityWatcher() {
  yield takeEvery(CREATE_CLIENT, create);
  yield takeEvery(UPDATE_CLIENT, update);
  yield takeEvery(DELETE_CLIENT, remove);
  yield takeEvery(GET_CLIENT_BY_ID, getById);
  yield takeEvery(SET_CURRENT_RECORD, setCurrentClient);
}

function* searchWatcher() {
  yield takeLatest(POST_CLIENT_SEARCH_REQUEST, postSearchRequest);
  yield takeLatest(SEARCH_CLIENTS, search);
}

export function* clientSaga() {
  yield all([
    entityWatcher(),
    searchWatcher()
  ]);
}