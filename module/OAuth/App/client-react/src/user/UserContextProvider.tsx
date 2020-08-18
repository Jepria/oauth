import React, { useEffect, useReducer, useContext } from 'react';
import { OAuthContext } from '@jfront/oauth-context';
import { UserContext } from './UserContext';
import { reducer } from './reducer';
import UserApi from './api/UserApi';
import { getCurrentUser, getCurrentUserSuccess, getCurrentUserFailure, isUserInRoles, isUserInRolesSuccess, isUserInRolesFailure } from './actions';
import { AxiosInstance } from 'axios';

export interface UserContextProps {
  baseUrl: string;
  axios?: AxiosInstance
}

export const UserContextProvider: React.FC<UserContextProps> = ({ baseUrl, axios, children }) => {
  const userApi = new UserApi(baseUrl, axios);
  const { accessToken } = useContext(OAuthContext);
  const [{
    currentUser,
    roles,
    isLoading
  }, dispatch] = useReducer(reducer, {
    currentUser: {
      username: "Guest"
    },
    roles: {},
    isLoading: false
  });

  useEffect(() => {
    if (currentUser.username === " Guest" && accessToken) {
      getUser();
    } else if (!accessToken && currentUser.username !== " Guest") {
      dispatch(getCurrentUserSuccess({
        username: "Guest"
      }));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accessToken]);

  const getUser = () => {
    dispatch(getCurrentUser());
    userApi.getCurrentUser()
      .then(user => dispatch(getCurrentUserSuccess(user)))
      .catch(error => dispatch(getCurrentUserFailure()));
  }

  const hasRole = (roleShortName: string): Promise<boolean> => {
    return new Promise<boolean>((resolve, reject) => {
      if (roles[roleShortName]) {
        resolve(roles[roleShortName] === 1);
      } else {
        dispatch(isUserInRoles());
        userApi.isUserInRole(roleShortName)
          .then(result => {
            dispatch(isUserInRolesSuccess(result))
            resolve(result[roleShortName] === 1);
          })
          .catch(error => {
            dispatch(isUserInRolesFailure());
            reject(error);
          })
      }
    })
  }

  const hasRoles = (roleShortNames: Array<string>): Promise<boolean> => {
    return new Promise<boolean>((resolve, reject) => {
      let result: boolean | null = false;
      for (const roleShortName in roleShortNames) {
        if (!roles[roleShortName]) {
          result = null;
          break;
        }
        if (roles[roleShortName] === 1) {
          result = true;
          break;
        }
      }
      if (result !== null) {
        resolve(result);
      }
      dispatch(isUserInRoles());
      userApi.isUserInRoles(roleShortNames)
        .then(result => {
          dispatch(isUserInRolesSuccess(result))
          for (const roleShortName in result) {
            if (result[roleShortName] === 1) {
              resolve(true);
            }
          }
          resolve(false);
        })
        .catch(error => {
          dispatch(isUserInRolesFailure());
          reject(error);
        })

    })
  }

  return (
    <UserContext.Provider value={{
      currentUser,
      isUserInRole: hasRole,
      isUserInRoles: hasRoles,
      isLoading
    }}>
      {children}
    </UserContext.Provider>
  );
}