import React, { useEffect } from 'react';
import { setCurrentRecord, searchClients, postSearchClientRequest } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { AppState } from '../../../redux/store';
import { ClientState, Client } from '../types';
import { GrantType, ApplicationType } from '@jfront/oauth-core';
import { TextCell } from '../../../components/cell/TextCell';
import { Grid } from '@jfront/ui-core';

export const ClientListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { records, current, searchId, searchRequest, resultSetSize } = useSelector<AppState, ClientState>(state => state.client);

  useEffect(() => {
    if (searchId && searchRequest) {
      dispatch(searchClients(searchId, 25, 1));
    } else if (!searchId && searchRequest) {
      dispatch(postSearchClientRequest(searchRequest));
    }
  }, [searchId, searchRequest, dispatch]);

  return (
    <Grid<Client> columns={[
      {
        Header: "ID приложения",
        accessor: "clientId",
        Cell: ({ value }: any) => <TextCell>{value}</TextCell>
      },
      {
        Header: "Секретное слово",
        accessor: "clientSecret",
        Cell: ({ value }: any) => <TextCell>{value}</TextCell>
      },
      {
        Header: "Наименование",
        accessor: "clientName",
        Cell: ({ value }: any) => <TextCell>{value}</TextCell>
      },
      {
        Header: "Наименование (англ)",
        accessor: "clientNameEn",
        Cell: ({ value }: any) => <TextCell>{value}</TextCell>
      },
      {
        Header: "Тип приложения",
        accessor: "applicationType",
        Cell: ({ value }: any) => <TextCell>{ApplicationType[value]}</TextCell>
      },
      {
        Header: "Разрешенные типы авторизации",
        accessor: (row: Client) => row.grantTypes.map((grantType) => GrantType[grantType]).join(', '),
        Cell: ({ value }: any) => <TextCell wrapText>{value}</TextCell>,
        disableSortBy: true
      }
    ]}
      data={React.useMemo(() => records, [records])}
      onSelection={(selected) => {
        if (selected) {
          if (selected.length === 1) {
            if (selected[0] !== current) {
              dispatch(setCurrentRecord(selected[0]))
            }
          } else if (current) {
            dispatch(setCurrentRecord(undefined))
          }
        }
      }}
      onPaging={(pageNumber, pageSize) => {
        if (searchId) {
          dispatch(searchClients(searchId, pageSize, pageNumber + 1))
        }
      }}
      onSort={(sortConfig) => {
        if (searchRequest) {
          const newSearchRequest = {
            ...searchRequest,
            listSortConfiguration: sortConfig
          }
          dispatch(postSearchClientRequest(newSearchRequest));
        } else {
          dispatch(postSearchClientRequest({ template: { maxRowCount: 25 }, listSortConfiguration: sortConfig }));
        }
      }}
      totalRowCount={resultSetSize}
      onDoubleClick={(record) => current !== record ? dispatch(setCurrentRecord(record,
        () => history.push(`/ui/client/${record?.clientId}/view`))) : history.push(`/ui/client/${record?.clientId}/view`)}
    />
  );
}