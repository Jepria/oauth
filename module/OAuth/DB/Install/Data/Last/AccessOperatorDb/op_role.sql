-- script: Install/Data/Last/AccessOperatorDb/op_role.sql
-- Создает роли, используемые модулем.
--

declare

  -- Число изменений
  nChanged integer := 0;



  /*
    Добавление или обновление роли.
  */
  procedure mergeRole(
    roleShortName varchar2
    , roleName varchar2
    , roleNameEn varchar2
    , description varchar2 := null
  )
  is

    changedFlag integer;

  begin
    changedFlag := pkg_AccessOperator.mergeRole(
      roleShortName   => roleShortName
      , roleName      => roleName
      , roleNameEn    => roleNameEn
      , description   => description
    );
    if changedFlag = 1 then
      dbms_output.put_line(
        'changed role: ' || roleShortName
      );
      nChanged := nChanged + 1;
    else
      dbms_output.put_line(
        'checked role: ' || roleShortName
      );
    end if;
  end mergeRole;



-- main
begin
  mergeRole(
    roleShortName => 'OAViewClient'
    , roleName    =>
        'OAuth: просмотр зарегистрированных клиентских приложений'
    , roleNameEn  =>
        'OAuth: OAViewClient'
  );
  mergeRole(
    roleShortName => 'OACreateClient'
    , roleName    =>
        'OAuth: регистрация клиентских приложений'
    , roleNameEn  =>
        'OAuth: OACreateClient'
  );
  mergeRole(
    roleShortName => 'OAEditClient'
    , roleName    =>
        'OAuth: редактирование учетных данных клиентских приложений'
    , roleNameEn  =>
        'OAuth: OAEditClient'
  );
  mergeRole(
    roleShortName => 'OADeleteClient'
    , roleName    =>
        'OAuth: удаление клиентских приложений'
    , roleNameEn  =>
        'OAuth: OADeleteClient'
  );
  mergeRole(
    roleShortName => 'OAViewSession'
    , roleName    =>
        'OAuth: просмотр пользовательских сессий'
    , roleNameEn  =>
        'OAuth: OAViewSession'
  );
  mergeRole(
    roleShortName => 'OACreateSession'
    , roleName    =>
        'OAuth: создание пользовательских сессий'
    , roleNameEn  =>
        'OAuth: OACreateSession'
  );
  mergeRole(
    roleShortName => 'OAEditSession'
    , roleName    =>
        'OAuth: редактирование пользовательских сессий'
    , roleNameEn  =>
        'OAuth: OAEditSession'
  );
  mergeRole(
    roleShortName => 'OADeleteSession'
    , roleName    =>
        'OAuth: удаление пользовательских сессий'
    , roleNameEn  =>
        'OAuth: OADeleteSession'
  );
  mergeRole(
    roleShortName => 'OAUpdateKey'
    , roleName    =>
        'OAuth: обновление ключей'
    , roleNameEn  =>
        'OAuth: OAUpdateKey'
  );
  mergeRole(
    roleShortName => 'OAViewKey'
    , roleName    =>
        'OAuth: просмотр ключей'
    , roleNameEn  =>
        'OAuth: OAViewKey'
  );

  dbms_output.put_line(
    'roles changed: ' || nChanged
  );
  commit;
end;
/
