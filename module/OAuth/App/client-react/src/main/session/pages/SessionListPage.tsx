import React, { useEffect, useRef } from 'react';
import { setCurrentRecord, searchSessions, postSearchSessionRequest } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { AppState } from '../../store';
import { SessionState, ColumnSortConfiguration, Session } from '../types';
import { TextCell } from '../../../components/cell/TextCell';
import { DateCell } from '../../../components/cell/DateCell';
import { NumberCell } from '../../../components/cell/NumberCell';
import { Grid, Page, Content } from '@jfront/ui-core';

const SessionListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { records, current, searchId, searchRequest, resultSetSize } = useSelector<AppState, SessionState>(state => state.session);

  useEffect(() => {
    if (searchId && searchRequest) {
      dispatch(searchSessions(searchId, 25, 1));
    } else if (!searchId && searchRequest) {
      dispatch(postSearchSessionRequest(searchRequest));
    } else {
      dispatch(postSearchSessionRequest({ template: { maxRowCount: 25 } }));
    }
  }, [searchId, searchRequest, dispatch]);
  const columnConfig = useRef(new Map<string, string>());


  const mapColumnConfig = (): Array<ColumnSortConfiguration> => {
    if (columnConfig.current) {
      return (Array.from(columnConfig.current) as Array<Array<string>>).map(entry => ({ columnName: entry[0], sortOrder: entry[1] }));
    } else {
      return [];
    }
  }

  const onColumnConfigChange = () => {
    console.log(columnConfig)
    if (searchRequest) {
      searchRequest.listSortConfiguration = mapColumnConfig();
      dispatch(postSearchSessionRequest(searchRequest));
    } else {
      dispatch(postSearchSessionRequest({ template: { maxRowCount: 25 }, listSortConfiguration: mapColumnConfig() }));
    }
  }

  const onSingleColumnSort = (colName: string) => {
    if (columnConfig.current?.get(colName)) {
      if (columnConfig.current?.get(colName) === "asc") {
        columnConfig.current.clear();
        columnConfig.current.set(colName, "desc");
      } else {
        columnConfig.current.clear();
        columnConfig.current.set(colName, "asc");
      }
    } else {
      columnConfig.current?.clear();
      columnConfig.current?.set(colName, "desc");
    }
    onColumnConfigChange();
  }

  const onMultiColumnSort = (colName: string) => {
    if (columnConfig.current?.get(colName)) {
      if (columnConfig.current?.get(colName) === "asc") {
        columnConfig.current.set(colName, "desc");
      } else {
        columnConfig.current.set(colName, "asc");
      }
    } else {
      columnConfig.current?.set(colName, "desc");
    }
    onColumnConfigChange();
  }
  const onColumnHeaderClick = (e: React.MouseEvent, colName: string) => {
    if (e.ctrlKey) {
      onMultiColumnSort(colName);
    } else {
      onSingleColumnSort(colName);
    }
  }
  
  return (
    <Page>
      <Content>
        <Grid<Session>
          columns={[
            {
              Header: "ID сессии",
              accessor: "sessionId",
              Cell: (value: any) => <TextCell>{value}</TextCell>
            },
            {
              Header: "Дата создания",
              accessor: "dateIns",
              Cell: (value: any) => <TextCell>{value}</TextCell>
            },
            {
              Header: "Логин оператора",
              accessor: "operatorLogin",
              Cell: (value: any) => <TextCell>{value}</TextCell>
            },
            {
              Header: "Имя оператора",
              id: "operatorName",
              accessor: (row: Session) => row.operator?.name,
              Cell: (value: any) => <TextCell>{value}</TextCell>
            },
            {
              Header: "ID оператора",
              id: "operatorId",
              accessor: (row: Session) => row.operator?.value,
              Cell: (value: any) => <TextCell>{value}</TextCell>
            },
            {
              Header: "URL переадресации",
              accessor: "redirectUri",
              Cell: (value: any) => <TextCell>{value}</TextCell>
            },
            {
              Header: "Наименование приложения",
              id: "clientName",
              accessor: (row: Session) => row.operator?.value,
              Cell: (value: any) => <TextCell>{value}</TextCell>
            },
            {
              Header: "ID приложения",
              id: "clientId",
              accessor: (row: Session) => row.operator?.value,
              Cell: (value: any) => <TextCell>{value}</TextCell>
            },
          ]}
          data={records}
          onSelection={(selected) => {
            if (selected && selected.length === 1) {
              dispatch(setCurrentRecord(selected[0]))
            } else {
              dispatch(setCurrentRecord(undefined))
            }
          }}
          onDoubleClick={(record) => current !== record ? dispatch(setCurrentRecord(record,
            () => history.push(`/ui/session/${record?.sessionId}/view`))) : history.push(`/ui/session/${record?.sessionId}/view`)}/>
      </Content>
    </Page>
  );
}

export default SessionListPage;