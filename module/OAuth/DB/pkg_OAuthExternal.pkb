create or replace package body pkg_OAuthExternal is
/* package body: pkg_OAuthExternal::body */



/* group: Переменные */

/* ivar: logger
  Логер пакета.
*/
logger lg_logger_t := lg_logger_t.getLogger(
  moduleName    => pkg_OAuthCommon.Module_Name
  , objectName  => 'pkg_OAuthExternal'
);



/* group: Функции */



/* group: Клиентское приложение */


/* func: findClient
  Поиск клиентского приложения
  (подробнее <pkg_OAuth.findClient>).
*/
function findClient(
  clientShortName varchar2 := null
  , clientName varchar2 := null
  , clientNameEn varchar2 := null
  , maxRowCount integer := null
  , operatorId integer
)
return sys_refcursor
is
begin
  return
    pkg_OAuth.findClient(
      clientShortName         => clientShortName
      , clientName            => clientName
      , clientNameEn          => clientNameEn
      , maxRowCount           => maxRowCount
      , operatorId            => operatorId
    )
  ;
end findClient;



/* group: Пользовательские сессии */


/* func: findSession
  Поиск пользовательской сессии
  (подробнее <pkg_OAuth.findSession>).
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
return sys_refcursor
is
begin
  return
    pkg_OAuth.findSession(
      sessionId               => sessionId
      , authCode              => authCode
      , clientShortName       => clientShortName
      , redirectUri           => redirectUri
      , operatorId            => operatorId
      , codeChallenge         => codeChallenge
      , accessToken           => accessToken
      , refreshToken          => refreshToken
      , sessionToken          => sessionToken
      , maxRowCount           => maxRowCount
      , operatorIdIns         => operatorIdIns
    )
  ;
end findSession;


end pkg_OAuthExternal;
/
