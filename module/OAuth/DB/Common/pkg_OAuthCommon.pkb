create or replace package body pkg_OAuthCommon is
/* package body: pkg_OAuthCommon::body */



/* group: Переменные */

/* ivar: logger
  Логер пакета.
*/
logger lg_logger_t := lg_logger_t.getLogger(
  moduleName    => Module_Name
  , objectName  => 'pkg_OAuthCommon'
);

/* ivar: cipherType
  Тип используемого шифра (кодировка пакета dbms_crypto).
*/
cipherType pls_integer;

/* ivar: cryptoKey
  Ключ шифрования/дешифрования.
*/
cryptoKey raw(100);



/* group: Функции */



/* group: Шифрование данных */

/* iproc: setCryptoConfig
  Устанавливает настройки шифрования, сохраняя их в переменных пакета
  <cipherType> и <cryptoKey>.
*/
procedure setCryptoConfig
is
begin
  cipherType :=
    dbms_crypto.ENCRYPT_AES256
    + dbms_crypto.CHAIN_CBC
    + dbms_crypto.PAD_PKCS5
  ;
  cryptoKey :=
    opt_option_list_t( moduleName => Module_Name)
    .getString( CryptoKey_OptSName)
  ;
  if cryptoKey is null then
    raise_application_error(
      pkg_Error.IllegalArgument
      , 'Необходимо задать ключ шифрования (настроечный параметр '
        || CryptoKey_OptSName || ').'
    );
  end if;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при определении настроек шифрования.'
      )
    , true
  );
end setCryptoConfig;

/* func: encrypt
  Возвращает зашифрованное значение.

  Параметры:
  inputString                 - входная строка

  Возврат:
  зашифрованная строка.
*/
function encrypt(
  inputString varchar2
)
return varchar2
is

  -- Зашифрованное значение
  outString varchar2(4000);

begin
  if cipherType is null then
    setCryptoConfig();
  end if;
  if inputString is not null then
    outString := rawtohex(
      dbms_crypto.encrypt(
        src   => utl_i18n.string_to_raw( inputString, 'AL32UTF8')
        , typ => cipherType
        , key => cryptoKey
      )
    );
  end if;
  return outString;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при шифровании значения.'
      )
    , true
  );
end encrypt;

/* func: decrypt
  Возвращает расшифрованное значение.

  Параметры:
  inputString                 - входная строка

  Возврат:
  расшифрованная строка.
*/
function decrypt(
  inputString varchar2
)
return varchar2
is

  -- Расшифрованное значение
  outString varchar2(4000);

begin
  if cipherType is null then
    setCryptoConfig();
  end if;
  if inputString is not null then
    outString := utl_i18n.raw_to_char(
      dbms_crypto.decrypt(
        src   => hextoraw( inputString)
        , typ => cipherType
        , key => cryptoKey
      )
      , 'AL32UTF8'
    );
  end if;
  return outString;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при расшифровке значения.'
      )
    , true
  );
end decrypt;



/* group: Клиентское приложение */

/* func: verifyClientCredentials
  Проверяет данные клиентского приложения.

  Параметры:
  clientShortName             - Краткое наименование приложения
  clientSecret                - Секретное слово приложения
                                (по умолчанию отсутствует)

  Возврат:
  Id оператора, привязанного к приложению (если привязка существует).
*/
function verifyClientCredentials(
  clientShortName varchar2
  , clientSecret varchar2 := null
)
return integer
is

  isFound integer;

  operatorId integer;

begin
  select
    count(*) as is_found
    , max(
        case when cg.grant_type is not null then
          cl.operator_id
        end
      )
      as operator_id
  into isFound, operatorId
  from
    oa_client cl
    left join oa_client_grant cg
      on cg.client_id = cl.client_id
        and cg.grant_type = ClientCredentials_GrantType
  where
    cl.client_short_name = clientShortName
    and (
      clientSecret is null
      or clientSecret is not null
        and cl.client_secret is not null
        and cl.client_secret = pkg_OAuthCommon.encrypt( clientSecret)
    )
  ;
  if isFound = 0 then
    raise_application_error(
      ClientWrong_ErrCode
      , 'Указаны неверные данные клиентского приложения ('
        || 'clientShortName="' || clientShortName || '"'
        || ').'
    );
  end if;
  return operatorId;
exception when others then
  if sqlcode = ClientWrong_ErrCode then
    raise;
  else
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при проверке данных клиентского приложения ('
          || 'clientShortName="' || clientShortName || '"'
          || ').'
        )
      , true
    );
  end if;
