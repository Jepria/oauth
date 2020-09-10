import React, { HTMLAttributes, useImperativeHandle } from 'react';
import { useHistory } from 'react-router-dom';
import { FormField, Label } from '../../../components/form/Field';
import { Formik, Form, Field } from 'formik';
import { useSelector, useDispatch } from 'react-redux';
import { ClientSearchTemplate } from '../types';
import { postSearchClientRequest } from '../state/redux/actions';
import { AppState } from '../../store';
import { Page, Content, FormContainer, TextInput } from '@jfront/ui-core';

const ClientSearchPage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const searchTemplate = useSelector<AppState, ClientSearchTemplate | undefined>(state => state.client.searchRequest?.template);
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
            innerRef={formik => formikRef = formik}
            initialValues={searchTemplate ? searchTemplate : { maxRowCount: 25 }}
            validate={(values) => {
              const errors: { maxRowCount?: string } = {};
              if (!values['maxRowCount']) {
                errors.maxRowCount = 'Поле должно быть заполнено'
              } else if (!/[0-9]/.test(`${values['maxRowCount']}`)) {
                errors.maxRowCount = 'Значение должно состоять из цифр'
              }
              return errors;
            }}
            onSubmit={(values: ClientSearchTemplate) => {
              dispatch(postSearchClientRequest({
                template: values
              }));
              history.push('/ui/client/list');
            }}>
            <Form {...props}>
              <FormField>
                <Label width={'250px'}>ID приложения:</Label>
                <Field name="clientId" as={TextInput} />
              </FormField>
              <FormField>
                <Label width={'250px'}>Наименование приложения:</Label>
                <Field name="clientName" as={TextInput} />
              </FormField>
              <FormField>
                <Label width={'250px'}>Наименование приложения (англ.):</Label>
                <Field name="clientNameEn" as={TextInput} />
              </FormField>
              <FormField>
                <Label width={'250px'}>Количество записей:</Label>
                <Field name="maxRowCount" as={TextInput} />
              </FormField>
            </Form>
          </Formik>
        </FormContainer>
      </Content>
    </Page>
  )
})

export default ClientSearchPage;