import { all, takeLatest, takeEvery } from "redux-saga/effects";
import { actions } from '../clientSlice';
import { create, update, remove, getById, postSearchRequest, search, setCurrentClient, getRoles } from "./workers";


function* entityWatcher() {
  yield takeEvery(actions.create.type, create);
  yield takeEvery(actions.update.type, update);
  yield takeEvery(actions.remove.type, remove);
  yield takeEvery(actions.getRecordById.type, getById);
  yield takeEvery(actions.setCurrentRecord.type, setCurrentClient);
  yield takeEvery(actions.getRoles.type, getRoles);
}

function* searchWatcher() {
  yield takeLatest(actions.postSearchTemplate.type, postSearchRequest);
  yield takeLatest(actions.search.type, search);
}

export function* clientSaga() {
  yield all([
    entityWatcher(),
    searchWatcher()
  ]);
}