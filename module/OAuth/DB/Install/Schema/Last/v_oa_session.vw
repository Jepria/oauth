-- view: v_oa_session
-- Пользовательские сессии (актуальные данные).
--
create or replace force view
  v_oa_session
as
select /*+ index( t) */
  -- SVN root: JEP/Module/OAuth
  t.session_id
  , t.auth_code
  , t.client_id
  , t.redirect_uri
  , t.date_finish
  , t.operator_id
  , t.code_challenge
  , t.access_token
  , t.access_token_date_ins
  , t.access_token_date_finish
  , t.refresh_token
  , t.refresh_token_date_ins
  , t.refresh_token_date_finish
  , t.session_token
  , t.session_token_date_ins
  , t.session_token_date_finish
  , t.is_manual_blocked
  , case when
        t.session_token is null
          and t.refresh_token is null
          and t.access_token_date_finish < systimestamp
        or t.session_token is not null
          and t.session_token_date_finish < systimestamp
        or t.refresh_token is not null
          and t.refresh_token_date_finish < systimestamp
        or t.is_manual_blocked = 1
      then 1
      else 0
    end
    as is_blocked
    -- выражение из индекса oa_session_ix_actual_date_ins
  , case when t.date_finish is null then t.date_ins end as date_ins
  , t.operator_id_ins
from
  oa_session t
where
  t.date_finish is null
/



comment on table v_oa_session is
  'Пользовательские сессии (актуальные данные) [ SVN root: JEP/Module/OAuth]'
/
comment on column v_oa_session.is_blocked is
  'Признак заблокированной сессии (1 да, 0 нет)'
/
@oms-run set-session-comment.sql v_oa_session
