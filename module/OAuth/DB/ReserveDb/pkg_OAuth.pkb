create or replace package body pkg_OAuth is
/* package body: pkg_OAuth( ReserveDb)::body */



/* group: Константы */



/* group: Переменные */

/* ivar: logger
  Логер пакета.
*/
logger lg_logger_t := lg_logger_t.getLogger(
  moduleName    => pkg_OAuthCommon.Module_Name
  , objectName  => 'pkg_OAuth'
);



/* group: Функции */



/* group: Клиентское приложение */

/* func: findClient
  Поиск клиентского приложения
  (подробнее <pkg_OAuthCommon.findClient>).
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
    pkg_OAuthCommon.findClient(
      clientShortName         => clientShortName
      , clientName            => clientName
      , clientNameEn          => clientNameEn
      , maxRowCount           => maxRowCount
      , operatorId            => operatorId
    )
  ;
end findClient;

/* func: getClientGrant
  Возвращает список грантов клиентского приложения
  (подробнее <pkg_OAuthCommon.getClientGrant>).
*/
function getClientGrant(
  clientShortName varchar2
  , operatorId integer
)
return sys_refcursor
is
begin
  return
    pkg_OAuthCommon.getClientGrant(
      clientShortName         => clientShortName
      , operatorId            => operatorId
    )
  ;
end getClientGrant;



/* group: URI клиентского приложения */

/* func: findClientUri
  Поиск URI клиентского приложения
  (подробнее <pkg_OAuthCommon.findClientUri>).
*/
function findClientUri(
  clientUriId integer := null
  , clientShortName varchar2 := null
  , maxRowCount integer := null
  , operatorId integer
)
return sys_refcursor
is
begin
  return
    pkg_OAuthCommon.findClientUri(
      clientUriId             => clientUriId
      , clientShortName       => clientShortName
      , maxRowCount           => maxRowCount
      , operatorId            => operatorId
    )
  ;
end findClientUri;



/* group: Пользовательские сессии */

/* iproc: checkSessionData
  Проверяет данные пользовательской сессии по данным из основной БД.

  Параметры:
  clientId                    - Id клиентского приложения
                                (возврат)
  sessionId                   - Id пользовательской сессии
                                (NULL при создании новой сессии)
  authСode                    - Авторизационный код (One-Time-Password)
  clientShortName             - Краткое наименование приложения
  redirectUri                 - URI для перенаправления
  accessToken                 - Уникальный UUID токена доступа
  refreshToken                - Уникальный UUID токена обновления
*/
procedure checkSessionData(
  clientId out integer
  , sessionId integer
  , authCode varchar2
  , clientShortName varchar2
  , redirectUri varchar2
  , accessToken varchar2
  , refreshToken varchar2
)
is

  isOk integer;



  /*
    Проверяет наличие redirectUri.
  */
  procedure checkRedirectUriExists
  is
  begin
    select
      count(*) as is_ok
    into isOk
    from
      oa_client_uri cu
    where
      cu.client_id = clientId
      and cu.client_uri = redirectUri
    ;
    if isOk != 1 then
      raise_application_error(
        pkg_OAuthCommon.ClientUriWrong_ErrCode
        , 'Указан несуществующий URI клиентского приложения ('
          || 'redirectUri="' || redirectUri || '"'
          || ', clientId=' || clientId
          || ').'
      );
    end if;
  end checkRedirectUriExists;



  /*
    Проверяет уникальность authCode по данным из основной БД.
  */
  procedure checkAuthCodeUnique
  is
  begin
    select
      1 - count(*) as is_ok
    into isOk
    from
      mv_oa_session ms
    where
      ms.auth_code = authCode
      and nullif( ms.session_id, sessionId) is not null
    ;
    if isOk != 1 then
      raise_application_error(
        pkg_Error.IllegalArgument
        , 'Авторизационный код уже использовался в основной БД ('
          || 'authCode="' || authCode || '"'
          || ').'
      );
    end if;
  end checkAuthCodeUnique;



  /*
    Проверяет уникальность accessToken по данным из основной БД.
  */
  procedure checkAccessTokenUnique
  is
  begin
    select
      1 - count(*) as is_ok
    into isOk
    from
      mv_oa_session ms
    where
      ms.access_token = accessToken
      and nullif( ms.session_id, sessionId) is not null
    ;
    if isOk != 1 then
      raise_application_error(
        pkg_OAuthCommon.AccessTokenWrong_ErrCode
        , 'Токен доступа уже использовался в основной БД ('
          || 'accessToken="' || accessToken || '"'
          || ').'
      );
    end if;
  end checkAccessTokenUnique;



  /*
    Проверяет уникальность refreshToken по данным из основной БД.
  */
  procedure checkRefreshTokenUnique
  is
  begin
    select
      1 - count(*) as is_ok
    into isOk
    from
      mv_oa_session ms
    where
      ms.refresh_token = refreshToken
      and nullif( ms.session_id, sessionId) is not null
    ;
    if isOk != 1 then
      raise_application_error(
        pkg_OAuthCommon.RefreshTokenWrong_ErrCode
        , 'Токен обновления уже использовался в основной БД ('
          || 'refreshToken="' || refreshToken || '"'
          || ').'
      );
    end if;
  end checkRefreshTokenUnique;



