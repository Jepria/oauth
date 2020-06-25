create or replace package body pkg_OAuth is
/* package body: pkg_OAuth::body */



/* group: Константы */



/* group: Коды ошибок */

/* iconst: UniqueViolated_ErrCode
  Код ошибки нарушения уникальности.
*/
UniqueViolated_ErrCode constant pls_integer := -1;

/* iconst: IllegalArgument_ErrCode
  Код ошибки "Указано некорректное значение входного параметра".
*/
IllegalArgument_ErrCode constant pls_integer := -20002;

/* iconst: ClientWrong_ErrCode
  Код ошибки "Указаны неверные данные клиентского приложения".
*/
ClientWrong_ErrCode constant pls_integer := -20003;

/* iconst: ClientUriWrong_ErrCode
  Код ошибки "Указан несуществующий URI клиентского приложения".
*/
ClientUriWrong_ErrCode constant pls_integer := -20004;

/* iconst: AccessTokenWrong_ErrCode
  Код ошибки "Нарушено ограничение уникальности accessToken".
*/
AccessTokenWrong_ErrCode constant pls_integer := -20005;

/* iconst: RefreshTokenWrong_ErrCode
  Код ошибки "Нарушено ограничение уникальности refreshToken".
*/
RefreshTokenWrong_ErrCode constant pls_integer := -20006;



/* group: Тип клиентского приложения */

/* iconst: Browser_AppType
  Тип приложения "browser".
*/
Browser_AppType constant varchar2(20) := 'browser';

/* iconst: Web_AppType
  Тип приложения "web".
*/
Web_AppType constant varchar2(20) := 'web';

/* iconst: Service_AppType
  Тип приложения "service".
*/
Service_AppType constant varchar2(20) := 'service';

/* iconst: Native_AppType
  Тип приложения "native".
*/
Native_AppType constant varchar2(20) := 'native';




/* group: Тип гранта */

/* iconst: AuthorizationCode_GrantType
  Тип гранта "authorization_code".
*/
AuthorizationCode_GrantType constant varchar2(20) := 'authorization_code';

/* iconst: Implicit_GrantType
  Тип гранта "implicit".
*/
Implicit_GrantType constant varchar2(20) := 'implicit';

/* iconst: ClientCredentials_GrantType
  Тип гранта "client_credentials".
*/
ClientCredentials_GrantType constant varchar2(20) := 'client_credentials';

/* iconst: Password_GrantType
  Тип гранта "password".
*/
Password_GrantType constant varchar2(20) := 'password';

/* iconst: RefreshToken_GrantType
  Тип гранта "refresh_token".
*/
RefreshToken_GrantType constant varchar2(20) := 'refresh_token';



/* group: Переменные */

/* ivar: logger
  Логер пакета.
*/
logger lg_logger_t := lg_logger_t.getLogger(
  moduleName    => Module_Name
  , objectName  => 'pkg_OAuth'
);



/* group: Функции */



/* group: Взаимодействие с AccessOperator */

/* iproc: addOperatorRole
  Добавляет роли для оператора.

  Параметры:
  operatorId                  - Id оператора
  roleShortNameList           - Список ролей из op_role через разделитель ","
  operatorIdIns               - Id оператора, выполняющего операцию
*/
procedure addOperatorRole(
  operatorId integer
  , roleShortNameList varchar2
  , operatorIdIns integer
)
is

  cursor roleCur is
    select
      a.role_short_name
      , rl.role_id
    from
      (
      select
        t.column_value as role_short_name
      from
        pkg_Common.split( roleShortNameList, delimiter => ',') t
      ) a
      left join v_op_role rl
        on rl.role_short_name = a.role_short_name
    where
      not exists
        (
        select
          null
        from
          op_operator_role opr
        where
          opr.operator_id = operatorId
          and opr.role_id = rl.role_id
        )
    order by
      1
  ;

