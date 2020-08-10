import { createContext, useContext } from 'react';
import { User } from './types';

export interface IUserContext {
  currentUser: User;
  isLoading: boolean;
  isUserInRoles: (roleShortNames: Array<string>) => Promise<boolean>;
  isUserInRole: (roleShortName: string) => Promise<boolean>;
}

export const UserContext = createContext<IUserContext>({
  currentUser: {
    username: "Guest"
  },
  isLoading: false,
  isUserInRoles: () => new Promise<boolean>(() => {}),
  isUserInRole: () => new Promise<boolean>(() => {})
});

export const useUser = (): IUserContext => {
  return useContext(UserContext);
}