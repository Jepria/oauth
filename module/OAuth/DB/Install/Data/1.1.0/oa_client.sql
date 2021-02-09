declare

  Client_SName constant varchar2(50) := 'OAuthClient';

  operatorId integer := pkg_Operator.getCurrentUserId();

  foundFlag integer;

  clientId integer;
  clientUriId integer;

begin
  clientUriId := pkg_OAuth.createClientUri(
    clientShortName => Client_SName
    , clientUri => '/oauth-admin/oauth'
    , operatorId => operatorId
  );
  dbms_output.put_line(
    'client uri created: ' || Client_SName
    || ' (client_uri=/oauth-admin/oauth)'
  );
end;
/
