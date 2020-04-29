import { all, takeLatest, takeEvery  } from "redux-saga/effects";
import { CREATE_CLIENT_URI, DELETE_CLIENT_URI, GET_CLIENT_URI_BY_ID, SEARCH_CLIENT_URI, SET_CURRENT_RECORD } from "../actions";
import { create,  remove, getById, search, setCurrentClient } from "./workers";


function* entityWatcher() {
  yield takeEvery(CREATE_CLIENT_URI, create);
  yield takeEvery(DELETE_CLIENT_URI, remove);
  yield takeEvery(GET_CLIENT_URI_BY_ID, getById);
  yield takeEvery(SET_CURRENT_RECORD, setCurrentClient);
}

function* searchWatcher() {
  yield takeLatest(SEARCH_CLIENT_URI, search);
}

export default function* clientUriSaga() {
  yield all([
    entityWatcher(),
    searchWatcher()
  ]);
}