import { ClientActionTypes, CLIENT_LOADING, CLIENT_FAILURE, CREATE_CLIENT_SUCCESS, UPDATE_CLIENT_SUCCESS, DELETE_CLIENT_SUCCESS, POST_CLIENT_SEARCH_REQUEST_SUCCESS, SEARCH_CLIENTS_SUCCESS, GET_CLIENT_BY_ID_SUCCESS, SET_CURRENT_RECORD } from "./actions";
import { ClientState } from "../../types";

export const initialState: ClientState = {
  isLoading: false
}

export function clientReducer(state: ClientState = initialState, action: ClientActionTypes): ClientState {
  switch (action.type) {
    case CLIENT_LOADING:
      return {
        ...state,
        isLoading: true,
        message: action.message
      }
    case CLIENT_FAILURE:
      return {
        ...state,
        isLoading: false
      }
    case CREATE_CLIENT_SUCCESS:
      return {
        ...state,
        current: action.payload,
        isLoading: false
      }
    case UPDATE_CLIENT_SUCCESS:
      return {
        ...state,
        current: action.payload,
        isLoading: false
      }
    case DELETE_CLIENT_SUCCESS:
      return {
        ...state,
        current: undefined,
        isLoading: false
      }
    case POST_CLIENT_SEARCH_REQUEST_SUCCESS:
      return {
        ...state,
        searchId: action.searchId,
        searchRequest: action.searchRequest,
        isLoading: false
      }
    case SEARCH_CLIENTS_SUCCESS:
      return {
        ...state,
        records: action.clients,
        resultSetSize: action.resultSetSize,
        isLoading: false
      }
    case GET_CLIENT_BY_ID_SUCCESS:
      return {
        ...state,
        current: action.client,
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