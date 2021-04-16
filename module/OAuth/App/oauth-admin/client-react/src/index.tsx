import 'core-js/stable';
import 'react-app-polyfill/ie11';
import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import * as serviceWorker from './serviceWorker';
import './i18n'
import {OAuthSecuredFragment, OAuthWebContext} from "@jfront/oauth-ui";
import axios from "axios";
import {Provider} from "react-redux";
import {ErrorNotification} from "@jfront/ui-core";
import {UserContextProvider} from "@jfront/oauth-user";
import configureStore from "./app/store/configureStore";

if (process.env.NODE_ENV !== 'development') {
  axios.defaults.headers['Pragma'] = 'no-cache';
}
const store = configureStore();


ReactDOM.render(
  // <React.StrictMode>
  <OAuthWebContext
    clientId={'OAuthClient'}
    redirectUri={`${process.env.NODE_ENV === 'development' ? "http://localhost:3000/oauth-admin/ui/oauth" : `/oauth-admin/oauth`}`}
    oauthContextPath={`${process.env.NODE_ENV === 'development' ? 'http://localhost:8080/oauth/api' : `/oauth/api`}`}
    axiosInstance={axios}>
    <Provider store={store}>
      <ErrorNotification>
        <UserContextProvider
          baseUrl={`${process.env.NODE_ENV === 'development' ? 'http://localhost:8080/oauth-admin/api' : `/oauth-admin/api`}`}>
          <App/>
        </UserContextProvider>
      </ErrorNotification>
    </Provider>
  </OAuthWebContext>,
  // </React.StrictMode>
  document.getElementById("root")
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
