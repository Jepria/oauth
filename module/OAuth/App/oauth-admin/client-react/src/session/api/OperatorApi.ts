import { buildError, ConnectorBase, handleAxiosError } from '@jfront/core-rest';
import axios from 'axios';
import { Operator } from '../types';

export default class OperatorApi extends ConnectorBase {


  constructor(url: string) {
    super(url, true, axios);
  }

  getOperators = (clientName?: string): Promise<Array<Operator>> => {
    return new Promise<Array<Operator>>((resolve, reject) => {
      axios.get(
        this.baseUrl + `?operatorName=${clientName}&maxRowCount=25`,
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