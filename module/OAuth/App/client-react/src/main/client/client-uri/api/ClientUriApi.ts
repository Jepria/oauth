import axios from 'axios';
import { ConnectorBase } from '../../../../rest/connector/ConnectorBase';
import { buildError, handleAxiosError } from '../../../../rest/connector/ConnectorCrud';
import { ClientUri } from '../types';

export default class ClientUriApi extends ConnectorBase {

  create = (clientId: string, clientUri: ClientUri): Promise<ClientUri> => {
    return new Promise<ClientUri>((resolve, reject) => {
      axios.post(
        `${this.baseUrl}/${clientId}/client-uri`,
        clientUri,
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
          reject(buildError(response))
        }
      }).catch(error => reject(handleAxiosError(error)));
    });
  }

  delete = (clientId: string, clientUriId: string): Promise<void> => {
    return new Promise<void>((resolve, reject) => {
      axios.delete(
        `${this.baseUrl}/${clientId}/client-uri/${clientUriId}`,
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8'
          }
        }
      ).then(response => {
        response.status === 200 ? resolve() : reject(buildError(response));
      }).catch(error => reject(handleAxiosError(error)));
    })
  }

  search = (clientId: string): Promise<Array<ClientUri>> => {
    return new Promise<Array<ClientUri>>((resolve, reject) => {
      axios.get(
        `${this.baseUrl}/${clientId}/client-uri`,
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
          reject(buildError(response))
        }
      }).catch(error => reject(handleAxiosError(error)));
    });
  }

  getRecordById = (clientId: string, clientUriId: string): Promise<ClientUri> => {
    return new Promise<ClientUri>((resolve, reject) => {
      axios.get(
        `${this.baseUrl}/${clientId}/client-uri/${clientUriId}`,
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
          reject(buildError(response))
        }
      }).catch(error => reject(handleAxiosError(error)));
    });
  }
}