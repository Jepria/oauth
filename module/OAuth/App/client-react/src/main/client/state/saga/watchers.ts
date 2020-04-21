import { all, takeLatest, takeEvery  } from "redux-saga/effects";
import { CREATE_CLIENT, UPDATE_CLIENT, DELETE_CLIENT, GET_CLIENT_BY_ID, POST_CLIENT_SEARCH_REQUEST, SEARCH_CLIENTS } from "../actions";
import { create, update, remove, getById, postSearchRequest } from "./workers";


function* entityWatcher() {
  yield takeEvery(CREATE_CLIENT, create);
  yield takeEvery(UPDATE_CLIENT, update);
  yield takeEvery(DELETE_CLIENT, remove);
  yield takeEvery(GET_CLIENT_BY_ID, getById)
}

function* searchWatcher() {
  yield takeLatest(POST_CLIENT_SEARCH_REQUEST, postSearchRequest);
  yield takeLatest(SEARCH_CLIENTS, postSearchRequest);
}

export default function* clientSaga() {
  yield all([
    entityWatcher(),
    searchWatcher()
  ]);
}