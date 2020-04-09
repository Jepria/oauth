import React from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";
import { OAuthProtectedFragment } from '../security/OAuthSecurityContext';
import ClientRoute from './client/ClientRoute';

const AppRouter: React.FC = () => {
  return (
    // <OAuthProtectedFragment>
      <Router>
        <Switch>
          <Route path="/client">
            <ClientRoute/>
          </Route>
          <Route path="/key">
            <div>Key</div>
          </Route>
          <Route path="/session">
            <div>Session</div>
          </Route>
        </Switch>
      </Router>
    // {/* </OAuthProtectedFragment> */}
  );
}

export default AppRouter;