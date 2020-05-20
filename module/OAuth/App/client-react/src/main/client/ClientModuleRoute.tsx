import React from 'react';
import {
  Switch,
  Route,
  useRouteMatch
} from "react-router-dom";
import ClientRoute from './ClientRoute';
import ClientUriRoute from './client-uri/ClientUriRoute';

const ClientModuleRoute: React.FC = () => {

  let { path } = useRouteMatch();

  return (
    <React.Fragment>
      <Switch>
        <Route path={`${path}/:clientId/client-uri`}>
          <ClientUriRoute/>
        </Route>
        <Route path={`${path}`}>
          <ClientRoute/>
        </Route>
      </Switch>
    </React.Fragment>
  );
}

export default ClientModuleRoute;