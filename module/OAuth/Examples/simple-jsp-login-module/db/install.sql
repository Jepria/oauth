declare
  rowCount number;
  resultId number;
begin
  select count(*) into rowCount from oa_client t where t.client_short_name like 'SimpleClient';
  if rowCount = 0 then
    resultId := Pkg_Oauth.createClient(clientShortName   => 'SimpleClient',
                                  clientName        => 'SimpleClient',
                                  clientNameEn      => 'SimpleClient',
                                  applicationType   => 'web',
                                  grantTypeList     => 'authorization_code',
                                  roleShortNameList => null,
                                  loginModuleUri    => '/login-module',
                                  operatorId        => 1);
    resultId := Pkg_Oauth.createClientUri(clientShortName => 'SimpleClient',
                                     clientUri       => '/client',
                                     operatorId      => 1);
    commit;
  end if;
  select count(*) into rowCount from oa_client t where t.client_short_name like 'SimpleClient';
  if rowCount = 0 then
    resultId := Pkg_Oauth.createClient(clientShortName   => 'SimpleJspLoginModule',
                                  clientName        => 'SimpleJspLoginModule',
                                  clientNameEn      => 'SimpleJspLoginModule',
                                  applicationType   => 'web',
                                  grantTypeList     => 'client_credentials',
                                  roleShortNameList => 'OALoginModule,OAViewSession,OAEditSession',
                                  loginModuleUri    => null,
                                  operatorId        => 1);
    commit;
  end if;
end;