-- checkSessionData
begin
  if clientShortName is not null then
    clientId := pkg_OAuthCommon.getClientId(
      clientShortName => clientShortName
    );
  end if;
  if redirectUri is not null then
    checkRedirectUriExists();
  end if;
  checkAuthCodeUnique();
  if accessToken is not null then
    checkAccessTokenUnique();
  end if;
  if refreshToken is not null then
    checkRefreshTokenUnique();
  end if;
end checkSessionData;

/* func: createSession
  Создает пользовательскую сессию
  (подробнее <pkg_OAuthCommon.createSession>).
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
return integer
is

  clientId integer;

begin
  begin
    pkg_Operator.isRole(
      operatorId      => operatorIdIns
      , roleShortName => pkg_OAuthCommon.OACreateSession_RoleSName
    );
    checkSessionData(
      clientId          => clientId
      , sessionId       => null
      , authCode        => authCode
      , clientShortName => clientShortName
      , redirectUri     => redirectUri
      , accessToken     => accessToken
      , refreshToken    => refreshToken
    );
  exception when others then
    if sqlcode in (
            pkg_OAuthCommon.ClientWrong_ErrCode
            , pkg_OAuthCommon.ClientUriWrong_ErrCode
            , pkg_OAuthCommon.AccessTokenWrong_ErrCode
            , pkg_OAuthCommon.RefreshTokenWrong_ErrCode
          )
        then
      raise;
    else
      raise_application_error(
        pkg_Error.ErrorStackInfo
        , logger.errorStack(
            'Ошибка при создании пользовательской сессии в резервной БД ('
            || 'authCode="' || authCode || '"'
            || ', clientShortName="' || clientShortName || '"'
            || ', operatorId=' || operatorId
            || ', redirectUri="' || redirectUri || '"'
            || ', accessToken="' || accessToken || '"'
            || ', refreshToken="' || refreshToken || '"'
            || ', operatorIdIns=' || operatorIdIns
            || ').'
          )
        , true
      );
    end if;
  end;
  return
    pkg_OAuthCommon.createSession(
      authCode                  => authCode
      , clientShortName         => clientShortName
      , redirectUri             => redirectUri
      , operatorId              => operatorId
      , codeChallenge           => codeChallenge
      , accessToken             => accessToken
      , accessTokenDateIns      => accessTokenDateIns
      , accessTokenDateFinish   => accessTokenDateFinish
      , refreshToken            => refreshToken
      , refreshTokenDateIns     => refreshTokenDateIns
      , refreshTokenDateFinish  => refreshTokenDateFinish
      , sessionToken            => sessionToken
      , sessionTokenDateIns     => sessionTokenDateIns
      , sessionTokenDateFinish  => sessionTokenDateFinish
      , operatorIdIns           => operatorIdIns
      , clientId                => clientId
    )
  ;
end createSession;

/* iproc: copySessionIfAbsent
  При отсутствии сессии в локальной таблице oa_session добавляет ее путем
  копирования данных основной БД.
*/
procedure copySessionIfAbsent(
  sessionId integer
)
is
begin
  -- если Id сессии из основной БД
  if mod( sessionId, 2) = 1 then
    insert into
      oa_session
    (
      session_id
      , auth_code
      , client_id
      , redirect_uri
      , operator_id
      , code_challenge
      , access_token
      , access_token_date_ins
      , access_token_date_finish
      , refresh_token
      , refresh_token_date_ins
      , refresh_token_date_finish
      , session_token
      , session_token_date_ins
      , session_token_date_finish
      , is_manual_blocked
      , date_finish
      , date_ins
      , operator_id_ins
    )
    select
      d.session_id
      , d.auth_code
      , d.client_id
      , d.redirect_uri
      , d.operator_id
      , d.code_challenge
      , d.access_token
      , d.access_token_date_ins
      , d.access_token_date_finish
      , d.refresh_token
      , d.refresh_token_date_ins
      , d.refresh_token_date_finish
      , d.session_token
      , d.session_token_date_ins
      , d.session_token_date_finish
      , d.is_manual_blocked
      , d.date_finish
      , d.date_ins
      , d.operator_id_ins
    from
      mv_oa_session d
    where
      d.session_id = sessionId
      and not exists
        (
        select
          null
        from
          oa_session ss
        where
          ss.session_id = d.session_id
        )
    ;
    logger.trace(
      'copySessionIfAbsent: sessionId=' || sessionId
      ||', inserted rows: ' || sql%rowcount
    );
  end if;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при копировании данных сессии в таблицу oa_session ('
        || 'sessionId=' || sessionId
        || ').'
      )
    , true
  );
