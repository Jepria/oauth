import React, { useEffect } from 'react';
import { actions as searchActions } from '../state/clientUriSearchSlice';
import { actions as crudActions } from '../state/clientUriCrudSlice';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useParams, useLocation } from 'react-router-dom';
import { AppState } from '../../../app/store/reducer';
import { ClientUri, ClientUriSearchState } from '../types';
import { HistoryState } from '../../../app/common/components/HistoryState';
import { TextCell } from '../../../app/common/components/cell/TextCell';
import { Grid } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';
import { EntityState } from '@jfront/core-redux-saga';

export const ClientUriListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { clientId } = useParams<any>();
  const { t } = useTranslation();
  const { records, isLoading } = useSelector<AppState, ClientUriSearchState>(state => state.clientUri.searchSlice);
  const { currentRecord } = useSelector<AppState, EntityState<ClientUri>>(state => state.clientUri.crudSlice);
  const { state } = useLocation<HistoryState>();

  useEffect(() => {
    if (clientId) {
      dispatch(searchActions.search({ clientId }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [clientId, dispatch]);

  return (
    <Grid<ClientUri>
      isLoading={isLoading}
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
      onRefresh={() => {
        dispatch(searchActions.search({ clientId }))
      }}
      data={React.useMemo(() => records, [records])}
      onSelection={(records) => {
        if (records) {
          if (records.length === 1) {
            if (records[0] !== currentRecord) {
              dispatch(crudActions.setCurrentRecord({ currentRecord: records[0] }));
              dispatch(crudActions.selectRecords({ selectedRecords: records }));
            }
          } else if (currentRecord) {
            dispatch(crudActions.setCurrentRecord({}));
            dispatch(crudActions.selectRecords({ selectedRecords: records }));
          }
        }
      }}
      onDoubleClick={(record) => {
        if (currentRecord !== record) {
          dispatch(crudActions.setCurrentRecord({
            currentRecord: record,
            callback: () => history.push(`/ui/client/${clientId}/client-uri/${record?.clientUriId}/view`, state)
          }))
        } else {
          history.push(`/ui/client/${clientId}/client-uri/${record?.clientUriId}/view`, state)
        }
      }} />
  );
}