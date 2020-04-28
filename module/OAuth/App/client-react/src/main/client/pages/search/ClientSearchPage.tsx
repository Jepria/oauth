import React, { useEffect } from 'react';
import { ToolBar } from '../../../../components/toolbar';
import * as DefaultButtons from '../../../../components/toolbar/ToolBarButtons';
import { useHistory } from 'react-router-dom';
import { Page, Content } from '../../../../components/page/Layout';
import { TabPanel, SelectedTab } from '../../../../components/tabpanel/TabPanel';
import { FormField, Label } from '../../../../components/form/Field';
import { Formik, Form, Field } from 'formik';
import { TextInput } from '../../../../components/form/input/TextInput';
import { useSelector, useDispatch } from 'react-redux';
import { ClientSearchTemplate } from '../../types';
import { postSearchClientRequest } from '../../state/redux/actions';
import { AppState } from '../../../store';

const ClientSearchPage: React.FC = () => {
  const dispatch = useDispatch();
  const history = useHistory();
  const searchTemplate = useSelector<AppState, ClientSearchTemplate | undefined>(state => state.client.searchRequest?.template);
  let formRef: any;

  return (
    <Page>
      <TabPanel>
        <SelectedTab>Клиент</SelectedTab>
      </TabPanel>
      <ToolBar>
        <DefaultButtons.CreateButton onCreate={() => { history.push('/ui/client/create') }} />
        <DefaultButtons.SaveButton onSave={() => { }} disabled />
        <DefaultButtons.EditButton onEdit={() => { }} disabled />
        <DefaultButtons.ViewButton onView={() => { }} disabled />
        <DefaultButtons.DeleteButton onDelete={() => { }} disabled />
        <DefaultButtons.Splitter />
        <DefaultButtons.ListButton onList={() => { }} disabled />
        <DefaultButtons.SearchButton onSearch={() => { }} disabled />
        <DefaultButtons.DoSearchButton onDoSearch={() => formRef.handleSubmit()} />
      </ToolBar>
      <Content>
        <Formik
          innerRef={formik => formRef = formik}
          initialValues={searchTemplate ? searchTemplate : {}}
          onSubmit={(values: ClientSearchTemplate) => {
            dispatch(postSearchClientRequest({
              template: values
            }));
            history.push('/ui/client/list');
          }}>
          <Form>
            <FormField>
              <Label width={'250px'}>Имя клиентского приложения:</Label>
              <Field name="clientName" as={TextInput} />
            </FormField>
            <FormField>
              <Label width={'250px'}>Имя клиентского приложения(англ):</Label>
              <Field name="clientNameEn" as={TextInput} />
            </FormField>
          </Form>
        </Formik>
      </Content>
    </Page>
  )
}

export default ClientSearchPage;