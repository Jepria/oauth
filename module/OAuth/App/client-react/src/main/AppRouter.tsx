import React from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";
import { Provider } from 'react-redux';
import { OAuthProtectedFragment } from '../security/OAuthSecurityContext';
import ClientRoute from './client/ClientRoute';
import { configureStore } from '../redux/configureStore';
import sagas, { reducers } from './store';

const AppRouter: React.FC = () => {

  const store = configureStore({}, sagas, reducers);

  return (
    <OAuthProtectedFragment>
      <Provider store={store}>
        <Router basename=''>
          <Switch>
            <Route path="/ui/client">
              <ClientRoute />
            </Route>
            <Route path="/ui/key">
              <div>Key</div>
            </Route>
            <Route path="/ui/session">
              <div>Session</div>
            </Route>
          </Switch>
        </Router>
      </Provider>
    </OAuthProtectedFragment>
  );
}

export default AppRouter;