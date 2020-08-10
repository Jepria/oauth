import { User, Roles } from "./types";

export const GET_CURRENT_USER = "GET_CURRENT_USER";
export const GET_CURRENT_USER_SUCCESS = "GET_CURRENT_USER_SUCCESS";
export const GET_CURRENT_USER_FAILURE = "GET_CURRENT_USER_FAILURE";
export const IS_USER_IN_ROLES = "IS_USER_IN_ROLES";
export const IS_USER_IN_ROLES_SUCCESS = "IS_USER_IN_ROLES_SUCCESS";
export const IS_USER_IN_ROLES_FAILURE = "IS_USER_IN_ROLES_FAILURE";

export interface GetCurrentUserAction {
  type: typeof GET_CURRENT_USER;
}

export interface GetCurrentUserSuccessAction {
  type: typeof GET_CURRENT_USER_SUCCESS;
  user: User;
}

export interface GetCurrentUserFailureAction {
  type: typeof GET_CURRENT_USER_FAILURE;
}

export interface IsUserInRolesAction {
  type: typeof IS_USER_IN_ROLES;
}

export interface IsUserInRolesSuccessAction {
  type: typeof IS_USER_IN_ROLES_SUCCESS;
  result: Roles;
}

export interface IsUserInRolesFailureAction {
  type: typeof IS_USER_IN_ROLES_FAILURE;
}

export type UserActionTypes = GetCurrentUserAction |
  GetCurrentUserSuccessAction |
  GetCurrentUserFailureAction |
  IsUserInRolesAction |
  IsUserInRolesSuccessAction |
  IsUserInRolesFailureAction;

export function getCurrentUser(): UserActionTypes {
  return {
    type: GET_CURRENT_USER
  }
}

export function getCurrentUserSuccess(user: User): UserActionTypes {
  return {
    type: GET_CURRENT_USER_SUCCESS,
    user
  }
}

export function getCurrentUserFailure(): UserActionTypes {
  return {
    type: GET_CURRENT_USER_FAILURE
  }
}

export function isUserInRoles(): UserActionTypes {
  return {
    type: IS_USER_IN_ROLES
  }
}

export function isUserInRolesSuccess(result: Roles): UserActionTypes {
  return {
    type: IS_USER_IN_ROLES_SUCCESS,
    result
  }
}

export function isUserInRolesFailure(): UserActionTypes {
  return {
    type: IS_USER_IN_ROLES
  }
}