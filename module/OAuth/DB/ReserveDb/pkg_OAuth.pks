create or replace package pkg_OAuth is
/* package: pkg_OAuth( ReserveDb)
  Интерфейсный пакет модуля OAuth для резервной БД.

  SVN root: JEP/Module/OAuth
*/



/* group: Функции */



/* group: Клиентское приложение */

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



/* group: URI клиентского приложения */

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
