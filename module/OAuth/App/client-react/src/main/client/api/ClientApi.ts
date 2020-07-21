import axios from 'axios';
import { ClientSearchTemplate, SearchRequest, Client, Option } from '../types';

export default class ClientApi {

  private url: string;

  constructor(url: string) {
    this.url = `${url}/client`;
    axios.defaults.withCredentials = true;
  }

  create = (client: Client): Promise<Client> => {
    return new Promise<Client>((resolve, reject) => {
      axios.post(
        this.url,
        client,
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8'
          }
        }
      ).then(response => {
        if (response.status === 201) {
          let location: string = response.headers['location'];
          axios.get(
            location,
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
        } else {
          reject(response);
        }
      }).catch(error => reject(error));
    });
  }

  update = (clientId: string, client: Client): Promise<Client> => {
    return new Promise<Client>((resolve, reject) => {
      axios.put(
        this.url + `/${clientId}`,
        client,
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8'
          }
        }
      ).then(response => {
        if (response.status === 200) {
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
        } else {
          reject(response);
        }
      }).catch(error => reject(error));
    });
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

  getClients = (clientName?: string): Promise<Array<Client>> => {
    return new Promise<Array<Client>>((resolve, reject) => {
      axios.get(
        this.url + `?clientName=${clientName}`,
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

  postSearchRequest = (searchRequest: SearchRequest<ClientSearchTemplate>) => {
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

  search = (searchId: string, pageSize: number, page: number): Promise<Array<Client>> => {
    return new Promise<Array<Client>>((resolve, reject) => {
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

  getRecordById = (clientId: string): Promise<Client> => {
    return new Promise<Client>((resolve, reject) => {
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

  getRoles = (roleName?: string): Promise<Array<Option>> => {
    return new Promise<Array<Option>>((resolve, reject) => {
      axios.get(
        this.url + `/role?roleName=${roleName}&maxRowCount=25`,
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
}