import { createContext, useContext } from 'react';
import { User } from './types';

export interface IUserContext {
  currentUser: User;
  isUserLoading: boolean;
  isRoleLoading: boolean;
  isUserInRoles: (roleShortNames: Array<string>) => Promise<boolean>;
  isUserInRole: (roleShortName: string) => Promise<boolean>;
}

export const UserContext = createContext<IUserContext>({
  currentUser: {
    username: "Guest"
  },
  isUserLoading: false,
  isRoleLoading: false,
  isUserInRoles: () => new Promise<boolean>(() => {}),
  isUserInRole: () => new Promise<boolean>(() => {})
});

export const useUser = (): IUserContext => {
  return useContext(UserContext);
}