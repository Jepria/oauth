import React from 'react';
import { OAuthSecurityProvider } from './security/OAuthSecurityContext';
import AppRouter from './main/AppRouter';

function getOrigin() {
  if (!window.location.origin) {
    /** IE 10 support **/
    return window.location.protocol + "//" 
      + window.location.hostname 
      + (window.location.port ? ':' + window.location.port : '');
  } else {
    return window.location.origin;
  }
}

function App() {
  return (
    <OAuthSecurityProvider
        clientId={'OAuthRFI'}
        redirectUri={`${getOrigin()}${process.env.NODE_ENV === 'development' ? '' : `${process.env.PUBLIC_URL}`}/oauth`}
        oauthContextPath={`${process.env.NODE_ENV === 'development' ? 'http://localhost:8082/oauth/api' : `${getOrigin()}/oauth/api`}`}>
      <AppRouter/>
    </OAuthSecurityProvider>
  );
}

export default App;
