import { all, takeLatest, takeEvery  } from "redux-saga/effects";
import { actions } from "../keySlice";
import { updateKey, getKey } from "./workers";


function* watchers() {
  yield takeEvery(actions.update.type, updateKey);
  yield takeLatest(actions.getRecordById.type, getKey);
}

export function* keySaga() {
  yield all([
    watchers()
  ]);
}