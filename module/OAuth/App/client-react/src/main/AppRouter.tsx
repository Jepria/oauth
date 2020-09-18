import React from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";
import { Provider } from 'react-redux';
import ClientRoute from './client/ClientModuleRoute';
import { configureStore } from '../redux/configureStore';
import { sagas, reducers } from './store';
import SessionRoute from './session/SessionRoute';
import KeyRoute from './key/KeyRoute';
import { OAuthSecuredFragment } from '@jfront/oauth-ui';

const AppRouter: React.FC = () => {

  const store = configureStore({}, sagas, reducers);

  return (
    <OAuthSecuredFragment>
      <Provider store={store}>
        <Router basename={`${process.env.NODE_ENV === 'development' ? '' : process.env.PUBLIC_URL}`}>
          <Switch>
            <Route path="/ui/client">
              <ClientRoute />
            </Route>
            <Route path="/ui/key">
              <KeyRoute/>
            </Route>
            <Route path="/ui/session">
              <SessionRoute/>
            </Route>
          </Switch>
        </Router>
      </Provider>
    </OAuthSecuredFragment>
  );
}

export default AppRouter;