end copySessionIfAbsent;

/* proc: updateSession
  Обновляет пользовательскую сессию
  (подробнее <pkg_OAuthCommon.updateSession>).
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
)
is

  clientId integer;

begin
  begin
    pkg_Operator.isRole(
      operatorId      => operatorIdIns
      , roleShortName => pkg_OAuthCommon.OAEditSession_RoleSName
    );
    checkSessionData(
      clientId          => clientId
      , sessionId       => sessionId
      , authCode        => authCode
      , clientShortName => clientShortName
      , redirectUri     => redirectUri
      , accessToken     => accessToken
      , refreshToken    => refreshToken
    );
    copySessionIfAbsent( sessionId => sessionId);
  exception when others then
    if sqlcode in (
            pkg_OAuthCommon.ClientWrong_ErrCode
            , pkg_OAuthCommon.ClientUriWrong_ErrCode
            , pkg_OAuthCommon.AccessTokenWrong_ErrCode
            , pkg_OAuthCommon.RefreshTokenWrong_ErrCode
          )
        then
      raise;
    else
      raise_application_error(
        pkg_Error.ErrorStackInfo
        , logger.errorStack(
            'Ошибка при обновлении пользовательской сессии в резервной БД ('
            || 'sessionId=' || sessionId
            || ', authCode="' || authCode || '"'
            || ', clientShortName="' || clientShortName || '"'
            || ', operatorId=' || operatorId
            || ', redirectUri="' || redirectUri || '"'
            || ', accessToken="' || accessToken || '"'
            || ', refreshToken="' || refreshToken || '"'
            || ', operatorIdIns=' || operatorIdIns
            || ').'
          )
        , true
      );
    end if;
  end;
  pkg_OAuthCommon.updateSession(
    sessionId                 => sessionId
    , authCode                => authCode
    , clientShortName         => clientShortName
    , redirectUri             => redirectUri
    , operatorId              => operatorId
    , codeChallenge           => codeChallenge
    , accessToken             => accessToken
    , accessTokenDateIns      => accessTokenDateIns
    , accessTokenDateFinish   => accessTokenDateFinish
    , refreshToken            => refreshToken
    , refreshTokenDateIns     => refreshTokenDateIns
    , refreshTokenDateFinish  => refreshTokenDateFinish
    , sessionToken            => sessionToken
    , sessionTokenDateIns     => sessionTokenDateIns
    , sessionTokenDateFinish  => sessionTokenDateFinish
    , operatorIdIns           => operatorIdIns
    , clientId                => clientId
  );
end updateSession;

/* proc: blockSession
  Блокирует пользовательскую сессию
  (подробнее <pkg_OAuthCommon.blockSession>).
*/
procedure blockSession(
  sessionId integer
  , operatorId integer
)
is
begin
  begin
    pkg_Operator.isRole(
      operatorId      => operatorId
      , roleShortName => pkg_OAuthCommon.OAEditSession_RoleSName
    );
    copySessionIfAbsent( sessionId => sessionId);
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при блокировке пользовательской сессии в резервной БД ('
          || 'sessionId=' || sessionId
          || ', operatorId=' || operatorId
          || ').'
        )
      , true
    );
  end;
  pkg_OAuthCommon.blockSession(
    sessionId     => sessionId
    , operatorId  => operatorId
  );
end blockSession;

/* func: findSession
  Поиск пользовательской сессии
  (подробнее <pkg_OAuthCommon.findSession>).
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
    pkg_OAuthCommon.findSession(
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



/* group: Хранилище ключей */

/* func: getKey
  Возвращает ключи
  (подробнее <pkg_OAuthCommon.getKey>).
*/
function getKey(
  isExpired integer
  , operatorId integer
)
return sys_refcursor
is
begin
  return
    pkg_OAuthCommon.getKey(
      isExpired               => isExpired
      , operatorId            => operatorId
    )
  ;
end getKey;

end pkg_OAuth;
/
