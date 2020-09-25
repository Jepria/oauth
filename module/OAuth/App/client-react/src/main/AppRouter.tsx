import React, { useContext } from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";
import ClientRoute from './client/ClientModuleRoute';
import SessionRoute from './session/SessionRoute';
import KeyRoute from './key/KeyRoute';
import { Loader, OAuthSecuredFragment } from '@jfront/oauth-ui';
import { UserContext } from '../user/UserContext';

const AppRouter: React.FC = () => {

  const { currentUser, isUserLoading } = useContext(UserContext);

  return (
    <OAuthSecuredFragment>
        {currentUser.username !== "Guest" && !isUserLoading &&
          <Router basename={`${process.env.NODE_ENV === 'development' ? '' : process.env.PUBLIC_URL}`}>
            <Switch>
              <Route path="/ui/client">
                <ClientRoute />
              </Route>
              <Route path="/ui/key">
                <KeyRoute />
              </Route>
              <Route path="/ui/session">
                <SessionRoute />
              </Route>
            </Switch>
          </Router>}
        {isUserLoading && <Loader title="OAuth" text="Загрузка данных о пользователе" />}
    </OAuthSecuredFragment>
  );
}

export default AppRouter;