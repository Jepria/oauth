drop sequence oa_session_seq
/

alter table
  oa_session
drop constraint
  oa_session_pk
drop index
/

declare

  updateFlag integer;

begin
  select
    count(*)
  into updateFlag
  from
    oa_session t
  where
    mod( t.session_id, 2) = 0
    and rownum <= 1
  ;
  if updateFlag = 1 then
    update
      oa_session d
    set
      -- делаем нечетными
      d.session_id = d.session_id * 2 - 1
    ;
    dbms_output.put_line(
      'updated: ' || sql%rowcount
    );
    commit;
  end if;
end;
/

alter table
  oa_session
add (
  constraint oa_session_pk primary key
    ( session_id)
    using index tablespace &indexTablespace
)
/



declare

  startValue integer;

begin
  select
    coalesce( max( t.session_id) + 2, 1)
  into startValue
  from
    oa_session t
  ;
  execute immediate
'create sequence
  oa_session_seq
start with ' || startValue || ' increment by 2
'
  ;
  dbms_output.put_line(
    'oa_session_seq: created, start with: ' || startValue
  );
end;
/
