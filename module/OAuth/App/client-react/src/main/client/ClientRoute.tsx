import React from 'react';
import {
  Switch,
  Route,
  useRouteMatch
} from "react-router-dom";
import ClientCreatePage from './pages/create/ClientCreatePage';
import ClientEditPage from './pages/edit/ClientEditPage';
import ClientViewPage from './pages/view/ClientViewPage';
import { AppState } from '../store';
import { useSelector } from 'react-redux';
import { LoadingPanel } from '../../components/mask';
import { ClientState } from './types';
import ClientSearchPage from './pages/search/ClientSearchPage';
import { ClientListPage } from './pages/list/ClientListPage';

const ClientRoute: React.FC = () => {

  let { path } = useRouteMatch();
  const { isLoading, message } = useSelector<AppState, ClientState>(state => state.client)

  return (
    <React.Fragment>
      {isLoading && <LoadingPanel text={message} />}
      <Switch>
        <Route path={`${path}/:clientId/client-uri`}>
          <div>Client Uri</div>
        </Route>
        <Route path={`${path}/*`}>
          <Switch>
            <Route path={`${path}/create`}>
              <ClientCreatePage />
            </Route>
            <Route path={`${path}/:clientId/edit`}>
              <ClientEditPage />
            </Route>
            <Route path={`${path}/:clientId/view`}>
              <ClientViewPage />
            </Route>
            <Route path={`${path}/search`}>
              <ClientSearchPage/>
            </Route>
            <Route path={`${path}/list`}>
              <ClientListPage/>
            </Route>
          </Switch>
        </Route>
      </Switch>
    </React.Fragment>
  );
}

export default ClientRoute;