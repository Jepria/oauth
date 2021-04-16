import React, { useContext, useEffect, useState } from "react";
import { Switch, Route, useRouteMatch } from "react-router-dom";
import ClientRoute from "./ClientRoute";
import ClientUriRoute from "./client-uri/ClientUriRoute";
import { Loader } from "@jfront/oauth-ui";
import { UserContext } from "@jfront/oauth-user";
import { Forbidden } from "@jfront/oauth-ui";
import ClientErrorBoundary from "./components/ClientErrorBoundary";
import ClientUriErrorBoundary from "./client-uri/components/ClientUriErrorBoundary";

const ClientModuleRoute: React.FC = () => {
  const { isRoleLoading, isUserInRoles, currentUser } = useContext(UserContext);
  const [hasRoles, setHasRoles] = useState<boolean | null>(null);
  let { path } = useRouteMatch();

  useEffect(() => {
    if (currentUser.username !== "Guest") {
      isUserInRoles([
        "OAViewClient",
        "OACreateClient",
        "OAEditClient",
        "OADeleteClient",
      ])
        .then(setHasRoles)
        .catch((error) => setHasRoles(false));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentUser]);

  return (
    <>
      {isRoleLoading && <Loader title="OAuth" text="Проверка ролей" />}
      {hasRoles === false && <Forbidden />}
      {hasRoles === true && (
        <Switch>
          <Route path={`${path}/:clientId/client-uri`}>
            <ClientUriErrorBoundary>
              <ClientUriRoute />
            </ClientUriErrorBoundary>
          </Route>
          <Route path={`${path}`}>
            <ClientErrorBoundary>
              <ClientRoute />
            </ClientErrorBoundary>
          </Route>
        </Switch>
      )}
    </>
  );
};

export default ClientModuleRoute;
