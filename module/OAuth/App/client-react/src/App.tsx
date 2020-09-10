import React from 'react';
import { OAuthWebContext } from '@jfront/oauth-ui';
import AppRouter from './main/AppRouter';
import { UserContextProvider } from './user/UserContextProvider';
import axios from 'axios'

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
        redirectUri={`${process.env.NODE_ENV === 'development' ? "http://localhost:3000/oauth": `/oauth/oauth`}`}
        oauthContextPath={`${process.env.NODE_ENV === 'development' ? 'http://localhost:8082/oauth/api' : `/oauth/api`}`}
        axiosInstance={axios}
        configureAxios>
      <UserContextProvider baseUrl={`${process.env.NODE_ENV === 'development' ? 'http://localhost:8082/oauth/api' : `/oauth/api`}`}>
        <AppRouter/>
      </UserContextProvider>
    </OAuthWebContext>
  );
}

export default App;
