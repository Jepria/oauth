import React, { useEffect, HTMLAttributes, useImperativeHandle } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Client, ClientState } from '../types';
import { AppState } from '../../store';
import { getClientById, updateClient } from '../state/redux/actions';
import { Formik, Form, Field, FieldProps } from 'formik';
import { FormField, Label } from '../../../components/form/Field';
import { TextInput } from '../../../components/form/input/TextInput';
import { ApplicationGrantType, GrantType, ApplicationType } from '../../../security/OAuth';
import { Page, Content, FormContainer, ComboBoxField, CheckBoxListField } from 'jfront-components';

const ClientEditPage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { clientId } = useParams();
  const { current } = useSelector<AppState, ClientState>(state => state.client);
  let formikRef: any;
  const applicationTypeOptions = [
    { name: "Native", value: "native" },
    { name: "WEB application", value: "web" },
    { name: "Browser (client-side) application", value: "browser" },
    { name: "Service", value: "service" },
  ]

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

  const initialValues: Client = {
    clientId: '',
    clientName: '',
    clientNameEn: '',
    applicationType: '',
    grantTypes: [],
    ...current
  }

  console.log(initialValues)

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
                    <ComboBoxField
                      options={applicationTypeOptions}
                      name={props.field.name}
                      initialValue={props.field.value ? {name: ApplicationType[props.field.value], value: props.field.value} : undefined}
                      hasEmptyOption
                      touched={props.meta.touched}
                      error={props.meta.error}
                      onChangeValue={(field, value) => {
                        if (value !== props.field.value) {
                          props.form.setFieldValue('grantTypes', []);
                          props.form.setFieldValue(field, value);
                        }
                      }} width='250px' />)}
                </Field>
              </FormField>
              <FormField>
                <Label width={'250px'}> Доступные гранты:</Label>
                <Field name='grantTypes'>
                  {(props: FieldProps) => {
                    const grantTypeOptions = ApplicationGrantType[props.form.values["applicationType"]]?.map(grantType => ({name: GrantType[grantType], value: grantType}));
                    return (
                      <CheckBoxListField
                        options={grantTypeOptions ? grantTypeOptions : []}
                        name={props.field.name}
                        initialValue={props.field.value?.map((value: any) => ({name: GrantType[value], value: value}))}
                        onChangeValue={props.form.setFieldValue}
                        touched={props.meta.touched}
                        error={props.meta.error} />
                    );
                  }}
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