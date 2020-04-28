import React, { useState } from 'react';
import styled from 'styled-components';
import { Page, Content, Footer } from '../Layout';
import { isFunction } from '../../utils';
import { GridHeader, GridHeaderCell } from './GridHeader';
import { GridPagingBar, GridPagingBarProps } from './GridPagingBar';

export const TableContainer = styled.div`
  width: 100%;
  overflow: auto;
`;

export const Table = styled.table`
  box-sizing: border-box;
  border-collapse: collapse;
  margin: 0;
  padding: 0;
  table-layout: fixed;
  width: 100%;
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    border: 0;
  }
}
`;

export const TableHeader = styled.thead`
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    display: none; 
  }
`;

export const TableHeaderCell = styled.th`
  border-bottom: 1px solid #ddd;
  border-right: 1px solid #ddd;
  padding-bottom: 3px;
  padding-left: 5px;
  padding-right: 3px;
  padding-top: 3px;
  text-align: left;
  color: black;
  text-shadow: none;
  font: 11px tahoma, arial, verdana, sans-serif;
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
  background-color: #ededed;
  padding: .625em;
  text-align: center;
`

export const TableBody = styled.tbody`
`;

interface TableRowProps extends React.HTMLAttributes<HTMLTableRowElement> {
  selected?: boolean
}

export const TableRow = styled.tr<TableRowProps>`
  padding: .35em;
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    display: block;
    margin-bottom: .625em;
    border-bottom: 1px solid #ddd;
  }
  ${props => props.selected ? `background: #dfe8f6;` : `
    background: #fff;
    &:nth-child(odd) {
      background: #fafafa;
    }
    &:hover {
      background: #eee;
    }
  `}
`;

export interface TableColumnProps extends React.HTMLAttributes<HTMLTableCellElement> {
  label?: string
}

export const TableColumn = styled.td<TableColumnProps>`
  padding: .625em;
  @media only screen and (min-width: 761px) {
    padding-bottom: 3px;
    padding-left: 5px;
    padding-right: 3px;
    padding-top: 3px;
    overflow: hidden;
    font: 11px tahoma, arial, verdana, sans-serif;
    text-overflow: ellipsis;
    white-space: nowrap;
    border-collapse: collapse;
    border-bottom-color: #ededed;
    border-bottom-style: solid;
    border-bottom-width: 1px;
    border-left-color: #ededed;
    border-left-style: solid;
    border-left-width: 0;
    border-right-color: #ededed;
    border-right-style: solid;
    border-right-width: 0;
    border-top-color: #fafafa;
    border-top-style: solid;
    border-top-width: 1px;
  }
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    display: block;
    font-size: .8em;
    &::before {    
      display: inline-block;
      content: "${props => props.label ? `${props.label}:` : ''}";
      padding-right: 5px;
    }
  }
`

export interface TextCellProps {
  wrapText?: boolean;
}

export const TextCell = styled.div<TextCellProps>`
  display: inline-block;
  text-align: left;
  ${props => props.wrapText ? 'white-space: normal;' : `
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  `}
  @media only screen and (min-width: 761px) {
    width: 100%;
  }
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    vertical-align: bottom;
  }
`;

export const NumberCell = styled.div`
  display: inline-block;
  text-align: right;
  @media only screen and (min-width: 761px) {
    width: 100%;
  }
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    vertical-align: bottom;
  }
`;

export const DateCell = styled.div`
  display: inline-block;
  text-align: center;
  @media only screen and (min-width: 761px) {
    width: 100%;
  }
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    vertical-align: bottom;
  }
`;

export type Grid = React.FC & {
  Table: React.FC;
  Header: React.FC;
  HeaderCell: React.FC;
  Body: React.FC;
  Row: React.FC<TableRowProps>;
  Column: React.FC<TableColumnProps>;
  PagingBar: React.FC<GridPagingBarProps>;
}

export const Grid: Grid = ({ children }) => {
  return (
    <Page>
      {isFunction(children) ? children() : children}
    </Page>
  );
}

Grid.Table = ({ children }) => {
  return (
    <Content>
      <Table>
        {isFunction(children) ? children() : children}
      </Table>
    </Content>
  );
}

Grid.Header = ({ children }) => {
  return (
    <GridHeader>
      {isFunction(children) ? children() : children}
    </GridHeader>
  );
}

Grid.HeaderCell = ({ children }) => {
  return (
    <GridHeaderCell>
      {isFunction(children) ? children() : children}
    </GridHeaderCell>
  );
}

Grid.Body = ({ children }) => {
  return (
    <TableBody>
      {isFunction(children) ? children() : children}
    </TableBody>
  );
}


Grid.Row = (props) => {
  return (
    <TableRow {...props}>
      {isFunction(props.children) ? props.children() : props.children}
    </TableRow>
  );
}

Grid.Column = (props) => {
  return (
    <TableColumn {...props}>
      {isFunction(props.children) ? props.children() : props.children}
    </TableColumn>
  );
}

Grid.PagingBar = (props: GridPagingBarProps) => {
  return (
    <Footer>
      <GridPagingBar {...props} />
    </Footer>
  );
}
