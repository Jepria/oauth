create or replace package pkg_OAuth is
/* package: pkg_OAuth
  Интерфейсный пакет модуля OAuth.

  SVN root: JEP/Module/OAuth
*/



/* group: Константы */

/* const: Module_Name
  Название модуля, к которому относится пакет.
*/
Module_Name constant varchar2(30) := 'OAuth';



/* group: Роли */

/* const: OAViewClient_RoleSName
  Краткое имя роли "Просмотр зарегистрированных клиентских приложений".
*/
OAViewClient_RoleSName constant varchar2(50) := 'OAViewClient';

/* const: OACreateClient_RoleSName
  Краткое имя роли "Регистрация клиентских приложений".
*/
OACreateClient_RoleSName constant varchar2(50) := 'OACreateClient';

/* const: OAEditClient_RoleSName
  Краткое имя роли "Редактирование учетных данных клиентских приложений".
*/
OAEditClient_RoleSName constant varchar2(50) := 'OAEditClient';

/* const: OADeleteClient_RoleSName
  Краткое имя роли "Удаление клиентских приложений".
*/
OADeleteClient_RoleSName constant varchar2(50) := 'OADeleteClient';

/* const: OAViewSession_RoleSName
  Краткое имя роли "Просмотр пользовательских сессий".
*/
OAViewSession_RoleSName constant varchar2(50) := 'OAViewSession';

/* const: OACreateSession_RoleSName
  Краткое имя роли "Создание пользовательских сессий".
*/
OACreateSession_RoleSName constant varchar2(50) := 'OACreateSession';

/* const: OAEditSession_RoleSName
  Краткое имя роли "Редактирование пользовательских сессий".
*/
OAEditSession_RoleSName constant varchar2(50) := 'OAEditSession';

/* const: OADeleteSession_RoleSName
  Краткое имя роли "Удаление пользовательских сессий".
*/
OADeleteSession_RoleSName constant varchar2(50) := 'OADeleteSession';

/* const: OAUpdateKey_RoleSName
  Краткое имя роли "Обновление ключей".
*/
OAUpdateKey_RoleSName constant varchar2(50) := 'OAUpdateKey';

/* const: OAViewKey_RoleSName
  Краткое имя роли "Просмотр ключей".
*/
OAViewKey_RoleSName constant varchar2(50) := 'OAViewKey';



/* group: Функции */



/* group: Клиентское приложение */

/* pfunc: createClient
  Создает клиентское приложение.

  Параметры:
  clientShortName             - Краткое наименование приложения
  clientName                  - Наименование клиентского приложения на языке
                                по умолчанию
  clientNameEn                - Наименование клиентского приложения на
                                английском языке
  applicationType             - Тип клиентского приложения
  grantTypeList               - Список грантов через разделитель ","
  roleShortNameList           - Список ролей из op_role через разделитель ","
  operatorId                  - Id оператора, выполняющего операцию

  Возврат:
  Id созданной записи.

  ( <body::createClient>)
*/
function createClient(
  clientShortName varchar2
  , clientName varchar2
  , clientNameEn varchar2
  , applicationType varchar2
  , grantTypeList varchar2
  , roleShortNameList varchar2
  , operatorId integer
)
return integer;

/* pproc: updateClient
  Обновляет клиентское приложение.

  Параметры:
  clientShortName             - Краткое наименование приложения
  clientName                  - Наименование клиентского приложения на языке
                                по умолчанию
  clientNameEn                - Наименование клиентского приложения на
                                английском языке
  applicationType             - Тип клиентского приложения
  grantTypeList               - Список грантов через разделитель ","
  roleShortNameList           - Список ролей из op_role через разделитель ","
  operatorId                  - Id оператора, выполняющего операцию

  ( <body::updateClient>)
*/
procedure updateClient(
  clientShortName varchar2
  , clientName varchar2
  , clientNameEn varchar2
  , applicationType varchar2
  , grantTypeList varchar2
  , roleShortNameList varchar2
  , operatorId integer
);

/* pproc: deleteClient
  Удаляет клиентское приложение.

  Параметры:
  clientShortName             - Краткое наименование приложения
  operatorId                  - Id оператора, выполняющего операцию

  ( <body::deleteClient>)
*/
procedure deleteClient(
  clientShortName varchar2
  , operatorId integer
);

/* pfunc: findClient
  Поиск клиентского приложения.

  Параметры:
  clientShortName             - Краткое наименование приложения
                                (по умолчанию без ограничений)
  clientName                  - Наименование клиентского приложения на языке
                                по умолчанию
                                (по умолчанию без ограничений)
  clientNameEn                - Наименование клиентского приложения на
                                английском языке
                                (по умолчанию без ограничений)
  maxRowCount                 - Максимальное количество выводимыых строк
                                (по умолчанию без ограничений)
  operatorId                  - Id оператора, выполняющего операцию

  Возврат ( курсор):
  client_short_name           - Краткое наименование приложения
  client_secret               - Случайная криптографически устойчивая строка
  client_name                 - Имя клиентского приложения
  client_name_en              - Имя клиентского приложения на английском
  application_type            - Тип клиентского приложения
  date_ins                    - Дата создания записи
  create_operator_id          - Id оператора, создавшего запись
  create_operator_name        - Имя оператора, создавшего запись
  create_operator_name_en     - Имя оператора, создавшего запись на англ.
  change_date                 - Дата последнего изменения записи
  change_operator_id          - Id оператора, изменившего запись
  change_operator_name        - Имя оператора, изменившего запись
  change_operator_name_en     - Имя оператора, изменившего запись на англ.

  (сортировка по date_ins в обратном порядке)

  ( <body::findClient>)
*/
function findClient(
  clientShortName varchar2 := null
  , clientName varchar2 := null
  , clientNameEn varchar2 := null
  , maxRowCount integer := null
  , operatorId integer
)
return sys_refcursor;

/* pfunc: verifyClientCredentials
  Проверяет данные клиентского приложения.

  Параметры:
  clientShortName             - Краткое наименование приложения
  clientSecret                - Секретное слово приложения
                                (по умолчанию отсутствует)

  Возврат:
  Id оператора, привязанного к приложению (если привязка существует).

  ( <body::verifyClientCredentials>)
*/
function verifyClientCredentials(
  clientShortName varchar2
  , clientSecret varchar2 := null
)
return integer;

end pkg_OAuth;
/
