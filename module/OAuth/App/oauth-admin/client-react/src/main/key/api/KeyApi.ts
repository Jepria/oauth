import axios from 'axios';
import { Key } from '../types';

export default class KeyApi {

  private url: string;

  constructor(url: string) {
    this.url = `${url}/key`;
    axios.defaults.withCredentials = true;
  }

  getKey = (): Promise<Key> => {
    return new Promise<Key>((resolve, reject) => {
      axios.get(
        this.url,
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

  updateKey = (): Promise<void> => {
    return new Promise<void>((resolve, reject) => {
      axios.post(
        this.url,
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8'
          }
        }
      ).then(response => {
        if (response.status === 200) {
          resolve();
        } else {
          reject(response);
        }
      }).catch(error => reject(error));
    });
  }
}