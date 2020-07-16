set feedback off

begin
  pkg_OAuthTest.testUserApi(
    testCaseNumber => '&testCaseNumber'
    , saveDataFlag => '&saveDataFlag'
  );
end;
/

set feedback on
