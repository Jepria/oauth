import React, { useState, useLayoutEffect, useRef, useEffect } from 'react';
import styled from 'styled-components';
import { Page, Content, Footer } from '../Layout';
import { isFunction } from '../../utils';
import { GridHeader, GridHeaderCell } from './GridHeader';
import { GridPagingBar, GridPagingBarProps } from './GridPagingBar';
import { throttle } from 'lodash';

export const Table = styled.table`
  box-sizing: border-box;
  position:relative;
  border-collapse: collapse;
  margin: 0;
  padding: 0;
  table-layout: fixed;
  width: 100%;
  height: 100%;
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    border: 0;
  }
}
`;

export const TableHeader = styled.thead`
  display: block;
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

interface TableBodyProps {
  height?: string;
}

export const TableBody = styled.tbody<TableBodyProps>`
  width: 100%;
  display: block;
  overflow-y: auto;
  ${props => props.height ? `position: absolute; height: ${props.height};` : ''}
`;

interface TableRowProps extends React.HTMLAttributes<HTMLTableRowElement> {
  selected?: boolean
}

export const TableRow = styled.tr<TableRowProps>`
  padding: .35em;
  display: table;
  width: 100%;
  table-layout: fixed;
  cursor: pointer;
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

const ContentContainer = styled.div`
  height: 100%;
  display: table-row;
`;

const ContentOverflow = styled.div`
  height: inherit;
  overflow-y: hidden;
  overflow-x: auto;
`;

Grid.Table = ({ children }) => {
  return (
    <ContentContainer>
      <ContentOverflow>
        <Table>
          {isFunction(children) ? children() : children}
        </Table>
      </ContentOverflow>
    </ContentContainer>
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

const GridBody: React.FC = ({ children }) => {

  const ref = useRef<HTMLTableSectionElement>(null);
  const [height, setHeight] = useState<number | undefined>(undefined);

  const resize = () => {
    let tableSize = ref.current?.parentElement?.offsetHeight;
    let thead = ref.current?.parentElement?.getElementsByTagName('thead')[0];
    setHeight(thead?.offsetHeight && tableSize ? tableSize - thead.offsetHeight : tableSize);
  }

  useLayoutEffect(() => {
    const handleResize = throttle(() => {
      resize();
    }, 50);
    resize();
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  return (
    <TableBody ref={ref} height={height ? `${height}px` : undefined}>
      {isFunction(children) ? children() : children}
    </TableBody>
  );
}

Grid.Body = ({ children }) => {
  return (
    <GridBody>
      {isFunction(children) ? children() : children}
    </GridBody>
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
    <GridPagingBar {...props} />
  );
}
