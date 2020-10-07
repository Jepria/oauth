import React, { useEffect } from 'react';
import { setCurrentRecord, searchClients, postSearchClientRequest, selectRecords } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { AppState } from '../../../redux/store';
import { ClientState, Client } from '../types';
import { GrantType, ApplicationType } from '@jfront/oauth-core';
import { TextCell } from '../../../components/cell/TextCell';
import { Grid } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';

export const ClientListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { t } = useTranslation();
  const { records, current, searchId, searchRequest, resultSetSize, recordsLoading } = useSelector<AppState, ClientState>(state => state.client);

  useEffect(() => {
    if (searchId && searchRequest) {
      dispatch(searchClients(searchId, 25, 1, t("dataLoadingMessage")));
    } else if (!searchId && searchRequest) {
      dispatch(postSearchClientRequest(searchRequest, t("dataLoadingMessage")));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchId, searchRequest, dispatch]);
  
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
      isLoading={recordsLoading}
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
      onPaging={(pageNumber, pageSize) => {
        if (searchId) {
          dispatch(searchClients(searchId, pageSize, pageNumber + 1, t("dataLoadingMessage")))
        }
      }}
      onSort={(sortConfig) => {
        if (searchRequest) {
          const newSearchRequest = {
            ...searchRequest,
            listSortConfiguration: sortConfig
          }
          dispatch(postSearchClientRequest(newSearchRequest, t("dataLoadingMessage")));
        } else {
          dispatch(postSearchClientRequest({ template: { maxRowCount: 25 }, listSortConfiguration: sortConfig }, t("dataLoadingMessage")));
        }
      }}
      totalRowCount={resultSetSize}
      onDoubleClick={(record) => current !== record ? dispatch(setCurrentRecord(record,
        () => history.push(`/ui/client/${record?.clientId}/view`))) : history.push(`/ui/client/${record?.clientId}/view`)}
    />
  );
}