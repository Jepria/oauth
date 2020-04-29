import { ClientUriActionTypes, LOADING, FAILURE, SET_CURRENT_RECORD, CREATE_CLIENT_URI_SUCCESS, DELETE_CLIENT_URI_SUCCESS, GET_CLIENT_URI_BY_ID_SUCCESS, SEARCH_CLIENT_URI_SUCCESS } from './actions'
import { ClientUriState } from "../../types";

export const initialState: ClientUriState = {
  isLoading: false
}

export function clientUriReducer(state: ClientUriState = initialState, action: ClientUriActionTypes): ClientUriState {
  switch (action.type) {
    case LOADING:
      return {
        ...state,
        isLoading: true,
        message: action.message
      }
    case FAILURE:
      return {
        ...state,
        isLoading: false
      }
    case CREATE_CLIENT_URI_SUCCESS:
      return {
        ...state,
        current: action.payload,
        isLoading: false
      }
    case DELETE_CLIENT_URI_SUCCESS:
      return {
        ...state,
        current: undefined,
        isLoading: false
      }
    case SEARCH_CLIENT_URI_SUCCESS:
      return {
        ...state,
        records: action.clientUris,
        isLoading: false
      }
    case GET_CLIENT_URI_BY_ID_SUCCESS:
      return {
        ...state,
        current: action.clientUri,
        isLoading: false
      }
    case SET_CURRENT_RECORD:
      return {
        ...state,
        current: action.payload
      }
    default: {
      return state;
    }
  }
}