package org.jepria.oauth.model.client;

import org.jepria.server.data.OptionDto;

import java.util.List;

public interface ClientService {
  /**
   * @return список типов грантов
   */
  List<OptionDto<String>> getGrantType();

  /**
   * @param grantTypeCodes список грантов
   * @return список типов ответа для указанных грантов
   */
  List<OptionDto<String>> getGrantResponseType(List<String> grantTypeCodes);

  /**
   * @param clientId ID клиентского приложения
   * @return список грантов указанных для выбранного клиента
   */
  List<OptionDto<String>> getClientGrantTypes(String clientId);

  /**
   * @param clientId ID клиентского приложения
   * @return список типов ответа указанных для выбранного клиента
   */
  List<OptionDto<String>> getClientResponseTypes(String clientId);
}
