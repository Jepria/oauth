# JepRia OAuth 2.0
OAuth 2.0 RFC 6749 implementation based on JepRia REST and JFront UI frameworks
### Maven commands
Execute Maven CLI commands in module/OAuth/App
* __mvn clean__ - clean all packages
* __mvn clean -pl *submodule name*__ - clean one submodule with specified name
* __mvn test__ - run all tests 
* __mvn package -DskipITs__ - build module skipping integration tests
* __mvn package -P standalone-build__ - build modules to separate wars, requires rewriting rules to map all path's to /oauth 
(profile standalone-build sets %PUBLIC_URL% for client-react to /oauth-react). React application can be served on Node JS server either. 
* __mvn tomcat7:deploy -N__ - deploy to Tomcat
### URL Reference
* */oauth/api/authorize - Authorization Endpoint
* */oauth/api/logout - Logout Endpoint
* */oauth/api/token - Token Endpoint
* */oauth/api/token/introspect - Token introspection Endpoint
* */oauth/api/token/revoke - Token revocation endpoint