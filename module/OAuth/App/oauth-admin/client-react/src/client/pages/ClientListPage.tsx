import React, { useEffect, useState } from 'react';
import { actions as searchActions } from '../state/clientSearchSlice';
import { actions as crudActions } from '../state/clientCrudSlice';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useLocation } from 'react-router-dom';
import { AppState } from '../../app/store/reducer';
import { Client, ClientSearchTemplate } from '../types';
import { GrantType, ApplicationType } from '@jfront/oauth-core';
import { TextCell } from '../../app/common/components/cell/TextCell';
import { Grid } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';
import { EntityState, SearchState } from '@jfront/core-redux-saga';
import { useQuery } from '../../app/common/useQuery';


export const ClientListPage: React.FC = () => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { t } = useTranslation();
  const { ...template } = useQuery();
  const { currentRecord } = useSelector<AppState, EntityState<Client>>(state => state.client.crudSlice);
  const { records, searchId, searchRequest, resultSetSize, isLoading } = useSelector<AppState, SearchState<ClientSearchTemplate, Client>>(state => state.client.searchSlice);
  const [page, setPage] = useState({
    pageSize: 25,
    pageNumber: 1
  });

  useEffect(() => {
    if (searchId) {
      dispatch(searchActions.search({ searchId, pageSize: page.pageSize, pageNumber: page.pageNumber }))
    }
  }, [searchId, page, dispatch])

  return (
    <Grid<Client>
      columns={[
        {
          Header: t('client.clientId'),
          accessor: "clientId",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: t('client.clientSecret'),
          accessor: "clientSecret",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: t('client.clientName'),
          accessor: "clientName",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: t('client.clientNameEn'),
          accessor: "clientNameEn",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: t('client.applicationType'),
          accessor: "applicationType",
          Cell: ({ value }: any) => <TextCell>{ApplicationType[value]}</TextCell>
        },
        {
          Header: t('client.grantTypes'),
          accessor: (row: Client) => row.grantTypes?.map((grantType) => GrantType[grantType]).join(', '),
          Cell: ({ value }: any) => <TextCell wrapText>{value}</TextCell>,
          disableSortBy: true
        }
      ]}
      isLoading={isLoading}
      data={React.useMemo(() => records, [records])}
      onSelection={(records) => {
        if (records) {
          if (records.length === 1) {
            if (records[0] !== currentRecord) {
              dispatch(crudActions.setCurrentRecord({ currentRecord: records[0] }));
              dispatch(crudActions.selectRecords({ selectedRecords: records }));
            }
          } else if (currentRecord) {
            dispatch(crudActions.setCurrentRecord({} as any));
            dispatch(crudActions.selectRecords({ selectedRecords: records }));
          }
        }
      }}
      onPaging={(pageNumber, pageSize) => {
        setPage({
          pageNumber,
          pageSize
        })
      }}
      onSort={(sortConfig) => {
        const newSearchRequest = {
          template: {
            maxRowCount: 25,
            ...template,
            ...searchRequest?.template
          },
          listSortConfiguration: sortConfig
        }
        dispatch(searchActions.postSearchRequest({ searchTemplate: newSearchRequest }));
      }}
      totalRowCount={resultSetSize}
      onDoubleClick={(record) => currentRecord !== record ? dispatch(crudActions.setCurrentRecord({
        currentRecord: record,
        callback: () => history.push(`/ui/client/${record?.clientId}/detail`)
      })) : history.push(`/ui/client/${record?.clientId}/detail`)}
    />
  );
}