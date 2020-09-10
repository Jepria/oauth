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
  maxRowCount                 - Максимальное количество выводимых строк
                                (по умолчанию без ограничений)
  operatorId                  - Id оператора, выполняющего операцию

  Возврат (курсор):
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
  client_operator_id          - ID оператора клиентского приложения

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

/* pfunc: getClientGrant
  Возвращает список грантов клиентского приложения.

  Параметры:
  clientShortName             - Краткое наименование приложения
  operatorId                  - Id оператора, выполняющего операцию

  Возврат (курсор):
  client_short_name           - Краткое наименование приложения
  grant_type                  - Тип гранта

  (сортировка по grant_type)

  ( <body::getClientGrant>)
*/
function getClientGrant(
  clientShortName varchar2
  , operatorId integer
)
return sys_refcursor;

/* pfunc: getRoles
  Возвращает список ролей.

  Параметры:
  roleName                    - Наименование роли
  roleNameEn                  - Наименование роли англ.
  maxRowCount                 - Максимальное количество выводимых строк
                                (по умолчанию без ограничений)
  operatorId                  - Id оператора, выполняющего операцию

  Возврат (курсор):
  role_id	                    - Идентификатор роли
  short_name	                - Краткое наименование роли
  role_name	                  - Наименование роли на языке по умолчанию
  role_name_en	              - Наименование роли на английском языке
  description	                - Описание роли на языке по умолчанию
  date_ins	                  - Дата создания записи
  operator_id	                - Пользователь, создавший запись
  operator_name	              - Пользователь на языке по умолчанию, создавший
                                запись
  operator_name_en	          - Пользователь на английском языке, создавший
                                запись
  is_unused                   - Признак неиспользуемой роли


  ( <body::getRoles>)
*/
function getRoles(
  roleName varchar2 := null
  , roleNameEn varchar2 := null
  , maxRowCount integer := null
  , operatorId integer
)
return sys_refcursor;



/* group: URI клиентского приложения */

/* pfunc: createClientUri
  Добавляет URI в whitelist клиентского приложения.

  Параметры:
  clientShortName             - Краткое наименование приложения
  clientUri                   - URI клиентского приложения
  operatorId                  - Id оператора, выполняющего операцию

  Возврат:
  Id созданной записи.

  ( <body::createClientUri>)
*/
function createClientUri(
  clientShortName varchar2
  , clientUri varchar2
  , operatorId integer
)
return integer;

/* pproc: deleteClientUri
  Удаляет URI из whitelist клиентского приложения.

  Параметры:
  clientUriId                 - Id записи с URI клиентского приложения
  operatorId                  - Id оператора, выполняющего операцию

  ( <body::deleteClientUri>)
*/
procedure deleteClientUri(
  clientUriId integer
  , operatorId integer
);

/* pfunc: findClientUri
  Поиск URI клиентского приложения.

  Параметры:
  clientUriId                 - Id записи с URI клиентского приложения
                                (по умолчанию без ограничений)
  clientShortName             - Краткое наименование приложения
                                (по умолчанию без ограничений)
  maxRowCount                 - Максимальное количество выводимых строк
                                (по умолчанию без ограничений)
  operatorId                  - Id оператора, выполняющего операцию

  Возврат (курсор):
  client_uri_id               - Id записи с URI клиентского приложения
  client_short_name           - Краткое наименование приложения
  client_uri                  - URI клиентского приложения
  date_ins                    - Дата создания записи
  operator_id                 - Id оператора, создавшего запись
  operator_name               - Имя оператора, создавшего запись
  operator_name_en            - Имя оператора, создавшего запись на англ.

  (сортировка по date_ins в обратном порядке)

  ( <body::findClientUri>)
*/
function findClientUri(
  clientUriId integer := null
  , clientShortName varchar2 := null
  , maxRowCount integer := null
  , operatorId integer
)
return sys_refcursor;



/* group: Пользовательские сессии */

/* pfunc: createSession
  Создает пользовательскую сессию.

  Параметры:
  authСode                    - Авторизационный код (One-Time-Password)
  clientShortName             - Краткое наименование приложения
  redirectUri                 - URI для перенаправления
  operatorId                  - Id пользователя, владельца сессии
  codeChallenge               - Криптографически случайная строка,
                                используется при авторизации по PKCE
  accessToken                 - Уникальный UUID токена доступа
  accessTokenDateIns          - Дата создания токена доступа
  accessTokenDateFinish       - Дата окончания действия токена доступа
  refreshToken                - Уникальный UUID токена обновления
  refreshTokenDateIns         - Дата создания токена обновления
  refreshTokenDateFinish      - Дата окончания действия токена обновления
  sessionToken                - UUID токена сессии
  sessionTokenDateIns         - Дата создания токена сессии
  sessionTokenDateFinish      - Дата окончания действия токена сессии
  operatorIdIns               - Id оператора, выполняющего операцию

  Возврат:
  Id созданной записи

  ( <body::createSession>)
*/
function createSession(
  authCode varchar2
  , clientShortName varchar2
  , redirectUri varchar2
  , operatorId integer
  , codeChallenge varchar2
  , accessToken varchar2
  , accessTokenDateIns timestamp with time zone
  , accessTokenDateFinish timestamp with time zone
  , refreshToken varchar2
  , refreshTokenDateIns timestamp with time zone
  , refreshTokenDateFinish timestamp with time zone
  , sessionToken varchar2
  , sessionTokenDateIns timestamp with time zone
  , sessionTokenDateFinish timestamp with time zone
  , operatorIdIns integer
)
return integer;

