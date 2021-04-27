import { buildError, ConnectorCrud, handleAxiosError } from '@jfront/core-rest';
import { ClientUri, ClientUriCreateDto, ClientUriPrimaryKey } from '../types';
import {API_PATH} from "../../../config";
import axios from "axios";

export class ClientUriCrudApi extends ConnectorCrud<ClientUri, ClientUriPrimaryKey, ClientUriCreateDto> {

  constructor() {
    super(API_PATH + '/client', true, axios);
  }

  create = (createDto: ClientUriCreateDto): Promise<ClientUri> => {
    const clientId = createDto.clientId;
    delete createDto.clientId;
    return new Promise<ClientUri>((resolve, reject) => {
      this.getAxios().post(
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
          this.getAxios().get(
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
      this.getAxios().delete(
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
      this.getAxios().get(
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

export const clientUriCrudApi = new ClientUriCrudApi();