-- addOperatorRole
begin
  for rlr in roleCur loop
    if rlr.role_id is null then
      raise_application_error(
        pkg_Error.IllegalArgument
        , 'Роль не найдена (role_short_name="' || rlr.role_short_name || '").'
      );
    end if;
    pkg_AccessOperator.createOperatorRole(
      operatorId            => operatorId
      , roleId              => rlr.role_id
      , operatorIdIns	      => operatorIdIns
    );
  end loop;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при добавлении ролей оператору ('
        || 'operatorId=' || operatorId
        || ', roleShortNameList="' || roleShortNameList || '"'
        || ', operatorIdIns=' || operatorIdIns
        || ').'
      )
    , true
  );
end addOperatorRole;

/* ifunc: createOperator
  Создает оператора для клиентского приложения.

  Параметры:
  clientShortName             - Краткое наименование приложения
  clientName                  - Наименование клиентского приложения на языке
                                по умолчанию
  clientNameEn                - Наименование клиентского приложения на
                                английском языке
  roleShortNameList           - Список ролей из op_role через разделитель ","
  operatorIdIns               - Id оператора, выполняющего операцию
*/
function createOperator(
  clientShortName varchar2
  , clientName varchar2
  , clientNameEn varchar2
  , roleShortNameList varchar2
  , operatorIdIns integer
)
return integer
is

  operatorId integer;

begin
  operatorId := pkg_AccessOperator.createOperator(
    operatorName            => clientName
    , operatorNameEn        => clientNameEn
    , login                 => clientShortName
    , password              => rawtohex( dbms_crypto.randomBytes( 25))
    , changePassword        => 0
    , operatorIdIns         => operatorIdIns
    , operatorComment       =>
        'OAuth: Оператор для клиентского приложения (таблица oa_client)'
  );
  if roleShortNameList is not null then
    addOperatorRole(
      operatorId            => operatorId
      , roleShortNameList   => roleShortNameList
      , operatorIdIns	      => operatorIdIns
    );
  end if;
  return operatorId;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при создании оператора для клиентского приложения ('
        || 'clientShortName="' || clientShortName || '"'
        || ', clientName="' || clientName || '"'
        || ', roleShortNameList="' || roleShortNameList || '"'
        || ').'
      )
    , true
  );
end createOperator;

/* iproc: updateOperator
  Обновляет оператора для клиентского приложения.

  Параметры:
  operatorId                  - Id оператора
  clientName                  - Наименование клиентского приложения на языке
                                по умолчанию
  clientNameEn                - Наименование клиентского приложения на
                                английском языке
  roleShortNameList           - Список ролей из op_role через разделитель ","
  operatorIdIns               - Id оператора, выполняющего операцию
*/
procedure updateOperator(
  operatorId integer
  , clientName varchar2
  , clientNameEn varchar2
  , roleShortNameList varchar2
  , operatorIdIns integer
)
is

  rec op_operator%rowtype;



  /*
    Блокирует и возвращает данные оператора.
  */
  procedure lockOperator
  is
  begin
    select
      t.*
    into rec
    from
      op_operator t
    where
      t.operator_id = operatorId
    for update nowait;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при блокировке оператора.'
        )
      , true
    );
  end lockOperator;


  /*
    Обновляет роли оператора.
  */
  procedure refreshOperatorRole
  is

    cursor delRoleCur is
      select
        opr.role_id
      from
        op_operator_role opr
      where
        opr.operator_id = operatorId
      minus
      select
        rl.role_id
      from
        pkg_Common.split( roleShortNameList, delimiter => ',') t
        inner join v_op_role rl
          on rl.role_short_name = t.column_value
      order by
        1
    ;

  begin
    for rlr in delRoleCur loop
      pkg_AccessOperator.deleteOperatorRole(
        operatorId            => operatorId
        , roleId              => rlr.role_id
        , operatorIdIns	      => operatorIdIns
      );
    end loop;
    if roleShortNameList is not null then
      addOperatorRole(
        operatorId            => operatorId
        , roleShortNameList   => roleShortNameList
        , operatorIdIns	      => operatorIdIns
      );
    end if;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при обновлении ролей оператора.'
        )
      , true
    );
  end refreshOperatorRole;



