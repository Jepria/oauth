-- script: Install/Grant/Last/grant-external.sql
-- ������ ���� � �������� ��������� �� ������� ������ OAuth ��� ������� �������

-- ���������: ��� ������������ ��� ������ ����

define UserName = "&1"

grant execute on pkg_OAuthExternal to &UserName;

create or replace synonym &UserName..pkg_OAuthExternal for pkg_OAuthExternal;

undefine toUserName
