import { ConnectorBase, buildError, handleAxiosError } from '@jfront/core-rest';
import { Client, Option } from '../types';

export class ClientOptionsApi extends ConnectorBase {

  getClients = (clientName?: string): Promise<Array<Client>> => {
    return new Promise<Array<Client>>((resolve, reject) => {
      this.getAxios().get(
        this.baseUrl + `?clientName=${clientName}`,
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

  getRoles = (roleName?: string): Promise<Array<Option>> => {
    return new Promise<Array<Option>>((resolve, reject) => {
      this.getAxios().get(
        this.baseUrl + `/role?roleName=${roleName}&maxRowCount=25`,
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