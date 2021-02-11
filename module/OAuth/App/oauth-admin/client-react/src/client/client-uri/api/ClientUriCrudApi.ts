import { buildError, ConnectorCrud, handleAxiosError } from '@jfront/core-rest';
import axios from 'axios';
import { ClientUri, ClientUriCreateDto, ClientUriPrimaryKey } from '../types';

export default class ClientUriCrudApi extends ConnectorCrud<ClientUri, ClientUriPrimaryKey, ClientUriCreateDto> {

  create = (createDto: ClientUriCreateDto): Promise<ClientUri> => {
    const clientId = createDto.clientId;
    delete createDto.clientId;
    return new Promise<ClientUri>((resolve, reject) => {
      axios.post(
        `${this.baseUrl}/${clientId}/client-uri`,
        createDto,
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

  delete = (primaryKey: ClientUriPrimaryKey): Promise<void> => {
    return new Promise<void>((resolve, reject) => {
      axios.delete(
        `${this.baseUrl}/${primaryKey.clientId}/client-uri/${primaryKey.clientUriId}`,
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

  getRecordById = (primaryKey: ClientUriPrimaryKey): Promise<ClientUri> => {
    return new Promise<ClientUri>((resolve, reject) => {
      axios.get(
        `${this.baseUrl}/${primaryKey.clientId}/client-uri/${primaryKey.clientUriId}`,
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