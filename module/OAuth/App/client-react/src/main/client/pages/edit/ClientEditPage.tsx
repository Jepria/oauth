import React from 'react';
import { ToolBar } from '../../../../components/toolbar/ToolBar';
import * as DefaultButtons from '../../../../components/toolbar/ToolBarButtons';
import { useHistory, Link } from 'react-router-dom';
import { Content, Page } from '../../../../components/page/Layout';
import { TabPanel, SelectedTab, Tab } from '../../../../components/tabpanel/TabPanel';
import { ComboBox, ComboBoxPopup } from '../../../../components/form/input/ComboBox';

const ClientEditPage: React.FC = () => {
  const history = useHistory();
  
  return (
    <Page>
      <TabPanel>
        <SelectedTab>Клиент</SelectedTab>
        <Tab onClick={() => history.push('/client/client-uri')}>URL</Tab>
      </TabPanel>
      <ToolBar>
        <DefaultButtons.CreateButton onCreate={() => { history.push('/client/create') }} disabled={false} />
        <DefaultButtons.SaveButton onSave={() => { history.push('/client/view') }} disabled={false} />
        <DefaultButtons.EditButton onEdit={() => { history.push('/client/edit') }} disabled={true} />
        <DefaultButtons.ViewButton onView={() => { history.push('/client/:id/view') }} disabled={false} />
        <DefaultButtons.DeleteButton onDelete={() => { window.alert('deleted') }} disabled={false} />
      </ToolBar>
      <Content>
        
      </Content>
    </Page>
  )
}

export default ClientEditPage;