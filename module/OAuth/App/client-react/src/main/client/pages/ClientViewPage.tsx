import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import styled from 'styled-components';
import { FormField, Label, Text } from '../../../components/form/Field';
import { AppState } from '../../../redux/store';
import { ClientState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getClientById } from '../state/redux/actions';
import { Panel, Column } from '@jfront/ui-core';
import { GrantType, ApplicationType } from '@jfront/oauth-core';
import { useTranslation } from 'react-i18next';

const List = styled.ul`
  display: inline;
  padding: 2px;
  list-style: none;
`;

const ListOption = styled.li`
  font-family: tahoma, arial, helvetica, sans-serif;
  font-size: 12px;
  word-wrap: break-word;
`

const ClientViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { clientId } = useParams<any>();
  const { t } = useTranslation();
  const { current } = useSelector<AppState, ClientState>(state => state.client);

  useEffect(() => {
    if (!current && clientId) {
      dispatch(getClientById(clientId));
    }
  }, [current, clientId, dispatch]);

  return (
    <Panel>
      <Panel.Content>
        <Column>
          <FormField>
            <Label width={'250px'}>{t('client.clientId')}:</Label>
            <Text>{current?.clientId}</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}>{t('client.clientSecret')}:</Label>
            <Text>{current?.clientSecret}</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}>{t('client.clientName')}:</Label>
            <Text>{current?.clientName}</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}>{t('client.clientNameEn')}:</Label>
            <Text>{current?.clientNameEn}</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}>{t('client.applicationType')}:</Label>
            <Text>{current && current.applicationType ? ApplicationType[current.applicationType] : ''}</Text>
          </FormField>
          <FormField>
            <Label width={'250px'}>{t('client.grantTypes')}:</Label>
            <Text>{current?.grantTypes?.map((grantType) => GrantType[grantType]).join(', ')}</Text>
          </FormField>
          {current?.grantTypes?.includes("client_credentials") &&
            <FormField>
              <Label width={'250px'}>{t('client.scopes')}:</Label>
              <List>
                {current?.scopes?.map(scope => <ListOption key={scope.value}>{scope.name}</ListOption>)}
              </List>
            </FormField>
          }
        </Column>
      </Panel.Content>
    </Panel>
  )

}

export default ClientViewPage;