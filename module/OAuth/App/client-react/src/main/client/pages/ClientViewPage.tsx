import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Page, Content, VerticalLayout, Form as FormContainer } from '../../../components/Layout';
import { FormField, Label, Text } from '../../../components/form/Field';
import { AppState } from '../../store';
import { ClientState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getClientById } from '../state/redux/actions';
import { GrantType, ApplicationType } from '../../../security/OAuth';

const ClientViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { clientId } = useParams();
  const { current } = useSelector<AppState, ClientState>(state => state.client);

  useEffect(() => {
    if (!current && clientId) {
      dispatch(getClientById(clientId));
    }
  }, [current, clientId, dispatch]);

  return (
    <Page>
      <Content>
        <FormContainer>
          <VerticalLayout>
            <FormField>
              <Label width={'250px'}>ID клиентского приложения:</Label>
              <Text>{current?.clientId}</Text>
            </FormField>
            <FormField>
              <Label width={'250px'}>Секретное слово:</Label>
              <Text>{current?.clientSecret}</Text>
            </FormField>
            <FormField>
              <Label width={'250px'}>Имя клиентского приложения:</Label>
              <Text>{current?.clientName}</Text>
            </FormField>
            <FormField>
              <Label width={'250px'}>Имя клиентского приложения(англ):</Label>
              <Text>{current?.clientNameEn}</Text>
            </FormField>
            <FormField>
              <Label width={'250px'}>Тип приложения:</Label>
              <Text>{current ? ApplicationType[current.applicationType] : ''}</Text>
            </FormField>
            <FormField>
              <Label width={'250px'}> Доступные гранты:</Label>
              <Text>{current?.grantTypes.map((grantType) => GrantType[grantType]).join(', ')}</Text>
            </FormField>
          </VerticalLayout>
        </FormContainer>
      </Content>
    </Page>
  )

}

export default ClientViewPage;