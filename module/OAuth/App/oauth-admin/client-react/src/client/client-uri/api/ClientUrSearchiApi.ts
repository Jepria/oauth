import { buildError, ConnectorBase, handleAxiosError } from '@jfront/core-rest';
import axios from 'axios';
import { ClientUri } from '../types';

export default class ClientUriApi extends ConnectorBase {
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
}