import { all, takeLatest, takeEvery  } from "redux-saga/effects";
import { actions as crudActons } from "../sessionCrudSlice";
import { actions as clientSlice } from "../sessionClientSlice";
import { actions as operatorActions } from "../sessionOperatorSlice";
import { getClients, getOperators, removeAll } from "./workers";

export function* sessionSaga() {
  yield all([
    yield takeEvery(crudActons.removeAll.type, removeAll),
    yield takeLatest(clientSlice.getOptionsStart.type, getClients),
    yield takeLatest(operatorActions.getOptionsStart.type, getOperators)
  ]);
}