-- updateOperator
begin
  lockOperator();
  if rec.date_finish is not null then
    pkg_AccessOperator.restoreOperator(
      operatorId              => operatorId
      , operatorIdIns         => operatorIdIns
    );
  end if;
  if rec.operator_name = clientName
        and rec.operator_name_en = clientNameEn
      then
    null;
  else
    pkg_AccessOperator.updateOperator(
      operatorId              => operatorId
      , operatorName          => clientName
      , operatorNameEn        => clientNameEn
      , login                 => rec.login
        -- не меняем пароль
      , password              => null
      , changePassword        => 0
      , operatorIdIns         => operatorIdIns
      , operatorComment       =>
          'OAuth: Оператор для клиентского приложения (таблица oa_client)'
        -- указываем, т.к. если не указать возникает ошибка
      , loginAttemptGroupId   => rec.login_attempt_group_id
    );
  end if;
  refreshOperatorRole();
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при обновлении оператора для клиентского приложения ('
        || 'operatorId=' || operatorId
        || ', clientName="' || clientName || '"'
        || ', roleShortNameList="' || roleShortNameList || '"'
        || ').'
      )
    , true
  );
end updateOperator;

/* iproc: deleteOperator
  Блокирует оператора для клиентского приложения.

  Параметры:
  operatorId                  - Id оператора
  operatorIdIns               - Id оператора, выполняющего операцию
*/
procedure deleteOperator(
  operatorId integer
  , operatorIdIns integer
)
is
begin
  pkg_AccessOperator.deleteOperator(
    operatorId      => operatorId
    , operatorIdIns => operatorIdIns
    , operatorComment   =>
        'OAuth: Оператор для клиентского приложения (таблица oa_client)'
        || ' - заблокирован'
  );
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при блокировке оператора для клиентского приложения ('
        || 'operatorId=' || operatorId
        || ', operatorIdIns=' || operatorIdIns
        || ').'
      )
    , true
  );
end deleteOperator;



/* group: Клиентское приложение */

/* iproc: checkClientArgs
  Проверяет корректность входных параметров при создании или обновлении
  клиентского приложения.

  Параметры:
  grantTypeTab                - Список грантов в виде таблицы
                                (возврат)
  applicationType             - Тип клиентского приложения
  grantTypeList               - Список грантов через разделитель ","
*/
procedure checkClientArgs(
  grantTypeTab out nocopy cmn_string_table_t
  , applicationType varchar2
  , grantTypeList varchar2
)
is

  badGrantTab cmn_string_table_t;

begin
  if applicationType in (
        Browser_AppType
        , Web_AppType
        , Service_AppType
        , Native_AppType
      )
    then null;
    else
      raise_application_error(
        IllegalArgument_ErrCode
        , 'Указано некорректное значение входного параметра'
          || ' applicationType: "' || applicationType || '".'
      );
  end if;
  if grantTypeList is not null then
    select
      pkg_Common.split( grantTypeList, delimiter => ',')
    into grantTypeTab
    from
      dual
    ;
    badGrantTab := grantTypeTab multiset except cmn_string_table_t(
      AuthorizationCode_GrantType
      , Implicit_GrantType
      , ClientCredentials_GrantType
      , Password_GrantType
      , RefreshToken_GrantType
    );
    if badGrantTab is not null and badGrantTab.count() > 0 then
      raise_application_error(
        IllegalArgument_ErrCode
        , 'Указано некорректное значение в параметре grantTypeList:'
          || ' "' || badGrantTab(1) || '".'
      );
    end if;
  end if;
end checkClientArgs;

