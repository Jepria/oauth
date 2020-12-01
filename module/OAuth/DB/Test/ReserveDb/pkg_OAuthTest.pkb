create or replace package body pkg_OAuthTest is
/* package body: pkg_OAuthTest( ReserveDb)::body */



/* group: Константы */

/* iconst: None_Integer
  Число, указываемая в качестве значения параметра по умолчанию, позволяющая
  определить отсутствие явно заданного значения.
*/
None_Integer constant integer := -9582095482058325832950482954832;

/* iconst: Test_Pr
  Префикс тестовых данных (используется в client_short_name и др.)
*/
Test_Pr constant varchar2(20) := '$OAuth.TstR$:';



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
    oa_session t
  where
    t.auth_code like Test_Pr || '%'
  ;
  delete
    oa_session t
  where
    t.session_token like Test_Pr || '%'
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
  client1 oa_client%rowtype;
  client1crOpName op_operator.operator_name%type;
  client1crOpNameEn op_operator.operator_name_en%type;
  client1chgOpName op_operator.operator_name%type;
  client1chgOpNameEn op_operator.operator_name_en%type;
  client1grantCount integer;

  client1uri1 oa_client_uri%rowtype;
  client1uri1OpName op_operator.operator_name%type;
  client1uri1OpNameEn op_operator.operator_name_en%type;
  client1uri2 oa_client_uri%rowtype;

  testClientId integer;
  testClientSName oa_client.client_short_name%type;
  testClientName oa_client.client_name%type;
  testClientNameEn oa_client.client_name_en%type;
  testClientUri oa_client_uri.client_uri%type;
  testClientUri2 oa_client_uri.client_uri%type;
  testClientSessionCount integer;
  testClientMainSession mv_oa_session%rowtype;
  testClientMainSession2 mv_oa_session%rowtype;

  testUsedMainRefreshToken mv_oa_session.refresh_token%type;

  testKey oa_key%rowtype;



  /*
    Подготовка данных для теста.
  */
  procedure prepareTestData
  is

    cursor client1Cur is
      select
        d.*
      from
        oa_client d
      where
        not exists
          (
          select
            null
          from
            oa_client t
          where
            upper( t.client_name) = upper( d.client_name)
            and t.client_id != d.client_id
          )
        and not exists
          (
          select
            null
          from
            oa_client t
          where
            upper( t.client_name_en) = upper( d.client_name_en)
            and t.client_id != d.client_id
          )
        and
          (
          select count(*) from oa_client_uri t where t.client_id = d.client_id
          ) = 2
        and
          (
          select
            count(*)
          from
            v_oa_session ss
          where
            ss.client_id = d.client_id
            and ss.is_blocked = 0
            and ss.local_row_flag = 0
            and ss.access_token is not null
            and rownum <= 2
          ) >= 2
        and rownum <= 1
      ;

      cursor clientUriCur( clientId integer) is
        select
          d.*
        from
          oa_client_uri d
        where
          d.client_id = clientId
        order by
          d.client_uri
      ;

      cursor mainSessionCur is
        select /*+ first_rows(2) */
          ms.*
        from
          mv_oa_session ms
        where
          ms.session_id in
            (
            select
              ss.session_id
            from
              v_oa_session ss
            where
              ss.client_id = testClientId
              and ss.is_blocked = 0
              and ss.local_row_flag = 0
              and ss.access_token is not null
            )
        order by
          ms.session_id
      ;

      cursor keyCur is
        select
          d.*
        from
          oa_key d
        where
          d.is_actual = 1
      ;



      /*
        Возвращает наименование оператора.
      */
      procedure getOperatorName(
        operatorName out varchar2
        , operatorNameEn out varchar2
        , operatorId integer
      )
      is
      begin
        if operatorId is not null then
          select
            t.operator_name
            , t.operator_name_en
          into operatorName, operatorNameEn
          from
            op_operator t
          where
            t.operator_id = operatorId
          ;
        end if;
      exception when others then
        raise_application_error(
          pkg_Error.ErrorStackInfo
          , logger.errorStack(
              'Ошибка при получении наименования оператора ('
              || 'operatorId=' || operatorId
              || ').'
            )
          , true
        );
      end getOperatorName;



    -- prepareTestData
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
      open client1Cur;
      fetch client1Cur into client1;
      close client1Cur;
      if client1.client_id is null then
        raise_application_error(
          pkg_Error.ProcessError
          , 'Не удалось найти клиента для теста (client1).'
        );
      end if;
      getOperatorName(
        client1crOpName, client1crOpNameEn, client1.operator_id_ins
      );
      getOperatorName(
        client1chgOpName, client1chgOpNameEn, client1.change_operator_id
      );
      select
        count(*)
      into client1grantCount
      from
        oa_client_grant t
      where
        t.client_id = client1.client_id
      ;
      open clientUriCur( clientId => client1.client_id);
      fetch clientUriCur into client1uri1;
      getOperatorName(
        client1uri1OpName, client1uri1OpNameEn, client1uri1.operator_id
      );
      fetch clientUriCur into client1uri2;
      close clientUriCur;

      testClientId := client1.client_id;
      testClientSName := client1.client_short_name;
      testClientName := client1.client_name;
      testClientNameEn := client1.client_name_en;
      testClientUri := client1uri1.client_uri;
      testClientUri2 := client1uri2.client_uri;
      logger.trace( 'testClientSName: "' || testClientSName || '"');

      open mainSessionCur;
      fetch mainSessionCur into testClientMainSession;
      fetch mainSessionCur into testClientMainSession2;
      close mainSessionCur;
      select
        count(*)
      into testClientSessionCount
      from
        v_oa_session t
      where
        t.client_id = testClientId
        and t.is_blocked = 0
      ;
      select
        max( ms.refresh_token)
      into testUsedMainRefreshToken
      from
        mv_oa_session ms
      where
        ms.refresh_token is not null
        and rownum <= 1
      ;
      open keyCur;
      fetch keyCur into testKey;
      close keyCur;
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



      /*
        Проверяет тестовый случай.
      */
      procedure checkCase(
        functionName varchar2
        , caseDescription varchar2
        , clientShortName varchar2 := null
        , clientName varchar2 := null
        , clientNameEn varchar2 := null
        , maxRowCount integer := null
        , operatorId integer := testOperId
        , resultRowCount integer := null
        , resultCsv clob := null
        , clientCsv clob := null
        , clientGrantCsv clob := null
        , execErrorCode integer := null
        , execErrorMessageMask varchar2 := null
        , nextCaseUsedCount pls_integer := null
      )
      is

        -- Описание тестового случая
        cinfo varchar2(200) :=
          'CASE ' || to_char( checkCaseNumber + 1)
          || ': ' || functionName || ': ' || caseDescription
          || ' (test_client_id=' || client1.client_id || '): '
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
            replace( replace( replace( replace( replace( replace( replace(
              srcCsv
              , '$(client_short_name)'
                , client1.client_short_name)
              , '$(clientSecretDec)'
                , pkg_OAuthCommon.decrypt( client1.client_secret))
              , '$(client_name)'
                , client1.client_name)
              , '$(client_name_en)'
                , client1.client_name_en)
              , '$(application_type)'
                , client1.application_type)
              , '$(date_ins)'
                , client1.date_ins)
              , '$(create_operator_id)'
                , client1.operator_id_ins)
              , '$(create_operator_name)', client1crOpName)
              , '$(create_operator_name_en)', client1crOpNameEn)
              , '$(change_date)'
                , client1.change_date)
              , '$(change_operator_id)'
                , client1.change_operator_id)
              , '$(change_operator_name)', client1chgOpName)
              , '$(change_operator_name_en)', client1chgOpNameEn)
              , '$(client_operator_id)'
                , client1.operator_id)
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
            when 'findClient' then
              resRc := pkg_OAuth.findClient(
                clientShortName               => clientShortName
                , clientName                  => clientName
                , clientNameEn                => clientNameEn
                , maxRowCount                 => maxRowCount
                , operatorId                  => operatorId
              );
            when 'getClientGrant' then
              resRc := pkg_OAuth.getClientGrant(
                clientShortName               => clientShortName
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
          if chId is null and clientShortName is not null then
            select
              max( t.client_id)
            into chId
            from
              oa_client t
            where
              t.client_short_name = clientShortName
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
        'findClient', 'нет данных'
        , clientShortName       => Test_Pr || 'absent client'
        , resultRowCount => 0
      );
      checkCase(
        'findClient', 'all args'
        , clientShortName       => client1.client_short_name
        , clientName            => client1.client_name
        , clientNameEn          => client1.client_name_en
        , maxRowCount           => 50
        , resultCsv             =>
'
CLIENT_SHORT_NAME    ; CLIENT_SECRET      ; CLIENT_NAME      ; CLIENT_NAME_EN     ; APPLICATION_TYPE     ; DATE_INS     ; CREATE_OPERATOR_ID    ; CREATE_OPERATOR_NAME     ; CREATE_OPERATOR_NAME_EN     ; CHANGE_DATE    ; CHANGE_OPERATOR_ID    ; CHANGE_OPERATOR_NAME     ; CHANGE_OPERATOR_NAME_EN    ; CLIENT_OPERATOR_ID
-------------------- ; ------------------ ; ---------------- ; ------------------ ; -------------------- ; ------------ ; --------------------- ; ------------------------ ; --------------------------- ; -------------- ; --------------------- ; ------------------------ ; -------------------------- ; ----------------------
$(client_short_name) ; $(clientSecretDec) ; $(client_name)   ; $(client_name_en)  ; $(application_type)  ; $(date_ins)  ; $(create_operator_id) ; $(create_operator_name)  ; $(create_operator_name_en)  ; $(change_date) ; $(change_operator_id) ; $(change_operator_name)  ; $(change_operator_name_en) ; $(client_operator_id)
'
    );
    checkCase(
      'findClient', 'by clientShortName'
      , clientShortName       => client1.client_short_name
      , resultRowCount        => 1
    );
    checkCase(
      'findClient', 'by clientName'
      , clientName            => client1.client_name
      , resultRowCount        => 1
    );
    checkCase(
      'findClient', 'by clientNameEn'
      , clientNameEn          => client1.client_name_en
      , resultRowCount        => 1
    );
    checkCase(
      'getClientGrant', 'client1'
      , clientShortName       => client1.client_short_name
      , resultRowCount        => client1grantCount
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



  /*
    Проверяет функции %ClientUri.
  */
  procedure checkClientUriApi
  is



    /*
      Проверяет тестовый случай.
    */
    procedure checkCase(
      functionName varchar2
      , caseDescription varchar2
      , clientShortName varchar2 := null
      , clientUriId integer := null
      , maxRowCount integer := null
      , operatorId integer := testOperId
      , resultRowCount integer := null
      , resultCsv clob := null
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
          replace( replace( replace( replace( replace( replace( replace(
            srcCsv
            , '$(client_uri_id)'
              , client1uri1.client_uri_id)
            , '$(testClientSName)'
              , testClientSName)
            , '$(client_uri)'
              , client1uri1.client_uri)
            , '$(date_ins)'
              , client1uri1.date_ins)
            , '$(operator_id)'
              , client1uri1.operator_id)
            , '$(operator_name)'
              , client1uri1OpName)
            , '$(operator_name_en)'
              , client1uri1OpNameEn)
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
      'findClientUri', 'нет данных'
      , clientShortName       => Test_Pr || 'absent client'
    );
    checkCase(
      'findClientUri', 'all args'
      , clientUriId           => client1uri1.client_uri_id
      , clientShortName       => testClientSName
      , maxRowCount           => 50
      , resultCsv             =>
'
CLIENT_URI_ID     ; CLIENT_SHORT_NAME    ; CLIENT_URI     ; DATE_INS    ; OPERATOR_ID    ; OPERATOR_NAME    ; OPERATOR_NAME_EN
----------------- ; -------------------- ; -------------- ; ----------- ; -------------- ; ---------------- ; ------------------
$(client_uri_id)  ; $(testClientSName)   ; $(client_uri)  ; $(date_ins) ; $(operator_id) ; $(operator_name) ; $(operator_name_en)
'
    );
    checkCase(
      'findClientUri', 'by clientUriId'
      , clientUriId           => client1uri2.client_uri_id
      , resultRowCount        => 1
    );
    checkCase(
      'findClientUri', 'by clientShortName'
      , clientShortName       => testClientSName
      , resultRowCount        => 2
    );
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
      , exactTokenFlag integer := null
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
              replace( replace( replace( replace( replace(
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
            , '$(testClientId)', to_char( testClientId))
            , '$(testClientSName)', testClientSName)
            , '$(testClientName)', testClientName)
            , '$(testClientNameEn)', testClientNameEn)
            , '$(testClientUri)', testClientUri)
            , '$(testClientUri2)', testClientUri2)
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
                  case when exactTokenFlag = 1 then
                    authCode
                  else
                    nullif( Test_Pr || authCode, Test_Pr)
                  end
              , clientShortName         => clientShortName
              , redirectUri             => redirectUri
              , operatorId              => operatorId
              , codeChallenge           => codeChallenge
              , accessToken             =>
                  case when exactTokenFlag = 1 then
                    accessToken
                  else
                    nullif( Test_Pr || accessToken, Test_Pr)
                  end
              , accessTokenDateIns      => accessTokenDateIns
              , accessTokenDateFinish   => accessTokenDateFinish
              , refreshToken            =>
                  case when exactTokenFlag = 1 then
                    refreshToken
                  else
                    nullif( Test_Pr || refreshToken, Test_Pr)
                  end
              , refreshTokenDateIns     => refreshTokenDateIns
              , refreshTokenDateFinish  => refreshTokenDateFinish
              , sessionToken            =>
                  case when exactTokenFlag = 1 then
                    sessionToken
                  else
                    nullif( Test_Pr || sessionToken, Test_Pr)
                  end
              , sessionTokenDateIns     => sessionTokenDateIns
              , sessionTokenDateFinish  => sessionTokenDateFinish
              , operatorIdIns           => operatorIdIns
            );
            chId := resNum;
          when 'updateSession' then
            pkg_OAuth.updateSession(
              sessionId                 => sessionId
              , authCode                =>
                  case when exactTokenFlag = 1 then
                    authCode
                  else
                    nullif( Test_Pr || authCode, Test_Pr)
                  end
              , clientShortName         => clientShortName
              , redirectUri             => redirectUri
              , operatorId              => operatorId
              , codeChallenge           => codeChallenge
              , accessToken             =>
                  case when exactTokenFlag = 1 then
                    accessToken
                  else
                    nullif( Test_Pr || accessToken, Test_Pr)
                  end
              , accessTokenDateIns      => accessTokenDateIns
              , accessTokenDateFinish   => accessTokenDateFinish
              , refreshToken            =>
                  case when exactTokenFlag = 1 then
                    refreshToken
                  else
                    nullif( Test_Pr || refreshToken, Test_Pr)
                  end
              , refreshTokenDateIns     => refreshTokenDateIns
              , refreshTokenDateFinish  => refreshTokenDateFinish
              , sessionToken            =>
                  case when exactTokenFlag = 1 then
                    sessionToken
                  else
                    nullif( Test_Pr || sessionToken, Test_Pr)
                  end
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
      , clientShortName       => testClientSName
      , redirectUri           => 'absent uri'
      , execErrorCode         => -20004
      , execErrorMessageMask  =>
          'ORA-20004: Указан несуществующий URI клиентского приложения (redirectUri="absent uri", clientId=%).%'
    );
    checkCase(
      'findSession', 'ранее созданные сессии'
      , clientShortName       => testClientSName
      , resultRowCount        => testClientSessionCount
    );
    checkCase(
      'createSession', 'all args'
      , authCode                => 'auth code'
      , clientShortName         => testClientSName
      , redirectUri             => testClientUri
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
AUTH_CODE               ; CLIENT_ID       ; REDIRECT_URI      ; OPERATOR_ID   ; CODE_CHALLENGE  ; ACCESS_TOKEN               ; ACCESS_TOKEN_DATE_INS            ; ACCESS_TOKEN_DATE_FINISH         ; REFRESH_TOKEN               ; REFRESH_TOKEN_DATE_INS           ; REFRESH_TOKEN_DATE_FINISH        ; SESSION_TOKEN               ; SESSION_TOKEN_DATE_INS           ; SESSION_TOKEN_DATE_FINISH        ; IS_MANUAL_BLOCKED ; DATE_FINISH  ; DATE_INS                         ; OPERATOR_ID_INS
----------------------- ; --------------- ; ----------------- ; ------------- ; --------------- ; -------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; ----------------- ; ------------ ; -------------------------------- ; ---------------
$(Test_Pr)auth code     ; $(testClientId) ; $(testClientUri)  ; $(testOperId) ; code challenge  ; $(Test_Pr)access token     ; $(accessTokenDateIns)            ; $(accessTokenDateFinish)         ; $(Test_Pr)refresh token     ; $(refreshTokenDateIns)           ; $(refreshTokenDateFinish)        ; $(Test_Pr)session token     ; $(sessionTokenDateIns)           ; $(sessionTokenDateFinish)        ;                   ;              ; $(dateIns)                       ; $(testOperId)
'
    );
    checkCase(
      'findSession', 'all args'
      , sessionId             => lastRec.session_id
      , clientShortName       => testClientSName
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
SESSION_ID      ; AUTH_CODE               ; CLIENT_SHORT_NAME     ; CLIENT_NAME                   ; CLIENT_NAME_EN            ; REDIRECT_URI     ; OPERATOR_ID   ; OPERATOR_NAME    ; OPERATOR_LOGIN    ; CODE_CHALLENGE  ; ACCESS_TOKEN               ; ACCESS_TOKEN_DATE_INS            ; ACCESS_TOKEN_DATE_FINISH         ; REFRESH_TOKEN               ; REFRESH_TOKEN_DATE_INS           ; REFRESH_TOKEN_DATE_FINISH        ; SESSION_TOKEN               ; SESSION_TOKEN_DATE_INS           ; SESSION_TOKEN_DATE_FINISH        ; DATE_INS                         ; OPERATOR_ID_INS
--------------- ; ----------------------- ; --------------------- ; ----------------------------- ; ------------------------- ; ---------------- ; ------------- ; ---------------- ; ------------------; --------------- ; -------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; -------------------------------- ; ---------------
$(sessionId)    ; $(Test_Pr)auth code     ; $(testClientSName)    ; $(testClientName)             ; $(testClientNameEn)       ; $(testClientUri) ; $(testOperId) ; $(testOperName)  ; $(testOperLogin)  ; code challenge  ; $(Test_Pr)access token     ; $(accessTokenDateIns)            ; $(accessTokenDateFinish)         ; $(Test_Pr)refresh token     ; $(refreshTokenDateIns)           ; $(refreshTokenDateFinish)        ; $(Test_Pr)session token     ; $(sessionTokenDateIns)           ; $(sessionTokenDateFinish)        ; $(dateIns)                       ; $(testOperId)
'
    );
    checkCase(
      'createSession', 'Дублирование authCode с основной БД'
      , authCode              => testClientMainSession.auth_code
      , clientShortName       => testClientSName
      , redirectUri           => testClientUri
      , exactTokenFlag        => 1
      , execErrorMessageMask  =>
          '%ORA-20195: Авторизационный код уже использовался в основной БД (authCode="%").%'
    );
    checkCase(
      'createSession', 'Неуникальный accessToken'
      , authCode              => 'new auth_code'
      , clientShortName       => testClientSName
      , redirectUri           => testClientUri
      , accessToken           => 'access token'
      , execErrorCode         => -20005
      , execErrorMessageMask  =>
          'ORA-20005: Ошибка при создании пользовательской сессии %'
          || '%OA_SESSION_UK_ACCESS_TOKEN%'
    );
    checkCase(
      'createSession', 'Дублирование accessToken с основной БД'
      , authCode              => 'new auth_code'
      , clientShortName       => testClientSName
      , redirectUri           => testClientUri
      , accessToken           => testClientMainSession.access_token
      , exactTokenFlag        => 1
      , execErrorCode         => -20005
      , execErrorMessageMask  =>
          'ORA-20005: Токен доступа уже использовался в основной БД (accessToken="%").%'
    );
    checkCase(
      'createSession', 'Неуникальный refreshToken'
      , authCode              => 'new auth_code'
      , clientShortName       => testClientSName
      , redirectUri           => testClientUri
      , refreshToken          => 'refresh token'
      , execErrorCode         => -20006
      , execErrorMessageMask  =>
          'ORA-20006: Ошибка при создании пользовательской сессии %'
          || '%OA_SESSION_UK_REFRESH_TOKEN%'
    );
    checkCase(
      'createSession', 'Дублирование refreshToken с основной БД'
      , authCode              => 'new auth_code'
      , clientShortName       => testClientSName
      , redirectUri           => testClientUri
      , refreshToken          => testUsedMainRefreshToken
      , exactTokenFlag        => 1
      , execErrorCode         => -20006
      , execErrorMessageMask  =>
          'ORA-20006: Токен обновления уже использовался в основной БД (refreshToken="%").%'
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
      , clientShortName       => testClientSName
      , redirectUri           => 'absent uri'
      , execErrorCode         => -20004
      , execErrorMessageMask  =>
          'ORA-20004: Указан несуществующий URI клиентского приложения (redirectUri="absent uri", clientId=%).%'
    );
    checkCase(
      'updateSession', 'all args'
      , sessionId               => lastRec.session_id
      , authCode                => 'auth code2'
      , clientShortName         => testClientSName
      , redirectUri             => testClientUri2
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
      , nextCaseUsedCount       => 99
      , sessionCsv =>
'
AUTH_CODE               ; CLIENT_ID       ; REDIRECT_URI      ; OPERATOR_ID   ; CODE_CHALLENGE  ; ACCESS_TOKEN               ; ACCESS_TOKEN_DATE_INS            ; ACCESS_TOKEN_DATE_FINISH         ; REFRESH_TOKEN               ; REFRESH_TOKEN_DATE_INS           ; REFRESH_TOKEN_DATE_FINISH        ; SESSION_TOKEN               ; SESSION_TOKEN_DATE_INS           ; SESSION_TOKEN_DATE_FINISH        ; IS_MANUAL_BLOCKED ; DATE_FINISH  ; DATE_INS                         ; OPERATOR_ID_INS
----------------------- ; --------------- ; ----------------- ; ------------- ; --------------- ; -------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; --------------------------- ; -------------------------------- ; -------------------------------- ; ----------------- ; ------------ ; -------------------------------- ; ---------------
$(Test_Pr)auth code2    ; $(testClientId) ; $(testClientUri2) ; $(testOperId) ; code challenge2 ; $(Test_Pr)access token2    ; $(accessTokenDateIns)            ; $(accessTokenDateFinish)         ; $(Test_Pr)refresh token2    ; $(refreshTokenDateIns)           ; $(refreshTokenDateFinish)        ; $(Test_Pr)session token2    ; $(sessionTokenDateIns)           ; $(sessionTokenDateFinish)        ;                   ;              ; $(dateIns)                       ; $(testOperId)
'
    );
    checkCase(
      'findSession', 'by sessionId'
      , sessionId           => lastRec.session_id
      , resultRowCount      => 1
    );
    checkCase(
      'findSession', 'by clientShortName'
      , clientShortName       => testClientSName
      , resultRowCount        => testClientSessionCount + 1
    );
    checkCase(
      'blockSession', 'block session'
      , sessionId           => lastRec.session_id
      , nextCaseUsedCount   => 99
      , sessionCsv          =>
'
SESSION_ID      ; IS_MANUAL_BLOCKED
--------------- ; -----------------
$(sessionId)    ;                 1
'
    );
    checkCase(
      'findSession', 'after block'
      , clientShortName       => testClientSName
      , resultRowCount        => testClientSessionCount + 0
    );
    checkCase(
      'createSession', 'session #3'
      , authCode                => 'auth code3'
      , clientShortName         => testClientSName
      , accessToken             => 'access token3'
      , refreshToken            => 'refresh token3'
      , nextCaseUsedCount       => 99
      , sessionCount            => 1
    );
    checkCase(
      'updateSession', 'Дублирование authCode с основной БД'
      , sessionId             => lastRec.session_id
      , authCode              => testClientMainSession.auth_code
      , clientShortName       => testClientSName
      , redirectUri           => testClientUri
      , exactTokenFlag        => 1
      , execErrorMessageMask  =>
          '%ORA-20195: Авторизационный код уже использовался в основной БД (authCode="%").%'
    );
    checkCase(
      'updateSession', 'Неуникальный accessToken'
      , sessionId             => lastRec.session_id
      , authCode              => 'new auth_code'
      , clientShortName       => testClientSName
      , redirectUri           => testClientUri
      , accessToken           => 'access token2'
      , execErrorCode         => -20005
      , execErrorMessageMask  =>
          'ORA-20005: Ошибка при обновлении пользовательской сессии %'
          || '%OA_SESSION_UK_ACCESS_TOKEN%'
    );
    checkCase(
      'updateSession', 'Дублирование accessToken с основной БД'
      , sessionId             => lastRec.session_id
      , authCode              => 'new auth_code'
      , clientShortName       => testClientSName
      , redirectUri           => testClientUri
      , accessToken           => testClientMainSession.access_token
      , exactTokenFlag        => 1
      , execErrorCode         => -20005
      , execErrorMessageMask  =>
          'ORA-20005: Токен доступа уже использовался в основной БД (accessToken="%").%'
    );
    checkCase(
      'updateSession', 'Неуникальный refreshToken'
      , sessionId             => lastRec.session_id
      , authCode              => 'new auth_code'
      , clientShortName       => testClientSName
      , redirectUri           => testClientUri
      , refreshToken          => 'refresh token2'
      , execErrorCode         => -20006
      , execErrorMessageMask  =>
          'ORA-20006: Ошибка при обновлении пользовательской сессии %'
          || '%OA_SESSION_UK_REFRESH_TOKEN%'
    );
    checkCase(
      'updateSession', 'Дублирование refreshToken с основной БД'
      , sessionId             => lastRec.session_id
      , authCode              => 'new auth_code'
      , clientShortName       => testClientSName
      , redirectUri           => testClientUri
      , refreshToken          => testUsedMainRefreshToken
      , exactTokenFlag        => 1
      , execErrorCode         => -20006
      , execErrorMessageMask  =>
          'ORA-20006: Токен обновления уже использовался в основной БД (refreshToken="%").%'
    );
    checkCase(
      'createSession', 'minimal args'
      , authCode              => 'minimal args'
      , nextCaseUsedCount     => 99
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
    checkCase(
      'updateSession', 'Обновление сессии основной БД'
      , sessionId             => testClientMainSession.session_id
      , authCode              => testClientMainSession.auth_code
      , clientShortName       => testClientSName
      , redirectUri           => testClientUri
      , operatorId            => testClientMainSession.operator_id
      , accessToken           => testClientMainSession.access_token
      , refreshToken          => testClientMainSession.refresh_token
      , sessionToken          => Test_Pr || 'new ses_token'
      , exactTokenFlag        => 1
      , sessionCsv            =>
'
SESSION_ID      ; SESSION_TOKEN
--------------- ; -----------------------
$(sessionId)    ; $(Test_Pr)new ses_token
'
    );
    checkCase(
      'updateSession', 'Повторное обновление сессии основной БД'
      , sessionId             => testClientMainSession.session_id
      , authCode              => testClientMainSession.auth_code
      , clientShortName       => testClientSName
      , redirectUri           => testClientUri
      , operatorId            => testClientMainSession.operator_id
      , accessToken           => testClientMainSession.access_token
      , refreshToken          => testClientMainSession.refresh_token
      , sessionToken          => Test_Pr || 'new ses_token_2'
      , exactTokenFlag        => 1
      , sessionCsv            =>
'
SESSION_ID      ; SESSION_TOKEN
--------------- ; -----------------------
$(sessionId)    ; $(Test_Pr)new ses_token_2
'
    );
    checkCase(
      'findSession', 'before block main session'
      , clientShortName       => testClientSName
      , resultRowCount        => testClientSessionCount + 1
      , nextCaseUsedCount     => 2
    );
    checkCase(
      'blockSession', 'block main session'
      , sessionId             => testClientMainSession2.session_id
      , nextCaseUsedCount     => 99
      , sessionCsv            =>
'
SESSION_ID      ; IS_MANUAL_BLOCKED
--------------- ; -----------------
$(sessionId)    ;                 1
'
    );
    checkCase(
      'findSession', 'after block main session'
      , clientShortName       => testClientSName
      , resultRowCount        => testClientSessionCount + 0
    );
    checkCase(
      'updateSession', 'unblock after block main session'
      , sessionId             => testClientMainSession2.session_id
      , authCode              => 'unblock after block'
      , clientShortName       => testClientSName
      , sessionCsv            =>
'
SESSION_ID      ; AUTH_CODE                     ; IS_MANUAL_BLOCKED
--------------- ; ----------------------------- ; -----------------
$(sessionId)    ; $(Test_Pr)unblock after block ;
'
    );
    checkCase(
      'findSession', 'after unblock session'
      , clientShortName       => testClientSName
      , resultRowCount        => testClientSessionCount + 1
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



    /*
      Проверяет тестовый случай.
    */
    procedure checkCase(
      functionName varchar2
      , caseDescription varchar2
      , isExpired integer := null
      , operatorId integer := testOperId
      , resultRowCount integer := null
      , resultCsv clob := null
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
          replace( replace( replace( replace(
            srcCsv
            , '$(key_id)', testKey.key_id)
            , '$(public_key)', testKey.public_key)
            , '$(private_key)', testKey.private_key)
            , '$(date_ins)', testKey.date_ins)
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
      'getKey', 'exists key'
      , resultCsv             =>
'
KEY_ID     ; PUBLIC_KEY      ; PRIVATE_KEY     ; IS_EXPIRED ; DATE_INS
---------- ; --------------- ; --------------- ; ---------- ; ------------
$(key_id)  ; $(public_key)   ; $(private_key)  ;            ; $(date_ins)
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
    select
      a.*
    into
      testClientSName
      , testClientUri
    from
      (
      select
        cl.client_short_name
        , cu.client_uri
      from
        oa_client cl
        inner join oa_client_uri cu
          on cu.client_id = cl.client_id
      order by
        cl.client_id
        , cu.client_uri
      ) a
    where
      rownum <= 1
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
    Проверяет тестовый случай.
  */
  procedure checkCase(
    functionName varchar2
    , caseDescription varchar2
    , toTime timestamp with time zone := null
    , maxExecTime interval day to second := null
    , addBlockSessionCount integer := null
    , addBlockSessionDateFinish timestamp with time zone := null
    , resultNumber number := None_Integer
    , blockSessionCount integer := null
    , oldSessionCount integer := null
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
    begin
      case functionName
        when 'setSessionDateFinish' then
          resNum := pkg_OAuthInternal.setSessionDateFinish();
        when 'clearOldData' then
          resNum := pkg_OAuthInternal.clearOldData(
            toTime          => toTime
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
          , filterCondition   => 'is_blocked=1 and local_row_flag=1'
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
      , toTime                => toTime
      , oldSessionCount       => 0
      , nextCaseUsedCount     => 2
    );
    checkCase(
      'clearOldData', 'повторный запуск'
      , toTime                => toTime
      , resultNumber          => 0
      , oldSessionCount       => 0
      , nextCaseUsedCount     => 1
    );
    checkCase(
      'clearOldData', 'есть данные для обработки'
      , addBlockSessionCount  => 2
      , addBlockSessionDateFinish  => oldTime
      , toTime                => toTime
      , resultNumber          => 2
      , oldSessionCount       => 0
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
