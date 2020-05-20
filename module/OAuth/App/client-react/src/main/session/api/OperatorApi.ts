import axios from 'axios';
import { Operator } from '../types';

export default class OperatorApi {

  private url: string;

  constructor(url: string) {
    this.url = `${url}/operators`;
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
          reject(response);
        }
      }).catch(error => reject(error));
    });
  }
}