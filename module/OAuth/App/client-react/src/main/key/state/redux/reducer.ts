import { KEY_LOADING, KEY_FAILURE, KeyActionTypes, GET_KEY_SUCCESS, UPDATE_KEY_SUCCESS } from "./actions";
import { KeyState } from "../../types";

export const initialState: KeyState = {
  isLoading: false
}

export function keyReducer(state: KeyState = initialState, action: KeyActionTypes): KeyState {
  switch (action.type) {
    case GET_KEY_SUCCESS:
      return {
        ...state,
        isLoading: false,
        current: action.key
      }
      case UPDATE_KEY_SUCCESS:
        return {
          ...state,
          isLoading: false,
          current: undefined
        }
    case KEY_LOADING:
      return {
        ...state,
        isLoading: true,
        message: action.message
      }
    case KEY_FAILURE:
      return {
        ...state,
        isLoading: false
      }
    default: {
      return state;
    }
  }
}