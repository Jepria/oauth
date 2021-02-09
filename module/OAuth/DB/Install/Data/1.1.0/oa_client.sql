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
    clientUriId := pkg_OAuth.createClientUri(
      clientShortName => Client_SName
      , clientUri => '/oauth-admin/oauth'
      , operatorId => operatorId
    );
    dbms_output.put_line(
      'client created: ' || Client_SName
      || ' (client_id=' || clientId || ')'
    );
  end if;
end;
/
