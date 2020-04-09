import React from 'react';
import {
  Switch,
  Route
} from "react-router-dom";
import ClientCreatePage from './pages/create/ClientCreatePage';
import ClientEditPage from './pages/edit/ClientEditPage';
import ClientViewPage from './pages/view/ClientViewPage';

const ClientRoute: React.FC = () => {
  
  return (
    <Switch>
      <Route path='/client/create'>
        <ClientCreatePage/>
      </Route>
      <Route path='/client/edit'>
        <ClientEditPage/>
      </Route>
      <Route path='/client/view'>
        <ClientViewPage/>
      </Route>
      <Route path='/client/search'>
        <div>Client Search</div>
      </Route>
      <Route path='/client/list'>
        <div>Client List</div>
      </Route>
    </Switch>
  );
}

export default ClientRoute;