import React, { HTMLAttributes, useImperativeHandle, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import { FormField, Label } from '../../../components/form/Field';
import { Formik, Form, Field, FieldProps } from 'formik';
import { TextInput } from '../../../components/form/input/TextInput';
import { useSelector, useDispatch } from 'react-redux';
import { SessionSearchTemplate, SessionState } from '../types';
import { postSearchSessionRequest, getClients, getOperators } from '../state/redux/actions';
import { AppState } from '../../store';
import { ComboBox, ComboBoxInput, ComboBoxPopup, ComboBoxList, ComboBoxOption } from '../../../components/form/input/combobox';
import { Page, Content, FormContainer } from 'jfront-components';

const SessionSearchPage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { clients, operators, searchRequest } = useSelector<AppState, SessionState>(state => state.session);
  let formikRef: any;

  useImperativeHandle(ref, () => ({
    handleSubmit: () => {
      formikRef.handleSubmit();
    }
  }));

  useEffect(() => {
    dispatch(getOperators(""));
    dispatch(getClients(""));
  },[]);

  return (
    <Page>
      <Content>
        <FormContainer>
          <Formik
            innerRef={formik => formikRef = formik}
            initialValues={searchRequest?.template ? searchRequest.template : { maxRowCount: 25 }}
            onSubmit={(values: SessionSearchTemplate) => {
              dispatch(postSearchSessionRequest({
                template: values
              }));
              history.push('/ui/session/list');
            }}>
            <Form {...props}>
              <FormField>
                <Label>Пользователь:</Label>
                <Field name="operatorId">
                  {(props: FieldProps) => (
                    <ComboBox name={props.field.name} value={props.field.value} touched={props.meta.touched} error={props.meta.error} onChange={(field, value) => {
                      if (value !== props.field.value) {
                        props.form.setFieldValue('grantTypes', []);
                        props.form.setFieldValue(field, value);
                      }
                    }} width='250px'>
                      <ComboBoxInput onChange={e => dispatch(getOperators(e.target.value))} placeholder='Введите имя пользователя' />
                      <ComboBoxPopup>
                        <ComboBoxList>
                          <ComboBoxOption name="" value={undefined} />
                          {operators?.map(operator => <ComboBoxOption key={operator.value} name={operator.name} value={operator.value} />)}
                        </ComboBoxList>
                      </ComboBoxPopup>
                    </ComboBox>)}
                </Field>
              </FormField>
              <FormField>
                <Label>Приложение:</Label>
                <Field name="clientId">
                  {(props: FieldProps) => (
                    <ComboBox name={props.field.name} value={props.field.value} touched={props.meta.touched} error={props.meta.error} width='250px'>
                      <ComboBoxInput onChange={e => dispatch(getClients(e.target.value))} placeholder='Введите имя приложения' />
                      <ComboBoxPopup>
                        <ComboBoxList>
                          <ComboBoxOption name="" value="" />
                          {clients?.map(client => <ComboBoxOption key={client.clientId} name={client.clientName} value={client.clientId} />)}
                        </ComboBoxList>
                      </ComboBoxPopup>
                    </ComboBox>)}
                </Field>
              </FormField>
              <FormField>
                <Label>Количество записей:</Label>
                <Field name="maxRowCount" as={TextInput} />
              </FormField>
            </Form>
          </Formik>
        </FormContainer>
      </Content>
    </Page>
  )
})

export default SessionSearchPage;