/* iproc: addClientGrant
  Добавляет записи в oa_client_grant.

  Параметры:
  clientId                    - Id клиентского приложения
  grantTypeTab                - Список грантов в виде таблицы
  operatorId                  - Id оператора, выполняющего операцию
*/
procedure addClientGrant(
  clientId integer
  , grantTypeTab cmn_string_table_t
  , operatorId integer
)
is
begin
  insert into
    oa_client_grant
  (
    client_id
    , grant_type
    , operator_id
  )
  select
    clientId as client_id
    , t.column_value as grant_type
    , operatorId as operator_id
  from
    table( grantTypeTab) t
  ;
  logger.trace( 'addClientGrant: added rows: ' || sql%rowcount);
end addClientGrant;

/* func: createClient
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
return integer
is

  -- Список грантов в виде таблицы
  grantTypeTab cmn_string_table_t;

  rec oa_client%rowtype;



  /*
    Заполняет поля записи для oa_client.
  */
  procedure fillRec
  is

    curTime oa_client.change_date%type := systimestamp;

  begin
    rec.client_id                 := oa_client_seq.nextval;
    rec.client_short_name         := clientShortName;
    if applicationType in ( Web_AppType, Service_AppType) then
      rec.client_secret := pkg_OptionCrypto.encrypt(
        rawtohex( dbms_crypto.randomBytes( 32))
      );
    end if;
    rec.client_name               := clientName;
    rec.client_name_en            := clientNameEn;
    rec.application_type          := applicationType;
    rec.date_ins                  := curTime;
    rec.operator_id_ins           := operatorId;
    rec.change_date               := curTime;
    rec.change_operator_id        := operatorId;
    if applicationType in ( Web_AppType, Service_AppType)
          and ClientCredentials_GrantType member of grantTypeTab
        then
      rec.operator_id := createOperator(
        clientShortName       => clientShortName
        , clientName          => clientName
        , clientNameEn        => clientNameEn
        , roleShortNameList   => roleShortNameList
        , operatorIdIns       => operatorId
      );
    end if;
    rec.is_deleted                := 0;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при заполнении полей записи для oa_client.'
        )
      , true
    );
  end fillRec;



-- createClient
begin
  savepoint pkg_OAuth_createClient;
  pkg_Operator.isRole(
    operatorId      => operatorId
    , roleShortName => OACreateClient_RoleSName
  );
  checkClientArgs(
    grantTypeTab        => grantTypeTab
    , applicationType   => applicationType
    , grantTypeList     => grantTypeList
  );
  fillRec();
  insert into
    oa_client
  values
    rec
  ;
  if grantTypeTab is not empty then
    addClientGrant(
      clientId        => rec.client_id
      , grantTypeTab  => grantTypeTab
      , operatorId    => operatorId
    );
  end if;
  return rec.client_id;
exception when others then
  rollback to pkg_OAuth_createClient;
  if sqlcode = IllegalArgument_ErrCode then
    raise;
  else
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при создании клиентского приложения ('
          || 'clientShortName="' || clientShortName || '"'
          || ', clientName="' || clientName || '"'
          || ', applicationType="' || applicationType || '"'
          || ', grantTypeList="' || grantTypeList || '"'
          || ', operatorId=' || operatorId
          || ').'
        )
      , true
    );
  end if;
end createClient;

/* iproc: lockClient
  Блокирует и возвращает данные записи в oa_client.

  Параметры:
  dataRec                     - Данные записи
                                (возврат)
  grantTypeTab                - Список грантов в виде таблицы
                                (возврат)
  clientShortName             - Краткое наименование приложения
*/
procedure lockClient(
  dataRec out nocopy oa_client%rowtype
  , grantTypeTab out nocopy cmn_string_table_t
  , clientShortName varchar2
)
is
begin
  select
    t.*
  into dataRec
  from
    oa_client t
  where
    t.client_short_name = clientShortName
  for update nowait
  ;
  select
    t.grant_type
  bulk collect into grantTypeTab
  from
    oa_client_grant t
  where
    t.client_id = dataRec.client_id
  ;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при блокировке записи в oa_client ('
        || ' clientShortName="' || clientShortName || '"'
        || ').'
      )
    , true
  );
end lockClient;

