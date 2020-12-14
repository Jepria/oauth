import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Text } from '../../../app/common/components/form/Field';
import { AppState } from '../../../app/store/reducer';
import { ClientUriState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { actions } from '../state/clientUriSlice';
import { Form } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';

export const ClientUriViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { clientId, clientUriId } = useParams<any>();
  const { t } = useTranslation();
  const { current } = useSelector<AppState, ClientUriState>(state => state.clientUri);

  useEffect(() => {
    if (!current && clientId && clientUriId) {
      dispatch(actions.getRecordById({ clientId, clientUriId, loadingMessage: t('dataLoadingMessage') }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Form>
      <Form.Field>
        <Form.Label >{t('clientUri.clientUriId')}:</Form.Label>
        <Text>{current?.clientUriId}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('clientUri.clientUri')}:</Form.Label>
        <Text>{current?.clientUri}</Text>
      </Form.Field>
    </Form>
  )
}