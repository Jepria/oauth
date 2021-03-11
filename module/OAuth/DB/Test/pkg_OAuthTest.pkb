create or replace package body pkg_OAuthTest is
/* package body: pkg_OAuthTest::body */



/* group: Константы */

/* iconst: None_Integer
  Число, указываемая в качестве значения параметра по умолчанию, позволяющая
  определить отсутствие явно заданного значения.
*/
None_Integer constant integer := -9582095482058325832950482954832;

/* iconst: Test_Pr
  Префикс тестовых данных (используется в client_short_name и др.)
*/
Test_Pr constant varchar2(20) := '$OAuth.Test$:';



/* group: Переменные */

/* ivar: logger
  Логер пакета.
*/
logger lg_logger_t := lg_logger_t.getLogger(
  moduleName    => pkg_OAuthCommon.Module_Name
  , objectName  => 'pkg_OAuthTest'
);



/* group: Функции */

/* proc: clearTestData
  Удаляет тестовые данные (выполняет commit).
*/
procedure clearTestData
is
begin
  delete
    oa_client t
  where
    t.client_short_name like Test_Pr || '%'
  ;
  update
    op_operator op
  set
    op.login = op.login || ':$' || to_char( op.operator_id)
    , op.operator_name = op.operator_name || ':$' || to_char( op.operator_id)
    , op.date_finish = sysdate
  where
    upper( op.login) like upper( Test_Pr || '%')
    and op.login not like '%:$' || to_char( op.operator_id)
  ;
  delete
    oa_key t
  where
    t.public_key like Test_Pr || '%'
  ;
  commit;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при очистке тестовых данных.'
      )
    , true
  );
end clearTestData;

