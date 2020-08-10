import React, { HTMLAttributes, useImperativeHandle, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import { FormField, Label } from '../../../components/form/Field';
import { Formik, Form, Field, FieldProps } from 'formik';
import { TextInput } from '../../../components/form/input/TextInput';
import { useSelector, useDispatch } from 'react-redux';
import { SessionSearchTemplate, SessionState } from '../types';
import { postSearchSessionRequest, getClients, getOperators } from '../state/redux/actions';
import { AppState } from '../../store';
import { Page, Content, FormContainer, ComboBoxField } from 'jfront-components';

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
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Page>
      <Content>
        <FormContainer>
          <Formik
            innerRef={formik => formikRef = formik}
            initialValues={{ maxRowCount: 25, ...searchRequest?.template, operatorId: undefined, clientId: undefined}}
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
                    <ComboBoxField
                      options={operators ? operators : []}
                      name={props.field.name}
                      hasEmptyOption
                      touched={props.meta.touched}
                      error={props.meta.error}
                      placeholder='Введите имя пользователя'
                      onChange={(e: { target: { value: string | undefined; }; }) => dispatch(getOperators(e.target.value))}
                      onChangeValue={props.form.setFieldValue} width='250px' />)}
                </Field>
              </FormField>
              <FormField>
                <Label>Приложение:</Label>
                <Field name="clientId">
                  {(props: FieldProps) => (
                    <ComboBoxField
                      options={clients ? clients : []}
                      name={props.field.name}
                      hasEmptyOption
                      touched={props.meta.touched}
                      error={props.meta.error}
                      onChange={(e: { target: { value: string | undefined; }; }) => dispatch(getClients(e.target.value))}
                      getOptionName={(option: { clientName: any; }) => {
                        return option.clientName;
                      }}
                      getOptionValue={(option: { clientId: any; }) => option.clientId}
                      onChangeValue={props.form.setFieldValue} width='250px' />)}
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