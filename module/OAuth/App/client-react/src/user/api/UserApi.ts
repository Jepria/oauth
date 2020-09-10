import axios, { AxiosInstance } from 'axios';
import { Roles, User } from '../types';

export default class UserApi {

  private url: string;
  private axios: AxiosInstance;

  constructor(baseUrl: string, axiosInstance?: AxiosInstance) {
    this.url = `${baseUrl}/meta/current-user`;
    this.axios = axiosInstance ? axiosInstance : axios;
    this.axios.defaults.withCredentials = true;
  }

  getCurrentUser = (): Promise<User> => {
    return new Promise<User>((resolve, reject) => {
      this.axios.get(
        this.url,
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8',
            'Cache-Control': 'no-cache'
          }
        }
      ).then(response => {
        if (response.status === 200) {
          resolve(response.data);
        } else {
          reject(response);
        }
      }).catch(error => reject(error));
    });
  }

  isUserInRoles = (roleShortNames: Array<string>): Promise<Roles> => {
    return new Promise<Roles>((resolve, reject) => {
      this.axios.get(
        this.url + '/test-roles?roles=' + roleShortNames.join(','),
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8',
            'Cache-Control': 'no-cache'
          }
        }
      ).then(response => {
        if (response.status === 200) {
          resolve(response.data);
        } else {
          reject(response);
        }
      }).catch(error => reject(error));
    });
  }

  isUserInRole = (roleShortName: string): Promise<Roles> => {
    return new Promise<Roles>((resolve, reject) => {
      this.axios.get(
        this.url + '/test-role?role=' + roleShortName,
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8',
            'Cache-Control': 'no-cache'
          }
        }
      ).then(response => {
        if (response.status === 200) {
          resolve(response.data);
        } else {
          reject(response);
        }
      }).catch(error => reject(error));
    });
  }

}