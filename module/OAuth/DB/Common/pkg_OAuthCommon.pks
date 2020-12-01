create or replace package pkg_OAuthCommon is
/* package: pkg_OAuthCommon
  Пакет модуля OAuth, устанавливаемый в основную и резервную БД.

  SVN root: JEP/Module/OAuth
*/



/* group: Константы */

/* const: Module_Name
  Название модуля, к которому относится пакет.
*/
Module_Name constant varchar2(30) := 'OAuth';



/* group: Настроечные параметры */

/* const: CryptoKey_OptSName
  Краткое наименование параметра
  "Ключ шифрования, используемый в модуле"
*/
CryptoKey_OptSName constant varchar2(50) := 'CryptoKey';



/* group: Роли */

/* const: OAViewClient_RoleSName
  Краткое имя роли "Просмотр зарегистрированных клиентских приложений".
*/
OAViewClient_RoleSName constant varchar2(50) := 'OAViewClient';

/* const: OACreateClient_RoleSName
  Краткое имя роли "Регистрация клиентских приложений".
*/
OACreateClient_RoleSName constant varchar2(50) := 'OACreateClient';

/* const: OAEditClient_RoleSName
  Краткое имя роли "Редактирование учетных данных клиентских приложений".
*/
OAEditClient_RoleSName constant varchar2(50) := 'OAEditClient';

/* const: OADeleteClient_RoleSName
  Краткое имя роли "Удаление клиентских приложений".
*/
OADeleteClient_RoleSName constant varchar2(50) := 'OADeleteClient';

/* const: OAViewSession_RoleSName
  Краткое имя роли "Просмотр пользовательских сессий".
*/
OAViewSession_RoleSName constant varchar2(50) := 'OAViewSession';

/* const: OACreateSession_RoleSName
  Краткое имя роли "Создание пользовательских сессий".
*/
OACreateSession_RoleSName constant varchar2(50) := 'OACreateSession';

/* const: OAEditSession_RoleSName
  Краткое имя роли "Редактирование пользовательских сессий".
*/
OAEditSession_RoleSName constant varchar2(50) := 'OAEditSession';

/* const: OADeleteSession_RoleSName
  Краткое имя роли "Удаление пользовательских сессий".
*/
OADeleteSession_RoleSName constant varchar2(50) := 'OADeleteSession';

/* const: OAUpdateKey_RoleSName
  Краткое имя роли "Обновление ключей".
*/
OAUpdateKey_RoleSName constant varchar2(50) := 'OAUpdateKey';

/* const: OAViewKey_RoleSName
  Краткое имя роли "Просмотр ключей".
*/
OAViewKey_RoleSName constant varchar2(50) := 'OAViewKey';



/* group: Коды ошибок */

/* iconst: UniqueViolated_ErrCode
  Код ошибки нарушения уникальности.
*/
UniqueViolated_ErrCode constant pls_integer := -1;

/* iconst: IllegalArgument_ErrCode
  Код ошибки "Указано некорректное значение входного параметра".
*/
IllegalArgument_ErrCode constant pls_integer := -20002;

/* iconst: ClientWrong_ErrCode
  Код ошибки "Указаны неверные данные клиентского приложения".
*/
ClientWrong_ErrCode constant pls_integer := -20003;

/* iconst: ClientUriWrong_ErrCode
  Код ошибки "Указан несуществующий URI клиентского приложения".
*/
ClientUriWrong_ErrCode constant pls_integer := -20004;

/* iconst: AccessTokenWrong_ErrCode
  Код ошибки "Нарушено ограничение уникальности accessToken".
*/
AccessTokenWrong_ErrCode constant pls_integer := -20005;

/* iconst: RefreshTokenWrong_ErrCode
  Код ошибки "Нарушено ограничение уникальности refreshToken".
*/
RefreshTokenWrong_ErrCode constant pls_integer := -20006;



/* group: Функции */



/* group: Шифрование данных */