end verifyClientCredentials;

/* func: findClient
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

  -- Возвращаемый курсор
  rc sys_refcursor;

  -- Динамически формируемый текст запроса
  dsql dyn_dynamic_sql_t := dyn_dynamic_sql_t( '
select
  a.client_short_name
  , pkg_OAuthCommon.decrypt( a.client_secret) as client_secret
  , a.client_name
  , a.client_name_en
  , a.application_type
  , a.date_ins
  , a.create_operator_id
  , a.create_operator_name
  , a.create_operator_name_en
  , a.change_date
  , a.change_operator_id
  , a.change_operator_name
  , a.change_operator_name_en
  , a.client_operator_id
from
  (
  select
    t.client_short_name
    , t.client_secret
    , t.client_name
    , t.client_name_en
    , t.application_type
    , t.date_ins
    , t.operator_id_ins as create_operator_id
    , iop.operator_name as create_operator_name
    , iop.operator_name_en as create_operator_name_en
    , t.change_date
    , t.change_operator_id
    , cop.operator_name as change_operator_name
    , cop.operator_name_en as change_operator_name_en
    , t.operator_id as client_operator_id
  from
    oa_client t
    inner join op_operator iop
      on iop.operator_id = t.operator_id_ins
    inner join op_operator cop
      on cop.operator_id = t.change_operator_id
  where
    t.is_deleted = 0
    and (
      $(condition)
    )
  order by
    t.date_ins desc
    , t.client_id desc
  ) a
where
  $(rownumCondition)
'
  );

-- findClient
begin
  pkg_Operator.isRole(
    operatorId      => operatorId
    , roleShortName => OAViewClient_RoleSName
  );
  dsql.addCondition(
    'upper( t.client_short_name) like upper( :clientShortName)'
    , clientShortName is null
  );
  if clientName is not null and clientNameEn is not null then
    dsql.addCondition(
      '( upper( t.client_name) like upper( :clientName)'
      || ' or upper( t.client_name_en) like upper( :clientNameEn))'
    );
  else
    dsql.addCondition(
      'upper( t.client_name) like upper( :clientName)'
      , clientName is null
    );
    dsql.addCondition(
      'upper( t.client_name_en) like upper( :clientNameEn)'
      , clientNameEn is null
    );
  end if;
  dsql.useCondition( 'condition');
  dsql.addCondition( 'rownum <=', maxRowCount is null);
  dsql.useCondition( 'rownumCondition');
  open rc for
    dsql.getSqlText()
  using
    clientShortName
    , clientName
    , clientNameEn
    , maxRowCount
  ;
  return rc;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при поиске клиентского приложения ('
        || ' clientShortName="' || clientShortName || '"'
        || ', clientName="' || clientName || '"'
        || ', clientNameEn="' || clientNameEn || '"'
        || ', operatorId=' || operatorId
        || ').'
      )
    , true
  );
end findClient;

/* func: getClientGrant
  Возвращает список грантов клиентского приложения.

  Параметры:
  clientShortName             - Краткое наименование приложения
  operatorId                  - Id оператора, выполняющего операцию

  Возврат (курсор):
  client_short_name           - Краткое наименование приложения
  grant_type                  - Тип гранта

  (сортировка по grant_type)
*/
function getClientGrant(
  clientShortName varchar2
  , operatorId integer
)
return sys_refcursor
is

  -- Возвращаемый курсор
  rc sys_refcursor;

begin
  pkg_Operator.isRole(
    operatorId      => operatorId
    , roleShortName => OAViewClient_RoleSName
  );
  open rc for
    select
      cl.client_short_name
      , cg.grant_type
    from
      oa_client cl
      inner join oa_client_grant cg
        on cg.client_id = cl.client_id
    where
      cl.client_short_name = clientShortName
    order by
      cg.grant_type
  ;
  return rc;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при возврате списка грантов клиентского приложения ('
        || 'clientShortName="' || clientShortName || '"'
        || ', operatorId=' || operatorId
        || ').'
      )
    , true
  );
end getClientGrant;

/* func: getClientId
  Возвращает Id клиентского приложения либо выбрасывает исключение
  <ClientWrong_ErrCode> если приложение не найдено.

  Параметры:
  clientShortName             - Краткое наименование приложения
*/
function getClientId(
  clientShortName varchar2
)
return integer
is

  clientId integer;