/* pproc: updateSession
  Обновляет пользовательскую сессию.

  Параметры:
  sessionId                   - Id пользовательской сессии
  authСode                    - Авторизационный код (One-Time-Password)
  clientShortName             - Краткое наименование приложения
  redirectUri                 - URI для перенаправления
  operatorId                  - Id пользователя, владельца сессии
  codeChallenge               - Криптографически случайная строка,
                                используется при авторизации по PKCE
  accessToken                 - Уникальный UUID токена доступа
  accessTokenDateIns          - Дата создания токена доступа
  accessTokenDateFinish       - Дата окончания действия токена доступа
  refreshToken                - Уникальный UUID токена обновления
  refreshTokenDateIns         - Дата создания токена обновления
  refreshTokenDateFinish      - Дата окончания действия токена обновления
  sessionToken                - UUID токена сессии
  sessionTokenDateIns         - Дата создания токена сессии
  sessionTokenDateFinish      - Дата окончания действия токена сессии
  operatorIdIns               - Id оператора, выполняющего операцию

  ( <body::updateSession>)
*/
procedure updateSession(
  sessionId integer
  , authCode varchar2
  , clientShortName varchar2
  , redirectUri varchar2
  , operatorId integer
  , codeChallenge varchar2
  , accessToken varchar2
  , accessTokenDateIns timestamp with time zone
  , accessTokenDateFinish timestamp with time zone
  , refreshToken varchar2
  , refreshTokenDateIns timestamp with time zone
  , refreshTokenDateFinish timestamp with time zone
  , sessionToken varchar2
  , sessionTokenDateIns timestamp with time zone
  , sessionTokenDateFinish timestamp with time zone
  , operatorIdIns integer
);

/* pproc: blockSession
  Блокирует пользовательскую сессию.

  Параметры:
  sessionId                   - Id пользовательской сессии
  operatorIdIns               - Id оператора, выполняющего операцию

  ( <body::blockSession>)
*/
procedure blockSession(
  sessionId integer
  , operatorId integer
);

/* pfunc: findSession
  Поиск пользовательской сессии.

  Параметры:
  sessionId                   - Id пользовательской сессии
                                (по умолчанию без ограничений)
  authСode                    - Авторизационный код (One-Time-Password)
                                (по умолчанию без ограничений)
  clientShortName             - Краткое наименование приложения
                                (по умолчанию без ограничений)
  redirectUri                 - URI для перенаправления
                                (по умолчанию без ограничений)
  operatorId                  - Id пользователя, владельца сессии
                                (по умолчанию без ограничений)
  codeChallenge               - Криптографически случайная строка,
                                используется при авторизации по PKCE
                                (по умолчанию без ограничений)
  accessToken                 - Уникальный UUID токена доступа
                                (по умолчанию без ограничений)
  refreshToken                - Уникальный UUID токена обновления
                                (по умолчанию без ограничений)
  sessionToken                - UUID токена сессии
                                (по умолчанию без ограничений)
  maxRowCount                 - Максимальное количество выводимых строк
                                (по умолчанию без ограничений)
  operatorIdIns               - Id оператора, выполняющего операцию

  Возврат (курсор):
  session_id                  - Идентификатор записи
  auth_code                   - Авторизационный код (One-Time-Password)
  client_short_name           - Краткое наименование приложения
  client_name                 - Имя клиентского приложения
  client_name_en              - Имя клиентского приложения на английском
  redirect_uri                - URI для перенаправления
  operator_id                 - Id пользователя, владельца сессии
  operator_name               - Имя пользователя, владельца сессии
  operator_login              - Логин пользователя, владельца сессии
  code_challenge              - Криптографически случайная строка,
                                используется при авторизации по PKCE
  access_token                - Уникальный UUID токена доступа
  access_token_date_ins       - Дата создания токена доступа
  access_token_date_finish    - Дата окончания действия токена доступа
  refresh_token               - Уникальный UUID токена обновления
  refresh_token_date_ins      - Дата создания токена обновления
  refresh_token_date_finish   - Дата окончания действия токена обновления
  session_token               - Уникальный UUID токена сессии
  session_token_date_ins      - Дата создания токена сессии
  session_token_date_finish   - Дата окончания действия токена сессии
  date_ins                    - Дата создания записи
  operator_id_ins             - ID пользователя, создавшего запись

  (сортировка по date_ins в обратном порядке)

  ( <body::findSession>)
*/
function findSession(
  sessionId integer := null
  , authCode varchar2 := null
  , clientShortName varchar2 := null
  , redirectUri varchar2 := null
  , operatorId integer := null
  , codeChallenge varchar2 := null
  , accessToken varchar2 := null
  , refreshToken varchar2 := null
  , sessionToken varchar2 := null
  , maxRowCount integer := null
  , operatorIdIns integer
)
return sys_refcursor;



/* group: Хранилище ключей */

/* pfunc: setKey
  Устанавливает актуальные ключи.

  Параметры:
  publicKey                   - Публичный ключ
  privateKey                  - Приватный ключ
  operatorId                  - Id оператора, выполняющего операцию

  Возврат:
  Id созданной записи

  ( <body::setKey>)
*/
function setKey(
  publicKey varchar2
  , privateKey varchar2
  , operatorId integer
)
return integer;

/* pfunc: getKey
  Возвращает ключи.

  Параметры:
  isExpired                   - Признак истекшего срока действия ключа
  operatorId                  - Id оператора, выполняющего операцию

  Возврат (курсор):
  key_id                      - Id записи с ключами
  public_key                  - Публичный ключ
  private_key                 - Приватный ключ
  is_expired                  - Признак истекшего срока действия ключа
  date_ins                    - Дата создания записи

  ( <body::getKey>)
*/
function getKey(
  isExpired integer
  , operatorId integer
)
return sys_refcursor;

end pkg_OAuth;
/