/* proc: updateClient
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
*/
procedure updateClient(
  clientShortName varchar2
  , clientName varchar2
  , clientNameEn varchar2
  , applicationType varchar2
  , grantTypeList varchar2
  , roleShortNameList varchar2
  , operatorId integer
)
is

  -- Список грантов в виде таблицы
  grantTypeTab cmn_string_table_t;

  -- Список текущих грантов в виде таблицы
  oldGrantTypeTab cmn_string_table_t;

  rec oa_client%rowtype;



  /*
    Заполняет поля записи для oa_client.
  */
  procedure fillRec
  is

    curTime oa_client.change_date%type := systimestamp;

    blockFlag integer;

  begin
    if applicationType in ( Web_AppType, Service_AppType) then
      if rec.client_secret is null then
        rec.client_secret := pkg_OptionCrypto.encrypt(
          rawtohex( dbms_crypto.randomBytes( 32))
        );
      end if;
    else
      rec.client_secret := null;
    end if;
    rec.client_name               := clientName;
    rec.client_name_en            := clientNameEn;
    rec.application_type          := applicationType;
    rec.change_date               := curTime;
    rec.change_operator_id        := operatorId;
    if applicationType in ( Web_AppType, Service_AppType)
          and ClientCredentials_GrantType member of grantTypeTab
        then
      if rec.operator_id is null then
        rec.operator_id := createOperator(
          clientShortName       => clientShortName
          , clientName          => clientName
          , clientNameEn        => clientNameEn
          , roleShortNameList   => roleShortNameList
          , operatorIdIns       => operatorId
        );
      else
        updateOperator(
          operatorId            => rec.operator_id
          , clientName          => clientName
          , clientNameEn        => clientNameEn
          , roleShortNameList   => roleShortNameList
          , operatorIdIns       => operatorId
        );
      end if;
    elsif rec.operator_id is not null
        and (
          grantTypeTab is null
          or ClientCredentials_GrantType not member of grantTypeTab
        )
        and ClientCredentials_GrantType member of oldGrantTypeTab
      then
        deleteOperator(
          operatorId      => rec.operator_id
          , operatorIdIns => operatorId
        );
    end if;
    rec.is_deleted                := 0;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при заполнении полей записи для oa_client.'
        )
      , true
    );
  end fillRec;



  /*
    Обновляет данные в oa_client_grant.
  */
  procedure refreshClientGrant
  is

    dTab cmn_string_table_t;

  begin
    dTab := oldGrantTypeTab multiset except grantTypeTab;
    if dTab is not empty then
      delete
        oa_client_grant d
      where
        d.client_id = rec.client_id
        and d.grant_type member of dTab
      ;
      logger.trace( 'refreshClientGrant: deleted rows: ' || sql%rowcount);
    end if;
    dTab := grantTypeTab multiset except oldGrantTypeTab;
    if dTab is not empty then
      addClientGrant(
        clientId        => rec.client_id
        , grantTypeTab  => dTab
        , operatorId    => operatorId
      );
    end if;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при обновлении oa_client_grant.'
        )
      , true
    );
  end refreshClientGrant;



-- updateClient
begin
  savepoint pkg_OAuth_updateClient;
  pkg_Operator.isRole(
    operatorId      => operatorId
    , roleShortName => OAEditClient_RoleSName
  );
  checkClientArgs(
    grantTypeTab        => grantTypeTab
    , applicationType   => applicationType
    , grantTypeList     => grantTypeList
  );
  lockClient(
    rec
    , grantTypeTab    => oldGrantTypeTab
    , clientShortName => clientShortName
  );
  fillRec();
  update
    oa_client d
  set
    d.client_secret             = rec.client_secret
    , d.client_name             = rec.client_name
    , d.client_name_en          = rec.client_name_en
    , d.application_type        = rec.application_type
    , d.change_date             = rec.change_date
    , d.change_operator_id      = rec.change_operator_id
    , d.operator_id             = rec.operator_id
    , d.is_deleted              = rec.is_deleted
  where
    d.client_id = rec.client_id
  ;
  refreshClientGrant();