begin
  select
    max( cl.client_id)
  into clientId
  from
    oa_client cl
  where
    cl.client_short_name = clientShortName
    and cl.is_deleted = 0
  ;
  if clientId is null then
    raise_application_error(
      ClientWrong_ErrCode
      , 'Указаны неверные данные клиентского приложения ('
        || 'clientShortName="' || clientShortName || '"'
        || ').'
    );
  end if;
  return clientId;
end getClientId;



/* group: URI клиентского приложения */

/* func: findClientUri
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
*/
function findClientUri(
  clientUriId integer := null
  , clientShortName varchar2 := null
  , maxRowCount integer := null
  , operatorId integer
)
return sys_refcursor
is

  -- Возвращаемый курсор
  rc sys_refcursor;

  -- Динамически формируемый текст запроса
  dsql dyn_dynamic_sql_t := dyn_dynamic_sql_t( '
select
  a.*
from
  (
  select
    t.client_uri_id
    , cl.client_short_name
    , t.client_uri
    , t.date_ins
    , t.operator_id
    , op.operator_name
    , op.operator_name_en
  from
    oa_client_uri t
    inner join oa_client cl
      on cl.client_id = t.client_id
    inner join op_operator op
      on op.operator_id = t.operator_id
  where
    $(condition)
  order by
    t.date_ins desc
    , t.client_id desc
  ) a
where
  $(rownumCondition)
'
  );

-- findClientUri
begin
  pkg_Operator.isRole(
    operatorId      => operatorId
    , roleShortName => OAViewClient_RoleSName
  );
  dsql.addCondition( 't.client_uri_id =', clientUriId is null);
  dsql.addCondition( 'cl.client_short_name =', clientShortName is null);
  dsql.useCondition( 'condition');
  dsql.addCondition( 'rownum <=', maxRowCount is null);
  dsql.useCondition( 'rownumCondition');
  open rc for
    dsql.getSqlText()
  using
    clientUriId
    , clientShortName
    , maxRowCount
  ;
  return rc;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при поиске URI клиентского приложения ('
        || 'clientUriId=' || clientUriId
        || ', clientShortName="' || clientShortName || '"'
        || ', operatorId=' || operatorId
        || ').'
      )
    , true
  );
end findClientUri;



/* group: Пользовательские сессии */

/* func: createSession
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

  Дополнительные параметры в резервной БД:
  clientId                    - Id клиентского приложения
                                (NULL если не определено (по умолчанию))

  Возврат:
  Id созданной записи
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
  , clientId integer := null
)
return integer
is

  rec oa_session%rowtype;

begin
  pkg_Operator.isRole(
    operatorId      => operatorIdIns
    , roleShortName => OACreateSession_RoleSName
  );
  rec.session_id                  := oa_session_seq.nextval;
  rec.auth_code                   := authCode;
  if clientShortName is not null then
    rec.client_id := coalesce(
      clientId
      , getClientId( clientShortName => clientShortName)
    );
  end if;
  rec.redirect_uri                := redirectUri;
  rec.operator_id                 := operatorId;
  rec.code_challenge              := codeChallenge;
  rec.access_token                := accessToken;
  rec.access_token_date_ins       := accessTokenDateIns;
  rec.access_token_date_finish    := accessTokenDateFinish;
  rec.refresh_token               := refreshToken;
  rec.refresh_token_date_ins      := refreshTokenDateIns;
  rec.refresh_token_date_finish   := refreshTokenDateFinish;
  rec.session_token               := sessionToken;
  rec.session_token_date_ins      := sessionTokenDateIns;
  rec.session_token_date_finish   := sessionTokenDateFinish;
  rec.date_ins                    := systimestamp;
  rec.operator_id_ins             := operatorIdIns;
  insert into
    oa_session
  values
    rec
  ;
  return rec.session_id;
exception when others then
  if sqlcode = ClientWrong_ErrCode then
    raise;
  else
    raise_application_error(
      coalesce(
          case
              when sqlcode = pkg_Error.ParentKeyNotFound
                    and sqlerrm like '%OA_SESSION_FK_CLIENT_URI%' then
                ClientUriWrong_ErrCode
              when sqlcode = UniqueViolated_ErrCode then
                case
                  when sqlerrm like '%OA_SESSION_UK_ACCESS_TOKEN%' then
                    AccessTokenWrong_ErrCode
                  when sqlerrm like '%OA_SESSION_UK_REFRESH_TOKEN%' then
                    RefreshTokenWrong_ErrCode
                end
            end
          , pkg_Error.ErrorStackInfo
        )
      , logger.errorStack(
          'Ошибка при создании пользовательской сессии ('
          || 'authCode="' || authCode || '"'
          || ', clientShortName="' || clientShortName || '"'
          || ', operatorId=' || operatorId
          || ', redirectUri="' || redirectUri || '"'
          || ', accessToken="' || accessToken || '"'
          || ', refreshToken="' || refreshToken || '"'
          || ', operatorIdIns=' || operatorIdIns
          || ', clientId=' || clientId
          || ', rec.client_id=' || rec.client_id
          || ').'
        )
      , true
    );
  end if;
end createSession;

/* iproc: lockSession
  Блокирует и возвращает данные записи в oa_session.

  Параметры:
  dataRec                     - Данные записи
                                (возврат)
  sessionId                   - Id пользовательской сессии
*/
procedure lockSession(
  dataRec out nocopy oa_session%rowtype
  , sessionId integer
)
is
begin
  select
    t.*
  into dataRec
  from
    oa_session t
  where
    t.session_id = sessionId
  for update nowait
  ;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при блокировке записи в oa_session ('
        || ' sessionId=' || sessionId
        || ').'
      )
    , true
  );
