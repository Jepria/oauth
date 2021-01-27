import { createOptionsSlice, OptionState } from "@jfront/core-redux-saga";
import { API_PATH } from "../../config";
import OperatorApi from "../api/OperatorApi";
import { Operator } from "../types";

const operatorApi = new OperatorApi(API_PATH + "/session/operators");

export const initialOperatorState: OptionState<Operator> = {
  options: [],
  isLoading: false
}

const slice = createOptionsSlice({
  name: "sessionSlice/operator",
  initialState: initialOperatorState,
  reducers: {}
})

export const sessionOperatorSaga = slice.createSagaMiddleware((operatorName: string) => operatorApi.getOperators(operatorName))

export const { name, actions, reducer } = slice;