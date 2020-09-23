import React from 'react';
import { OAuthWebContext } from '@jfront/oauth-ui';
import AppRouter from './main/AppRouter';
import { UserContextProvider } from './user/UserContextProvider';
import axios from 'axios'

function App() {
  return (
    <OAuthWebContext
        clientId={'OAuthClient'}
        redirectUri={`${process.env.NODE_ENV === 'development' ? "http://localhost:3000/oauth": `/oauth/oauth`}`}
        oauthContextPath={`${process.env.NODE_ENV === 'development' ? 'http://localhost:8080/oauth/api' : `/oauth/api`}`}
        axiosInstance={axios}
        configureAxios>
      <UserContextProvider baseUrl={`${process.env.NODE_ENV === 'development' ? 'http://localhost:8080/oauth/api' : `/oauth/api`}`}>
        <AppRouter/>
      </UserContextProvider>
    </OAuthWebContext>
  );
}

export default App;