end lockSession;

/* proc: updateSession
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

  Дополнительные параметры в резервной БД:
  clientId                    - Id клиентского приложения
                                (NULL если не определено (по умолчанию))
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
  , clientId integer := null
)
is

  rec oa_session%rowtype;

begin
  pkg_Operator.isRole(
    operatorId      => operatorIdIns
    , roleShortName => OAEditSession_RoleSName
  );
  lockSession( rec, sessionId);
  rec.client_id :=
    case when clientShortName is not null then
      coalesce(
        clientId
        , getClientId( clientShortName => clientShortName)
      )
    end
  ;

  update
    oa_session d
  set
    d.auth_code                   = authCode
    , d.client_id                 = rec.client_id
    , d.redirect_uri              = redirectUri
    , d.operator_id               = operatorId
    , d.code_challenge            = codeChallenge
    , d.access_token              = accessToken
    , d.access_token_date_ins     = accessTokenDateIns
    , d.access_token_date_finish  = accessTokenDateFinish
    , d.refresh_token             = refreshToken
    , d.refresh_token_date_ins    = refreshTokenDateIns
    , d.refresh_token_date_finish = refreshTokenDateFinish
    , d.session_token             = sessionToken
    , d.session_token_date_ins    = sessionTokenDateIns
    , d.session_token_date_finish = sessionTokenDateFinish
    -- разблокируем сессию при редактировании
    , d.is_manual_blocked = null
    , d.date_finish = null
  where
    d.session_id = rec.session_id
  ;
exception when others then
  if sqlcode = ClientWrong_ErrCode then
    raise;
  else
    raise_application_error(
      coalesce(
          case
              when sqlcode = pkg_Error.ParentKeyNotFound
                    and sqlerrm like '%OA_SESSION_FK_CLIENT_URI%' then
                ClientUriWrong_ErrCode
              when sqlcode = UniqueViolated_ErrCode then
                case
                  when sqlerrm like '%OA_SESSION_UK_ACCESS_TOKEN%' then
                    AccessTokenWrong_ErrCode
                  when sqlerrm like '%OA_SESSION_UK_REFRESH_TOKEN%' then
                    RefreshTokenWrong_ErrCode
                end
            end
          , pkg_Error.ErrorStackInfo
        )
      , logger.errorStack(
          'Ошибка при обновлении пользовательской сессии ('
          || 'sessionId=' || sessionId
          || ', authCode="' || authCode || '"'
          || ', clientShortName="' || clientShortName || '"'
          || ', operatorId=' || operatorId
          || ', redirectUri="' || redirectUri || '"'
          || ', accessToken="' || accessToken || '"'
          || ', refreshToken="' || refreshToken || '"'
          || ', operatorIdIns=' || operatorIdIns
          || ', clientId=' || clientId
          || ', rec.client_id=' || rec.client_id
          || ').'
        )
      , true
    );
  end if;
end updateSession;

/* proc: blockSession
  Блокирует пользовательскую сессию.

  Параметры:
  sessionId                   - Id пользовательской сессии
  operatorIdIns               - Id оператора, выполняющего операцию
*/
procedure blockSession(
  sessionId integer
  , operatorId integer
)
is

  rec oa_session%rowtype;

