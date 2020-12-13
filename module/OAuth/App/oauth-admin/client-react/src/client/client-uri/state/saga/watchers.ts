import { all, takeLatest, takeEvery  } from "redux-saga/effects";
import { actions } from '../clientUriSlice'
import { create,  remove, getById, search, setCurrentClient } from "./workers";


function* entityWatcher() {
  yield takeEvery(actions.create.type, create);
  yield takeEvery(actions.remove.type, remove);
  yield takeEvery(actions.getRecordById.type, getById);
  yield takeEvery(actions.setCurrentRecord.type, setCurrentClient);
}

function* searchWatcher() {
  yield takeLatest(actions.search.type, search);
}

export function* clientUriSaga() {
  yield all([
    entityWatcher(),
    searchWatcher()
  ]);
}