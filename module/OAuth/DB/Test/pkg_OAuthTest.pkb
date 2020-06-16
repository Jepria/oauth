create or replace package body pkg_OAuthTest is
/* package body: pkg_OAuthTest::body */



/* group: Константы */

/* iconst: None_Integer
  Число, указываемая в качестве значения параметра по умолчанию, позволяющая
  определить отсутствие явно заданного значения.
*/
None_Integer constant integer := -9582095482058325832950482954832;



/* group: Переменные */

/* ivar: logger
  Логер пакета.
*/
logger lg_logger_t := lg_logger_t.getLogger(
  moduleName    => pkg_OAuth.Module_Name
  , objectName  => 'pkg_OAuthTest'
);



/* group: Функции */

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

  -- Префикс тестовых данных (используется в client_short_name)
  Test_Pr constant varchar2(20) := '$OAuth.Test$:';

  -- Оператор по умолчанию для тестов
  testOperId integer := pkg_Operator.getCurrentUserId();
  testOperName op_operator.operator_name%type;
  testOperNameEn op_operator.operator_name_en%type;



  /*
    Подготовка данных для теста.
  */
  procedure prepareTestData
  is

    pragma autonomous_transaction;

  begin
    select
      max( t.operator_name)
      , max( t.operator_name_en)
    into testOperName, testOperNameEn
    from
      op_operator t
    where
      t.operator_id = testOperId
    ;
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
      and op.date_finish is null
    ;
    commit;
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
    lastClientRec oa_client%rowtype;



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

      clientId integer;
      clRec oa_client%rowtype;



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
            , '$(clientSecretDec)'
                , case when clRec.client_secret is not null then
                    pkg_OptionCrypto.decrypt( clRec.client_secret)
                  end
              )
            , '$(dateIns)', to_char( clRec.date_ins))
            , '$(changeDate)', to_char( clRec.change_date))
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
              , grantTypeList               => grantTypeList
              , roleShortNameList           => roleShortNameList
              , operatorId                  => operatorId
            );
            clientId := resNum;
          when 'updateClient' then
            pkg_OAuth.updateClient(
              clientShortName               => Test_Pr || clientShortName
              , clientName                  => Test_Pr || clientName
              , clientNameEn                => Test_Pr || clientNameEn
              , applicationType             => applicationType
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
        if clientId is null and clientShortName is not null then
          select
            max( t.client_id)
          into clientId
          from
            oa_client t
          where
            t.client_short_name = Test_Pr || clientShortName
          ;
        end if;
        if clientId is not null then
          select
            t.*
          into clRec
          from
            oa_client t
          where
            t.client_id = clientId
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
                'client_id=' || coalesce( to_char( clientId), 'null')
            , expectedCsv       => replaceMacros( clientCsv)
            , failMessagePrefix => cinfo
          );
        end if;
        if clientGrantCsv is not null then
          pkg_TestUtility.compareQueryResult(
            tableName           => 'oa_client_grant'
            , idColumnName      => 'client_id'
            , filterCondition   =>
                'client_id=' || coalesce( to_char( clientId), 'null')
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
      cl.client_id=' || coalesce( to_char( clientId), 'null') || '
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
      cl.client_id=' || coalesce( to_char( clientId), 'null') || '
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
        if clRec.client_id is not null then
          lastClientRec := clRec;
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
      , grantTypeList         =>
          'authorization_code,implicit,client_credentials,password,refresh_token'
      , roleShortNameList     => 'OAViewSession,OACreateSession'
      , nextCaseUsedCount     => 99
      , clientCsv =>
'
CLIENT_SHORT_NAME          ; is_CLIENT_SECRET  ; CLIENT_NAME                  ; CLIENT_NAME_EN            ; APPLICATION_TYPE  ; OPERATOR_ID_INS   ; CHANGE_OPERATOR_ID ; is_OPERATOR_ID  ; IS_DELETED
-------------------------- ; ----------------- ; ---------------------------- ; ------------------------- ; ----------------- ; ----------------- ; ------------------ ; --------------- ; ----------
$(Test_Pr)client1          ;                 1 ; $(Test_Pr)Тестовый клиент 1  ; $(Test_Pr)Test client 1   ; web               ; $(testOperId)     ;  $(testOperId)     ;               1 ;          0
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
      'findClient', 'all args'
      , clientShortName       => 'client1'
      , clientName            => 'Тестовый клиент 1'
      , clientNameEn          => 'Test client 1'
      , maxRowCount           => 50
      , resultCsv             =>
'
CLIENT_SHORT_NAME  ; CLIENT_SECRET      ; CLIENT_NAME                  ; CLIENT_NAME_EN            ; APPLICATION_TYPE  ; DATE_INS    ; CREATE_OPERATOR_ID ; CREATE_OPERATOR_NAME  ; CREATE_OPERATOR_NAME_EN  ; CHANGE_DATE    ; CHANGE_OPERATOR_ID ; CHANGE_OPERATOR_NAME        ; CHANGE_OPERATOR_NAME_EN
------------------ ; ------------------ ; ---------------------------- ; ------------------------- ; ----------------- ; ----------- ; ------------------ ; --------------------- ; ------------------------ ; -------------- ; ------------------ ; --------------------------- ; ----------------------------
$(Test_Pr)client1  ; $(clientSecretDec) ; $(Test_Pr)Тестовый клиент 1  ; $(Test_Pr)Test client 1   ; web               ; $(dateIns)  ;      $(testOperId) ; $(testOperName)       ; $(testOperNameEn)        ; $(changeDate)  ;      $(testOperId) ; $(testOperName)             ; $(testOperNameEn)
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
          pkg_OptionCrypto.decrypt( lastClientRec.client_secret)
      , execErrorCode         => -20003
      , execErrorMessageMask  =>
          'ORA-20003: Указаны неверные данные клиентского приложения (clientShortName="%").%'
    );
    checkCase(
      'verifyClientCredentials', 'web-client: no secret'
      , clientShortName       => 'client1'
      , clientSecret          => null
      , execErrorCode         => -20003
    );
    checkCase(
      'verifyClientCredentials', 'web-client: bad secret'
      , clientShortName       => 'client1'
      , clientSecret          => lastClientRec.client_secret
      , execErrorCode         => -20003
    );
    checkCase(
      'verifyClientCredentials', 'web-client: good secret'
      , clientShortName       => 'client1'
      , clientSecret          =>
          pkg_OptionCrypto.decrypt( lastClientRec.client_secret)
      , resultNumber          => lastClientRec.operator_id
    );
    checkCase(
      'updateClient', 'web-клиент 1_1'
      , clientShortName       => 'client1'
      , clientName            => 'Тестовый клиент 1_1'
      , clientNameEn          => 'Test client 1_1'
      , applicationType       => 'web'
      , grantTypeList         =>
          'authorization_code,implicit,client_credentials'
      , roleShortNameList     => ''
      , clientCsv =>
'
CLIENT_SHORT_NAME          ; is_CLIENT_SECRET  ; CLIENT_NAME                   ; CLIENT_NAME_EN            ; APPLICATION_TYPE  ; OPERATOR_ID_INS   ; CHANGE_OPERATOR_ID ; is_OPERATOR_ID  ; IS_DELETED
-------------------------- ; ----------------- ; ----------------------------- ; ------------------------- ; ----------------- ; ----------------- ; ------------------ ; --------------- ; ----------
$(Test_Pr)client1          ;                 1 ; $(Test_Pr)Тестовый клиент 1_1 ; $(Test_Pr)Test client 1_1 ; web               ; $(testOperId)     ;  $(testOperId)     ;               1 ;          0
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
      , grantTypeList         =>
          'authorization_code'
      , roleShortNameList     => ''
      , clientCsv =>
'
CLIENT_SHORT_NAME          ; is_CLIENT_SECRET  ; CLIENT_NAME                   ; CLIENT_NAME_EN            ; APPLICATION_TYPE  ; OPERATOR_ID_INS   ; CHANGE_OPERATOR_ID ; is_OPERATOR_ID  ; IS_DELETED
-------------------------- ; ----------------- ; ----------------------------- ; ------------------------- ; ----------------- ; ----------------- ; ------------------ ; --------------- ; ----------
$(Test_Pr)client1          ;                 0 ; $(Test_Pr)Тестовый клиент 1_3 ; $(Test_Pr)Test client 1_3 ; native            ; $(testOperId)     ;  $(testOperId)     ;               1 ;          0
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
      , grantTypeList         =>
          'authorization_code,client_credentials'
      , roleShortNameList     => ''
      , clientCsv =>
'
CLIENT_SHORT_NAME          ; is_CLIENT_SECRET  ; CLIENT_NAME                   ; CLIENT_NAME_EN            ; APPLICATION_TYPE  ; OPERATOR_ID_INS   ; CHANGE_OPERATOR_ID ; is_OPERATOR_ID  ; IS_DELETED
-------------------------- ; ----------------- ; ----------------------------- ; ------------------------- ; ----------------- ; ----------------- ; ------------------ ; --------------- ; ----------
$(Test_Pr)client1          ;                 1 ; $(Test_Pr)Тестовый клиент 1_4 ; $(Test_Pr)Test client 1_4 ; service           ; $(testOperId)     ;  $(testOperId)     ;               1 ;          0
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
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при проверке функций %Client.'
        )
      , true
    );
  end checkClientApi;



-- testUserApi
begin
  prepareTestData();
  savepoint pkg_OAuthTest_testUserApi;
  pkg_TestUtility.beginTest( 'user API');

  checkClientApi();

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

end pkg_OAuthTest;
/
