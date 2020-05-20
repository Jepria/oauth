import React, { useRef } from 'react';
import {
  Switch,
  Route,
  useRouteMatch
} from "react-router-dom";
import SessionViewPage from './pages/KeyViewPage';
import { AppState } from '../store';
import { useSelector, useDispatch } from 'react-redux';
import { LoadingPanel } from '../../components/mask';
import { KeyState } from './types';
import { Page, Content, Header } from '../../components/Layout';
import { TabPanel, SelectedTab } from '../../components/tabpanel/TabPanel';
import { ToolBar, ToolBarButton } from '../../components/toolbar';
import { updateKey, getKey } from './state/redux/actions';
import change_password from './change_password.png';

const KeyRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const dispatch = useDispatch();
  const { isLoading, message, error } = useSelector<AppState, KeyState>(state => state.key);
  
  return (
    <Page>
      {isLoading && <LoadingPanel text={message} />}
      <Header>
        <TabPanel>
          <SelectedTab>Ключ безопасности</SelectedTab>
        </TabPanel>
        <ToolBar>
          <ToolBarButton onClick={() => dispatch(updateKey(() => dispatch(getKey())))} tooltip='Обновить ключ безопасности'>
            <img src={change_password} alt='Обновить ключ безопасности'/>
          </ToolBarButton>
        </ToolBar>
      </Header>
      <Content>
        <Switch>
          <Route path={`${path}/view`}>
            <SessionViewPage />
          </Route>
        </Switch>
      </Content>
    </Page>
  );
}

export default KeyRoute;