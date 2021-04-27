import React, { useEffect, useState } from 'react';
import { actions as searchActions } from '../state/clientSearchSlice';
import { actions as crudActions } from '../state/clientCrudSlice';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
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
  const { currentRecord, selectedRecords } = useSelector<AppState, EntityState<Client>>(state => state.client.crudSlice);
  const { records, searchRequest, resultSetSize, isLoading } = useSelector<AppState, SearchState<ClientSearchTemplate, Client>>(state => state.client.searchSlice);

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
          Header: t('client.loginModuleUri'),
          accessor: "loginModuleUri",
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
          if (records.join() !== selectedRecords.join()){
            dispatch(crudActions.setCurrentRecord({} as any));
            dispatch(crudActions.selectRecords({ selectedRecords: records }));
          }
          if (records.length === 1) {
            if (records[0] !== currentRecord) {
              dispatch(crudActions.setCurrentRecord({ currentRecord: records[0] }));
              dispatch(crudActions.selectRecords({ selectedRecords: records }));
            }
          }
        }
      }}
      manualPaging
      manualSort
      fetchData={(pageNumber, pageSize, sortConfigs) => {
        const newSearchRequest = {
          template: {
            maxRowCount: 25,
            ...template,
            ...searchRequest?.template,
          },
          listSortConfiguration: sortConfigs,
        };
        dispatch(
          searchActions.search({
            searchTemplate: newSearchRequest,
            pageNumber,
            pageSize,
          })
        );
      }}
      totalRowCount={resultSetSize}
      onDoubleClick={(record) => currentRecord !== record ? dispatch(crudActions.setCurrentRecord({
        currentRecord: record,
        callback: () => history.push(`/client/${record?.clientId}/detail`)
      })) : history.push(`/client/${record?.clientId}/detail`)}
    />
  );
}