/* pfunc: encrypt
  Возвращает зашифрованное значение.

  Параметры:
  inputString                 - входная строка

  Возврат:
  зашифрованная строка.

  ( <body::encrypt>)
*/
function encrypt(
  inputString varchar2
)
return varchar2;

/* pfunc: decrypt
  Возвращает расшифрованное значение.

  Параметры:
  inputString                 - входная строка

  Возврат:
  расшифрованная строка.

  ( <body::decrypt>)
*/
function decrypt(
  inputString varchar2
)
return varchar2;



/* group: Клиентское приложение */

/* pfunc: findClient
  Поиск клиентского приложения.

  Параметры:
  clientShortName             - Краткое наименование приложения
                                (по умолчанию без ограничений)
  clientName                  - Наименование клиентского приложения на языке
                                по умолчанию
                                (по умолчанию без ограничений)
  clientNameEn                - Наименование клиентского приложения на
                                английском языке
                                (по умолчанию без ограничений)
  maxRowCount                 - Максимальное количество выводимых строк
                                (по умолчанию без ограничений)
  operatorId                  - Id оператора, выполняющего операцию

  Возврат (курсор):
  client_short_name           - Краткое наименование приложения
  client_secret               - Случайная криптографически устойчивая строка
  client_name                 - Имя клиентского приложения
  client_name_en              - Имя клиентского приложения на английском
  application_type            - Тип клиентского приложения
  date_ins                    - Дата создания записи
  create_operator_id          - Id оператора, создавшего запись
  create_operator_name        - Имя оператора, создавшего запись
  create_operator_name_en     - Имя оператора, создавшего запись на англ.
  change_date                 - Дата последнего изменения записи
  change_operator_id          - Id оператора, изменившего запись
  change_operator_name        - Имя оператора, изменившего запись
  change_operator_name_en     - Имя оператора, изменившего запись на англ.
  client_operator_id          - ID оператора клиентского приложения

  (сортировка по date_ins в обратном порядке)

  ( <body::findClient>)
*/
function findClient(
  clientShortName varchar2 := null
  , clientName varchar2 := null
  , clientNameEn varchar2 := null
  , maxRowCount integer := null
  , operatorId integer
)
return sys_refcursor;

/* pfunc: getClientGrant
  Возвращает список грантов клиентского приложения.

  Параметры:
  clientShortName             - Краткое наименование приложения
  operatorId                  - Id оператора, выполняющего операцию

  Возврат (курсор):
  client_short_name           - Краткое наименование приложения
  grant_type                  - Тип гранта

  (сортировка по grant_type)

  ( <body::getClientGrant>)
*/
function getClientGrant(
  clientShortName varchar2
  , operatorId integer
)
return sys_refcursor;

/* pfunc: getClientId
  Возвращает Id клиентского приложения либо выбрасывает исключение
  <ClientWrong_ErrCode> если приложение не найдено.

  Параметры:
  clientShortName             - Краткое наименование приложения

  ( <body::getClientId>)
*/
function getClientId(
  clientShortName varchar2
)
return integer;



/* group: URI клиентского приложения */

/* pfunc: findClientUri
  Поиск URI клиентского приложения.

  Параметры:
  clientUriId                 - Id записи с URI клиентского приложения
                                (по умолчанию без ограничений)
  clientShortName             - Краткое наименование приложения
                                (по умолчанию без ограничений)
  maxRowCount                 - Максимальное количество выводимых строк
                                (по умолчанию без ограничений)
  operatorId                  - Id оператора, выполняющего операцию

  Возврат (курсор):
  client_uri_id               - Id записи с URI клиентского приложения
  client_short_name           - Краткое наименование приложения
  client_uri                  - URI клиентского приложения
  date_ins                    - Дата создания записи
  operator_id                 - Id оператора, создавшего запись
  operator_name               - Имя оператора, создавшего запись
  operator_name_en            - Имя оператора, создавшего запись на англ.

  (сортировка по date_ins в обратном порядке)

  ( <body::findClientUri>)
*/
function findClientUri(
  clientUriId integer := null
  , clientShortName varchar2 := null
  , maxRowCount integer := null
  , operatorId integer
)
return sys_refcursor;



