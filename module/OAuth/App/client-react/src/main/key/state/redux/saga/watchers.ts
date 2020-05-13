import { all, takeLatest, takeEvery  } from "redux-saga/effects";
import { UPDATE_KEY, GET_KEY } from "../actions";
import { updateKey, getKey } from "./workers";


function* watchers() {
  yield takeEvery(UPDATE_KEY, updateKey);
  yield takeLatest(GET_KEY, getKey);
}

export default function* keySaga() {
  yield all([
    watchers()
  ]);
}