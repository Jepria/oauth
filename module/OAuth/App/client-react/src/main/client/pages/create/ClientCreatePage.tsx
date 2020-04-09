import React from 'react';
import { ToolBar } from '../../../../components/toolbar/ToolBar';
import * as DefaultButtons from '../../../../components/toolbar/ToolBarButtons';
import { useHistory } from 'react-router-dom';
import { Page, Content } from '../../../../components/page/Layout';
import { TabPanel, SelectedTab } from '../../../../components/tabpanel/TabPanel';
import { ComboBox, ComboBoxPopup, ComboBoxOption, ComboBoxInput, ComboBoxList, ComboBoxButton } from '../../../../components/form/input/ComboBox';
import { FormField, Label, Text } from '../../../../components/form/Field';
import { ListBox, ListBoxOptionList, ListBoxOption } from '../../../../components/form/input/ListBox';

const ClientCreatePage: React.FC = () => {
  const history = useHistory();

  return (
    <Page>
      <TabPanel>
        <SelectedTab>Клиент</SelectedTab>
      </TabPanel>
      <ToolBar>
        <DefaultButtons.CreateButton onCreate={() => { history.push('/client/create') }} disabled={true} />
        <DefaultButtons.SaveButton onSave={() => { history.push('/client/view') }} disabled={false} />
        <DefaultButtons.EditButton onEdit={() => { history.push('/client/edit') }} disabled={true} />
        <DefaultButtons.ViewButton onView={() => { history.push('/client/view') }} disabled={true} />
        <DefaultButtons.DeleteButton onDelete={() => { window.alert('deleted') }} disabled={true} />
      </ToolBar>
      <Content>
        <FormField>
          <Label width={'250px'}>Имя клиентского приложения:</Label>
          <Text>Тестовый клиент</Text>
        </FormField>
        <FormField>
          <Label width={'250px'}>Имя клиентского приложения(англ):</Label>
          <Text>Test client</Text>
        </FormField>
        <FormField>
          <Label>Тип приложения:</Label>
          <ComboBox name="test" openOnFocus width='250px'>
            <ComboBoxInput />
            <ComboBoxButton />
            <ComboBoxPopup>
              <ComboBoxList>
                <ComboBoxOption name="Native" value="native" />
                <ComboBoxOption name="WEB application" value="web" />
                <ComboBoxOption name="Browser (client-side) application" value="browser" />
                <ComboBoxOption name="Service" value="service" />
              </ComboBoxList>
            </ComboBoxPopup>
          </ComboBox>
        </FormField>
        <FormField>
          <Label width={'250px'}> Доступные гранты:</Label>
          {/* <Text>{['', '', '', '', ''].join(', ')}</Text> */}
          <ListBox>
            <ListBoxOptionList>
              <ListBoxOption value='authorization_code' name='Authorization code'/>
              <ListBoxOption value='implicit' name='Implicit'/>
              <ListBoxOption value='client_credentials' name='Client credentials'/>
              <ListBoxOption value='password' name='User credentials'/>
              <ListBoxOption value='refresh_token' name='Refresh token'/>
            </ListBoxOptionList>
          </ListBox>
        </FormField>
      </Content>
    </Page>
  )
}

export default ClientCreatePage;