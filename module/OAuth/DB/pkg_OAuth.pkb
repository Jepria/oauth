create or replace package body pkg_OAuth is
/* package body: pkg_OAuth::body */



/* group: Константы */



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




/* group: Переменные */

/* ivar: logger
  Логер пакета.
*/
logger lg_logger_t := lg_logger_t.getLogger(
  moduleName    => pkg_OAuthCommon.Module_Name
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
        pkg_OAuthCommon.IllegalArgument_ErrCode
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
      pkg_OAuthCommon.AuthorizationCode_GrantType
      , pkg_OAuthCommon.Implicit_GrantType
      , pkg_OAuthCommon.ClientCredentials_GrantType
      , pkg_OAuthCommon.Password_GrantType
      , pkg_OAuthCommon.RefreshToken_GrantType
    );
    if badGrantTab is not null and badGrantTab.count() > 0 then
      raise_application_error(
        pkg_OAuthCommon.IllegalArgument_ErrCode
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
  loginModuleUri              - URI логин модуля
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
  , loginModuleUri varchar2
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
      rec.client_secret := pkg_OAuthCommon.encrypt(
        rawtohex( dbms_crypto.randomBytes( 32))
      );
    end if;
    rec.client_name               := clientName;
    rec.client_name_en            := clientNameEn;
    rec.application_type          := applicationType;
    rec.date_ins                  := curTime;
    rec.operator_id_ins           := operatorId;
    rec.login_module_uri          := loginModuleUri;
    rec.change_date               := curTime;
    rec.change_operator_id        := operatorId;
    if applicationType in ( Web_AppType, Service_AppType)
          and pkg_OAuthCommon.ClientCredentials_GrantType member of grantTypeTab
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
    , roleShortName => pkg_OAuthCommon.OACreateClient_RoleSName
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
  if sqlcode = pkg_OAuthCommon.IllegalArgument_ErrCode then
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
          || ', loginModuleUri="' || loginModuleUri || '"'
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
  loginModuleUri              - URI логин модуля
  operatorId                  - Id оператора, выполняющего операцию
*/
procedure updateClient(
  clientShortName varchar2
  , clientName varchar2
  , clientNameEn varchar2
  , applicationType varchar2
  , grantTypeList varchar2
  , roleShortNameList varchar2
  , loginModuleUri varchar2
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
        rec.client_secret := pkg_OAuthCommon.encrypt(
          rawtohex( dbms_crypto.randomBytes( 32))
        );
      end if;
    else
      rec.client_secret := null;
    end if;
    rec.client_name               := clientName;
    rec.client_name_en            := clientNameEn;
    rec.application_type          := applicationType;
    rec.login_module_uri          := loginModuleUri;
    rec.change_date               := curTime;
    rec.change_operator_id        := operatorId;
    if applicationType in ( Web_AppType, Service_AppType)
          and pkg_OAuthCommon.ClientCredentials_GrantType member of grantTypeTab
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
          or pkg_OAuthCommon.ClientCredentials_GrantType
              not member of grantTypeTab
        )
        and pkg_OAuthCommon.ClientCredentials_GrantType
              member of oldGrantTypeTab
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
    , roleShortName => pkg_OAuthCommon.OAEditClient_RoleSName
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
  if sqlcode = pkg_OAuthCommon.IllegalArgument_ErrCode then
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
    , roleShortName => pkg_OAuthCommon.OADeleteClient_RoleSName
  );
  lockClient(
    rec
    , grantTypeTab    => oldGrantTypeTab
    , clientShortName => clientShortName
  );
  if rec.operator_id is not null
        and pkg_OAuthCommon.ClientCredentials_GrantType
            member of oldGrantTypeTab
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
  Проверяет данные клиентского приложения
  (подробнее <pkg_OAuthCommon.verifyClientCredentials>).
*/
function verifyClientCredentials(
  clientShortName varchar2
  , clientSecret varchar2 := null
)
return integer
is
begin
  return
    pkg_OAuthCommon.verifyClientCredentials(
      clientShortName   => clientShortName
      , clientSecret    => clientSecret
    )
  ;
end verifyClientCredentials;

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
    , roleShortName => pkg_OAuthCommon.OACreateClient_RoleSName
  );
  rec.client_id     :=
    pkg_OAuthCommon.getClientId( clientShortName => clientShortName)
  ;
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
  if sqlcode = pkg_OAuthCommon.ClientWrong_ErrCode then
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
    , roleShortName => pkg_OAuthCommon.OAEditClient_RoleSName
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
begin
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
    )
  ;
end createSession;

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
begin
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
    , roleShortName => pkg_OAuthCommon.OAUpdateKey_RoleSName
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
