import React, { useEffect } from 'react';
import { actions } from '../state/clientUriSlice';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useParams, useLocation } from 'react-router-dom';
import { AppState } from '../../../app/store/reducer';
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
      dispatch(actions.search({ clientId, loadingMessage: t('dataLoadingMessage') }));
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
      onSelection={(records) => {
        if (records) {
          if (records.length === 1) {
            if (records[0] !== current) {
              dispatch(actions.setCurrentRecord({ currentRecord: records[0] }));
              dispatch(actions.selectRecords({ records }));
            }
          } else if (current) {
            dispatch(actions.setCurrentRecord({}));
            dispatch(actions.selectRecords({ records }));
          }
        }
      }}
      onDoubleClick={(record) => {
        if (current !== record) {
          dispatch(actions.setCurrentRecord({
            currentRecord: record,
            callback: () => history.push(`/ui/client/${clientId}/client-uri/${record?.clientUriId}/view`, state)
          }))
        } else {
          history.push(`/ui/client/${clientId}/client-uri/${record?.clientUriId}/view`, state)
        }
      }} />
  );
}