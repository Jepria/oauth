import { all, takeLatest, takeEvery  } from "redux-saga/effects";
import { actions } from "../sessionSlice";
import { remove, getById, postSearchRequest, search, setCurrentSession, getClients, getOperators, removeAll } from "./workers";


function* entityWatcher() {
  yield takeEvery(actions.remove.type, remove);
  yield takeEvery(actions.removeAll.type, removeAll);
  yield takeEvery(actions.getRecordById.type, getById);
  yield takeEvery(actions.setCurrentRecord.type, setCurrentSession);
}

function* searchWatcher() {
  yield takeLatest(actions.postSearchTemplate.type, postSearchRequest);
  yield takeLatest(actions.search.type, search);
  yield takeLatest(actions.getClients.type, getClients);
  yield takeLatest(actions.getOperators.type, getOperators);
}

export function* sessionSaga() {
  yield all([
    entityWatcher(),
    searchWatcher()
  ]);
}