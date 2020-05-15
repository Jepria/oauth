import React from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";
import { Provider } from 'react-redux';
import { OAuthProtectedFragment } from '../security/OAuthSecurityContext';
import ClientRoute from './client/ClientModuleRoute';
import { configureStore } from '../redux/configureStore';
import sagas, { reducers } from './store';
import SessionRoute from './session/SessionRoute';
import KeyRoute from './key/KeyRoute';

const AppRouter: React.FC = () => {

  const store = configureStore({}, sagas, reducers);

  return (
    <OAuthProtectedFragment>
      <Provider store={store}>
        <Router>
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
    </OAuthProtectedFragment>
  );
}

export default AppRouter;