import React, { useEffect } from 'react';
import { actions } from '../state/clientSlice';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useLocation } from 'react-router-dom';
import { AppState } from '../../app/store/reducer';
import { ClientState, Client, ClientSearchTemplate } from '../types';
import { GrantType, ApplicationType } from '@jfront/oauth-core';
import { TextCell } from '../../app/common/components/cell/TextCell';
import { Grid } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';
import queryString from 'query-string';

const useQuery = () => {
  return queryString.parse(useLocation().search);
}

export const ClientListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { t } = useTranslation();
  const { pageSize, page, ...searchTemplate } = useQuery();
  const { records, current, searchId, searchRequest, resultSetSize, recordsLoading } = useSelector<AppState, ClientState>(state => state.client);

  useEffect(() => {
    if (searchId && searchRequest) {
      dispatch(actions.search({ searchId, pageSize: 25, page: 1, loadingMessage: t("dataLoadingMessage") }));
    } else if (!searchId && searchRequest) {
      dispatch(actions.postSearchTemplate({ searchRequest, loadingMessage: t("dataLoadingMessage") }));
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
      onPaging={(pageNumber, pageSize) => {
        if (searchId) {
          dispatch(actions.search({ searchId, pageSize, page: pageNumber, loadingMessage: t("dataLoadingMessage") }))
        }
      }}
      onSort={(sortConfig) => {
        if (searchRequest) {
          const newSearchRequest = {
            ...searchRequest,
            listSortConfiguration: sortConfig
          }
          dispatch(actions.postSearchTemplate({ searchRequest: newSearchRequest, loadingMessage: t('dataLoadingMessage') }));
        } else {
          if (pageSize && page) {
            dispatch(actions.postSearchTemplate({
              searchRequest: { template: searchTemplate as unknown as ClientSearchTemplate, listSortConfiguration: sortConfig },
              loadingMessage: t('dataLoadingMessage')
            }));
          } else {
            dispatch(actions.postSearchTemplate({
              searchRequest: {
                template: { maxRowCount: 25 },
                listSortConfiguration: sortConfig
              },
              loadingMessage: t('dataLoadingMessage')
            }));
          }
        }
      }}
      totalRowCount={resultSetSize}
      onDoubleClick={(record) => current !== record ? dispatch(actions.setCurrentRecord({
        currentRecord: record,
        callback: () => history.push(`/ui/client/${record?.clientId}/view`)
      })) : history.push(`/ui/client/${record?.clientId}/view`)}
    />
  );
}