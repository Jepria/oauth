import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import styled from 'styled-components';
import { FormField, Label, Text } from '../../../components/form/Field';
import { AppState } from '../../store';
import { ClientState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getClientById } from '../state/redux/actions';
import { Page, Content, FormContainer, VerticalLayout } from '@jfront/ui-core';
import { GrantType, ApplicationType } from '@jfront/oauth-core';

const List = styled.ul`
  display: inline;
  padding: 2px;
`;

const ListOption = styled.li`
  font-family: tahoma, arial, helvetica, sans-serif;
  font-size: 12px;
  word-wrap: break-word;
`

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
              <Label width={'250px'}>ID приложения:</Label>
              <Text>{current?.clientId}</Text>
            </FormField>
            <FormField>
              <Label width={'250px'}>Секретное слово:</Label>
              <Text>{current?.clientSecret}</Text>
            </FormField>
            <FormField>
              <Label width={'250px'}>Наименование приложения:</Label>
              <Text>{current?.clientName}</Text>
            </FormField>
            <FormField>
              <Label width={'250px'}>Наименование приложения(англ):</Label>
              <Text>{current?.clientNameEn}</Text>
            </FormField>
            <FormField>
              <Label width={'250px'}>Тип приложения:</Label>
              <Text>{current ? ApplicationType[current.applicationType] : ''}</Text>
            </FormField>
            <FormField>
              <Label width={'250px'}>Разрешения на авторизацию:</Label>
              <Text>{current?.grantTypes.map((grantType) => GrantType[grantType]).join(', ')}</Text>
            </FormField>
            {current?.grantTypes?.includes("client_credentials") &&
              <FormField>
                <Label width={'250px'}>Права доступа:</Label>
                <List>
                  {current?.scopes?.map(scope => <ListOption>{scope.name}</ListOption>)}
                </List>
              </FormField>
            }
          </VerticalLayout>
        </FormContainer>
      </Content>
    </Page>
  )

}

export default ClientViewPage;