import axios from 'axios';
import { buildError, handleAxiosError } from '../../app/common/rest/connector/ConnectorCrud';
import { Operator } from '../types';

export default class OperatorApi {

  private url: string;

  constructor(url: string) {
    this.url = `${url}/session/operators`;
    axios.defaults.withCredentials = true;
  }

  getOperators = (clientName?: string): Promise<Array<Operator>> => {
    return new Promise<Array<Operator>>((resolve, reject) => {
      axios.get(
        this.url + `?operatorName=${clientName}&maxRowCount=25`,
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