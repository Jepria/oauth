import React from 'react';
import { OAuthWebContext } from '@jfront/oauth-ui';
import AppRouter from './main/AppRouter';
import { UserContextProvider } from './user/UserContextProvider';
import axios from 'axios'
import { Provider } from 'react-redux';
import { configureStore } from './redux/configureStore';
import { sagas, reducers } from './redux/store';

function App() {

  const store = configureStore({}, sagas, reducers);

  return (
    <OAuthWebContext
      clientId={'OAuthClient'}
      redirectUri={`${process.env.NODE_ENV === 'development' ? "http://localhost:3000/oauth" : `/oauth/oauth`}`}
      oauthContextPath={`${process.env.NODE_ENV === 'development' ? 'http://localhost:8080/oauth/api' : `/oauth/api`}`}
      axiosInstance={axios}
      configureAxios>
      <Provider store={store}>
        <UserContextProvider baseUrl={`${process.env.NODE_ENV === 'development' ? 'http://localhost:8080/oauth/api' : `/oauth/api`}`}>
          <AppRouter />
        </UserContextProvider>
      </Provider>
    </OAuthWebContext>
  );
}

export default App;
