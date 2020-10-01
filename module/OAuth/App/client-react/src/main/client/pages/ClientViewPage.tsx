import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import styled from 'styled-components';
import { Text } from '../../../components/form/Field';
import { AppState } from '../../../redux/store';
import { ClientState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getClientById } from '../state/redux/actions';
import { Form } from '@jfront/ui-core';
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
      dispatch(getClientById(clientId, t("dataLoadingMessage")));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [current, clientId, dispatch]);

  return (
    <Form>
      <Form.Field>
        <Form.Label>{t('client.clientId')}:</Form.Label>
        <Text>{current?.clientId}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.clientSecret')}:</Form.Label>
        <Text>{current?.clientSecret}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.clientName')}:</Form.Label>
        <Text>{current?.clientName}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.clientNameEn')}:</Form.Label>
        <Text>{current?.clientNameEn}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.applicationType')}:</Form.Label>
        <Text>{current && current.applicationType ? ApplicationType[current.applicationType] : ''}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.grantTypes')}:</Form.Label>
        <Text>{current?.grantTypes?.map((grantType) => GrantType[grantType]).join(', ')}</Text>
      </Form.Field>
      {current?.grantTypes?.includes("client_credentials") &&
        <Form.Field>
          <Form.Label>{t('client.scopes')}:</Form.Label>
          <List>
            {current?.scopes?.map(scope => <ListOption key={scope.value}>{scope.name}</ListOption>)}
          </List>
        </Form.Field>
      }
    </Form>
  )

}

export default ClientViewPage;