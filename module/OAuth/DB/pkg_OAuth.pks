create or replace package pkg_OAuth is
/* package: pkg_OAuth
  Интерфейсный пакет модуля OAuth.

  SVN root: JEP/Module/OAuth
*/



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
  Поиск клиентского приложения
  (подробнее <pkg_OAuthCommon.findClient>).

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
  Возвращает список грантов клиентского приложения
  (подробнее <pkg_OAuthCommon.getClientGrant>).

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
  Поиск URI клиентского приложения
  (подробнее <pkg_OAuthCommon.findClientUri>).

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
  Создает пользовательскую сессию
  (подробнее <pkg_OAuthCommon.createSession>).

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
  Обновляет пользовательскую сессию
  (подробнее <pkg_OAuthCommon.updateSession>).

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
  Блокирует пользовательскую сессию
  (подробнее <pkg_OAuthCommon.blockSession>).

  ( <body::blockSession>)
*/
procedure blockSession(
  sessionId integer
  , operatorId integer
);

/* pfunc: findSession
  Поиск пользовательской сессии
  (подробнее <pkg_OAuthCommon.findSession>).

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
  Возвращает ключи
  (подробнее <pkg_OAuthCommon.getKey>).

  ( <body::getKey>)
*/
function getKey(
  isExpired integer
  , operatorId integer
)
return sys_refcursor;

end pkg_OAuth;
/
