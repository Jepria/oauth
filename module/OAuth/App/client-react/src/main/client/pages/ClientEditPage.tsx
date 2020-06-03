import React, { useEffect, HTMLAttributes, useImperativeHandle } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Client, ClientState } from '../types';
import { AppState } from '../../store';
import { getClientById, updateClient } from '../state/redux/actions';
import { Formik, Form, Field, FieldProps } from 'formik';
import { FormField, Label } from '../../../components/form/Field';
import { TextInput } from '../../../components/form/input/TextInput';
import { ApplicationGrantType, GrantType } from '../../../security/OAuth';
import { Page, Content, FormContainer, ComboBox, ComboBoxInput, ComboBoxList, ComboBoxOption, SelectAllCheckBox, CheckBoxList, CheckBoxOptionList, CheckBoxOption } from 'jfront-components';

const ClientEditPage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { clientId } = useParams();
  const { current } = useSelector<AppState, ClientState>(state => state.client);
  let formikRef: any;

  useImperativeHandle(ref, () => ({
    handleSubmit: () => {
      formikRef.handleSubmit();
    }
  }));

  useEffect(() => {
    if (!current && clientId) {
      dispatch(getClientById(clientId));
    }
  }, [current, clientId, dispatch]);

  const initialValues: Client = current ? current : {
      clientName: '',
      applicationType: '',
      grantTypes: []
    }

  return (
    <Page>
      <Content>
        <FormContainer>
          <Formik enableReinitialize
            innerRef={formik => formikRef = formik}
            initialValues={initialValues}
            onSubmit={(values: Client) => {
              if (clientId) {
                dispatch(updateClient(clientId, values, (client: Client) => {
                  history.push(`/ui/client/${client.clientId}/view/`);
                }));
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
            <Form {...props}>
              <FormField>
                <Label width={'250px'}>Краткое наименование приложения:</Label>
                <Field name="clientId">
                  {(props: FieldProps) => (
                      <TextInput
                          name={props.field.name}
                          value={props.field.value}
                          onChange={props.field.onChange}
                          onBlur={props.field.onBlur}
                          touched={props.meta.touched}
                          error={props.meta.error}
                          maxLength={16}
                          pattern="[A-Za-z0-9]{16}"
                          disabled
                      />
                  )}
                </Field>
              </FormField>
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
                        <ComboBoxList>
                          <ComboBoxOption name="Native" value="native" />
                          <ComboBoxOption name="WEB application" value="web" />
                          <ComboBoxOption name="Browser (client-side) application" value="browser" />
                          <ComboBoxOption name="Service" value="service" />
                        </ComboBoxList>
                    </ComboBox>)}
                </Field>
              </FormField>
              <FormField>
                <Label width={'250px'}> Доступные гранты:</Label>
                <Field name='grantTypes'>
                  {(props: FieldProps) => (
                    <CheckBoxList name={props.field.name} value={props.field.value} onChange={props.form.setFieldValue}>
                      <CheckBoxOptionList>
                        {() => {
                          const applicationType = ApplicationGrantType[props.form.values["applicationType"]];
                          if (applicationType) {
                            return applicationType.map(grantType =>
                              <CheckBoxOption key={grantType} value={grantType} name={GrantType[grantType]} />
                            );
                          }
                        }}
                      </CheckBoxOptionList>
                      <SelectAllCheckBox />
                    </CheckBoxList>
                  )}
                </Field>
              </FormField>
            </Form>
          </Formik>
        </FormContainer>
      </Content>
    </Page>
  )
})

export default ClientEditPage;