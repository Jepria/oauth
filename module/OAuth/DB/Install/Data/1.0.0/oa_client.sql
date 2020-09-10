declare

  Client_SName constant varchar2(50) := 'OAuthClient';

  operatorId integer := pkg_Operator.getCurrentUserId();

  foundFlag integer;

  clientId integer;
  clientUriId integer;

begin
  select
    count(*)
  into foundFlag
  from
    oa_client cl
  where
    cl.client_short_name = Client_SName
  ;
  if foundFlag = 0 then
    clientId := pkg_OAuth.createClient(
      clientShortName => Client_SName
      , clientName => 'OAuth 2.0'
      , clientNameEn => 'OAuth 2.0'
      , applicationType => 'web'
      , grantTypeList => 'authorization_code,password,refresh_token'
      , roleShortNameList => null
      , operatorId => operatorId
    );
    clientUriId := pkg_OAuth.createClientUri(
      clientShortName => Client_SName
      , clientUri => '/oauth/oauth'
      , operatorId => operatorId
    );
    dbms_output.put_line(
      'client created: ' || Client_SName
      || ' (client_id=' || clientId || ')'
    );
  end if;
end;
/
