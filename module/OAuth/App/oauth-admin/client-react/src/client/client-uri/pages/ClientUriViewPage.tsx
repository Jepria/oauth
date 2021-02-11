import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Text } from '../../../app/common/components/form/Field';
import { AppState } from '../../../app/store/reducer';
import { ClientUri } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { actions } from '../state/clientUriCrudSlice';
import { Form } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';
import { EntityState } from '@jfront/core-redux-saga';

export const ClientUriViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { clientId, clientUriId } = useParams<any>();
  const { t } = useTranslation();
  const { currentRecord } = useSelector<AppState, EntityState<ClientUri>>(state => state.clientUri.crudSlice);

  useEffect(() => {
    if (!currentRecord && clientId && clientUriId) {
      dispatch(actions.getRecordById({ primaryKey: { clientId, clientUriId } }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Form>
      <Form.Field>
        <Form.Label >{t('clientUri.clientUriId')}:</Form.Label>
        <Text>{currentRecord?.clientUriId}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('clientUri.clientUri')}:</Form.Label>
        <Text>{currentRecord?.clientUri}</Text>
      </Form.Field>
    </Form>
  )
}