/* group: Пользовательские сессии */

/* pfunc: createSession
  Создает пользовательскую сессию.

  Параметры:
  authСode                    - Авторизационный код (One-Time-Password)
  clientShortName             - Краткое наименование приложения
  redirectUri                 - URI для перенаправления
  operatorId                  - Id пользователя, владельца сессии
  codeChallenge               - Криптографически случайная строка,
                                используется при авторизации по PKCE
  accessToken                 - Уникальный UUID токена доступа
  accessTokenDateIns          - Дата создания токена доступа
  accessTokenDateFinish       - Дата окончания действия токена доступа
  refreshToken                - Уникальный UUID токена обновления
  refreshTokenDateIns         - Дата создания токена обновления
  refreshTokenDateFinish      - Дата окончания действия токена обновления
  sessionToken                - UUID токена сессии
  sessionTokenDateIns         - Дата создания токена сессии
  sessionTokenDateFinish      - Дата окончания действия токена сессии
  operatorIdIns               - Id оператора, выполняющего операцию

  Дополнительные параметры в резервной БД:
  clientId                    - Id клиентского приложения
                                (NULL если не определено (по умолчанию))

  Возврат:
  Id созданной записи

  ( <body::createSession>)
*/
function createSession(
  authCode varchar2
  , clientShortName varchar2
  , redirectUri varchar2
  , operatorId integer
  , codeChallenge varchar2
  , accessToken varchar2
  , accessTokenDateIns timestamp with time zone
  , accessTokenDateFinish timestamp with time zone
  , refreshToken varchar2
  , refreshTokenDateIns timestamp with time zone
  , refreshTokenDateFinish timestamp with time zone
  , sessionToken varchar2
  , sessionTokenDateIns timestamp with time zone
  , sessionTokenDateFinish timestamp with time zone
  , operatorIdIns integer
  , clientId integer := null
)
return integer;

/* pproc: updateSession
  Обновляет пользовательскую сессию.

  Параметры:
  sessionId                   - Id пользовательской сессии
  authСode                    - Авторизационный код (One-Time-Password)
  clientShortName             - Краткое наименование приложения
  redirectUri                 - URI для перенаправления
  operatorId                  - Id пользователя, владельца сессии
  codeChallenge               - Криптографически случайная строка,
                                используется при авторизации по PKCE
  accessToken                 - Уникальный UUID токена доступа
  accessTokenDateIns          - Дата создания токена доступа
  accessTokenDateFinish       - Дата окончания действия токена доступа
  refreshToken                - Уникальный UUID токена обновления
  refreshTokenDateIns         - Дата создания токена обновления
  refreshTokenDateFinish      - Дата окончания действия токена обновления
  sessionToken                - UUID токена сессии
  sessionTokenDateIns         - Дата создания токена сессии
  sessionTokenDateFinish      - Дата окончания действия токена сессии
  operatorIdIns               - Id оператора, выполняющего операцию

  Дополнительные параметры в резервной БД:
  clientId                    - Id клиентского приложения
                                (NULL если не определено (по умолчанию))

  ( <body::updateSession>)
*/
procedure updateSession(
  sessionId integer
  , authCode varchar2
  , clientShortName varchar2
  , redirectUri varchar2
  , operatorId integer
  , codeChallenge varchar2
  , accessToken varchar2
  , accessTokenDateIns timestamp with time zone
  , accessTokenDateFinish timestamp with time zone
  , refreshToken varchar2
  , refreshTokenDateIns timestamp with time zone
  , refreshTokenDateFinish timestamp with time zone
  , sessionToken varchar2
  , sessionTokenDateIns timestamp with time zone
  , sessionTokenDateFinish timestamp with time zone
  , operatorIdIns integer
  , clientId integer := null
);

