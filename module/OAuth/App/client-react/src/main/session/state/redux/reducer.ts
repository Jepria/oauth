import { SessionActionTypes, SESSION_LOADING, SESSION_FAILURE, DELETE_SESSION_SUCCESS, POST_SESSION_SEARCH_REQUEST_SUCCESS, SEARCH_SESSIONS_SUCCESS, GET_SESSION_BY_ID_SUCCESS, SET_CURRENT_RECORD, GET_CLIENTS_SUCCESS, GET_OPERATORS_SUCCESS } from "./actions";
import { SessionState } from "../../types";

export const initialState: SessionState = {
  records: [],
  isLoading: false
}

export function sessionReducer(state: SessionState = initialState, action: SessionActionTypes): SessionState {
  switch (action.type) {
    case SESSION_LOADING:
      return {
        ...state,
        isLoading: true,
        message: action.message
      }
    case SESSION_FAILURE:
      return {
        ...state,
        isLoading: false
      }
    case DELETE_SESSION_SUCCESS:
      return {
        ...state,
        current: undefined,
        isLoading: false
      }
    case POST_SESSION_SEARCH_REQUEST_SUCCESS:
      return {
        ...state,
        searchId: action.searchId,
        searchRequest: action.searchRequest,
        isLoading: false
      }
    case SEARCH_SESSIONS_SUCCESS:
      return {
        ...state,
        records: action.sessions,
        resultSetSize: action.resultSetSize,
        isLoading: false
      }
    case GET_SESSION_BY_ID_SUCCESS:
      return {
        ...state,
        current: action.session,
        isLoading: false
      }
    case SET_CURRENT_RECORD:
      return {
        ...state,
        current: action.payload
      }
    case GET_CLIENTS_SUCCESS:
      return {
        ...state,
        clients: action.clients
      }
    case GET_OPERATORS_SUCCESS:
      return {
        ...state,
        operators: action.operators
      }
    default: {
      return state;
    }
  }
}