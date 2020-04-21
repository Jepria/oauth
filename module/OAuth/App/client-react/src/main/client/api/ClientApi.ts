import axios from 'axios';
import { ClientSearchTemplate, SearchRequest, Client } from '../types';

export default class ClientApi {

  private url: string;

  constructor(url: string) {
    this.url = url;
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
        }else {
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

  postSearchRequest = (searchRequest: SearchRequest<ClientSearchTemplate>) => {
    return new Promise<string>((resolve, reject) => {
      axios.post(
        this.url + '/search',
        searchRequest,
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8',
            'Cache-Control': 'no-cache'
          }
        }
      ).then(response => {
        if (response.status === 201) {
          let location: string = response.headers['Location'];
          resolve(location.split('/').pop());
        } else {
          reject(response);
        }
      }).catch(error => reject(error));
    });
  }

  search = (searchId: string): Promise<Array<Client>> => {
    return new Promise<Array<Client>>((resolve, reject) => {
      axios.get(
        this.url + `/search/${searchId}/resultset`,
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
}