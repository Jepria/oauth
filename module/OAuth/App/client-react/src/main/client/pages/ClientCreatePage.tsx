import React, { HTMLAttributes, useImperativeHandle } from 'react';
import { useHistory } from 'react-router-dom';
import { FormField, Label } from '../../../components/form/Field';
import { Formik, Form, Field, FieldProps } from 'formik';
import { TextInput } from '../../../components/form/input/TextInput';
import { useDispatch } from 'react-redux';
import { Client } from '../types';
import { createClient } from '../state/redux/actions';
import { ApplicationGrantType, ApplicationType, GrantType } from '../../../security/OAuth';
import { Page, Content, FormContainer, ComboBoxField, CheckBoxListField } from 'jfront-components';

const ClientCreatePage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const applicationTypeOptions = [
    { name: "Native", value: "native" },
    { name: "WEB application", value: "web" },
    { name: "Browser (client-side) application", value: "browser" },
    { name: "Service", value: "service" },
  ]
  let formikRef: any;

  useImperativeHandle(ref, () => ({
    handleSubmit: () => {
      formikRef.handleSubmit();
    }
  }));

  return (
    <Page>
      <Content>
        <FormContainer>
          <Formik
            innerRef={instance => formikRef = instance}
            initialValues={{clientId: '', clientName: '', clientNameEn: '', applicationType: '', grantTypes: [] }}
            onSubmit={(values: Client) => {
              dispatch(createClient(values, (client: Client) => {
                history.push(`/ui/client/${client.clientId}/view/`);
              }));
            }}
            validate={(values) => {
              const errors: { clientId?: string, clientName?: string, applicationType?: string, grantTypes?: string } = {};
              if (!values['clientId']) {
                errors.clientId = 'Поле должно быть заполнено'
              } else {
                if (!/[A-Za-z0-9]/.test(values['clientId'])) {
                  errors.clientId = 'Значение должно состоять из букв английского алфавита и цифр'
                }
                if (values['clientId'].length > 16) {
                  errors.clientId = 'Максимальная длина значения не больше 16 символов'
                }
              }
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
                <Label width={'250px'}>Имя клиентского приложения:</Label>
                <Field name="clientId">
                  {(props: FieldProps) => (
                    <TextInput
                      name={props.field.name}
                      value={props.field.value}
                      onChange={props.field.onChange}
                      onBlur={props.field.onBlur}
                      touched={props.meta.touched}
                      error={props.meta.error}
                    />
                  )}
                </Field>
              </FormField>
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
                      error={props.meta.error} />
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
});

export default ClientCreatePage;