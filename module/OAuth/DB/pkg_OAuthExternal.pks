create or replace package pkg_OAuthExternal is
/* package: pkg_OAuthExternal
  Внешние функции модуля OAuth, для вызова из других модулей.

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



/* group: Пользовательские сессии */


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


end pkg_OAuthExternal;
/
