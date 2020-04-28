import React from 'react';
import {
  Switch,
  Route,
  useRouteMatch
} from "react-router-dom";
import ClientRoute from './ClientRoute';

const ClientModuleRoute: React.FC = () => {

  let { path } = useRouteMatch();

  return (
    <React.Fragment>
      <Switch>
        <Route path={`${path}/:clientId/client-uri`}>
          <div>Client Uri</div>
        </Route>
        <Route path={`${path}`}>
          <ClientRoute/>
        </Route>
      </Switch>
    </React.Fragment>
  );
}

export default ClientModuleRoute;