exception when others then
  rollback to pkg_OAuth_updateClient;
  if sqlcode = IllegalArgument_ErrCode then
    raise;
  else
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при обновлении клиентского приложения ('
          || 'clientShortName="' || clientShortName || '"'
          || ', clientName="' || clientName || '"'
          || ', applicationType="' || applicationType || '"'
          || ', grantTypeList="' || grantTypeList || '"'
          || ', operatorId=' || operatorId
          || ').'
        )
      , true
    );
  end if;
end updateClient;

/* proc: deleteClient
  Удаляет клиентское приложение.

  Параметры:
  clientShortName             - Краткое наименование приложения
  operatorId                  - Id оператора, выполняющего операцию
*/
procedure deleteClient(
  clientShortName varchar2
  , operatorId integer
)
is

  rec oa_client%rowtype;

  -- Список текущих грантов в виде таблицы
  oldGrantTypeTab cmn_string_table_t;

begin
  savepoint pkg_OAuth_deleteClient;
  pkg_Operator.isRole(
    operatorId      => operatorId
    , roleShortName => OADeleteClient_RoleSName
  );
  lockClient(
    rec
    , grantTypeTab    => oldGrantTypeTab
    , clientShortName => clientShortName
  );
  if rec.operator_id is not null
        and ClientCredentials_GrantType member of oldGrantTypeTab
      then
    deleteOperator(
      operatorId      => rec.operator_id
      , operatorIdIns => operatorId
    );
  end if;
  update
    oa_client d
  set
    d.is_deleted = 1
  where
    d.client_id = rec.client_id
  ;
  delete
    oa_client_grant d
  where
    d.client_id = rec.client_id
  ;
exception when others then
  rollback to pkg_OAuth_deleteClient;
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при удалении клиентского приложения ('
        || 'clientShortName="' || clientShortName || '"'
        || ', operatorId=' || operatorId
        || ').'
      )
    , true
  );
end deleteClient;

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
        and cl.client_secret is null
      or clientSecret is not null
        and cl.client_secret is not null
        and cl.client_secret = pkg_OptionCrypto.encrypt( clientSecret)
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
  , pkg_OptionCrypto.decrypt( a.client_secret) as client_secret
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

/* func: getRoles
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

*/
function getRoles(
  roleName varchar2 := null
  , roleNameEn varchar2 := null
  , maxRowCount integer := null
  , operatorId integer
)
return sys_refcursor
is
begin
  return
    pkg_AccessOperator.findRole(
      roleName        => roleName
      , roleNameEn    => roleNameEn
      , rowCount      => maxRowCount
      , operatorId    => operatorId
    )
  ;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при возврате списка ролей ('
        || 'roleName="' || roleName || '"'
        || ', roleNameEn="' || roleNameEn || '"'
        || ', maxRowCount=' || maxRowCount
        || ', operatorId=' || operatorId
        || ').'
      )
    , true
  );
end getRoles;

/* ifunc: getClientId
  Возвращает Id клиентского приложения либо выбрасывает исключение
  ClientWrong_ErrCode если приложение не найдено.

  Параметры:
  clientShortName             - Краткое наименование приложения
  notFoundErrorCode           - Код ошибки, выбрасываемой если запись не найдена
                                (по умолчанию ClientWrong_ErrCode)
*/
function getClientId(
  clientShortName varchar2
  , notFoundErrorCode integer := null
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
      coalesce( notFoundErrorCode, ClientWrong_ErrCode)
      , 'Указаны неверные данные клиентского приложения ('
        || 'clientShortName="' || clientShortName || '"'
        || ').'
    );
  end if;
  return clientId;
end getClientId;



/* group: URI клиентского приложения */

/* func: createClientUri
  Добавляет URI в whitelist клиентского приложения.

  Параметры:
  clientShortName             - Краткое наименование приложения
  clientUri                   - URI клиентского приложения
  operatorId                  - Id оператора, выполняющего операцию

  Возврат:
  Id созданной записи.
*/
function createClientUri(
  clientShortName varchar2
  , clientUri varchar2
  , operatorId integer
)
return integer
is

  -- Данные создаваемой записи
  rec oa_client_uri%rowtype;

