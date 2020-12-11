import React, { useEffect } from 'react';
import { setCurrentRecord, searchClientUri, selectRecords } from '../state/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useParams, useLocation } from 'react-router-dom';
import { AppState } from '../../../app/store';
import { ClientUriState, ClientUri } from '../types';
import { HistoryState } from '../../../app/common/components/HistoryState';
import { TextCell } from '../../../app/common/components/cell/TextCell';
import { Grid } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';

export const ClientUriListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { clientId } = useParams<any>();
  const { t } = useTranslation();
  const { records, current } = useSelector<AppState, ClientUriState>(state => state.clientUri);
  const { state } = useLocation<HistoryState>();

  useEffect(() => {
    if (clientId) {
      dispatch(searchClientUri(clientId, t('dataLoadingMessage')));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [clientId, dispatch]);

  return (
    <Grid<ClientUri>
      columns={[
        {
          Header: t('clientUri.clientUriId'),
          accessor: "clientUriId",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: t('clientUri.clientUri'),
          accessor: "clientUri",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
      ]}
      data={React.useMemo(() => records, [records])}
      onSelection={(selected) => {
        if (selected) {
          if (selected.length === 1) {
            if (selected[0] !== current) {
              dispatch(setCurrentRecord(selected[0]))
              dispatch(selectRecords(selected))
            }
          } else if (current) {
            dispatch(setCurrentRecord(undefined))
            dispatch(selectRecords(selected))
          }
        }
      }}
      onDoubleClick={(record) => current !== record ? dispatch(setCurrentRecord(record,
        () => history.push(`/ui/client/${clientId}/client-uri/${record?.clientUriId}/view`, state)))
        : history.push(`/ui/client/${clientId}/client-uri/${record?.clientUriId}/view`,
          state)} />
  );
}