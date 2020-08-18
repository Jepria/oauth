import React, { HTMLAttributes, useImperativeHandle, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import { FormField, Label } from '../../../components/form/Field';
import { Formik, Form, Field, FieldProps } from 'formik';
import { TextInput } from '../../../components/form/input/TextInput';
import { useDispatch, useSelector } from 'react-redux';
import { Client, ClientState } from '../types';
import { createClient, getRoles } from '../state/redux/actions';
import { GrantType, ApplicationType, ApplicationGrantType } from '@jfront/oauth-core';
import { Page, Content, FormContainer, ComboBoxField, CheckBoxListField } from '@jfront/ui-core';
import { DualListField } from '../../../components/form/input/DualListField';
import { AppState } from '../../store';

const ClientCreatePage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { roles } = useSelector<AppState, ClientState>(state => state.client);
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
    dispatch(getRoles(""));
  }, []);

  return (
    <Page>
      <Content>
        <FormContainer>
          <Formik
            innerRef={instance => formikRef = instance}
            initialValues={{ clientName: '', applicationType: '', grantTypes: [] }}
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
                if (values['clientId'].length > 32) {
                  errors.clientId = 'Максимальная длина значения не больше 32 символов'
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
            {({ values }) => (
              <Form {...props}>
                <FormField>
                  <Label width={'250px'}>ID приложения:</Label>
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
                  <Label width={'250px'}>Наименование приложения:</Label>
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
                  <Label width={'250px'}>Наименование приложения(англ):</Label>
                  <Field name="clientNameEn" as={TextInput} />
                </FormField>
                <FormField>
                  <Label width={'250px'}>Тип приложения:</Label>
                  <Field name="applicationType">
                    {(props: FieldProps) => (
                      <ComboBoxField
                        options={applicationTypeOptions}
                        name={props.field.name}
                        initialValue={props.field.value ? { name: ApplicationType[props.field.value], value: props.field.value } : undefined}
                        touched={props.meta.touched}
                        error={props.meta.error}
                        onChangeValue={(field: string, value: any) => {
                          if (value !== props.field.value) {
                            props.form.setFieldValue('grantTypes', []);
                            props.form.setFieldValue(field, value);
                          }
                        }} width='250px' />)}
                  </Field>
                </FormField>
                <FormField>
                  <Label width={'250px'}>Разрешения на авторизацию:</Label>
                  <Field name='grantTypes'>
                    {(props: FieldProps) => {
                      const grantTypeOptions = ApplicationGrantType[props.form.values["applicationType"]]?.map(grantType => ({ name: GrantType[grantType], value: grantType }));
                      return (
                        <CheckBoxListField
                          options={grantTypeOptions ? grantTypeOptions : []}
                          name={props.field.name}
                          initialValue={props.field.value?.map((value: any) => ({ name: GrantType[value], value: value }))}
                          onChangeValue={props.form.setFieldValue}
                          touched={props.meta.touched}
                          error={props.meta.error} />
                      );
                    }}
                  </Field>
                </FormField>
                {values["grantTypes"]?.includes('client_credentials') &&
                  <FormField>
                    <Label width={'250px'}>Права доступа:</Label>
                    <Field name='scopes'>
                      {(props: FieldProps) => (
                        <DualListField
                          options={roles ? roles : []}
                          placeholder="Введите имя роли"
                          name={props.field.name}
                          onChange={e => dispatch(getRoles(e.target.value))}
                          onChangeValue={props.form.setFieldValue}
                          touched={props.meta.touched}
                          error={props.meta.error} />
                      )}
                    </Field>
                  </FormField>
                }
              </Form>
            )}
          </Formik>
        </FormContainer>
      </Content>
    </Page>
  )
});

export default ClientCreatePage;