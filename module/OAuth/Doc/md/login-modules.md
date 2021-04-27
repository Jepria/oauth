#Создание кастомизированных логин-модулей

## Введение

Для реализации дополнительных или расширенных процессов аутентификации 
(DFA, дополнительные проверки, авторизация через сторонние сервисы) 
потребуется добавить логин модуль согласно данной документации.

## Регистрация логин модуля
Зарегистрировать логин модуль через вызов пакетной функции или интерфейс. 
Добавить ему тип авторизации _client\_credentials_ и роли _OALoginModule,OAViewSession,OAEditSession_.

``` sql
pkg_OAuth.createClient(clientShortName   => 'LoginModule',
                       clientName        => 'LoginModule',
                       clientNameEn      => 'LoginModule',
                       applicationType   => 'web',
                       grantTypeList     => 'client_credentials',
                       roleShortNameList => 'OALoginModule,OAViewSession,OAEditSession',
                       loginModuleUri    => null,
                       operatorId        => 1);
```
## Изменения в мета-информации о приложении
В описание приложения в системе **OAuth** добавлено новое поле _loginModuleUri_. 
Оно содержит в себе URL стартовой точки аутентификации. 
Если значение поля _null_, то используется стандартная аутентификация **AccessOperator**.
``` sql
pkg_Oauth.createClient(clientShortName   => 'Client',
                       clientName        => 'Client',
                       clientNameEn      => 'Client',
                       applicationType   => 'web',
                       grantTypeList     => 'authorization_code',
                       roleShortNameList => null,
                       loginModuleUri    => '/login-module',
                       operatorId        => 1);
```
## Принцип работы логин модуля
Предположим, что у нас существует зарегистрированное в **OAuth** приложение:
```
pkg_Oauth.createClient(clientShortName   => 'Client',
                       clientName        => 'Client',
                       clientNameEn      => 'Client En',
                       applicationType   => 'web',
                       grantTypeList     => 'authorization_code,refresh_token',
                       roleShortNameList => null,
                       loginModuleUri    => '/login',
                       operatorId        => 1);
```
```
pkg_Oauth.createClientUri(clientShortName => 'Client',
                          clientUri       => '/oauth-callback',
                          operatorId      => 1);
```
И логин-модуль:
```
зkg_Oauth.createClient(clientShortName   => 'LoginModule',
                        clientName        => 'Login',
                        clientNameEn      => 'Login',
                        applicationType   => 'web',
                        grantTypeList     => 'client_credentials',
                        roleShortNameList => 'OALoginModule,OAViewSession,OAEditSession',
                        loginModuleUri    => null,
                        operatorId        => 1);
```
Тогда можно следовать следующему алгоритму:

1. OAuth Authorization Endpoint (/authorize) инициирует авторизационную сессию и 
    переводит пользователя на страницу логина, указанную в loginModuleUri в нашем случае 
    /login?response\_type=code&client\_id=Client&redirect\_uri=/oauth-callback&state=state&session\_id=SID
    
    Параметры, переданные на логин страницу, потребуются в дальнейшем для подтверждения 
    аутентификации в OAuth (мета информация о сессии)

2. Пользователь проходит аутентификацию.
3. Если все проверки пройдены, то:
    1. Сервис логин модуля получает *clientSecret* по *clientId* вызовом следующей функции:
        ``` sql
        ? := pkg_OAuth.findClient(
                clientShortName => LoginModule,
                maxRowCount => 1,
                operatorId => 1
              );
        end;
        ```
        Возвращается курсор с описанием клиентского приложения, нужное поле **CLIENT\_SECRET**.
        В _JepRia_ есть утилитарный класс OAuthDBHelper, который реализует этот функционал.
        Пункт 2.i можно пропустить, если указать client\_secret из базы явным образом,
        например, через переменную окружения Tomcat. Но это потребует конфигурации на всех окружениях. 
        Потому что client\_secret везде будет разный.

4. Логин модуль обновляет сессию авторизации 
    вызовом сервиса _…/oauth/api/session/{sessionId}_ с мета-информацией о пользователе.
    В HTTP заголовок Authorization нужно подставить значение: ```Basic base64(client_id:client_secret)```
    передается JSON вида:
    ``` json 
        {
            "state": "state",
            "operatorId": "integer",
            "username": "string",
            "responseType": "code",
            "redirectUri":"/oauth-callback", 
            "clientId": "Client",
        }
    ```
5. При успешном подтверждении авторизации OAuth вернет ответ с кодом 302 и в HTTP заголовок Location = /oauth-callback?code=authorization\_code&state=state
6. Нужно перевести пользователя на callback URL из заголовка Location
7. Клиентская часть запрашивает токен POST запросом к /oauth/api/token

## Пример реализации
Пример реализации можно посмотреть в Examples/simple-jsp-login-module.

