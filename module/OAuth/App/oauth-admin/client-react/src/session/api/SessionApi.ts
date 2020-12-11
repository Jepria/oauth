import { buildError, ConnectorCrud, handleAxiosError } from '../../app/common/rest/connector/ConnectorCrud';
import { SessionSearchTemplate, Session } from '../types';

export default class SessionApi extends ConnectorCrud<Session, any, any, SessionSearchTemplate> {

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
        response.status === 200 ? resolve() : reject(buildError(response))
      }).catch(error => reject(handleAxiosError(error)));
    })
  }
}