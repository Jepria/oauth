import { all, takeLatest } from "redux-saga/effects";
import { actions } from '../clientUriSearchSlice'
import { search } from "./workers";

export function* clientUriSaga() {
  yield all([
    yield takeLatest(actions.search.type, search)
  ]);
}