/* proc: testUserApi
  Тестирует API функции.

  Параметры:
  testCaseNumber              - Номер проверяемого тестового случая
                                (по умолчанию без ограничений)
  saveDataFlag                - Флаг сохранения тестовых данных
                                (1 да, 0 нет ( по умолчанию))
*/
procedure testUserApi(
  testCaseNumber integer := null
  , saveDataFlag integer := null
)
is

  -- Порядковый номер проверяемого тестового случая
  checkCaseNumber integer := 0;

  -- Оператор по умолчанию для тестов
  testOperId integer := pkg_Operator.getCurrentUserId();
  testOperName op_operator.operator_name%type;
  testOperNameEn op_operator.operator_name_en%type;
  testOperLogin op_operator.login%type;

  -- Тестовые клиенты
  webClientSName oa_client.client_short_name%type;
  webClientId integer;
  webClientUri oa_client_uri.client_uri%type;
  webClientUri2 oa_client_uri.client_uri%type;



  /*
    Подготовка данных для теста.
  */
  procedure prepareTestData
  is
  begin
    select
      max( t.operator_name)
      , max( t.operator_name_en)
      , max( t.login)
    into testOperName, testOperNameEn, testOperLogin
    from
      op_operator t
    where
      t.operator_id = testOperId
    ;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при подготовке данных для теста.'
        )
      , true
    );
  end prepareTestData;



  /*
    Проверяет функции %Client.
  */
  procedure checkClientApi
  is

    -- Текущие данные тестовой записи
    lastRec oa_client%rowtype;



    /*
      Проверяет тестовый случай.
    */
    procedure checkCase(
      functionName varchar2
      , caseDescription varchar2
      , clientShortName varchar2 := null
      , clientName varchar2 := null
      , clientNameEn varchar2 := null
      , applicationType varchar2 := null
      , loginModuleUri varchar2 := null
      , grantTypeList varchar2 := null
      , roleShortNameList varchar2 := null
      , maxRowCount integer := null
      , clientSecret varchar2 := null
      , operatorId integer := testOperId
      , resultRowCount integer := null
      , resultNumber number := None_Integer
      , resultCsv clob := null
      , clientCsv clob := null
      , clientGrantCsv clob := null
      , operatorCsv clob := null
      , operatorRoleCsv clob := null
      , execErrorCode integer := null
      , execErrorMessageMask varchar2 := null
      , nextCaseUsedCount pls_integer := null
    )
    is

      -- Описание тестового случая
      cinfo varchar2(200) :=
        'CASE ' || to_char( checkCaseNumber + 1)
        || ': ' || functionName || ': ' || caseDescription || ': '
      ;

      -- Ожидается выполнение с ошибкой
      isWaitError boolean :=
        execErrorCode is not null or execErrorMessageMask is not null
      ;

      resErrorCode integer;
      resErrorMessage varchar2(32000);

      resNum integer;
      resRc sys_refcursor;

      chId integer;

      -- Данные проверяемой записи
      chRec oa_client%rowtype;



      /*
        Вполняет подстановку макросов в CSV-данные.
      */
      function replaceMacros(
        srcCsv clob
      )
      return clob
      is
      begin
        return
          replace( replace( replace( replace( replace( replace( replace(
              replace(
            srcCsv
            , '$(Test_Pr)', Test_Pr)
            , '$(testOperId)', to_char( testOperId))
            , '$(testOperName)', testOperName)
            , '$(testOperNameEn)', testOperNameEn)
            , '$(clientSecretDec)'
                , case when chRec.client_secret is not null then
                    pkg_OAuthCommon.decrypt( chRec.client_secret)
                  end
              )
            , '$(dateIns)', to_char( chRec.date_ins))
            , '$(changeDate)', to_char( chRec.change_date))
            , '$(lastRec.operator_id)', to_char( lastRec.operator_id))
        ;
      end replaceMacros;



    -- checkCase
    begin
      checkCaseNumber := checkCaseNumber + 1;
      if pkg_TestUtility.isTestFailed()
            or testCaseNumber is not null
              and testCaseNumber
                not between checkCaseNumber
                  and checkCaseNumber + coalesce( nextCaseUsedCount, 0)
          then
        return;
      end if;
      logger.info( '*** ' || cinfo);

      begin
        case functionName
          when 'createClient' then
            resNum := pkg_OAuth.createClient(
              clientShortName               => Test_Pr || clientShortName
              , clientName                  => Test_Pr || clientName
              , clientNameEn                => Test_Pr || clientNameEn
              , applicationType             => applicationType
              , loginModuleUri              => loginModuleUri
              , grantTypeList               => grantTypeList
              , roleShortNameList           => roleShortNameList
              , operatorId                  => operatorId
            );
            chId := resNum;
          when 'updateClient' then
            pkg_OAuth.updateClient(
              clientShortName               => Test_Pr || clientShortName
              , clientName                  => Test_Pr || clientName
              , clientNameEn                => Test_Pr || clientNameEn
              , applicationType             => applicationType
              , loginModuleUri              => loginModuleUri
              , grantTypeList               => grantTypeList
              , roleShortNameList           => roleShortNameList
              , operatorId                  => operatorId
            );
          when 'deleteClient' then
            pkg_OAuth.deleteClient(
              clientShortName               => Test_Pr || clientShortName
              , operatorId                  => operatorId
            );
          when 'findClient' then
            resRc := pkg_OAuth.findClient(
              clientShortName               =>
                  nullif( Test_Pr || clientShortName, Test_Pr)
              , clientName                  =>
                  nullif( Test_Pr || clientName, Test_Pr)
              , clientNameEn                =>
                  nullif( Test_Pr || clientNameEn, Test_Pr)
              , maxRowCount                 => maxRowCount
              , operatorId                  => operatorId
            );
          when 'getClientGrant' then
            resRc := pkg_OAuth.getClientGrant(
              clientShortName               =>
                  nullif( Test_Pr || clientShortName, Test_Pr)
              , operatorId                  => operatorId
            );
          when 'verifyClientCredentials' then
            resNum := pkg_OAuth.verifyClientCredentials(
              clientShortName               => Test_Pr || clientShortName
              , clientSecret                => clientSecret
            );
        end case;
        if isWaitError then
          pkg_TestUtility.failTest(
            failMessageText   =>
              cinfo || 'Успешное выполнение вместо ошибки'
          );
        end if;
      exception when others then
        if isWaitError then
          resErrorCode := sqlcode;
          resErrorMessage := logger.getErrorStack();
          if resErrorMessage not like execErrorMessageMask then
            pkg_TestUtility.compareChar(
              actualString        => resErrorMessage
              , expectedString    => execErrorMessageMask
              , failMessageText   =>
                  cinfo || 'Сообщение об ошибке не соответствует маске'
            );
          elsif execErrorCode is not null then
            pkg_TestUtility.compareChar(
              actualString        => resErrorCode
              , expectedString    => execErrorCode
              , failMessageText   =>
                  cinfo || 'Неожиданный код ошибки'
            );
          end if;
        else
          pkg_TestUtility.failTest(
            failMessageText   =>
              cinfo || 'Выполнение завершилось с ошибкой:'
              || chr(10) || logger.getErrorStack()
          );
        end if;
      end;

      -- Проверка успешного результата
      if not isWaitError and not pkg_TestUtility.isTestFailed() then
        if chId is null and clientShortName is not null then
          select
            max( t.client_id)
          into chId
          from
            oa_client t
          where
            t.client_short_name = Test_Pr || clientShortName
          ;
        end if;
        if chId is not null then
          select
            t.*
          into chRec
          from
            oa_client t
          where
            t.client_id = chId
          ;
        end if;
        if nullif( None_Integer, resultNumber) is not null then
          pkg_TestUtility.compareChar(
            actualString        => resNum
            , expectedString    => resultNumber
            , failMessageText   =>
                cinfo || 'Неожиданный результат выполнения функции'
          );
        end if;
        if resultRowCount is not null then
          pkg_TestUtility.compareRowCount(
            resRc
            , expectedRowCount => resultRowCount
            , failMessageText   =>
                cinfo || 'Неожиданное число записей в курсоре'
          );
        end if;
        if resultCsv is not null then
          pkg_TestUtility.compareQueryResult(
            resRc
            , expectedCsv       => replaceMacros( resultCsv)
            , failMessagePrefix => cinfo
          );
        end if;
        if clientCsv is not null then
          pkg_TestUtility.compareQueryResult(
            tableName           => 'oa_client'
            , idColumnName      => 'client_id'
            , tableExpression   =>
'(
select
  t.*
  , nvl2( t.client_secret, 1, 0) as is_CLIENT_SECRET
  , nvl2( t.operator_id, 1, 0) as is_OPERATOR_ID
from
  oa_client t
)'
            , filterCondition   =>
                'client_id=' || coalesce( to_char( chId), 'null')
            , expectedCsv       => replaceMacros( clientCsv)
            , failMessagePrefix => cinfo
          );
        end if;
        if clientGrantCsv is not null then
          pkg_TestUtility.compareQueryResult(
            tableName           => 'oa_client_grant'
            , idColumnName      => 'client_id'
            , filterCondition   =>
                'client_id=' || coalesce( to_char( chId), 'null')
            , orderByExpression => 'grant_type'
            , expectedCsv       => replaceMacros( clientGrantCsv)
            , failMessagePrefix => cinfo
          );
        end if;
        if operatorCsv is not null then
          pkg_TestUtility.compareQueryResult(
            tableName           => 'op_operator'
            , idColumnName      => 'operator_id'
            , tableExpression   =>
'(
select
  t.*
  , nvl2( t.date_finish, 1, 0) as is_DATE_FINISH
from
  op_operator t
where
  t.operator_id =
    (
    select
      cl.operator_id
    from
      oa_client cl
    where
      cl.client_id=' || coalesce( to_char( chId), 'null') || '
    )
)'
            , expectedCsv       => replaceMacros( operatorCsv)
            , failMessagePrefix => cinfo
          );
        end if;
        if operatorRoleCsv is not null then
          pkg_TestUtility.compareQueryResult(
            tableName           => 'op_operator_role'
            , idColumnName      => 'operator_id'
            , tableExpression   =>
'(
select
  t.operator_id
  , rl.role_short_name
from
  op_operator t
  inner join op_operator_role opr
    on opr.operator_id = t.operator_id
  inner join v_op_role rl
    on rl.role_id = opr.role_id
where
  t.operator_id =
    (
    select
      cl.operator_id
    from
      oa_client cl
    where
      cl.client_id=' || coalesce( to_char( chId), 'null') || '
    )
order by
  2
)'
            , expectedCsv       => operatorRoleCsv
            , failMessagePrefix => cinfo
          );
        end if;

        -- Обновляем после всех проверок (чтобы неявно не изменить возможно
        -- переданное по ссылке значение)
        if chRec.client_id is not null then
          lastRec := chRec;
        end if;
      end if;
    exception when others then
      raise_application_error(
        pkg_Error.ErrorStackInfo
        , logger.errorStack(
            'Ошибка при проверке тестового случая ('
            || ' caseNumber=' || checkCaseNumber
            || ', functionName="' || functionName || '"'
            || ', caseDescription="' || caseDescription || '"'
            || ').'
          )
        , true
      );
    end checkCase;



  -- checkClientApi
  begin
    checkCase(
      'createClient', 'NULL-значения параметров'
      , execErrorCode         => -20002
      , execErrorMessageMask  =>
          'ORA-20002: Указано некорректное значение входного параметра applicationType: "".%'
    );
    checkCase(
      'createClient', 'Некорректный applicationType'
      , applicationType       => 'badType'
      , execErrorCode         => -20002
      , execErrorMessageMask  =>
          'ORA-20002: Указано некорректное значение входного параметра applicationType: "badType".%'
    );
    checkCase(
      'createClient', 'Некорректный grantTypeList'
      , applicationType       => 'browser'
      , grantTypeList         => 'authorization_code,bad_type'
      , execErrorCode         => -20002
      , execErrorMessageMask  =>
          'ORA-20002: Указано некорректное значение в параметре grantTypeList: "bad_type".%'
    );
    checkCase(
      'findClient', 'нет данных'
      , clientShortName       => '%'
    );
    checkCase(
      'createClient', 'web-клиент 1'
      , clientShortName       => 'client1'
      , clientName            => 'Тестовый клиент 1'
      , clientNameEn          => 'Test client 1'
      , applicationType       => 'web'
      , loginModuleUri        => '/login'
      , grantTypeList         =>
          'authorization_code,implicit,client_credentials,password,refresh_token'
      , roleShortNameList     => 'OAViewSession,OACreateSession'
      , nextCaseUsedCount     => 99
      , clientCsv =>
'
CLIENT_SHORT_NAME          ; is_CLIENT_SECRET  ; CLIENT_NAME                  ; CLIENT_NAME_EN            ; APPLICATION_TYPE  ; OPERATOR_ID_INS   ; CHANGE_OPERATOR_ID ; is_OPERATOR_ID  ; IS_DELETED  ; LOGIN_MODULE_URI
-------------------------- ; ----------------- ; ---------------------------- ; ------------------------- ; ----------------- ; ----------------- ; ------------------ ; --------------- ; ----------- ; ----------------
$(Test_Pr)client1          ;                 1 ; $(Test_Pr)Тестовый клиент 1  ; $(Test_Pr)Test client 1   ; web               ; $(testOperId)     ;  $(testOperId)     ;               1 ;           0 ; /login  
'
      , clientGrantCsv =>
'
GRANT_TYPE           ; OPERATOR_ID
-------------------- ; -------------
authorization_code   ; $(testOperId)
client_credentials   ; $(testOperId)
implicit             ; $(testOperId)
password             ; $(testOperId)
refresh_token        ; $(testOperId)
'
      , operatorCsv =>
'
LOGIN                     ; OPERATOR_NAME                     ; OPERATOR_NAME_EN            ; is_DATE_FINISH
------------------------- ; --------------------------------- ; --------------------------- ; --------------
$(Test_Pr)client1         ; $(Test_Pr)Тестовый клиент 1       ; $(Test_Pr)Test client 1     ;              0
'
      , operatorRoleCsv =>
'
ROLE_SHORT_NAME
----------------
OACreateSession
OAViewSession
'
    );
    checkCase(
      'getClientGrant', 'client1'
      , clientShortName       => 'client1'
      , resultCsv =>
'
CLIENT_SHORT_NAME  ; GRANT_TYPE
------------------ ; --------------------
$(Test_Pr)client1  ; authorization_code
$(Test_Pr)client1  ; client_credentials
$(Test_Pr)client1  ; implicit
$(Test_Pr)client1  ; password
$(Test_Pr)client1  ; refresh_token
'
    );
    checkCase(
      'findClient', 'all args'
      , clientShortName       => 'client1'
      , clientName            => 'Тестовый клиент 1'
      , clientNameEn          => 'Test client 1'
      , maxRowCount           => 50
      , resultCsv             =>
'
CLIENT_SHORT_NAME  ; CLIENT_SECRET      ; CLIENT_NAME                  ; CLIENT_NAME_EN            ; APPLICATION_TYPE  ; DATE_INS    ; CREATE_OPERATOR_ID ; CREATE_OPERATOR_NAME  ; CREATE_OPERATOR_NAME_EN  ; CHANGE_DATE    ; CHANGE_OPERATOR_ID ; CHANGE_OPERATOR_NAME        ; CHANGE_OPERATOR_NAME_EN    ; CLIENT_OPERATOR_ID      ; LOGIN_MODULE_URI
------------------ ; ------------------ ; ---------------------------- ; ------------------------- ; ----------------- ; ----------- ; ------------------ ; --------------------- ; ------------------------ ; -------------- ; ------------------ ; --------------------------- ; -------------------------- ; ----------------------- ; ----------------
$(Test_Pr)client1  ; $(clientSecretDec) ; $(Test_Pr)Тестовый клиент 1  ; $(Test_Pr)Test client 1   ; web               ; $(dateIns)  ;      $(testOperId) ; $(testOperName)       ; $(testOperNameEn)        ; $(changeDate)  ;      $(testOperId) ; $(testOperName)             ; $(testOperNameEn)          ; $(lastRec.operator_id)  ; /login
'
    );
    checkCase(
      'findClient', 'by clientShortName'
      , clientShortName       => 'client1'
      , resultRowCount        => 1
    );
    checkCase(
      'findClient', 'by clientName'
      , clientName            => 'Тестовый клиент 1'
      , resultRowCount        => 1
    );
    checkCase(
      'findClient', 'by clientNameEn'
      , clientNameEn          => 'Test client 1'
      , resultRowCount        => 1
    );
    checkCase(
      'verifyClientCredentials', 'unknown client'
      , clientShortName       => '?unknown?'
      , clientSecret          =>
          pkg_OAuthCommon.decrypt( lastRec.client_secret)
      , execErrorCode         => -20003
      , execErrorMessageMask  =>
          'ORA-20003: Указаны неверные данные клиентского приложения (clientShortName="%").%'
    );
    checkCase(
      'verifyClientCredentials', 'web-client: no secret'
      , clientShortName       => 'client1'
      , clientSecret          => null
      , resultNumber          => lastRec.operator_id
    );
    checkCase(
      'verifyClientCredentials', 'web-client: bad secret'
      , clientShortName       => 'client1'
      , clientSecret          => lastRec.client_secret
      , execErrorCode         => -20003
    );
    checkCase(
      'verifyClientCredentials', 'web-client: good secret'
      , clientShortName       => 'client1'
      , clientSecret          =>
          pkg_OAuthCommon.decrypt( lastRec.client_secret)
      , resultNumber          => lastRec.operator_id
    );
    checkCase(
      'updateClient', 'web-клиент 1_1'
      , clientShortName       => 'client1'
      , clientName            => 'Тестовый клиент 1_1'
      , clientNameEn          => 'Test client 1_1'
      , applicationType       => 'web'
      , loginModuleUri        => '/new-login'
      , grantTypeList         =>
          'authorization_code,implicit,client_credentials'
      , roleShortNameList     => ''
      , clientCsv =>
'
CLIENT_SHORT_NAME          ; is_CLIENT_SECRET  ; CLIENT_NAME                   ; CLIENT_NAME_EN            ; APPLICATION_TYPE  ; OPERATOR_ID_INS   ; CHANGE_OPERATOR_ID ; is_OPERATOR_ID  ; IS_DELETED  ; LOGIN_MODULE_URI
-------------------------- ; ----------------- ; ----------------------------- ; ------------------------- ; ----------------- ; ----------------- ; ------------------ ; --------------- ; ----------- ; ----------------
$(Test_Pr)client1          ;                 1 ; $(Test_Pr)Тестовый клиент 1_1 ; $(Test_Pr)Test client 1_1 ; web               ; $(testOperId)     ;  $(testOperId)     ;               1 ;           0 ; /new-login
'
      , clientGrantCsv =>
'
GRANT_TYPE           ; OPERATOR_ID
-------------------- ; -------------
authorization_code   ; $(testOperId)
client_credentials   ; $(testOperId)
implicit             ; $(testOperId)
'
      , operatorCsv =>
'
LOGIN                     ; OPERATOR_NAME                     ; OPERATOR_NAME_EN            ; is_DATE_FINISH
------------------------- ; --------------------------------- ; --------------------------- ; --------------
$(Test_Pr)client1         ; $(Test_Pr)Тестовый клиент 1_1     ; $(Test_Pr)Test client 1_1   ;              0
'
      , operatorRoleCsv =>
'
ROLE_SHORT_NAME
----------------
'
    );
    checkCase(
      'updateClient', 'web-клиент 1_3: to native'
      , clientShortName       => 'client1'
      , clientName            => 'Тестовый клиент 1_3'
      , clientNameEn          => 'Test client 1_3'
      , applicationType       => 'native'
      , loginModuleUri        => '/new-login'
      , grantTypeList         =>
          'authorization_code'
      , roleShortNameList     => ''
      , clientCsv =>
'
CLIENT_SHORT_NAME          ; is_CLIENT_SECRET  ; CLIENT_NAME                   ; CLIENT_NAME_EN            ; APPLICATION_TYPE  ; OPERATOR_ID_INS   ; CHANGE_OPERATOR_ID ; is_OPERATOR_ID  ; IS_DELETED  ; LOGIN_MODULE_URI
-------------------------- ; ----------------- ; ----------------------------- ; ------------------------- ; ----------------- ; ----------------- ; ------------------ ; --------------- ; ----------- ; ----------------
$(Test_Pr)client1          ;                 0 ; $(Test_Pr)Тестовый клиент 1_3 ; $(Test_Pr)Test client 1_3 ; native            ; $(testOperId)     ;  $(testOperId)     ;               1 ;           0 ; /new-login
'
      , clientGrantCsv =>
'
GRANT_TYPE           ; OPERATOR_ID
-------------------- ; -------------
authorization_code   ; $(testOperId)
'
      , operatorCsv =>
'
LOGIN                     ; is_DATE_FINISH
------------------------- ; --------------
$(Test_Pr)client1         ;              1
'
    );
    checkCase(
      'verifyClientCredentials', 'native-client'
      , clientShortName       => 'client1'
      , clientSecret          => null
      , resultNumber          => null
    );
    checkCase(
      'updateClient', 'web-клиент 1_4'
      , clientShortName       => 'client1'
      , clientName            => 'Тестовый клиент 1_4'
      , clientNameEn          => 'Test client 1_4'
      , applicationType       => 'service'
      , loginModuleUri        => '/new-login'
      , grantTypeList         =>
          'authorization_code,client_credentials'
      , roleShortNameList     => ''
      , clientCsv =>
'
CLIENT_SHORT_NAME          ; is_CLIENT_SECRET  ; CLIENT_NAME                   ; CLIENT_NAME_EN            ; APPLICATION_TYPE  ; OPERATOR_ID_INS   ; CHANGE_OPERATOR_ID ; is_OPERATOR_ID  ; IS_DELETED  ; LOGIN_MODULE_URI
-------------------------- ; ----------------- ; ----------------------------- ; ------------------------- ; ----------------- ; ----------------- ; ------------------ ; --------------- ; ----------- ; ----------------
$(Test_Pr)client1          ;                 1 ; $(Test_Pr)Тестовый клиент 1_4 ; $(Test_Pr)Test client 1_4 ; service           ; $(testOperId)     ;  $(testOperId)     ;               1 ;           0 ; /new-login
'
      , clientGrantCsv =>
'
GRANT_TYPE           ; OPERATOR_ID
-------------------- ; -------------
authorization_code   ; $(testOperId)
client_credentials   ; $(testOperId)
'
      , operatorCsv =>
'
LOGIN                     ; OPERATOR_NAME                     ; OPERATOR_NAME_EN            ; is_DATE_FINISH
------------------------- ; --------------------------------- ; --------------------------- ; --------------
$(Test_Pr)client1         ; $(Test_Pr)Тестовый клиент 1_4     ; $(Test_Pr)Test client 1_4   ;              0
'
      , operatorRoleCsv =>
'
ROLE_SHORT_NAME
----------------
'
    );
    checkCase(
      'deleteClient', 'web-клиент 1_9'
      , clientShortName       => 'client1'
      , clientCsv =>
'
CLIENT_SHORT_NAME          ; CHANGE_OPERATOR_ID ; is_OPERATOR_ID  ; IS_DELETED
-------------------------- ; ------------------ ; --------------- ; ----------
$(Test_Pr)client1          ;  $(testOperId)     ;               1 ;          1
'
      , clientGrantCsv =>
'
GRANT_TYPE           ; OPERATOR_ID
-------------------- ; -------------
'
      , operatorCsv =>
'
LOGIN                     ; is_DATE_FINISH
------------------------- ; --------------
$(Test_Pr)client1         ;              1
'
    );
    pkg_TestUtility.compareRowCount (
      rc                    =>
          pkg_OAuth.getRoles(
            roleName      => 'OAuth: регистрация клиентских приложений'
            , roleNameEn  => 'OAuth: OACreateClient'
            , maxRowCount => 1
            , operatorId  => testOperId
          )
      , expectedRowCount    => 1
      , failMessageText     =>
          'getRoles: all args: Неожиданное число записей в курсоре'
    );

    -- Тестовые клиенты (создание)
    checkCase(
      'createClient', 'webClient'
      , clientShortName       => 'webClient'
      , clientName            => 'Тестовый web-клиент'
      , clientNameEn          => 'Test web-client'
      , applicationType       => 'web'
      , loginModuleUri        => '/login'
      , grantTypeList         =>
          'authorization_code,implicit,client_credentials,password,refresh_token'
      , roleShortNameList     => 'OAViewSession,OACreateSession'
      , nextCaseUsedCount     => 999
    );
    webClientSName := lastRec.client_short_name;
    webClientId := lastRec.client_id;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при проверке функций %Client.'
        )
      , true
    );
  end checkClientApi;



  /*
    Проверяет функции %ClientUri.
  */
  procedure checkClientUriApi
  is

    -- Текущие данные тестовой записи
    lastRec oa_client_uri%rowtype;



    /*
      Проверяет тестовый случай.
    */
    procedure checkCase(
      functionName varchar2
      , caseDescription varchar2
      , clientShortName varchar2 := null
      , clientUri varchar2 := null
      , clientUriId integer := null
      , maxRowCount integer := null
      , operatorId integer := testOperId
      , resultRowCount integer := null
      , resultNumber number := None_Integer
      , resultCsv clob := null
      , clientUriCount integer := null
      , clientUriCsv clob := null
      , execErrorCode integer := null
      , execErrorMessageMask varchar2 := null
      , nextCaseUsedCount pls_integer := null
    )
    is

      -- Описание тестового случая
      cinfo varchar2(200) :=
        'CASE ' || to_char( checkCaseNumber + 1)
        || ': ' || functionName || ': ' || caseDescription || ': '
      ;

      -- Ожидается выполнение с ошибкой
      isWaitError boolean :=
        execErrorCode is not null or execErrorMessageMask is not null
      ;

      resErrorCode integer;
      resErrorMessage varchar2(32000);

      resNum integer;
      resRc sys_refcursor;

      -- Данные проверяемой записи
      chId integer;
      chRec oa_client_uri%rowtype;



      /*
        Вполняет подстановку макросов в CSV-данные.
      */
      function replaceMacros(
        srcCsv clob
      )
      return clob
      is
      begin
        return
          replace( replace( replace( replace( replace( replace( replace( replace(
            srcCsv
            , '$(Test_Pr)', Test_Pr)
            , '$(testOperId)', to_char( testOperId))
            , '$(testOperName)', testOperName)
            , '$(testOperNameEn)', testOperNameEn)
            , '$(webClientId)', to_char( webClientId))
            , '$(webClientSName)', webClientSName)
            , '$(dateIns)', to_char( chRec.date_ins))
            , '$(clientUriId)', to_char( clientUriId))
        ;
      end replaceMacros;



    -- checkCase
    begin
      checkCaseNumber := checkCaseNumber + 1;
      if pkg_TestUtility.isTestFailed()
            or testCaseNumber is not null
              and testCaseNumber
                not between checkCaseNumber
                  and checkCaseNumber + coalesce( nextCaseUsedCount, 0)
          then
        return;
      end if;
      logger.info( '*** ' || cinfo);

      begin
        case functionName
          when 'createClientUri' then
            resNum := pkg_OAuth.createClientUri(
              clientShortName               => clientShortName
              , clientUri                   => clientUri
              , operatorId                  => operatorId
            );
            chId := resNum;
          when 'deleteClientUri' then
            pkg_OAuth.deleteClientUri(
              clientUriId                   => clientUriId
              , operatorId                  => operatorId
            );
          when 'findClientUri' then
            resRc := pkg_OAuth.findClientUri(
              clientUriId                   => clientUriId
              , clientShortName             => clientShortName
              , maxRowCount                 => maxRowCount
              , operatorId                  => operatorId
            );
        end case;
        if isWaitError then
          pkg_TestUtility.failTest(
            failMessageText   =>
              cinfo || 'Успешное выполнение вместо ошибки'
          );
        end if;
      exception when others then
        if isWaitError then
          resErrorCode := sqlcode;
          resErrorMessage := logger.getErrorStack();
          if resErrorMessage not like execErrorMessageMask then
            pkg_TestUtility.compareChar(
              actualString        => resErrorMessage
              , expectedString    => execErrorMessageMask
              , failMessageText   =>
                  cinfo || 'Сообщение об ошибке не соответствует маске'
            );
          elsif execErrorCode is not null then
            pkg_TestUtility.compareChar(
              actualString        => resErrorCode
              , expectedString    => execErrorCode
              , failMessageText   =>
                  cinfo || 'Неожиданный код ошибки'
            );
          end if;
        else
          pkg_TestUtility.failTest(
            failMessageText   =>
              cinfo || 'Выполнение завершилось с ошибкой:'
              || chr(10) || logger.getErrorStack()
          );
        end if;
      end;

      -- Проверка успешного результата
      if not isWaitError and not pkg_TestUtility.isTestFailed() then
        if chId is null and clientUriId is not null then
          select
            max( t.client_uri_id)
          into chId
          from
            oa_client_uri t
          where
            t.client_uri_id = clientUriId
          ;
        end if;
        if chId is not null then
          select
            t.*
          into chRec
          from
            oa_client_uri t
          where
            t.client_uri_id = chId
          ;
        end if;
        if nullif( None_Integer, resultNumber) is not null then
          pkg_TestUtility.compareChar(
            actualString        => resNum
            , expectedString    => resultNumber
            , failMessageText   =>
                cinfo || 'Неожиданный результат выполнения функции'
          );
        end if;
        if resultRowCount is not null then
          pkg_TestUtility.compareRowCount(
            resRc
            , expectedRowCount => resultRowCount
            , failMessageText   =>
                cinfo || 'Неожиданное число записей в курсоре'
          );
        end if;
        if resultCsv is not null then
          pkg_TestUtility.compareQueryResult(
            resRc
            , expectedCsv       => replaceMacros( resultCsv)
            , failMessagePrefix => cinfo
          );
        end if;
        if clientUriCount is not null then
          pkg_TestUtility.compareRowCount(
            tableName           => 'oa_client_uri'
            , filterCondition   =>
                'client_uri_id='
                || coalesce( to_char( clientUriId), 'null')
            , expectedRowCount  => clientUriCount
            , failMessageText   =>
                cinfo || 'Неожиданное число записей в таблице'
          );
        end if;
        if clientUriCsv is not null then
          pkg_TestUtility.compareQueryResult(
            tableName           => 'oa_client_uri'
            , idColumnName      => 'client_uri_id'
            , filterCondition   =>
                'client_uri_id=' || coalesce( to_char( chId), 'null')
            , expectedCsv       => replaceMacros( clientUriCsv)
            , failMessagePrefix => cinfo
          );
        end if;

        -- Обновляем после всех проверок (чтобы неявно не изменить возможно
        -- переданное по ссылке значение)
        if chRec.client_uri_id is not null then
          lastRec := chRec;
        end if;
      end if;
    exception when others then
      raise_application_error(
        pkg_Error.ErrorStackInfo
        , logger.errorStack(
            'Ошибка при проверке тестового случая ('
            || ' caseNumber=' || checkCaseNumber
            || ', functionName="' || functionName || '"'
            || ', caseDescription="' || caseDescription || '"'
            || ').'
          )
        , true
      );
    end checkCase;



  -- checkClientUriApi
  begin
    checkCase(
      'createClientUri', 'NULL-значения параметров'
      , execErrorCode         => -20003
      , execErrorMessageMask  =>
          'ORA-20003: Указаны неверные данные клиентского приложения (clientShortName="").%'
    );
    checkCase(
      'createClientUri', 'Некорректный clientShortName'
      , clientShortName       => Test_Pr || 'absent client'
      , execErrorCode         => -20003
      , execErrorMessageMask  =>
          'ORA-20003: Указаны неверные данные клиентского приложения (clientShortName="%absent client").%'
    );
    checkCase(
      'findClientUri', 'нет данных'
      , clientShortName       => webClientSName
    );
    checkCase(
      'createClientUri', 'webClient'
      , clientShortName       => webClientSName
      , clientUri             => 'Тестовый URI 1'
      , nextCaseUsedCount     => 99
      , clientUriCsv =>
'
CLIENT_ID       ; CLIENT_URI                   ; OPERATOR_ID
--------------- ; ---------------------------- ; --------------
$(webClientId)  ; Тестовый URI 1               ; $(testOperId)
'
    );
    checkCase(
      'findClientUri', 'all args'
      , clientUriId           => lastRec.client_uri_id
      , clientShortName       => webClientSName
      , maxRowCount           => 50
      , resultCsv             =>
'
CLIENT_URI_ID   ; CLIENT_SHORT_NAME  ; CLIENT_URI         ; DATE_INS    ; OPERATOR_ID   ; OPERATOR_NAME    ; OPERATOR_NAME_EN
--------------- ; ------------------ ; ------------------ ; ----------- ; ------------- ; ---------------- ; ------------------
$(clientUriId)  ; $(webClientSName)  ; Тестовый URI 1     ; $(dateIns)  ; $(testOperId) ; $(testOperName)  ; $(testOperNameEn)
'
    );
    checkCase(
      'findClientUri', 'by clientUriId'
      , clientUriId           => lastRec.client_uri_id
      , resultRowCount        => 1
    );
    checkCase(
      'findClientUri', 'by clientShortName'
      , clientShortName       => webClientSName
      , resultRowCount        => 1
    );
    checkCase(
      'deleteClientUri', 'Тестовый URI 1'
      , clientUriId           => lastRec.client_uri_id
      , clientUriCount        => 0
    );
    checkCase(
      'findClientUri', 'after delete'
      , clientShortName       => webClientSName
      , resultRowCount        => 0
    );

    -- Тестовые клиенты (создание)
    checkCase(
      'createClientUri', 'webClient: uri'
      , clientShortName       => webClientSName
      , clientUri             => 'web/client/uri'
      , nextCaseUsedCount     => 999
    );
    webClientUri := lastRec.client_uri;
    checkCase(
      'createClientUri', 'webClient: uri2'
      , clientShortName       => webClientSName
      , clientUri             => 'web/client/uri2'
      , nextCaseUsedCount     => 999
    );
    webClientUri2 := lastRec.client_uri;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при проверке функций %ClientUri.'
        )
      , true
    );
  end checkClientUriApi;



  /*
    Проверяет функции %Session.
  */
  procedure checkSessionApi
  is

    -- Данные для тестов
    tstRec oa_session%rowtype;

    -- Текущие данные тестовой записи
    lastRec oa_session%rowtype;



    /*
      Проверяет тестовый случай.
    */
    procedure checkCase(
      functionName varchar2
      , caseDescription varchar2
      , authCode varchar2 := null
      , clientShortName varchar2 := null
      , redirectUri varchar2 := null
      , operatorId integer := null
      , codeChallenge varchar2 := null
      , accessToken varchar2 := null
      , accessTokenDateIns timestamp with time zone := null
      , accessTokenDateFinish timestamp with time zone := null
      , refreshToken varchar2 := null
      , refreshTokenDateIns timestamp with time zone := null
      , refreshTokenDateFinish timestamp with time zone := null
      , sessionToken varchar2 := null
      , sessionTokenDateIns timestamp with time zone := null
      , sessionTokenDateFinish timestamp with time zone := null
      , operatorIdIns integer := testOperId
      , sessionId integer := null
      , maxRowCount integer := null
      , resultRowCount integer := null
      , resultNumber number := None_Integer
      , resultCsv clob := null
      , sessionCount integer := null
      , sessionCsv clob := null
      , execErrorCode integer := null
      , execErrorMessageMask varchar2 := null
      , nextCaseUsedCount pls_integer := null
    )
    is

      -- Описание тестового случая
      cinfo varchar2(200) :=
        'CASE ' || to_char( checkCaseNumber + 1)
        || ': ' || functionName || ': ' || caseDescription || ': '
      ;

      -- Ожидается выполнение с ошибкой
      isWaitError boolean :=
        execErrorCode is not null or execErrorMessageMask is not null
      ;

      resErrorCode integer;
      resErrorMessage varchar2(32000);

      resNum integer;
      resRc sys_refcursor;

      -- Данные проверяемой записи
      chId integer;
      chRec oa_session%rowtype;



      /*
        Вполняет подстановку макросов в CSV-данные.
      */
      function replaceMacros(
        srcCsv clob
      )
      return clob
      is
      begin
        return
          replace( replace( replace( replace( replace( replace( replace(
              replace( replace( replace( replace( replace( replace( replace(
              replace( replace( replace(
            srcCsv
            , '$(Test_Pr)', Test_Pr)
            , '$(testOperId)', to_char( testOperId))
            , '$(testOperName)', testOperName)
            , '$(testOperNameEn)', testOperNameEn)
            , '$(testOperLogin)', testOperLogin)
            , '$(accessTokenDateIns)' , to_char( accessTokenDateIns))
            , '$(accessTokenDateFinish)' , to_char( accessTokenDateFinish))
            , '$(refreshTokenDateIns)' , to_char( refreshTokenDateIns))
            , '$(refreshTokenDateFinish)' , to_char( refreshTokenDateFinish))
            , '$(sessionTokenDateIns)' , to_char( sessionTokenDateIns))
            , '$(sessionTokenDateFinish)' , to_char( sessionTokenDateFinish))
            , '$(webClientId)', to_char( webClientId))
            , '$(webClientSName)', webClientSName)
            , '$(webClientUri)', webClientUri)
            , '$(webClientUri2)', webClientUri2)
            , '$(dateIns)', to_char( chRec.date_ins))
            , '$(sessionId)', to_char( sessionId))
        ;
      end replaceMacros;



    -- checkCase
    begin
      checkCaseNumber := checkCaseNumber + 1;
      if pkg_TestUtility.isTestFailed()
            or testCaseNumber is not null
              and testCaseNumber
                not between checkCaseNumber
                  and checkCaseNumber + coalesce( nextCaseUsedCount, 0)
          then
        return;
      end if;
      logger.info( '*** ' || cinfo);

      begin
        case functionName
          when 'createSession' then
            resNum := pkg_OAuth.createSession(
              authCode                  =>
                  nullif( Test_Pr || authCode, Test_Pr)
              , clientShortName         => clientShortName
              , redirectUri             => redirectUri
              , operatorId              => operatorId
              , codeChallenge           => codeChallenge
              , accessToken             =>
                  nullif( Test_Pr || accessToken, Test_Pr)
              , accessTokenDateIns      => accessTokenDateIns
              , accessTokenDateFinish   => accessTokenDateFinish
              , refreshToken            =>
                  nullif( Test_Pr || refreshToken, Test_Pr)
              , refreshTokenDateIns     => refreshTokenDateIns
              , refreshTokenDateFinish  => refreshTokenDateFinish
              , sessionToken            =>
                  nullif( Test_Pr || sessionToken, Test_Pr)
              , sessionTokenDateIns     => sessionTokenDateIns
              , sessionTokenDateFinish  => sessionTokenDateFinish
              , operatorIdIns           => operatorIdIns
            );
            chId := resNum;
          when 'updateSession' then
            pkg_OAuth.updateSession(
              sessionId                 => sessionId
              , authCode                =>
                  nullif( Test_Pr || authCode, Test_Pr)
              , clientShortName         => clientShortName
              , redirectUri             => redirectUri
              , operatorId              => operatorId
              , codeChallenge           => codeChallenge
              , accessToken             =>
                  nullif( Test_Pr || accessToken, Test_Pr)
              , accessTokenDateIns      => accessTokenDateIns
              , accessTokenDateFinish   => accessTokenDateFinish
              , refreshToken            =>
                  nullif( Test_Pr || refreshToken, Test_Pr)
              , refreshTokenDateIns     => refreshTokenDateIns
              , refreshTokenDateFinish  => refreshTokenDateFinish
              , sessionToken            =>
                  nullif( Test_Pr || sessionToken, Test_Pr)
              , sessionTokenDateIns     => sessionTokenDateIns
              , sessionTokenDateFinish  => sessionTokenDateFinish
              , operatorIdIns           => operatorIdIns
            );
          when 'blockSession' then
            pkg_OAuth.blockSession(
              sessionId                     => sessionId
              , operatorId                  =>
                  coalesce( operatorId, testOperId)
            );
          when 'findSession' then
            resRc := pkg_OAuth.findSession(
              sessionId                 => sessionId
              , authCode                => authCode
              , clientShortName         => clientShortName
              , redirectUri             => redirectUri
              , operatorId              => operatorId
              , codeChallenge           => codeChallenge
              , accessToken             => accessToken
              , refreshToken            => refreshToken
              , sessionToken            => sessionToken
              , maxRowCount             => maxRowCount
              , operatorIdIns           => operatorIdIns
            );
        end case;
        if isWaitError then
          pkg_TestUtility.failTest(
            failMessageText   =>
              cinfo || 'Успешное выполнение вместо ошибки'
          );
        end if;
      exception when others then
        if isWaitError then
          resErrorCode := sqlcode;
          resErrorMessage := logger.getErrorStack();
          if resErrorMessage not like execErrorMessageMask then
            pkg_TestUtility.compareChar(
              actualString        => resErrorMessage
              , expectedString    => execErrorMessageMask
              , failMessageText   =>
                  cinfo || 'Сообщение об ошибке не соответствует маске'
            );
          elsif execErrorCode is not null then
            pkg_TestUtility.compareChar(
              actualString        => resErrorCode
              , expectedString    => execErrorCode
              , failMessageText   =>
                  cinfo || 'Неожиданный код ошибки'
            );
          end if;
        else
          pkg_TestUtility.failTest(
            failMessageText   =>
              cinfo || 'Выполнение завершилось с ошибкой:'
              || chr(10) || logger.getErrorStack()
          );
        end if;
      end;

      -- Проверка успешного результата
      if not isWaitError and not pkg_TestUtility.isTestFailed() then
        if chId is null and sessionId is not null then
          select
            max( t.session_id)
          into chId
          from
            oa_session t
          where
            t.session_id = sessionId
          ;
        end if;
        if chId is not null then
          select
            t.*
          into chRec
          from
            oa_session t
          where
            t.session_id = chId
          ;
        end if;
        if nullif( None_Integer, resultNumber) is not null then
          pkg_TestUtility.compareChar(
            actualString        => resNum
            , expectedString    => resultNumber
            , failMessageText   =>
                cinfo || 'Неожиданный результат выполнения функции'
          );
        end if;
        if resultRowCount is not null then
          pkg_TestUtility.compareRowCount(
            resRc
            , expectedRowCount => resultRowCount
            , failMessageText   =>
                cinfo || 'Неожиданное число записей в курсоре'
          );
        end if;
        if resultCsv is not null then
          pkg_TestUtility.compareQueryResult(
            resRc
            , expectedCsv       => replaceMacros( resultCsv)
            , failMessagePrefix => cinfo
          );
        end if;
        if sessionCount is not null then
          pkg_TestUtility.compareRowCount(
            tableName           => 'oa_session'
            , filterCondition   =>
                'session_id='
                || coalesce( to_char( sessionId), to_char( chId), 'null')
            , expectedRowCount  => sessionCount
            , failMessageText   =>
                cinfo || 'Неожиданное число записей в таблице'
          );
        end if;
        if sessionCsv is not null then
          pkg_TestUtility.compareQueryResult(
            tableName           => 'oa_session'
            , idColumnName      => 'session_id'
            , filterCondition   =>
                'session_id=' || coalesce( to_char( chId), 'null')
            , expectedCsv       => replaceMacros( sessionCsv)
            , failMessagePrefix => cinfo
          );
        end if;

        -- Обновляем после всех проверок (чтобы неявно не изменить возможно
        -- переданное по ссылке значение)
        if chRec.session_id is not null then
          lastRec := chRec;
        end if;
      end if;
    exception when others then
      raise_application_error(
        pkg_Error.ErrorStackInfo
        , logger.errorStack(
            'Ошибка при проверке тестового случая ('
            || ' caseNumber=' || checkCaseNumber
            || ', functionName="' || functionName || '"'
            || ', caseDescription="' || caseDescription || '"'
            || ').'
          )
        , true
      );
    end checkCase;



    /*
      Определяет тестовые значения.
    */
    procedure fillTstRec
    is

      -- На базе текущей даты, т.к. токены не должны быть просрочены
      baseDate date := trunc(sysdate) - 1;



      /*
        Устанавливает время по дате и строке с временем.
      */
      procedure setTm(
        tm out nocopy timestamp with time zone
        , dateShift number
        , timeStr varchar2
      )
      is
      begin
        tm := to_timestamp_tz(
          to_char( baseDate + dateShift, 'dd.mm.yyyy ')
            || '15:11:20.405'
            || ' +03:00'
          , 'dd.mm.yyyy hh24:mi:ss.ff tzh:tzm'
        );
      end setTm;



    -- fillTstRec
    begin
      setTm( tstRec.access_token_date_ins, 0, '15:11:20.405');
      setTm( tstRec.access_token_date_finish, 30, '17:11:20.406');
      setTm( tstRec.refresh_token_date_ins, 3, '15:11:21.407');
      setTm( tstRec.refresh_token_date_finish, 32, '17:11:22.701');
      setTm( tstRec.session_token_date_ins, 5, '15:11:23.702');
      setTm( tstRec.session_token_date_finish, 62, '17:11:24.703');
    exception when others then
      raise_application_error(
        pkg_Error.ErrorStackInfo
        , logger.errorStack(
            'Ошибка при определении тестовых значений.'
          )
        , true
      );
    end fillTstRec;



  -- checkSessionApi
  begin
    fillTstRec();
    checkCase(
      'createSession', 'NULL-значения параметров'
      , execErrorMessageMask  =>
          '%ORA-01400: cannot insert NULL into ("%"."OA_SESSION"."AUTH_CODE")%'
    );
    checkCase(
      'createSession', 'Некорректный clientShortName'
      , clientShortName       => Test_Pr || 'absent client'
      , execErrorCode         => -20003
      , execErrorMessageMask  =>
          'ORA-20003: Указаны неверные данные клиентского приложения (clientShortName="%absent client").%'
    );
    checkCase(
      'createSession', 'Некорректный URI'
      , authCode              => 'new auth_code'
      , clientShortName       => webClientSName
      , redirectUri           => 'absent uri'
      , execErrorCode         => -20004
      , execErrorMessageMask  =>
          'ORA-20004: Ошибка при создании пользовательской сессии %'
          || '%OA_SESSION_FK_CLIENT_URI%'
    );
    checkCase(
      'findSession', 'нет данных'
      , clientShortName       => webClientSName
    );
    checkCase(
      'createSession', 'all args'
      , authCode                => 'auth code'
      , clientShortName         => webClientSName
      , redirectUri             => webClientUri
      , operatorId              => testOperId
      , codeChallenge           => 'code challenge'
      , accessToken             => 'access token'
      , accessTokenDateIns      => tstRec.access_token_date_ins
      , accessTokenDateFinish   => tstRec.access_token_date_finish
      , refreshToken            => 'refresh token'
      , refreshTokenDateIns     => tstRec.refresh_token_date_ins
      , refreshTokenDateFinish  => tstRec.refresh_token_date_finish
      , sessionToken            => 'session token'
      , sessionTokenDateIns     => tstRec.session_token_date_ins
      , sessionTokenDateFinish  => tstRec.session_token_date_finish
      , nextCaseUsedCount       => 99
      , sessionCsv =>
'
AUTH_CODE               ; CLIENT_ID      ; REDIRECT_URI    ; OPERATOR_ID   ; CODE_CHALLENGE  ; ACCESS_TOKEN               ; ACCESS_TOKEN_DATE_INS            ; ACCESS_TOKEN_DATE_FINISH         ; REFRESH_TOKEN               ; REFRESH_TOKEN_DATE_INS           ; REFRESH_TOKEN_DATE_FINISH        ; SESSION_TOKEN               ; SESSION_TOKEN_DATE_INS           ; SESSION_TOKEN_DATE_FINISH        ; IS_MANUAL_BLOCKED ; DATE_FINISH  ; DATE_INS                         ; OPERATOR_ID_INS
----------------------- ; -------------- ; --------------- ; ------------- ; --------------- ; -------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; ----------------- ; ------------ ; -------------------------------- ; ---------------
$(Test_Pr)auth code     ; $(webClientId) ; web/client/uri  ; $(testOperId) ; code challenge  ; $(Test_Pr)access token     ; $(accessTokenDateIns)            ; $(accessTokenDateFinish)         ; $(Test_Pr)refresh token     ; $(refreshTokenDateIns)           ; $(refreshTokenDateFinish)        ; $(Test_Pr)session token     ; $(sessionTokenDateIns)           ; $(sessionTokenDateFinish)        ;                   ;              ; $(dateIns)                       ; $(testOperId)
'
    );
    checkCase(
      'findSession', 'all args'
      , sessionId             => lastRec.session_id
      , clientShortName       => webClientSName
      , maxRowCount           => 50
      ---- для контроля данных в курсоре
      , accessTokenDateIns      => tstRec.access_token_date_ins
      , accessTokenDateFinish   => tstRec.access_token_date_finish
      , refreshTokenDateIns     => tstRec.refresh_token_date_ins
      , refreshTokenDateFinish  => tstRec.refresh_token_date_finish
      , sessionTokenDateIns     => tstRec.session_token_date_ins
      , sessionTokenDateFinish  => tstRec.session_token_date_finish
      ----
      , resultCsv             =>
'
SESSION_ID      ; AUTH_CODE               ; CLIENT_SHORT_NAME     ; CLIENT_NAME                   ; CLIENT_NAME_EN            ; REDIRECT_URI    ; OPERATOR_ID   ; OPERATOR_NAME    ; OPERATOR_LOGIN    ; CODE_CHALLENGE  ; ACCESS_TOKEN               ; ACCESS_TOKEN_DATE_INS            ; ACCESS_TOKEN_DATE_FINISH         ; REFRESH_TOKEN               ; REFRESH_TOKEN_DATE_INS           ; REFRESH_TOKEN_DATE_FINISH        ; SESSION_TOKEN               ; SESSION_TOKEN_DATE_INS           ; SESSION_TOKEN_DATE_FINISH        ; DATE_INS                         ; OPERATOR_ID_INS
--------------- ; ----------------------- ; --------------------- ; ----------------------------- ; ------------------------- ; --------------- ; ------------- ; ---------------- ; ------------------; --------------- ; -------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; -------------------------------- ; ---------------
$(sessionId)    ; $(Test_Pr)auth code     ; $(webClientSName)     ; $(Test_Pr)Тестовый web-клиент ; $(Test_Pr)Test web-client ; $(webClientUri) ; $(testOperId) ; $(testOperName)  ; $(testOperLogin)  ; code challenge  ; $(Test_Pr)access token     ; $(accessTokenDateIns)            ; $(accessTokenDateFinish)         ; $(Test_Pr)refresh token     ; $(refreshTokenDateIns)           ; $(refreshTokenDateFinish)        ; $(Test_Pr)session token     ; $(sessionTokenDateIns)           ; $(sessionTokenDateFinish)        ; $(dateIns)                       ; $(testOperId)
'
    );
    checkCase(
      'createSession', 'Неуникальный accessToken'
      , authCode              => 'new auth_code'
      , clientShortName       => webClientSName
      , redirectUri           => webClientUri
      , accessToken           => 'access token'
      , execErrorCode         => -20005
      , execErrorMessageMask  =>
          'ORA-20005: Ошибка при создании пользовательской сессии %'
          || '%OA_SESSION_UK_ACCESS_TOKEN%'
    );
    checkCase(
      'createSession', 'Неуникальный refreshToken'
      , authCode              => 'new auth_code'
      , clientShortName       => webClientSName
      , redirectUri           => webClientUri
      , refreshToken          => 'refresh token'
      , execErrorCode         => -20006
      , execErrorMessageMask  =>
          'ORA-20006: Ошибка при создании пользовательской сессии %'
          || '%OA_SESSION_UK_REFRESH_TOKEN%'
    );
    checkCase(
      'updateSession', 'Некорректный clientShortName'
      , sessionId             => lastRec.session_id
      , clientShortName       => Test_Pr || 'absent client'
      , execErrorCode         => -20003
      , execErrorMessageMask  =>
          'ORA-20003: Указаны неверные данные клиентского приложения (clientShortName="%absent client").%'
    );
    checkCase(
      'updateSession', 'Некорректный URI'
      , sessionId             => lastRec.session_id
      , authCode              => 'new auth_code'
      , clientShortName       => webClientSName
      , redirectUri           => 'absent uri'
      , execErrorCode         => -20004
      , execErrorMessageMask  =>
          'ORA-20004: Ошибка при обновлении пользовательской сессии %'
          || '%OA_SESSION_FK_CLIENT_URI%'
    );
    checkCase(
      'updateSession', 'all args'
      , sessionId               => lastRec.session_id
      , authCode                => 'auth code2'
      , clientShortName         => webClientSName
      , redirectUri             => webClientUri2
      , operatorId              => testOperId
      , codeChallenge           => 'code challenge2'
      , accessToken             => 'access token2'
      , accessTokenDateIns      =>
          tstRec.access_token_date_ins + INTERVAL '65' SECOND
      , accessTokenDateFinish   =>
          tstRec.access_token_date_finish + INTERVAL '185' SECOND
      , refreshToken            => 'refresh token2'
      , refreshTokenDateIns     =>
          tstRec.refresh_token_date_ins + INTERVAL '111' SECOND
      , refreshTokenDateFinish  =>
          tstRec.refresh_token_date_finish + INTERVAL '118' SECOND
      , sessionToken            => 'session token2'
      , sessionTokenDateIns     =>
          tstRec.session_token_date_ins + INTERVAL '131' SECOND
      , sessionTokenDateFinish  =>
          tstRec.session_token_date_finish + INTERVAL '141' SECOND
      , sessionCsv =>
'
AUTH_CODE               ; CLIENT_ID      ; REDIRECT_URI     ; OPERATOR_ID   ; CODE_CHALLENGE  ; ACCESS_TOKEN               ; ACCESS_TOKEN_DATE_INS            ; ACCESS_TOKEN_DATE_FINISH         ; REFRESH_TOKEN               ; REFRESH_TOKEN_DATE_INS           ; REFRESH_TOKEN_DATE_FINISH        ; SESSION_TOKEN               ; SESSION_TOKEN_DATE_INS           ; SESSION_TOKEN_DATE_FINISH        ; IS_MANUAL_BLOCKED ; DATE_FINISH  ; DATE_INS                         ; OPERATOR_ID_INS
----------------------- ; -------------- ; ---------------- ; ------------- ; --------------- ; -------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; ----------------- ; ------------ ; -------------------------------- ; ---------------
$(Test_Pr)auth code2    ; $(webClientId) ; $(webClientUri2) ; $(testOperId) ; code challenge2 ; $(Test_Pr)access token2    ; $(accessTokenDateIns)            ; $(accessTokenDateFinish)         ; $(Test_Pr)refresh token2    ; $(refreshTokenDateIns)           ; $(refreshTokenDateFinish)        ; $(Test_Pr)session token2    ; $(sessionTokenDateIns)           ; $(sessionTokenDateFinish)        ;                   ;              ; $(dateIns)                       ; $(testOperId)
'
    );
    checkCase(
      'findSession', 'by sessionId'
      , sessionId           => lastRec.session_id
      , resultRowCount        => 1
    );
    checkCase(
      'findSession', 'by clientShortName'
      , clientShortName       => webClientSName
      , resultRowCount        => 1
    );
    checkCase(
      'blockSession', 'block session'
      , sessionId           => lastRec.session_id
      , sessionCsv          =>
'
SESSION_ID      ; IS_MANUAL_BLOCKED
--------------- ; -----------------
$(sessionId)    ;                 1
'
    );
    checkCase(
      'findSession', 'after block'
      , clientShortName       => webClientSName
      , resultRowCount        => 0
    );
    checkCase(
      'createSession', 'session #3'
      , authCode                => 'auth code3'
      , clientShortName         => webClientSName
      , accessToken             => 'access token3'
      , refreshToken            => 'refresh token3'
      , nextCaseUsedCount       => 99
      , sessionCount            => 1
    );
    checkCase(
      'updateSession', 'Неуникальный accessToken'
      , sessionId             => lastRec.session_id
      , authCode              => 'new auth_code'
      , clientShortName       => webClientSName
      , redirectUri           => webClientUri
      , accessToken           => 'access token2'
      , execErrorCode         => -20005
      , execErrorMessageMask  =>
          'ORA-20005: Ошибка при обновлении пользовательской сессии %'
          || '%OA_SESSION_UK_ACCESS_TOKEN%'
    );
    checkCase(
      'updateSession', 'Неуникальный refreshToken'
      , sessionId             => lastRec.session_id
      , authCode              => 'new auth_code'
      , clientShortName       => webClientSName
      , redirectUri           => webClientUri
      , refreshToken          => 'refresh token2'
      , execErrorCode         => -20006
      , execErrorMessageMask  =>
          'ORA-20006: Ошибка при обновлении пользовательской сессии %'
          || '%OA_SESSION_UK_REFRESH_TOKEN%'
    );
    checkCase(
      'createSession', 'minimal args'
      , authCode              => 'minimal args'
      , nextCaseUsedCount     => 1
      , sessionCsv =>
'
AUTH_CODE               ; CLIENT_ID      ; REDIRECT_URI    ; OPERATOR_ID   ; CODE_CHALLENGE  ; ACCESS_TOKEN               ; ACCESS_TOKEN_DATE_INS            ; ACCESS_TOKEN_DATE_FINISH         ; REFRESH_TOKEN               ; REFRESH_TOKEN_DATE_INS           ; REFRESH_TOKEN_DATE_FINISH        ; SESSION_TOKEN               ; SESSION_TOKEN_DATE_INS           ; SESSION_TOKEN_DATE_FINISH        ; IS_MANUAL_BLOCKED ; DATE_FINISH  ; DATE_INS                         ; OPERATOR_ID_INS
----------------------- ; -------------- ; --------------- ; ------------- ; --------------- ; -------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; ----------------- ; ------------ ; -------------------------------- ; ---------------
$(Test_Pr)minimal args  ;                ;                 ;               ;                 ;                            ;                                  ;                                  ;                             ;                                  ;                                  ;                             ;                                  ;                                  ;                   ;              ; $(dateIns)                       ; $(testOperId)
'
    );
    checkCase(
      'updateSession', 'minimal args'
      , sessionId             => lastRec.session_id
      , authCode              => 'minimal args2'
      , sessionCsv            =>
'
SESSION_ID      ; AUTH_CODE
--------------- ; -----------------------
$(sessionId)    ; $(Test_Pr)minimal args2
'
    );
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при проверке функций %Session.'
        )
      , true
    );
  end checkSessionApi;



  /*
    Проверяет функции %Key.
  */
  procedure checkKeyApi
  is

    -- Текущие данные тестовой записи
    lastRec oa_key%rowtype;



    /*
      Проверяет тестовый случай.
    */
    procedure checkCase(
      functionName varchar2
      , caseDescription varchar2
      , publicKey varchar2 := null
      , privateKey varchar2 := null
      , isExpired integer := null
      , operatorId integer := testOperId
      , resultRowCount integer := null
      , resultNumber number := None_Integer
      , resultCsv clob := null
      , keyCsv clob := null
      , execErrorCode integer := null
      , execErrorMessageMask varchar2 := null
      , nextCaseUsedCount pls_integer := null
    )
    is

      -- Описание тестового случая
      cinfo varchar2(200) :=
        'CASE ' || to_char( checkCaseNumber + 1)
        || ': ' || functionName || ': ' || caseDescription || ': '
      ;

      -- Ожидается выполнение с ошибкой
      isWaitError boolean :=
        execErrorCode is not null or execErrorMessageMask is not null
      ;

      resErrorCode integer;
      resErrorMessage varchar2(32000);

      resNum integer;
      resRc sys_refcursor;

      -- Данные проверяемой записи
      chId integer;
      chRec oa_key%rowtype;



      /*
        Вполняет подстановку макросов в CSV-данные.
      */
      function replaceMacros(
        srcCsv clob
      )
      return clob
      is
      begin
        return
          replace( replace( replace( replace( replace( replace( replace(
            srcCsv
            , '$(Test_Pr)', Test_Pr)
            , '$(testOperId)', to_char( testOperId))
            , '$(testOperName)', testOperName)
            , '$(testOperNameEn)', testOperNameEn)
            , '$(dateIns)', to_char( chRec.date_ins))
            , '$(l.dateIns)', to_char( lastRec.date_ins))
            , '$(l.keyId)', to_char( lastRec.key_id))
        ;
      end replaceMacros;



    -- checkCase
    begin
      checkCaseNumber := checkCaseNumber + 1;
      if pkg_TestUtility.isTestFailed()
            or testCaseNumber is not null
              and testCaseNumber
                not between checkCaseNumber
                  and checkCaseNumber + coalesce( nextCaseUsedCount, 0)
          then
        return;
      end if;
      logger.info( '*** ' || cinfo);

      begin
        case functionName
          when 'setKey' then
            resNum := pkg_OAuth.setKey(
              publicKey       => nullif( Test_Pr || publicKey, Test_Pr)
              , privateKey    => privateKey
              , operatorId    => operatorId
            );
            chId := resNum;
          when 'getKey' then
            resRc := pkg_OAuth.getKey(
              isExpired       => isExpired
              , operatorId    => operatorId
            );
        end case;
        if isWaitError then
          pkg_TestUtility.failTest(
            failMessageText   =>
              cinfo || 'Успешное выполнение вместо ошибки'
          );
        end if;
      exception when others then
        if isWaitError then
          resErrorCode := sqlcode;
          resErrorMessage := logger.getErrorStack();
          if resErrorMessage not like execErrorMessageMask then
            pkg_TestUtility.compareChar(
              actualString        => resErrorMessage
              , expectedString    => execErrorMessageMask
              , failMessageText   =>
                  cinfo || 'Сообщение об ошибке не соответствует маске'
            );
          elsif execErrorCode is not null then
            pkg_TestUtility.compareChar(
              actualString        => resErrorCode
              , expectedString    => execErrorCode
              , failMessageText   =>
                  cinfo || 'Неожиданный код ошибки'
            );
          end if;
        else
          pkg_TestUtility.failTest(
            failMessageText   =>
              cinfo || 'Выполнение завершилось с ошибкой:'
              || chr(10) || logger.getErrorStack()
          );
        end if;
      end;

      -- Проверка успешного результата
      if not isWaitError and not pkg_TestUtility.isTestFailed() then
        if chId is not null then
          select
            t.*
          into chRec
          from
            oa_key t
          where
            t.key_id = chId
          ;
        end if;
        if nullif( None_Integer, resultNumber) is not null then
          pkg_TestUtility.compareChar(
            actualString        => resNum
            , expectedString    => resultNumber
            , failMessageText   =>
                cinfo || 'Неожиданный результат выполнения функции'
          );
        end if;
        if resultRowCount is not null then
          pkg_TestUtility.compareRowCount(
            resRc
            , expectedRowCount => resultRowCount
            , failMessageText   =>
                cinfo || 'Неожиданное число записей в курсоре'
          );
        end if;
        if resultCsv is not null then
          pkg_TestUtility.compareQueryResult(
            resRc
            , expectedCsv       => replaceMacros( resultCsv)
            , failMessagePrefix => cinfo
          );
        end if;
        if keyCsv is not null then
          pkg_TestUtility.compareQueryResult(
            tableName           => 'oa_key'
            , idColumnName      => 'key_id'
            , filterCondition   =>
                'key_id=' || coalesce( to_char( chId), 'null')
            , expectedCsv       => replaceMacros( keyCsv)
            , failMessagePrefix => cinfo
          );
        end if;

        -- Обновляем после всех проверок (чтобы неявно не изменить возможно
        -- переданное по ссылке значение)
        if chRec.key_id is not null then
          lastRec := chRec;
        end if;
      end if;
    exception when others then
      raise_application_error(
        pkg_Error.ErrorStackInfo
        , logger.errorStack(
            'Ошибка при проверке тестового случая ('
            || ' caseNumber=' || checkCaseNumber
            || ', functionName="' || functionName || '"'
            || ', caseDescription="' || caseDescription || '"'
            || ').'
          )
        , true
      );
    end checkCase;



  -- checkKeyApi
  begin
    checkCase(
      'setKey', 'new key'
      , publicKey             => 'public key'
      , privateKey            => 'private key'
      , nextCaseUsedCount     => 99
      , keyCsv =>
'
PUBLIC_KEY              ; PRIVATE_KEY   ; IS_ACTUAL  ; OPERATOR_ID_INS
----------------------- ; ------------- ; ---------- ; ---------------
$(Test_Pr)public key    ; private key   ;          1 ; $(testOperId)
'
    );
    checkCase(
      'getKey', 'new key'
      , resultCsv             =>
'
KEY_ID     ; PUBLIC_KEY              ; PRIVATE_KEY   ; IS_EXPIRED ; DATE_INS
---------- ; ----------------------- ; ------------- ; ---------- ; ------------
$(l.keyId) ; $(Test_Pr)public key    ; private key   ;            ; $(l.dateIns)
'
    );
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при проверке функций %Key.'
        )
      , true
    );
  end checkKeyApi;



