import { ConnectorCrud as ConnectorCrudLib, buildError as buildErrorLib, handleAxiosError as handleAxiosErrorLib } from '@jfront/core-rest';
import { Session } from '../types';

export class SessionCrudApi extends ConnectorCrudLib<Session> {

  /**
   * Удаление всех сессий для выбранного пользователя.
   * 
   * Record deletion.
   * @param {number} id operator id
   */
  deleteAll = (operatorId: number): Promise<void> => {
    return new Promise<void>((resolve, reject) => {
      this.getAxios().delete(
        this.baseUrl + `/delete-all/${operatorId}`,
        {
          headers: {
            'Accept': 'application/json;charset=utf-8',
            'Content-Type': 'application/json;charset=utf-8'
          }
        }
      ).then(response => {
        response.status === 200 ? resolve() : reject(buildErrorLib(response))
      }).catch(error => reject(handleAxiosErrorLib(error)));
    })
  }
}