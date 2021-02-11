import React, { useContext, useEffect, useState } from 'react';
import {
  Switch,
  Route,
  useRouteMatch
} from "react-router-dom";
import KeyViewPage from './pages/KeyViewPage';
import { AppState } from '../app/store/reducer';
import { useSelector } from 'react-redux';
import { KeyState } from './types';
import {
  Panel,
  TabPanel, 
  Tab,
  Loader
} from '@jfront/ui-core';
import { UserPanel } from '@jfront/oauth-ui';
import { UserContext } from '@jfront/oauth-user'
import { useTranslation } from 'react-i18next';
import { KeyToolbar } from './components/KeyToolbar';

const KeyRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const { t } = useTranslation();
  const { isRoleLoading } = useContext(UserContext);
  const { isLoading, message } = useSelector<AppState, KeyState>(state => state.key);

  return (
    <Panel>
      {(isLoading || isRoleLoading) && <Loader text={message || "Загрузка данных"} />}
      <Panel.Header>
        <TabPanel>
          <Tab selected>{t('key.moduleName')}</Tab>
          <UserPanel />
        </TabPanel>
        <KeyToolbar/>
      </Panel.Header>
      <Panel.Content>
        <Switch>
          <Route path={`${path}/view`}>
            <KeyViewPage />
          </Route>
        </Switch>
      </Panel.Content>
    </Panel>
  );
}

export default KeyRoute;