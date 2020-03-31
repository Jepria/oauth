package org.jepria.oauth.client;

import java.util.List;

public interface ClientService {
  /**
   * @return список типов грантов
   */
  List<String> getGrantType();

  /**
   * @param grantTypeCodes список грантов
   * @return список типов ответа для указанных грантов
   */
  List<String> getGrantResponseType(List<String> grantTypeCodes);

  /**
   * @param clientId ID клиентского приложения
   * @return список грантов указанных для выбранного клиента
   */
  List<String> getClientGrantTypes(String clientId);

  /**
   * @param clientId ID клиентского приложения
   * @return список типов ответа указанных для выбранного клиента
   */
  List<String> getClientResponseTypes(String clientId);

  /**
   * @return список типов клиентских приложений
   */
  List<String> getApplicationTypes();

  List<String> getApplicationGrantTypes(String applicationTypeCodes);
}
