import React from 'react';
import { ToolBar } from '../../../../components/toolbar/ToolBar';
import * as DefaultButtons from '../../../../components/toolbar/ToolBarButtons';
import { useHistory } from 'react-router-dom';
import { TabPanel, SelectedTab, Tab } from '../../../../components/tabpanel/TabPanel';
import { Page, Content, VerticalLayout, HorizontalLayout } from '../../../../components/page/Layout';
import { FormField, Label, Text } from '../../../../components/form/Field';

const ClientViewPage: React.FC = () => {
  const history = useHistory();

  return (
    <Page>
      <TabPanel>
        <SelectedTab>Клиент</SelectedTab>
        <Tab onClick={() => history.push('/client/1213414134134/client-uri')}>URL</Tab>
      </TabPanel>
      <ToolBar>
        <DefaultButtons.CreateButton onCreate={() => { history.push('/client/create') }} disabled={false} />
        <DefaultButtons.SaveButton onSave={() => { history.push('/client/view') }} disabled={true} />
        <DefaultButtons.EditButton onEdit={() => { history.push('/client/edit') }} disabled={false} />
        <DefaultButtons.ViewButton onView={() => { history.push('/client/view') }} disabled={true} />
        <DefaultButtons.DeleteButton onDelete={() => { window.alert('deleted') }} disabled={false} />
      </ToolBar>
      <Content>
        <VerticalLayout>
          <HorizontalLayout>
            <FormField>
              <Label width={'250px'}>
                ID клиентского приложения:
              </Label>
              <Text>
                test
              </Text>
            </FormField>
            <FormField>
              <Label width={'100px'}>
                Секретное слово:
              </Label>
              <Text>
                testSecret
              </Text>
            </FormField>
          </HorizontalLayout>
          <FormField>
            <Label width={'250px'}>Имя клиентского приложения:</Label>
            <Text>Тестовый клиент</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}>Имя клиентского приложения(англ):</Label>
            <Text>Test client</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}>Тип приложения:</Label>
            <Text>WEB application</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}> Доступные гранты:</Label>
            <Text>{['Authorization code', 'Client credentials', 'Implicit', 'Resource owner password', 'Refresh token'].join(', ')}</Text>
          </FormField>
        </VerticalLayout>
      </Content>
    </Page>
  )
}

export default ClientViewPage;