import React, {Suspense, useContext, useEffect} from 'react';
import ClientRoute from "./client/ClientModuleRoute";
import KeyRoute from "./key/KeyRoute";
import SessionRoute from "./session/SessionRoute";
import {UserContext} from "@jfront/oauth-user";
import {Loader, OAuthSecuredFragment} from "@jfront/oauth-ui";
import {BrowserRouter as Router, Switch, Route} from "react-router-dom";
import {useTranslation} from "react-i18next";

function Main() {
  const {i18n, t} = useTranslation();
  const language = new URLSearchParams(window.location.search).get("locale");
  const {currentUser, isUserLoading} = useContext(UserContext);


  useEffect(() => {
    if (language) {
      i18n.changeLanguage(language);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [language]);

  return (
    <OAuthSecuredFragment>
      {currentUser.username !== "Guest" && !isUserLoading &&
      <Router basename={`${process.env.PUBLIC_URL}/ui`}>
        <Switch>
          <Route path="/client">
            <ClientRoute/>
          </Route>
          <Route path="/key">
            <KeyRoute/>
          </Route>
          <Route path="/session">
            <SessionRoute/>
          </Route>
        </Switch>
      </Router>}
      {isUserLoading && <Loader title="OAuth" text="Загрузка данных о пользователе"/>}
    </OAuthSecuredFragment>
  );
}


const App = () => {
  return (
    <Suspense fallback={<Loader title="OAuth" text="Загрузка приложения..." />}>
      <Main />
    </Suspense>
  );
};

export default App;
