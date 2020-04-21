import React, { useEffect } from 'react';
import { ToolBar } from '../../../../components/toolbar';
import * as DefaultButtons from '../../../../components/toolbar/ToolBarButtons';
import { useHistory } from 'react-router-dom';
import { Page, Content } from '../../../../components/page/Layout';
import { TabPanel, SelectedTab } from '../../../../components/tabpanel/TabPanel';
import { ComboBox, ComboBoxPopup, ComboBoxOption, ComboBoxInput, ComboBoxList } from '../../../../components/form/input/combobox';
import { FormField, Label } from '../../../../components/form/Field';
import { ListBox, ListBoxOptionList, ListBoxOption, SelectAllCheckBox } from '../../../../components/form/input/ListBox';
import { Formik, Form, Field, FieldProps } from 'formik';
import { TextInput } from '../../../../components/form/input/TextInput';
import { useSelector, useDispatch } from 'react-redux';
import { Client } from '../../types';
import { createClient } from '../../state/actions';
import { AppState } from '../../../store';
import { ApplicationGrantType, GrantType } from '../../../../security/OAuth';

const ClientCreatePage: React.FC = () => {
  const dispatch = useDispatch();
  const history = useHistory();
  const current = useSelector<AppState, Client | undefined>(state => state.client.current);
  let formRef: any;

  useEffect(
    () => {
      if (current) {
        history.push(`/ui/client/${current.clientId}/view/`);
      }
    }, [current, history]
  );

  return (
    <Page>
      <TabPanel>
        <SelectedTab>Клиент</SelectedTab>
      </TabPanel>
      <ToolBar>
        <DefaultButtons.CreateButton onCreate={() => { }} disabled={true} />
        <DefaultButtons.SaveButton onSave={() => { formRef.handleSubmit(); }} disabled={false} />
        <DefaultButtons.EditButton onEdit={() => { }} disabled={true} />
        <DefaultButtons.ViewButton onView={() => { }} disabled={true} />
        <DefaultButtons.DeleteButton onDelete={() => { }} disabled={true} />
        <DefaultButtons.Splitter />
      </ToolBar>
      <Content>
        <Formik 
        innerRef={formik => formRef = formik} 
        initialValues={{ clientName: '', applicationType: '', grantTypes: [] }} 
        onSubmit={(values: Client) => {
          dispatch(createClient(values));
        }}
        validate={(values) => {
          const errors: {clientName?: string, applicationType?: string, grantTypes?: string} = {};
          
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
                  <TextInput 
                  name={props.field.name} 
                  value={props.field.value} 
                  onChange={props.field.onChange} 
                  onBlur={props.field.onBlur} 
                  touched={props.meta.touched} 
                  error={props.meta.error}/>
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
                  <ComboBox name={props.field.name} value={props.field.value} touched={props.meta.touched} error={props.meta.error} onChange={(field, value) => {
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
                  <ListBox 
                  name={props.field.name} 
                  value={props.field.value} 
                  onChange={props.form.setFieldValue} 
                  touched={props.meta.touched} 
                  error={props.meta.error}>
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

export default ClientCreatePage;