begin
  pkg_Operator.isRole(
    operatorId      => operatorId
    , roleShortName => OACreateClient_RoleSName
  );
  rec.client_id     := getClientId( clientShortName => clientShortName);
  rec.client_uri_id := oa_client_uri_seq.nextval;
  rec.client_uri    := clientUri;
  rec.date_ins      := systimestamp;
  rec.operator_id   := operatorId;
  insert into
    oa_client_uri
  values
    rec
  ;
  return rec.client_uri_id;
exception when others then
  if sqlcode = ClientWrong_ErrCode then
    raise;
  else
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при добавлении URI клиентского приложения ('
          || 'clientShortName="' || clientShortName || '"'
          || ', clientUri="' || clientUri || '"'
          || ', operatorId=' || operatorId
          || ').'
        )
      , true
    );
  end if;
end createClientUri;

/* proc: deleteClientUri
  Удаляет URI из whitelist клиентского приложения.

  Параметры:
  clientUriId                 - Id записи с URI клиентского приложения
  operatorId                  - Id оператора, выполняющего операцию
*/
procedure deleteClientUri(
  clientUriId integer
  , operatorId integer
)
is
begin
  pkg_Operator.isRole(
    operatorId      => operatorId
    , roleShortName => OAEditClient_RoleSName
  );
  delete
    oa_client_uri d
  where
    d.client_uri_id = clientUriId
  ;
  if sql%rowcount = 0 then
    raise_application_error(
      pkg_Error.IllegalArgument
      , 'Запись не найдена.'
    );
  end if;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при удалении URI клиентского приложения ('
        || 'clientUriId=' || clientUriId
        || ', operatorId=' || operatorId
        || ').'
      )
    , true
  );
end deleteClientUri;

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
  rec.client_id := getClientId(
    clientShortName     => clientShortName
    , notFoundErrorCode => ClientUriWrong_ErrCode
  );
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
  if sqlcode = ClientUriWrong_ErrCode then
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
          || ', client_id=' || rec.client_id
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

  rec oa_session%rowtype;

begin
  pkg_Operator.isRole(
    operatorId      => operatorIdIns
    , roleShortName => OAEditSession_RoleSName
  );
  lockSession( rec, sessionId);
  rec.client_id := getClientId(
    clientShortName     => clientShortName
    , notFoundErrorCode => ClientUriWrong_ErrCode
  );

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
  if sqlcode = ClientUriWrong_ErrCode then
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
          || ', client_id=' || rec.client_id
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
  redirect_uri                - URI для перенаправления
  operator_id                 - Id пользователя, владельца сессии
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
    , t.redirect_uri
    , t.operator_id
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

/* func: setKey
  Устанавливает актуальные ключи.

  Параметры:
  publicKey                   - Публичный ключ
  privateKey                  - Приватный ключ
  operatorId                  - Id оператора, выполняющего операцию

  Возврат:
  Id созданной записи
*/
function setKey(
  publicKey varchar2
  , privateKey varchar2
  , operatorId integer
)
return integer
is

  rec oa_key%rowtype;

begin
  pkg_Operator.isRole(
    operatorId      => operatorId
    , roleShortName => OAUpdateKey_RoleSName
  );
  lock table oa_key in exclusive mode nowait;
  update
    oa_key d
  set
    d.is_actual = 0
  where
    d.is_actual = 1
  ;
  rec.key_id            := oa_key_seq.nextval;
  rec.public_key        := publicKey;
  rec.private_key       := privateKey;
  rec.date_ins          := systimestamp;
  rec.is_actual         := 1;
  rec.operator_id_ins   := operatorId;
  insert into
    oa_key
  values
    rec
  ;
  return rec.key_id;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при обновлении ключей ('
        || 'operatorId=' || operatorId
        || ').'
      )
    , true
  );
end setKey;

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

end pkg_OAuth;
/