-- testUserApi
begin
  clearTestData();
  savepoint pkg_OAuthTest_testUserApi;
  prepareTestData();
  pkg_TestUtility.beginTest( 'user API');

  checkClientApi();
  checkClientUriApi();
  checkSessionApi();
  checkKeyApi();

  pkg_TestUtility.endTest();
  if coalesce( saveDataFlag, 0) != 1 then
    rollback to pkg_OAuthTest_testUserApi;
  end if;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при тестировании API ('
        || 'testCaseNumber=' || testCaseNumber
        || ', saveDataFlag=' || saveDataFlag
        || ').'
      )
    , true
  );
end testUserApi;

/* proc: testInternal
  Тестирует служебные функции.

  Параметры:
  testCaseNumber              - Номер проверяемого тестового случая
                                (по умолчанию без ограничений)
  saveDataFlag                - Флаг сохранения тестовых данных
                                (1 да, 0 нет ( по умолчанию))
*/
procedure testInternal(
  testCaseNumber integer := null
  , saveDataFlag integer := null
)
is

  -- Порядковый номер проверяемого тестового случая
  checkCaseNumber integer := 0;

  -- Оператор по умолчанию для тестов
  testOperId integer := pkg_Operator.getCurrentUserId();

  -- Тестовые клиенты
  testClientSName oa_client.client_short_name%type := Test_Pr || 'internal';
  testClientUri oa_client_uri.client_uri%type := 'test uri';



  /*
    Подготовка данных для теста.
  */
  procedure prepareTestData
  is

    tmpId integer;

  begin
    tmpId := pkg_OAuth.createClient(
      clientShortName       => testClientSName
      , clientName          => testClientSName
      , clientNameEn        => testClientSName
      , applicationType     => 'native'
      , grantTypeList       => null
      , roleShortNameList   => null
      , operatorId          => testOperId
    );
    tmpId := pkg_OAuth.createClientUri(
      clientShortName       => testClientSName
      , clientUri           => testClientUri
      , operatorId          => testOperId
    );
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при подготовке данных для теста.'
        )
      , true
    );
  end prepareTestData;



  /*
    Добавляет заблокированные сессии.
  */
  procedure addBlockSession(
    rowCount integer
    , dateFinish timestamp with time zone
  )
  is

    tmpId integer;
    tmpUid varchar2(20);

    oldTime timestamp with time zone := systimestamp - INTERVAL '1' SECOND;

  begin
    for i in 1 .. rowCount loop
      tmpUid := to_char( oa_session_seq.nextval);
      tmpId := pkg_OAuth.createSession(
        authCode                  => Test_Pr || tmpUid
        , clientShortName         => testClientSName
        , redirectUri             => testClientUri
        , operatorId              => null
        , codeChallenge           => null
        , accessToken             => Test_Pr || tmpUid
        , accessTokenDateIns      => oldTime
        , accessTokenDateFinish   => oldTime
        , refreshToken            => null
        , refreshTokenDateIns     => null
        , refreshTokenDateFinish  => null
        , sessionToken            => null
        , sessionTokenDateIns     => null
        , sessionTokenDateFinish  => null
        , operatorIdIns           => testOperId
      );
      if dateFinish is not null then
        update
          oa_session d
        set
          d.is_manual_blocked = 1
          , d.date_finish = dateFinish
        where
          d.session_id = tmpId
        ;
      end if;
    end loop;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при добавлении заблокированных сессий.'
        )
      , true
    );
  end addBlockSession;



  /*
    Добавляет неактуальные ключи.
  */
  procedure addNonactualKey(
    rowCount integer
  )
  is

    tmpId integer;
    tmpUid varchar2(20);

  begin
    for i in 1 .. rowCount loop
      tmpId := oa_key_seq.nextval;
      tmpUid := to_char( tmpId);
      insert into
        oa_key
      (
        key_id
        , public_key
        , private_key
        , is_actual
        , operator_id_ins
      )
      values
      (
        tmpId
        , Test_Pr || tmpUid
        , Test_Pr || tmpUid
        , 0
        , testOperId
      );
    end loop;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при добавлении неактуальных ключей.'
        )
      , true
    );
  end addNonactualKey;



  /*
    Проверяет тестовый случай.
  */
  procedure checkCase(
    functionName varchar2
    , caseDescription varchar2
    , toTime timestamp with time zone := null
    , saveKeyCount integer := null
    , maxExecTime interval day to second := null
    , addBlockSessionCount integer := null
    , addBlockSessionDateFinish timestamp with time zone := null
    , addNonactualKeyCount integer := null
    , resultNumber number := None_Integer
    , blockSessionCount integer := null
    , oldSessionCount integer := null
    , nonactulKeyCount integer := null
    , execErrorCode integer := null
    , execErrorMessageMask varchar2 := null
    , nextCaseUsedCount pls_integer := null
  )
  is

    -- Описание тестового случая
    cinfo varchar2(200) :=
      'CASE ' || to_char( checkCaseNumber + 1)
      || ': ' || functionName || ': ' || caseDescription || ': '
    ;

    -- Ожидается выполнение с ошибкой
    isWaitError boolean :=
      execErrorCode is not null or execErrorMessageMask is not null
    ;

    resErrorCode integer;
    resErrorMessage varchar2(32000);

    resNum integer;

  -- checkCase
  begin
    checkCaseNumber := checkCaseNumber + 1;
    if pkg_TestUtility.isTestFailed()
          or testCaseNumber is not null
            and testCaseNumber
              not between checkCaseNumber
                and checkCaseNumber + coalesce( nextCaseUsedCount, 0)
        then
      return;
    end if;
    logger.info( '*** ' || cinfo);

    if addBlockSessionCount > 0 then
      addBlockSession(
        dateFinish  => addBlockSessionDateFinish
        , rowCount  => addBlockSessionCount
      );
    end if;
    if addNonactualKeyCount > 0 then
      addNonactualKey(
        rowCount  => addNonactualKeyCount
      );
    end if;
    begin
      case functionName
        when 'setSessionDateFinish' then
          resNum := pkg_OAuthInternal.setSessionDateFinish();
        when 'clearOldData' then
          resNum := pkg_OAuthInternal.clearOldData(
            toTime          => toTime
            , saveKeyCount  => saveKeyCount
            , maxExecTime   => maxExecTime
          );
      end case;
      if isWaitError then
        pkg_TestUtility.failTest(
          failMessageText   =>
            cinfo || 'Успешное выполнение вместо ошибки'
        );
      end if;
    exception when others then
      if isWaitError then
        resErrorCode := sqlcode;
        resErrorMessage := logger.getErrorStack();
        if resErrorMessage not like execErrorMessageMask then
          pkg_TestUtility.compareChar(
            actualString        => resErrorMessage
            , expectedString    => execErrorMessageMask
            , failMessageText   =>
                cinfo || 'Сообщение об ошибке не соответствует маске'
          );
        elsif execErrorCode is not null then
          pkg_TestUtility.compareChar(
            actualString        => resErrorCode
            , expectedString    => execErrorCode
            , failMessageText   =>
                cinfo || 'Неожиданный код ошибки'
          );
        end if;
      else
        pkg_TestUtility.failTest(
          failMessageText   =>
            cinfo || 'Выполнение завершилось с ошибкой:'
            || chr(10) || logger.getErrorStack()
        );
      end if;
    end;

    -- Проверка успешного результата
    if not isWaitError and not pkg_TestUtility.isTestFailed() then
      if nullif( None_Integer, resultNumber) is not null then
        pkg_TestUtility.compareChar(
          actualString        => resNum
          , expectedString    => resultNumber
          , failMessageText   =>
              cinfo || 'Неожиданный результат выполнения функции'
        );
      end if;
      if blockSessionCount is not null then
        pkg_TestUtility.compareRowCount(
          tableName           => 'v_oa_session'
          , filterCondition   => 'is_blocked=1'
          , expectedRowCount  => blockSessionCount
          , failMessageText   =>
              cinfo || 'Неожиданное число заблокированных сессий'
        );
      end if;
      if oldSessionCount is not null then
        pkg_TestUtility.compareRowCount(
          tableName           => 'oa_session'
          , filterCondition   =>
'date_finish < to_timestamp_tz('''
|| to_char( toTime, 'dd.mm.yyyy hh24:mi:ss.ff tzh:tzm')
|| ''', ''dd.mm.yyyy hh24:mi:ss.ff tzh:tzm'')'
          , expectedRowCount  => oldSessionCount
          , failMessageText   =>
              cinfo || 'Неожиданное число старых сессий'
        );
      end if;
      if nonactulKeyCount is not null then
        pkg_TestUtility.compareRowCount(
          tableName           => 'oa_key'
          , filterCondition   => 'is_actual=0'
          , expectedRowCount  => nonactulKeyCount
          , failMessageText   =>
              cinfo || 'Неожиданное число неактуальных ключей'
        );
      end if;
    end if;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при проверке тестового случая ('
          || ' caseNumber=' || checkCaseNumber
          || ', functionName="' || functionName || '"'
          || ', caseDescription="' || caseDescription || '"'
          || ').'
        )
      , true
    );
  end checkCase;



  /*
    Проверяет функцию setSessionDateFinish.
  */
  procedure checkSetSessionDateFinish
  is
  begin
    checkCase(
      'setSessionDateFinish', 'подготовка'
      , blockSessionCount     => 0
      , nextCaseUsedCount     => 2
    );
    checkCase(
      'setSessionDateFinish', 'повторный запуск'
      , resultNumber          => 0
      , blockSessionCount     => 0
      , nextCaseUsedCount     => 1
    );
    checkCase(
      'setSessionDateFinish', 'есть данные для обработки'
      , addBlockSessionCount  => 2
      , resultNumber          => 2
      , blockSessionCount     => 0
    );
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при проверке функции setSessionDateFinish.'
        )
      , true
    );
  end checkSetSessionDateFinish;



  /*
    Проверяет функцию clearOldData.
  */
  procedure checkClearOldData
  is

    toTime timestamp with time zone := systimestamp - INTERVAL '10' YEAR;
    oldTime toTime%type := toTime - INTERVAL '1' SECOND;

  begin
    checkCase(
      'clearOldData', 'подготовка'
      , addNonactualKeyCount  => 15
      , toTime                => toTime
      , saveKeyCount          => 9
      , oldSessionCount       => 0
      , nonactulKeyCount      => 9
      , nextCaseUsedCount     => 2
    );
    checkCase(
      'clearOldData', 'повторный запуск'
      , toTime                => toTime
      , saveKeyCount          => 9
      , resultNumber          => 0
      , oldSessionCount       => 0
      , nextCaseUsedCount     => 1
    );
    checkCase(
      'clearOldData', 'есть данные для обработки'
      , addBlockSessionCount  => 2
      , addBlockSessionDateFinish  => oldTime
      , addNonactualKeyCount  => 3
      , toTime                => toTime
      , saveKeyCount          => 9
      , resultNumber          => 5
      , oldSessionCount       => 0
      , nonactulKeyCount      => 9
    );
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при проверке функции clearOldData.'
        )
      , true
    );
  end checkClearOldData;



-- testInternal
begin
  clearTestData();
  savepoint pkg_OAuthTest_testInternal;
  prepareTestData();
  pkg_TestUtility.beginTest( 'internal functions');

  checkSetSessionDateFinish();
  checkClearOldData();

  pkg_TestUtility.endTest();
  if coalesce( saveDataFlag, 0) != 1 then
    rollback to pkg_OAuthTest_testInternal;
  end if;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при тестировании служебных функций ('
        || 'testCaseNumber=' || testCaseNumber
        || ', saveDataFlag=' || saveDataFlag
        || ').'
      )
    , true
  );
end testInternal;

end pkg_OAuthTest;
/
