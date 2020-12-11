import { ClientUriActionTypes, SET_CLIENT_URI_CURRENT_RECORD, CREATE_CLIENT_URI_SUCCESS, DELETE_CLIENT_URI_SUCCESS, GET_CLIENT_URI_BY_ID_SUCCESS, SEARCH_CLIENT_URI_SUCCESS, CREATE_CLIENT_URI_FAILURE, DELETE_CLIENT_URI_FAILURE, GET_CLIENT_URI_BY_ID_FAILURE, SEARCH_CLIENT_URI_FAILURE, CREATE_CLIENT_URI, DELETE_CLIENT_URI, GET_CLIENT_URI_BY_ID, SEARCH_CLIENT_URI, SELECT_CLIENT_URI_RECORDS } from './actions'
import { ClientUriState } from "../types";

export const initialState: ClientUriState = {
  records: [],
  selectedRecords: [],
  isLoading: false
}

export function clientUriReducer(state: ClientUriState = initialState, action: ClientUriActionTypes): ClientUriState {
  switch (action.type) {
    case CREATE_CLIENT_URI:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case CREATE_CLIENT_URI_SUCCESS:
      return {
        ...state,
        current: action.payload,
        isLoading: false
      }
    case CREATE_CLIENT_URI_FAILURE:
      return {
        ...state,
        error: action.error,
        isLoading: false
      }
    case DELETE_CLIENT_URI:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case DELETE_CLIENT_URI_SUCCESS:
      return {
        ...state,
        current: undefined,
        selectedRecords: [],
        isLoading: false
      }
    case DELETE_CLIENT_URI_FAILURE:
      return {
        ...state,
        error: action.error,
        isLoading: false
      }
    case SEARCH_CLIENT_URI:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case SEARCH_CLIENT_URI_SUCCESS:
      return {
        ...state,
        records: action.clientUris,
        isLoading: false
      }
    case SEARCH_CLIENT_URI_FAILURE:
      return {
        ...state,
        error: action.error,
        isLoading: false
      }
    case GET_CLIENT_URI_BY_ID:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case GET_CLIENT_URI_BY_ID_SUCCESS:
      return {
        ...state,
        current: action.clientUri,
        selectedRecords: [action.clientUri],
        isLoading: false
      }
    case GET_CLIENT_URI_BY_ID_FAILURE:
      return {
        ...state,
        error: action.error,
        isLoading: false
      }
    case SET_CLIENT_URI_CURRENT_RECORD:
      return {
        ...state,
        current: action.payload
      }
    case SELECT_CLIENT_URI_RECORDS:
      return {
        ...state,
        selectedRecords: action.records
      }
    default: {
      return state;
    }
  }
}