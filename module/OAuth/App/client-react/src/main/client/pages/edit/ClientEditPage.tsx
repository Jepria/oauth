import React, { useEffect, useRef, useMemo } from 'react';
import { ToolBar } from '../../../../components/toolbar';
import * as DefaultButtons from '../../../../components/toolbar/ToolBarButtons';
import { useHistory, useParams } from 'react-router-dom';
import { Content, Page } from '../../../../components/page/Layout';
import { TabPanel, SelectedTab, Tab } from '../../../../components/tabpanel/TabPanel';
import { ComboBox, ComboBoxPopup, ComboBoxInput, ComboBoxList, ComboBoxOption } from '../../../../components/form/input/combobox';
import { useDispatch, useSelector } from 'react-redux';
import { Client } from '../../types';
import { AppState } from '../../../store';
import { getClientById, updateClient, setCurrentRecord, deleteClient } from '../../state/actions';
import { Formik, Form, Field, FieldProps } from 'formik';
import { FormField, Label } from '../../../../components/form/Field';
import { TextInput } from '../../../../components/form/input/TextInput';
import { ListBox, ListBoxOptionList, ListBoxOption, SelectAllCheckBox } from '../../../../components/form/input/ListBox';
import { ApplicationGrantType, GrantType } from '../../../../security/OAuth';

const ClientEditPage: React.FC = () => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { clientId } = useParams();
  const current = useSelector<AppState, Client | undefined>(state => state.client.current);
  const currentRef = useRef(current);
  let formRef: any;

  useEffect(() => {
    if (!current && clientId) {
      dispatch(getClientById(clientId));
    }
  }, [current, clientId, dispatch]);

  useEffect(
    () => {
      if (currentRef.current && current && currentRef.current !== current) {
        history.push(`/ui/client/${current.clientId}/view/`);
      }
    }, [current, history]
  );

  const initialValues: Client = current ? {
    clientName: current.clientName,
    clientNameEn: current.clientNameEn,
    applicationType: current.applicationType,
    grantTypes: current.grantTypes.slice()
  } : {
      clientName: '',
      applicationType: '',
      grantTypes: []
    }
  return (
    <Page>
      <TabPanel>
        <SelectedTab>Клиент</SelectedTab>
        <Tab onClick={() => history.push('/client/client-uri')}>URL</Tab>
      </TabPanel>
      <ToolBar>
        <DefaultButtons.CreateButton onCreate={() => {
          dispatch(setCurrentRecord(undefined));
          history.push('/ui/client/create');
        }} />
        <DefaultButtons.SaveButton onSave={() => { formRef.handleSubmit() }} />
        <DefaultButtons.EditButton onEdit={() => { }} disabled />
        <DefaultButtons.ViewButton onView={() => { history.push(`/ui/client/${clientId}/view`) }} />
        <DefaultButtons.DeleteButton onDelete={() => {
          if (clientId) {
            dispatch(deleteClient(clientId));
            history.push('/ui/client/list');
          }
        }} />
      </ToolBar>
      <Content>
        <Formik enableReinitialize
          innerRef={formik => formRef = formik}
          initialValues={initialValues}
          onSubmit={(values: Client) => {
            if (clientId) {
              dispatch(updateClient(clientId, values));
            }
          }}
          validate={(values) => {
            const errors: { clientName?: string, applicationType?: string, grantTypes?: string } = {};

            if (!values['clientName']) {
              errors.clientName = 'Поле должно быть заполнено'
            }
            if (!values['applicationType']) {
              errors.applicationType = 'Поле должно быть заполнено'
            }
            if (!values['grantTypes'] || values['grantTypes'].length === 0) {
              errors.grantTypes = 'Поле должно быть заполнено'
            }
            return errors;
          }}>
          <Form>
            <FormField>
              <Label width={'250px'}>Имя клиентского приложения:</Label>
              <Field name="clientName">
                {(props: FieldProps) => (
                  <TextInput name={props.field.name} value={props.field.value} onChange={props.field.onChange} onBlur={props.field.onBlur} touched={props.meta.touched} error={props.meta.error} />
                )}
              </Field>
            </FormField>
            <FormField>
              <Label width={'250px'}>Имя клиентского приложения(англ):</Label>
              <Field name="clientNameEn" as={TextInput} />
            </FormField>
            <FormField>
              <Label width={'250px'}>Тип приложения:</Label>
              <Field name="applicationType">
                {(props: FieldProps) => (
                  <ComboBox name={props.field.name} value={props.field.value} onChange={(field, value) => {
                    if (value !== props.field.value) {
                      props.form.setFieldValue('grantTypes', []);
                      props.form.setFieldValue(field, value);
                    }
                  }} width='250px'>
                    <ComboBoxInput />
                    <ComboBoxPopup>
                      <ComboBoxList>
                        <ComboBoxOption name="Native" value="native" />
                        <ComboBoxOption name="WEB application" value="web" />
                        <ComboBoxOption name="Browser (client-side) application" value="browser" />
                        <ComboBoxOption name="Service" value="service" />
                      </ComboBoxList>
                    </ComboBoxPopup>
                  </ComboBox>)}
              </Field>
            </FormField>
            <FormField>
              <Label width={'250px'}> Доступные гранты:</Label>
              <Field name='grantTypes'>
                {(props: FieldProps) => (
                  <ListBox name={props.field.name} value={props.field.value} onChange={props.form.setFieldValue}>
                    <ListBoxOptionList>
                      {() => {
                        const applicationType = ApplicationGrantType[props.form.values["applicationType"]];
                        if (applicationType) {
                          return applicationType.map(grantType =>
                            <ListBoxOption key={grantType} value={grantType} name={GrantType[grantType]} />
                          );
                        } else {
                          return (<React.Fragment />)
                        }
                      }}
                    </ListBoxOptionList>
                    <SelectAllCheckBox />
                  </ListBox>
                )}
              </Field>
            </FormField>
          </Form>
        </Formik>
      </Content>
    </Page>
  )
}

export default ClientEditPage;