import React, { useContext, useEffect, useState } from 'react';
import {
  Switch,
  Route,
  useRouteMatch
} from "react-router-dom";
import KeyViewPage from './pages/KeyViewPage';
import { AppState } from '../app/store/reducer';
import { useSelector, useDispatch } from 'react-redux';
import { KeyState } from './types';
import { actions } from './state/keySlice';
import {
  Panel,
  TabPanel, Tab, Toolbar,
  ToolbarButtonBase,
  Loader
} from '@jfront/ui-core';
import { UserPanel } from '@jfront/oauth-ui';
import { UserContext } from '@jfront/oauth-user'
import { useTranslation } from 'react-i18next';

const KeyRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const dispatch = useDispatch();
  const { t } = useTranslation();
  const { isRoleLoading, isUserInRole } = useContext(UserContext);
  const [hasUpdateRole, setHasUpdateRole] = useState(false);
  const { isLoading, message } = useSelector<AppState, KeyState>(state => state.key);

  useEffect(() => {
    isUserInRole("OAUpdateKey")
      .then(setHasUpdateRole);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <Panel>
      {(isLoading || isRoleLoading) && <Loader text={message || "Загрузка данных"} />}
      <Panel.Header>
        <TabPanel>
          <Tab selected>{t('key.moduleName')}</Tab>
          <UserPanel />
        </TabPanel>
        <Toolbar>
          <ToolbarButtonBase onClick={() => {
            if (window.confirm(t('key.toolbar.updateMessage'))) {
              dispatch(actions.update({
                loadingMessage: t('dataLoadingMessage'),
                callback: () => dispatch(actions.getRecordById({ loadingMessage: t('dataLoadingMessage') }))
              }))
            }
          }} title={t('key.toolbar.update')} disabled={!hasUpdateRole}>
            <img src={process.env.PUBLIC_URL + '/images/change_password.png'} alt={t('key.toolbar.update')} title={t('key.toolbar.update')} />
          </ToolbarButtonBase>
        </Toolbar>
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