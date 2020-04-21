import React, { useEffect } from 'react';
import { ToolBar } from '../../../../components/toolbar';
import * as DefaultButtons from '../../../../components/toolbar/ToolBarButtons';
import { useHistory, useRouteMatch, useParams } from 'react-router-dom';
import { TabPanel, SelectedTab, Tab } from '../../../../components/tabpanel/TabPanel';
import { Page, Content, VerticalLayout } from '../../../../components/page/Layout';
import { FormField, Label, Text } from '../../../../components/form/Field';
import { AppState } from '../../../store';
import { Client } from '../../types';
import { useSelector, useDispatch } from 'react-redux';
import { getClientById, deleteClient, setCurrentRecord } from '../../state/actions';
import { GrantType, ApplicationType } from '../../../../security/OAuth';

const ClientViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { path } = useRouteMatch();
  const history = useHistory();
  const { clientId } = useParams();
  const current = useSelector<AppState, Client | undefined>(state => state.client.current);

  console.log(path)

  useEffect(() => {
    if (!current && clientId) {
      dispatch(getClientById(clientId));
    }
  }, [current, clientId, dispatch]);

  return (
    <Page>
      <TabPanel>
        <SelectedTab>Клиент</SelectedTab>
        <Tab onClick={() => history.push(`/ui/client/${clientId}/client-uri`)}>URL</Tab>
      </TabPanel>
      <ToolBar>
        <DefaultButtons.CreateButton onCreate={() => {
          dispatch(setCurrentRecord(undefined));
          history.push('/ui/client/create')
        }} disabled={false} />
        <DefaultButtons.SaveButton onSave={() => { }} disabled={true} />
        <DefaultButtons.EditButton onEdit={() => { history.push(`/ui/client/${clientId}/edit`) }} disabled={false} />
        <DefaultButtons.ViewButton onView={() => { }} disabled={true} />
        <DefaultButtons.DeleteButton onDelete={() => {
          if (clientId) {
            dispatch(deleteClient(clientId));
            history.push('/ui/client/list');
          }
        }} disabled={false} />
      </ToolBar>
      <Content>
        <VerticalLayout>
          <FormField>
            <Label width={'250px'}>ID клиентского приложения:</Label>
            <Text>{current?.clientId}</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}>Секретное слово:</Label>
            <Text>{current?.clientSecret}</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}>Имя клиентского приложения:</Label>
            <Text>{current?.clientName}</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}>Имя клиентского приложения(англ):</Label>
            <Text>{current?.clientNameEn}</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}>Тип приложения:</Label>
            <Text>{current ? ApplicationType[current.applicationType] : ''}</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}> Доступные гранты:</Label>
            <Text>{current?.grantTypes.map((grantType) => GrantType[grantType]).join(', ')}</Text>
          </FormField>
        </VerticalLayout>
      </Content>
    </Page>
  )

}

export default ClientViewPage;