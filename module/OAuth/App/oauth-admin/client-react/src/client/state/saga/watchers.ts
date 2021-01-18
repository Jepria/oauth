import { all, takeEvery } from "redux-saga/effects";
import { actions } from '../clientRoleSlice';
import { getRoles } from "./workers";

export function* clientSaga() {
  yield all([
    yield takeEvery(actions.getOptionsStart.type, getRoles),
  ]);
}