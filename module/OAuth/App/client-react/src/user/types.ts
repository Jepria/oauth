export type User = {
  username: string;
  operatorId?: number;
}

export type Roles = {
  [roleShortName: string]: number;
}

export interface UserState {
  currentUser: User;
  roles: Roles;
  isLoading: boolean;
}