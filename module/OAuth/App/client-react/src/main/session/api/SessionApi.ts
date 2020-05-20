import axios from 'axios';
import { SessionSearchTemplate, SearchRequest, Session } from '../types';

export default class ClientApi {

  private url: string;

  constructor(url: string) {
    this.url = `${url}/session`;
    axios.defaults.withCredentials = true;
  }

  delete = (clientId: string): Promise<void> => {
    return new Promise<void>((resolve, reject) => {
      axios.delete(
        this.url + `/${clientId}`,
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8'
          }
        }
      ).then(response => {
        response.status === 200 ? resolve() : reject(response);
      }).catch(error => reject(error));
    })
  }

  postSearchRequest = (searchRequest: SearchRequest<SessionSearchTemplate>) => {
    return new Promise<string>((resolve, reject) => {
      axios.post(
        this.url + '/search',
        searchRequest,
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8',
            'X-Cache-Control': 'no-cache'
          }
        }
      ).then(response => {
        if (response.status === 201) {
          let location: string = response.headers['location'];
          resolve(location.split('/').pop());
        } else {
          reject(response);
        }
      }).catch(error => reject(error));
    });
  }

  search = (searchId: string, pageSize: number, page: number): Promise<Array<Session>> => {
    return new Promise<Array<Session>>((resolve, reject) => {
      axios.get(
        this.url + `/search/${searchId}/resultset?pageSize=${pageSize}&page=${page}`,
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
        } else if (response.status === 204) {
          resolve([]);
        } else {
          reject(response);
        }
      }).catch(error => reject(error));
    });
  }

  getResultSetSize = (searchId: string): Promise<number> => {
    return new Promise<number>((resolve, reject) => {
      axios.get(
        this.url + `/search/${searchId}/resultset-size`,
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8',
            'X-Cache-Control': 'no-cache'
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

  getRecordById = (clientId: string): Promise<Session> => {
    return new Promise<Session>((resolve, reject) => {
      axios.get(
        this.url + `/${clientId}`,
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8'
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