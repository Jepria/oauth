import React, { useContext, useEffect, useState } from 'react';
import {
  Switch,
  Route,
  useRouteMatch
} from "react-router-dom";
import ClientRoute from './ClientRoute';
import ClientUriRoute from './client-uri/ClientUriRoute';
import { UserContext } from '../../user/UserContext';
import { Loader } from '@jfront/oauth-ui';
import { Forbidden } from '../../user/Forbidden';

const ClientModuleRoute: React.FC = () => {

  const { isRoleLoading, isUserInRoles, currentUser } = useContext(UserContext);
  const [ hasRoles, setHasRoles] = useState<boolean | null>(null);
  let { path } = useRouteMatch();

  useEffect(() => {
    if (currentUser.username !== "Guest") {
      isUserInRoles(["OAViewClient", "OACreateClient", "OAEditClient", "OADeleteClient"])
        .then(setHasRoles)
        .catch(error => setHasRoles(false))
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentUser])

  return (
    <>
      {isRoleLoading && <Loader title="OAuth" text="Проверка ролей"/>}
      {hasRoles === true && <Switch>
        <Route path={`${path}/:clientId/client-uri`}>
          <ClientUriRoute/>
        </Route>
        <Route path={`${path}`}>
          <ClientRoute/>
        </Route>
      </Switch>}
      {hasRoles === false && <Forbidden/>}
    </>
  );
}

export default ClientModuleRoute;