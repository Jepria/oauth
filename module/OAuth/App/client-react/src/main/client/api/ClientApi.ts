import axios from 'axios';
import { buildError, ConnectorCrud, handleAxiosError } from '../../../rest/connector/ConnectorCrud';
import { ClientSearchTemplate, Client, Option } from '../types';

export default class ClientApi extends ConnectorCrud<Client, Client, Client, ClientSearchTemplate> {

  getClients = (clientName?: string): Promise<Array<Client>> => {
    return new Promise<Array<Client>>((resolve, reject) => {
      axios.get(
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
      axios.get(
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