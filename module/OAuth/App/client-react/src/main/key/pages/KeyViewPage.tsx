import React, { useEffect } from 'react';
import { Text } from '../../../components/form/Field';
import { AppState } from '../../../redux/store';
import { KeyState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getKey } from '../state/redux/actions';
import { Form } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';

const KeyViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { current } = useSelector<AppState, KeyState>(state => state.key);
  const { t } = useTranslation();

  useEffect(() => {
    if (!current) {
      dispatch(getKey());
    }
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