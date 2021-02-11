import React, { useEffect } from 'react';
import { Text } from '../../app/common/components/form/Field';
import { AppState } from '../../app/store/reducer';
import { KeyState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { actions } from '../state/keySlice';
import { Form } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';

const KeyViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { current } = useSelector<AppState, KeyState>(state => state.key);
  const { t } = useTranslation();

  useEffect(() => {
    if (!current) {
      dispatch(actions.getRecordById({ loadingMessage: t('dataLoadingMessage') }));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [current, dispatch]);

  return (
    <Form>
      <Form.Field>
        <Form.Label>{t('key.keyId')}:</Form.Label>
        <Text>{current?.keyId}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('key.publicKey')}:</Form.Label>
        <Text width="200px">{current?.publicKey}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('key.dateIns')}:</Form.Label>
        <Text>{current?.dateIns ? new Date(current?.dateIns).toLocaleString() : ''}</Text>
      </Form.Field>
    </Form>
  )

}

export default KeyViewPage;