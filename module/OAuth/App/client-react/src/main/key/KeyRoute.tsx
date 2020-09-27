import React, { useContext, useEffect, useState } from 'react';
import {
  Switch,
  Route,
  useRouteMatch
} from "react-router-dom";
import KeyViewPage from './pages/KeyViewPage';
import { AppState } from '../../redux/store';
import { useSelector, useDispatch } from 'react-redux';
import { LoadingPanel } from '../../components/mask';
import { KeyState } from './types';
import { updateKey, getKey } from './state/redux/actions';
import change_password from './change_password.png';
import {
  Panel,
  TabPanel, Tab, Toolbar,
  ToolbarButtonBase
} from '@jfront/ui-core';
import { UserPanel } from '../../user/UserPanel';
import { UserContext } from '../../user/UserContext';
import { useTranslation } from 'react-i18next';

const KeyRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const dispatch = useDispatch();
  const { t } = useTranslation();
  const { isRoleLoading, isUserInRole } = useContext(UserContext);
  const [hasUpdateRole, setHasUpdateRole] = useState(false);
  const { isLoading, message, error } = useSelector<AppState, KeyState>(state => state.key);

  useEffect(() => {
    isUserInRole("OAUpdateKey")
      .then(setHasUpdateRole);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <Panel>
    {(isLoading || isRoleLoading) && <LoadingPanel text={message || "Загрузка данных"} />}
      <Panel.Header>
        <TabPanel>
          <Tab selected>{t('key.moduleName')}</Tab>
          <UserPanel />
        </TabPanel>
        <Toolbar>
          <ToolbarButtonBase onClick={() => {
            if (window.confirm(t('key.updateMessage'))) {
              dispatch(updateKey(() => dispatch(getKey())))
            }}} title={t('key.toolbar.update')} disabled={!hasUpdateRole}>
            <img src={change_password} alt={t('key.toolbar.update')} title={t('key.toolbar.update')}  />
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