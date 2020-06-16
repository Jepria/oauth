-- script: Install/Data/Last/op_group.sql
-- Создает группы ролей, используемые модулем.
--

declare

  -- Число добавленный групп
  nCreated integer := 0;

  -- Число ранее созданных групп
  nExists integer := 0;



  /*
    Добавляет группу в случае ее отсутствия (наличие проверяется по полю
    group_name) либо обновляет ее данные при наличии.
    Добавляет роли в группу в случае их отсутствия.
  */
  procedure setGroup(
    groupName varchar2
    , groupNameEn varchar2
    , isGrantOnly integer := null
    , roleList cmn_string_table_t := null
  )
  is

    cursor groupCur is
      select
        s.group_name
        , s.group_name_en
        , s.is_grant_only
        , gr.group_id
        , coalesce(
            (
            select
              1
            from
              op_group t
            where
              t.group_id = gr.group_id
              and t.group_name = s.group_name
              and t.group_name_en = s.group_name_en
              and t.is_grant_only = s.is_grant_only
            )
            , 0
          )
          as equal_flag
      from
        (
        select
          groupName as group_name
          , groupNameEn as group_name_en
          , coalesce( isGrantOnly, 0) as is_grant_only
        from
          dual
        ) s
        left join op_group gr
          on upper( gr.group_name) = upper( s.group_name)
    ;

    -- Роли для включения в группу
    cursor roleCur( groupId integer) is
      select
        a.*
        , r.role_id
        , coalesce(
            (
            select
              1
            from
              op_group_role gr
            where
              gr.group_id = groupId
              and gr.role_id = r.role_id
            )
            , 0
          )
          as is_exists
      from
        (
        select
          trim( t.column_value) as short_name
        from
          table( roleList) t
        ) a
        left outer join op_role r
          on r.short_name = a.short_name
      order by
        a.short_name
    ;

    -- Id группы
    groupId integer;

    isNewGroup boolean := false;

    nAddRole pls_integer := 0;

  -- setGroup
  begin

    -- Создание или обновление группы
    for rec in groupCur loop
      if rec.group_id is null then
        groupId := pkg_AccessOperator.createGroup(
          groupName       => rec.group_name
          , groupNameEn   => rec.group_name_en
          , isGrantOnly   => rec.is_grant_only
          , operatorId    => pkg_Operator.getCurrentUserId()
        );
        isNewGroup := true;
        nCreated := nCreated + 1;
      else
        if rec.equal_flag = 0 then
          pkg_AccessOperator.updateGroup(
            groupId         => rec.group_id
            , groupName     => rec.group_name
            , groupNameEn   => rec.group_name_en
            , isGrantOnly   => rec.is_grant_only
            , operatorId    => pkg_Operator.getCurrentUserId()
          );
          dbms_output.put_line(
            'group "' || groupName || '": updated'
          );
        end if;
        groupId := rec.group_id;
        nExists := nExists + 1;
      end if;
    end loop;

    -- Добавление ролей в группу
    for rec in roleCur( groupId) loop
      if rec.role_id is null then
        raise_application_error(
          pkg_Error.ProcessError
          , 'Не найдена роль для добавления в группу ('
            || ' short_name="' || rec.short_name || '"'
            || ').'
        );
      end if;
      if rec.is_exists = 0 then
        pkg_AccessOperator.createGroupRole(
          groupId         => groupId
          , roleId        => rec.role_id
          , operatorId    => pkg_Operator.getCurrentUserId()
        );
        nAddRole := nAddRole + 1;
        if not isNewGroup then
          dbms_output.put_line(
            'group "' || groupName || '": role added: ' || rec.short_name
          );
        end if;
      end if;
    end loop;

    if isNewGroup then
      dbms_output.put_line(
        'group created: "' || groupName || '" ( group_id=' || groupId || ')'
        || ', roles added: ' || nAddRole
      );
    end if;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , 'Ошибка при добавлении группы ('
        || ' groupName="' || groupName || '"'
        || ').'
      , true
    );
  end setGroup;



-- main
begin
  setGroup(
    groupName     => 'OAAdmin'
    , groupNameEn => 'OAuth: администратор'
    , roleList    =>
        cmn_string_table_t(
          'OAViewClient'
          , 'OACreateClient'
          , 'OAEditClient'
          , 'OADeleteClient'
          , 'OAViewSession'
          , 'OADeleteSession'
          , 'OAUpdateKey'
        )
  );

  dbms_output.put_line(
    'groups created: ' || nCreated
    || ' ( already exists: ' || nExists || ')'
  );
  commit;
end;
/

