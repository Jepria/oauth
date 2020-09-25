import React, { useContext, useEffect, useRef, useState } from 'react';
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

const KeyRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const dispatch = useDispatch();
  const { isRoleLoading, isUserInRole } = useContext(UserContext);
  const [hasUpdateRole, setHasUpdateRole] = useState(false);
  const { isLoading, message, error } = useSelector<AppState, KeyState>(state => state.key);

  useEffect(() => {
    isUserInRole("OAUpdateKey")
      .then(setHasUpdateRole);
  }, [])

  return (
    <Panel>
    {(isLoading || isRoleLoading) && <LoadingPanel text={message || "Загрузка данных"} />}
      <Panel.Header>
        <TabPanel>
          <Tab selected>Ключ безопасности</Tab>
          <UserPanel />
        </TabPanel>
        <Toolbar>
          <ToolbarButtonBase onClick={() => dispatch(updateKey(() => dispatch(getKey())))} title='Обновить ключ безопасности' disabled={!hasUpdateRole}>
            <img src={change_password} alt='Обновить ключ безопасности' />
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