/* pproc: blockSession
  Блокирует пользовательскую сессию.

  Параметры:
  sessionId                   - Id пользовательской сессии
  operatorIdIns               - Id оператора, выполняющего операцию

  ( <body::blockSession>)
*/
procedure blockSession(
  sessionId integer
  , operatorId integer
);

/* pfunc: findSession
  Поиск пользовательской сессии.

  Параметры:
  sessionId                   - Id пользовательской сессии
                                (по умолчанию без ограничений)
  authСode                    - Авторизационный код (One-Time-Password)
                                (по умолчанию без ограничений)
  clientShortName             - Краткое наименование приложения
                                (по умолчанию без ограничений)
  redirectUri                 - URI для перенаправления
                                (по умолчанию без ограничений)
  operatorId                  - Id пользователя, владельца сессии
                                (по умолчанию без ограничений)
  codeChallenge               - Криптографически случайная строка,
                                используется при авторизации по PKCE
                                (по умолчанию без ограничений)
  accessToken                 - Уникальный UUID токена доступа
                                (по умолчанию без ограничений)
  refreshToken                - Уникальный UUID токена обновления
                                (по умолчанию без ограничений)
  sessionToken                - UUID токена сессии
                                (по умолчанию без ограничений)
  maxRowCount                 - Максимальное количество выводимых строк
                                (по умолчанию без ограничений)
  operatorIdIns               - Id оператора, выполняющего операцию

  Возврат (курсор):
  session_id                  - Идентификатор записи
  auth_code                   - Авторизационный код (One-Time-Password)
  client_short_name           - Краткое наименование приложения
  client_name                 - Имя клиентского приложения
  client_name_en              - Имя клиентского приложения на английском
  redirect_uri                - URI для перенаправления
  operator_id                 - Id пользователя, владельца сессии
  operator_name               - Имя пользователя, владельца сессии
  operator_login              - Логин пользователя, владельца сессии
  code_challenge              - Криптографически случайная строка,
                                используется при авторизации по PKCE
  access_token                - Уникальный UUID токена доступа
  access_token_date_ins       - Дата создания токена доступа
  access_token_date_finish    - Дата окончания действия токена доступа
  refresh_token               - Уникальный UUID токена обновления
  refresh_token_date_ins      - Дата создания токена обновления
  refresh_token_date_finish   - Дата окончания действия токена обновления
  session_token               - Уникальный UUID токена сессии
  session_token_date_ins      - Дата создания токена сессии
  session_token_date_finish   - Дата окончания действия токена сессии
  date_ins                    - Дата создания записи
  operator_id_ins             - ID пользователя, создавшего запись

  (сортировка по date_ins в обратном порядке)

  ( <body::findSession>)
*/
function findSession(
  sessionId integer := null
  , authCode varchar2 := null
  , clientShortName varchar2 := null
  , redirectUri varchar2 := null
  , operatorId integer := null
  , codeChallenge varchar2 := null
  , accessToken varchar2 := null
  , refreshToken varchar2 := null
  , sessionToken varchar2 := null
  , maxRowCount integer := null
  , operatorIdIns integer
)
return sys_refcursor;



/* group: Хранилище ключей */

/* pfunc: getKey
  Возвращает ключи.

  Параметры:
  isExpired                   - Признак истекшего срока действия ключа
  operatorId                  - Id оператора, выполняющего операцию

  Возврат (курсор):
  key_id                      - Id записи с ключами
  public_key                  - Публичный ключ
  private_key                 - Приватный ключ
  is_expired                  - Признак истекшего срока действия ключа
  date_ins                    - Дата создания записи

  ( <body::getKey>)
*/
function getKey(
  isExpired integer
  , operatorId integer
)
return sys_refcursor;

end pkg_OAuthCommon;
/
