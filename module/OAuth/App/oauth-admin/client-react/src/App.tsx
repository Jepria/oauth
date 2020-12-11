import React from 'react';
import { OAuthWebContext } from '@jfront/oauth-ui';
import AppRouter from './app/AppRouter';
import { UserContextProvider } from '@jfront/oauth-user';
import axios from 'axios'
import { Provider } from 'react-redux';
import { configureStore } from './app/store/configureStore';
import { sagas, reducers } from './app/store';
import { ErrorNotification } from './app/common/components/ErrorNotification';

function App() {

  const store = configureStore({}, sagas, reducers);

  return (
    <OAuthWebContext
      clientId={'OAuthClient'}
      redirectUri={`${process.env.NODE_ENV === 'development' ? "http://localhost:3000/oauth" : `/oauth-admin/oauth`}`}
      oauthContextPath={`${process.env.NODE_ENV === 'development' ? 'http://localhost:8080/oauth/api' : `/oauth/api`}`}
      axiosInstance={axios}
      configureAxios>
      <Provider store={store}>
        <UserContextProvider baseUrl={`${process.env.NODE_ENV === 'development' ? 'http://localhost:8080/oauth-admin/api' : `/oauth-admin/api`}`}>
          <ErrorNotification>
            <AppRouter />
          </ErrorNotification>
        </UserContextProvider>
      </Provider>
    </OAuthWebContext>
  );
}

export default App;
