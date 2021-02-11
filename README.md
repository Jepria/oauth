# JepRia OAuth 2.0
OAuth 2.0 RFC 6749 implementation based on JepRia REST and JFront UI frameworks
### Maven commands
Execute Maven CLI commands in module/OAuth/App
* __mvn clean__ - clean all packages
* __mvn clean -pl *submodule name*__ - clean one submodule with specified name
* __mvn test__ - run all tests 
* __mvn package -DskipITs__ - build module skipping integration tests
* __mvn tomcat7:deploy -pl oauth-server__ - deploy oauth authorization API to Tomcat
* __mvn tomcat7:deploy -pl oauth-admin__ - deploy oauth administration UI to Tomcat
### URL Reference
* */oauth/api/authorize - Authorization Endpoint
* */oauth/api/logout - Logout Endpoint
* */oauth/api/token - Token Endpoint
* */oauth/api/token/introspect - Token introspection Endpoint
* */oauth/api/token/revoke - Token revocation endpoint