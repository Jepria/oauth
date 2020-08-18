import React from 'react';
import { OAuthWebContext } from '@jfront/oauth-ui';
import AppRouter from './main/AppRouter';
import { UserContextProvider } from './user/UserContextProvider';

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
    <OAuthWebContext
        clientId={'OAuthRFI'}
        redirectUri={`${getOrigin()}${process.env.NODE_ENV === 'development' ? '' : `${process.env.PUBLIC_URL}`}/oauth`}
        oauthContextPath={`${process.env.NODE_ENV === 'development' ? 'http://localhost:8082/oauth/api' : `${getOrigin()}/oauth/api`}`}>
      <UserContextProvider baseUrl={`${process.env.PUBLIC_URL}`}>
        <AppRouter/>
      </UserContextProvider>
    </OAuthWebContext>
  );
}

export default App;
