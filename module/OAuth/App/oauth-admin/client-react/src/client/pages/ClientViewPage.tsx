import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import styled from 'styled-components';
import { Text } from '../../app/common/components/form/Field';
import { AppState } from '../../app/store/reducer';
import { Client } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { actions } from '../state/clientCrudSlice';
import { Form } from '@jfront/ui-core';
import { GrantType, ApplicationType } from '@jfront/oauth-core';
import { useTranslation } from 'react-i18next';
import { EntityState } from '@jfront/core-redux-saga';

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
  const { currentRecord } = useSelector<AppState, EntityState<Client>>(state => state.client.crudSlice);

  useEffect(() => {
    if (!currentRecord && clientId) {
      dispatch(actions.getRecordById({ primaryKey: clientId }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Form>
      <Form.Field>
        <Form.Label>{t('client.clientId')}:</Form.Label>
        <Text>{currentRecord?.clientId}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.clientSecret')}:</Form.Label>
        <Text>{currentRecord?.clientSecret}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.clientName')}:</Form.Label>
        <Text>{currentRecord?.clientName}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.clientNameEn')}:</Form.Label>
        <Text>{currentRecord?.clientNameEn}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.applicationType')}:</Form.Label>
        <Text>{currentRecord && currentRecord.applicationType ? ApplicationType[currentRecord.applicationType] : ''}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.grantTypes')}:</Form.Label>
        <Text>{currentRecord?.grantTypes?.map((grantType) => GrantType[grantType]).join(', ')}</Text>
      </Form.Field>
      {currentRecord?.grantTypes?.includes("client_credentials") &&
        <Form.Field>
          <Form.Label>{t('client.scopes')}:</Form.Label>
          <List>
            {currentRecord?.scope?.map(scope => <ListOption key={scope.value}>{scope.name}</ListOption>)}
          </List>
        </Form.Field>
      }
    </Form>
  )

}

export default ClientViewPage;