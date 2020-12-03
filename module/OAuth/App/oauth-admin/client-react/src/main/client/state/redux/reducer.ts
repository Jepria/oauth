import { ClientActionTypes, CREATE_CLIENT_SUCCESS, UPDATE_CLIENT_SUCCESS, DELETE_CLIENT_SUCCESS, POST_CLIENT_SEARCH_REQUEST_SUCCESS, SEARCH_CLIENTS_SUCCESS, GET_CLIENT_BY_ID_SUCCESS, SET_CLIENT_CURRENT_RECORD, GET_ROLES_SUCCESS, GET_ROLES, SEARCH_CLIENTS, CREATE_CLIENT, DELETE_CLIENT, GET_CLIENT_BY_ID, POST_CLIENT_SEARCH_REQUEST, UPDATE_CLIENT, CREATE_CLIENT_FAILURE, DELETE_CLIENT_FAILURE, GET_CLIENT_BY_ID_FAILURE, GET_ROLES_FAILURE, POST_CLIENT_SEARCH_REQUEST_FAILURE, SEARCH_CLIENTS_FAILURE, UPDATE_CLIENT_FAILURE, SELECT_CLIENT_RECORDS } from "./actions";
import { ClientState } from "../../types";

export const initialState: ClientState = {
  records: [],
  selectedRecords: [],
  recordsLoading: false,
  rolesLoading: false,
  isLoading: false
}

export function clientReducer(state: ClientState = initialState, action: ClientActionTypes): ClientState {
  switch (action.type) {
    case CREATE_CLIENT:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case CREATE_CLIENT_SUCCESS:
      return {
        ...state,
        current: action.payload,
        isLoading: false
      }
    case CREATE_CLIENT_FAILURE:
      return {
        ...state,
        isLoading: false,
        error: action.error
      }
    case UPDATE_CLIENT:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case UPDATE_CLIENT_SUCCESS:
      return {
        ...state,
        current: action.payload,
        isLoading: false
      }
    case UPDATE_CLIENT_FAILURE:
      return {
        ...state,
        isLoading: false,
        error: action.error
      }
    case DELETE_CLIENT:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case DELETE_CLIENT_SUCCESS:
      return {
        ...state,
        current: undefined,
        selectedRecords: [],
        isLoading: false
      }
    case DELETE_CLIENT_FAILURE:
      return {
        ...state,
        isLoading: false,
        error: action.error
      }
    case POST_CLIENT_SEARCH_REQUEST:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case POST_CLIENT_SEARCH_REQUEST_SUCCESS:
      return {
        ...state,
        searchId: action.searchId,
        searchRequest: action.searchRequest,
        isLoading: false
      }
    case POST_CLIENT_SEARCH_REQUEST_FAILURE:
      return {
        ...state,
        isLoading: false,
        error: action.error
      }
    case SEARCH_CLIENTS:
      return {
        ...state,
        message: action.loadingMessage,
        recordsLoading: true
      }
    case SEARCH_CLIENTS_SUCCESS:
      return {
        ...state,
        records: action.clients,
        resultSetSize: action.resultSetSize,
        recordsLoading: false
      }
    case SEARCH_CLIENTS_FAILURE:
      return {
        ...state,
        recordsLoading: false,
        error: action.error
      }
    case GET_CLIENT_BY_ID:
      return {
        ...state,
        message: action.loadingMessage,
        isLoading: true
      }
    case GET_CLIENT_BY_ID_SUCCESS:
      return {
        ...state,
        current: action.client,
        selectedRecords: [action.client],
        isLoading: false
      }
    case GET_CLIENT_BY_ID_FAILURE:
      return {
        ...state,
        isLoading: false,
        error: action.error
      }
    case SET_CLIENT_CURRENT_RECORD:
      return {
        ...state,
        current: action.payload
      }
    case GET_ROLES:
      return {
        ...state,
        rolesLoading: true
      }
    case GET_ROLES_SUCCESS:
      return {
        ...state,
        roles: action.roles,
        rolesLoading: false
      }
    case GET_ROLES_FAILURE:
      return {
        ...state,
        rolesLoading: false,
        error: action.error
      }
    case SELECT_CLIENT_RECORDS:
      return {
        ...state,
        selectedRecords: action.records
      }
    default: {
      return state;
    }
  }
}