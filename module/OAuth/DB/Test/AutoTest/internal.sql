set feedback off

begin
  pkg_OAuthTest.testInternal(
    testCaseNumber => '&testCaseNumber'
    , saveDataFlag => '&saveDataFlag'
  );
end;
/

set feedback on
