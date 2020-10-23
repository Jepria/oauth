import { KeyActionTypes, GET_KEY_SUCCESS, UPDATE_KEY_SUCCESS, GET_KEY, GET_KEY_FAILURE, UPDATE_KEY, UPDATE_KEY_FAILURE } from "./actions";
import { KeyState } from "../../types";

export const initialState: KeyState = {
  isLoading: false
}

export function keyReducer(state: KeyState = initialState, action: KeyActionTypes): KeyState {
  switch (action.type) {
    case GET_KEY:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case GET_KEY_SUCCESS:
      return {
        ...state,
        isLoading: false,
        current: action.key
      }
    case GET_KEY_FAILURE:
      return {
        ...state,
        isLoading: false,
        error: action.error
      }
    case UPDATE_KEY:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true,
      }
    case UPDATE_KEY_SUCCESS:
      return {
        ...state,
        isLoading: false,
        current: undefined
      }
    case UPDATE_KEY_FAILURE:
      return {
        ...state,
        isLoading: false,
        error: action.error
      }
    default: {
      return state;
    }
  }
}