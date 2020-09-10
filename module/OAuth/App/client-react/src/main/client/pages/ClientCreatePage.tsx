import React, { HTMLAttributes, useImperativeHandle, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import { FormField, Label } from '../../../components/form/Field';
import { useFormik } from 'formik';
import { useDispatch, useSelector } from 'react-redux';
import { Client, ClientState } from '../types';
import { createClient, getRoles } from '../state/redux/actions';
import { GrantType, ApplicationType, ApplicationGrantType } from '@jfront/oauth-core';
import { Page, Content, FormContainer, ComboBox, CheckBoxListInput, TextInput, CheckBoxGroup, CheckBox } from '@jfront/ui-core';
import { DualListField } from '../../../components/form/input/DualListField';
import { AppState } from '../../store';

const ClientCreatePage = React.forwardRef<HTMLFormElement, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { roles } = useSelector<AppState, ClientState>(state => state.client);

  const formik = useFormik<Client>({
    initialValues: { clientName: '', applicationType: '', grantTypes: [] },
    onSubmit: (values: Client) => {
      dispatch(createClient(values, (client: Client) => {
        history.push(`/ui/client/${client.clientId}/view/`);
      }));
    },
    validate: (values) => {
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
    }
  })

  const applicationTypeOptions = [
    { name: "Native", value: "native" },
    { name: "WEB application", value: "web" },
    { name: "Browser (client-side) application", value: "browser" },
    { name: "Service", value: "service" },
  ]


  useEffect(() => {
    dispatch(getRoles(""));
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Page>
      <Content>
        <FormContainer>
          <form onSubmit={formik.handleSubmit} ref={ref}>
            <FormField>
              <Label width={'250px'}>ID приложения:</Label>
              <TextInput
                name="clientId"
                value={formik.initialValues.clientId}
                onChange={formik.handleChange}
                error={formik.errors.clientId} />
            </FormField>
            <FormField>
              <Label width={'250px'}>Наименование приложения:</Label>
              <TextInput
                name="clientName"
                value={formik.initialValues.clientName}
                onChange={formik.handleChange}
                error={formik.errors.clientName} />
            </FormField>
            <FormField>
              <Label width={'250px'}>Наименование приложения(англ):</Label>
              <TextInput
                name="clientNameEn"
                value={formik.initialValues.clientNameEn}
                onChange={formik.handleChange}
                error={formik.errors.clientNameEn} />
            </FormField>
            <FormField>
              <Label width={'250px'}>Тип приложения:</Label>
              <ComboBox
                options={applicationTypeOptions}
                name="applicationType"
                // initialValue={formik.initialValues.applicationType}
                error={formik.errors.applicationType}
                onChangeValue={formik.setFieldValue}
                style={{ width: "250px" }} />
            </FormField>
            <FormField>
              <Label width={'250px'}>Разрешения на авторизацию:</Label>
              <CheckBoxGroup
                name="grantTypes"
                value={formik.initialValues.grantTypes}
                style={{
                  minWidth: "200px",
                  minHeight: "150px"
                }}
                error={Array.isArray(formik.errors.grantTypes) ? formik.errors.grantTypes.join(", ") : formik.errors.grantTypes}
                onChange={(name = "grantTypes", value) => formik.setFieldValue(name, value)}>
                {ApplicationGrantType[formik.values["applicationType"]]?.map(grantType => ({ name: GrantType[grantType], value: grantType }))
                  .map(option => <CheckBox key={String(option.value)} label={option.name} value={option.value} />)}
              </CheckBoxGroup>
              {/* <CheckBoxListInput
                options={formik.values["applicationType"] ? ApplicationGrantType[formik.values["applicationType"]]?.map(grantType => ({ name: GrantType[grantType], value: grantType })) : []}
                name="grantTypes"
                // initialValue={formik.values.grantTypes?.map((value: any) => ({ name: GrantType[value], value: value }))}
                // initialValue={[]}
                onChangeValue={formik.setFieldValue}
                touched={formik.touched.grantTypes}
                error={Array.isArray(formik.errors.grantTypes) ? formik.errors.grantTypes.join(", ") : formik.errors.grantTypes} /> */}
            </FormField>
            {formik.values["grantTypes"]?.includes('client_credentials') &&
              <FormField>
                <Label width={'250px'}>Права доступа:</Label>
                <DualListField
                  options={roles ? roles : []}
                  placeholder="Введите имя роли"
                  name="scopes"
                  onChange={e => dispatch(getRoles(e.target.value))}
                  onChangeValue={formik.setFieldValue}
                  touched={formik.touched.scopes}
                  error={formik.errors.scopes} />
              </FormField>
            }
          </form>
        </FormContainer>
      </Content>
    </Page >
  )
});

export default ClientCreatePage;