begin
  pkg_Operator.isRole(
    operatorId      => operatorId
    , roleShortName => OAEditSession_RoleSName
  );
  lockSession( rec, sessionId);
  if rec.is_manual_blocked is null then
    update
      oa_session d
    set
      d.is_manual_blocked = 1
      , d.date_finish = systimestamp
    where
      d.session_id = rec.session_id
    ;
  end if;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при блокировке пользовательской сессии ('
        || 'sessionId=' || sessionId
        || ', operatorId=' || operatorId
        || ').'
      )
    , true
  );
end blockSession;

/* func: findSession
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

  -- Возвращаемый курсор
  rc sys_refcursor;

  -- Динамически формируемый текст запроса
  dsql dyn_dynamic_sql_t := dyn_dynamic_sql_t( '
select
  a.*
from
  (
  select
    t.session_id
    , t.auth_code
    , cl.client_short_name
    , cl.client_name
    , cl.client_name_en
    , t.redirect_uri
    , t.operator_id
    , op.operator_name
    , op.login as operator_login
    , t.code_challenge
    , t.access_token
    , t.access_token_date_ins
    , t.access_token_date_finish
    , t.refresh_token
    , t.refresh_token_date_ins
    , t.refresh_token_date_finish
    , t.session_token
    , t.session_token_date_ins
    , t.session_token_date_finish
    , t.date_ins
    , t.operator_id_ins
  from
    v_oa_session t
    inner join oa_client cl
      on cl.client_id = t.client_id
    left join op_operator op
      on op.operator_id = t.operator_id
  where
    t.is_blocked = 0
    and (
      $(condition)
    )
  order by
    t.date_ins desc
    , t.session_id desc
  ) a
where
  $(rownumCondition)
'
  );

-- findSession
begin
  pkg_Operator.isRole(
    operatorId      => operatorIdIns
    , roleShortName => OAViewSession_RoleSName
  );
  dsql.addCondition( 't.session_id =', sessionId is null);
  dsql.addCondition( 't.auth_code =', authCode is null);
  dsql.addCondition(
    'upper( cl.client_short_name) like upper( :clientShortName)'
    , clientShortName is null
  );
  dsql.addCondition(
    'upper( t.redirect_uri) like upper( :redirectUri)'
    , redirectUri is null
  );
  dsql.addCondition( 't.operator_id =', operatorId is null);
  dsql.addCondition( 't.code_challenge =', codeChallenge is null);
  dsql.addCondition( 't.access_token =', accessToken is null);
  dsql.addCondition( 't.refresh_token =', refreshToken is null);
  dsql.addCondition( 't.session_token =', sessionToken is null);
  dsql.useCondition( 'condition');
  dsql.addCondition( 'rownum <=', maxRowCount is null);
  dsql.useCondition( 'rownumCondition');
  open rc for
    dsql.getSqlText()
  using
    sessionId
    , authCode
    , clientShortName
    , redirectUri
    , operatorId
    , codeChallenge
    , accessToken
    , refreshToken
    , sessionToken
    , maxRowCount
  ;
  return rc;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при поиске пользовательской сессии ('
        || 'sessionId=' || sessionId
        || ', clientShortName="' || clientShortName || '"'
        || ', operatorId=' || operatorId
        || ', operatorIdIns=' || operatorIdIns
        || ').'
      )
    , true
  );
end findSession;



/* group: Хранилище ключей */

/* func: getKey
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
*/
function getKey(
  isExpired integer
  , operatorId integer
)
return sys_refcursor
is

  -- Возвращаемый курсор
  rc sys_refcursor;

begin
  pkg_Operator.isRole(
    operatorId      => operatorId
    , roleShortName => OAViewKey_RoleSName
  );
  open rc for
    select
      t.key_id
      , t.public_key
      , t.private_key
      , t.is_expired
      , t.date_ins
    from
      (
      select
        k.*
        , case when
              k.date_ins < systimestamp - INTERVAL '3' MONTH
            then 1
          end
          as is_expired
      from
        oa_key k
      where
        k.is_actual = 1
      ) t
    where
      nullif( isExpired, t.is_expired) is null
    order by
      t.date_ins desc
      , t.key_id desc
  ;
  return rc;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при возврате ключей ('
        || 'isExpired=' || isExpired
        || ', operatorId=' || operatorId
        || ').'
      )
    , true
  );
end getKey;

end pkg_OAuthCommon;
/
