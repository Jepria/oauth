import { SessionActionTypes, DELETE_SESSION_SUCCESS, POST_SESSION_SEARCH_REQUEST_SUCCESS, SEARCH_SESSIONS_SUCCESS, GET_SESSION_BY_ID_SUCCESS, SET_SESSION_CURRENT_RECORD, GET_CLIENTS_SUCCESS, GET_OPERATORS_SUCCESS, DELETE_SESSION_FAILURE, GET_CLIENTS_FAILURE, GET_OPERATORS_FAILURE, GET_SESSION_BY_ID_FAILURE, POST_SESSION_SEARCH_REQUEST_FAILURE, SEARCH_SESSIONS_FAILURE, DELETE_SESSION, GET_CLIENTS, GET_OPERATORS, GET_SESSION_BY_ID, POST_SESSION_SEARCH_REQUEST, SEARCH_SESSIONS, SELECT_SESSION_RECORDS } from "./actions";
import { SessionState } from "../../types";

export const initialState: SessionState = {
  records: [],
  selectedRecords: [],
  isLoading: false,
  recordsLoading: false,
  clientsLoading: false,
  operatorsLoading: false
}

export function sessionReducer(state: SessionState = initialState, action: SessionActionTypes): SessionState {
  switch (action.type) {
    case DELETE_SESSION:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case DELETE_SESSION_SUCCESS:
      return {
        ...state,
        current: undefined,
        selectedRecords: [],
        isLoading: false
      }
    case DELETE_SESSION_FAILURE:
      return {
        ...state,
        error: action.error,
        isLoading: false
      }
    case POST_SESSION_SEARCH_REQUEST:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case POST_SESSION_SEARCH_REQUEST_SUCCESS:
      return {
        ...state,
        searchId: action.searchId,
        searchRequest: action.searchRequest,
        isLoading: false
      }
    case POST_SESSION_SEARCH_REQUEST_FAILURE:
      return {
        ...state,
        error: action.error,
        isLoading: false
      }
    case SEARCH_SESSIONS:
      return {
        ...state,
        message: action.loadingMessage,
        recordsLoading: true
      }
    case SEARCH_SESSIONS_SUCCESS:
      return {
        ...state,
        records: action.sessions,
        resultSetSize: action.resultSetSize,
        recordsLoading: false
      }
    case SEARCH_SESSIONS_FAILURE:
      return {
        ...state,
        error: action.error,
        recordsLoading: false
      }
    case GET_SESSION_BY_ID:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case GET_SESSION_BY_ID_SUCCESS:
      return {
        ...state,
        current: action.session,
        selectedRecords: [action.session],
        isLoading: false
      }
    case GET_SESSION_BY_ID_FAILURE:
      return {
        ...state,
        error: action.error,
        isLoading: false
      }
    case SET_SESSION_CURRENT_RECORD:
      return {
        ...state,
        current: action.payload
      }
    case GET_CLIENTS:
      return {
        ...state,
        clientsLoading: true
      }
    case GET_CLIENTS_SUCCESS:
      return {
        ...state,
        clients: action.clients,
        clientsLoading: false
      }
    case GET_CLIENTS_FAILURE:
      return {
        ...state,
        error: action.error,
        clientsLoading: false
      }
    case GET_OPERATORS:
      return {
        ...state,
        operatorsLoading: true
      }
    case GET_OPERATORS_SUCCESS:
      return {
        ...state,
        operators: action.operators,
        operatorsLoading: false
      }
    case GET_OPERATORS_FAILURE:
      return {
        ...state,
        error: action.error,
        operatorsLoading: false
      }
    case SELECT_SESSION_RECORDS:
      return {
        ...state,
        selectedRecords: action.records
      }
    default: {
      return state;
    }
  }
}