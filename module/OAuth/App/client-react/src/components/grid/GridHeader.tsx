import React from 'react';
import { TableHeader, TableRow, TableHeaderCell } from './';
import { isFunction } from '../../utils';

export const GridHeader: React.FC = ({children}) => {
  return (
    <TableHeader>
      <TableRow>
        {isFunction(children) ? children() : children}
      </TableRow>
    </TableHeader>
  );
}

export const GridHeaderCell: React.FC = ({children}) => {
  return (
    <TableHeaderCell>
      {isFunction(children) ? children() : children}
    </TableHeaderCell>
  );
}