import React, { useState, useEffect } from 'react';
import styled from 'styled-components';
import bg from './images/bg.gif';
import { PagingToolBar } from '../PagingToolBar';

export const PagingBar = styled.div`
  font: 11px arial, tahoma, helvetica, sans-serif;
  margin: 0;
  padding: 2px 2px 2px 2px;
  border-style: solid;
  border-color: #99BBE8;
  border-width: 1px;
  background-color: #D0DEF0;
  background-image: url(${bg});
`;

const Container = styled(PagingBar)`
  display: table;
  width: 100%;
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    text-align: center;
    background-image: none;
    background-color: #D7E4F3;
  }
`;

const Left = styled.div`
  display: table-cell;
  width: 33.33%;
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    display: table-row;
    text-align: center;
    width: 100%;
    margin: 2px 0;
  }
`;

const Center = styled.div`
  display: table-cell;
  vertical-align: middle;
  text-align: center;
  width: 33.33%;
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    display: table-row;
    text-align: center;
    width: 100%;
    margin: 2px 0;
  }
`;

const Right = styled.div`
  display: table-cell;
  vertical-align: middle;
  text-align: right;
  width: 33.33%;
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    display: table-row;
    text-align: center;
    width: 100%;
    margin: 2px 0;
  }
`;

export interface GridPagingBarProps {
  currentPage?: number;
  maxRowCount?: number;
  visibleRowCount?: number;
  children?: never;
  onChange?(pageNumber: number, pageSize: number): void;
}

export const GridPagingBar: React.FC<GridPagingBarProps> = ({ currentPage = 1, maxRowCount, visibleRowCount = 25, onChange }) => {

  const [_visibleRowCount, setVisibleRowCount] = useState<number>(visibleRowCount);
  const [_currentPage, setCurrentPage] = useState<number>(currentPage);
  const visibleRowCountInputRef = React.createRef<HTMLInputElement>();
  const pageCount = maxRowCount ? Math.ceil(maxRowCount / _visibleRowCount) : 1;
  const [_maxRowCount, setMaxRowCount] = useState(maxRowCount);

  useEffect(() => {
    console.log(maxRowCount)
    setMaxRowCount(maxRowCount);
  }, [maxRowCount]);

  const onChangeValues = (pageNumber?: number, pageSize?: number) => {
    if (pageNumber && pageSize && pageSize >= 1 && onChange) {
      onChange(pageNumber, pageSize);
    }
  }

  const onKeyUp = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      let value = visibleRowCountInputRef.current?.value;
      let intValue: number;
      if (value) {
        intValue = parseInt(value);
        if (intValue < 0) {
          intValue = visibleRowCount;
        }
      } else {
        intValue = visibleRowCount;
      }
      if (visibleRowCountInputRef.current) {
        visibleRowCountInputRef.current.value = `${intValue}`;
      }
      setVisibleRowCount(intValue);
      onChangeValues(_currentPage, intValue);
    }
  }

  const onBlur = (e: React.FocusEvent<HTMLInputElement>) => {
    if (e.target.value && parseInt(e.target.value) > 0) {
      setVisibleRowCount(parseInt(e.target.value))
    }
  }

  return (
    <Container>
      <Left>
        <PagingToolBar startPageNumber={currentPage} pageCount={pageCount} onChange={page => {
          setCurrentPage(page);
          onChangeValues(page, _visibleRowCount);
        }} />
      </Left>
      <Center>
        {_maxRowCount ? `Записи ${_visibleRowCount * _currentPage - _visibleRowCount + 1} - ${_visibleRowCount * _currentPage} из ${_maxRowCount}` :
          'Записей не найдено'}
      </Center>
      <Right>
        <label>Записей на странице: <input
          ref={visibleRowCountInputRef}
          type='number'
          width='60px'
          min={1} max={_maxRowCount}
          defaultValue={_visibleRowCount}
          onBlur={onBlur}
          onKeyUp={onKeyUp} />
        </label>
      </Right>
    </Container>
  );
}