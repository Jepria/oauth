import React, { useState } from 'react';
import styled from 'styled-components';
import first from './images/first.gif';
import last from './images/last.gif';
import next from './images/next.gif';
import prev from './images/prev.gif';
import refresh from './images/refresh.gif';
import split from './images/split.gif';

const Item = styled.button`
  font: 11px arial,tahoma,verdana,helvetica;
  height: 100%;
  padding: 1px 1px;
  background-color: transparent;
  background-image: none;
  border: solid 1px transparent;
  &:hover {
    border: solid 1px #99BBE8;
    background: #DDEFFF;
  }
`;


const Splitter = styled.span`
  display: inline-block;
  vertical-align: top;
  background-position: center;
  background-repeat: repeat;
  height: 22px;
  width: 2px;
  margin-left: 2px;
  margin-right: 2px;
  background-image: url(${split});
`;

const NumberInput = styled.input.attrs({ type: 'number' })`
  width: 60px;
  margin: 0 5px;
`;

interface PagingToolBarProps {
  startPageNumber?: number;
  pageCount: number;
  onChange?: (currentPageNumber: number) => void;
}

export const PagingToolBar: React.FC<PagingToolBarProps> = ({ startPageNumber = 1, pageCount, onChange }) => {

  const [currentPage, setCurrentPage] = useState<number | undefined>(startPageNumber);

  const changeValue = (page?: number) => {
    if (page && page >= 1 && page <= pageCount && onChange) {
      setCurrentPage(page);
      onChange(page);
    }
  }

  const onInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = parseInt(e.target.value);
    setCurrentPage(value ? value : undefined);
  }

  const onKeyPressed = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      if (currentPage && currentPage >= 1 && currentPage <= pageCount && onChange) {
        onChange(currentPage);
      }
    }
  }

  return (
    <div style={{ whiteSpace: 'nowrap' }}>
      <Item onClick={() => {
        if (currentPage !== 1) changeValue(1);
      }}>
        <img src={first} title="Первая" alt="Первая" />
      </Item>
      <Item onClick={() => currentPage && changeValue(currentPage - 1)}>
        <img src={prev} title="Предыдушая" alt="Предыдушая" />
      </Item>
      <Splitter />
      <label style={{ display: 'inline-block', height: '22px', verticalAlign: 'top' }}>
        Стр. <NumberInput value={currentPage} onChange={onInputChange} onKeyUp={onKeyPressed} max={pageCount} min={1} /> из {pageCount}
      </label>
      <Splitter />
      <Item onClick={() => currentPage && changeValue(currentPage + 1)}>
        <img src={next} title="Следующая" alt="Следующая" />
      </Item>
      <Item onClick={() => {
        if (currentPage !== pageCount) changeValue(pageCount);
      }}>
        <img src={last} title="Последняя" alt="Последняя" />
      </Item>
      <Splitter />
      <Item onClick={() => currentPage && onChange && onChange(currentPage)}>
        <img src={refresh} title="Обновить" alt="Обновить" />
      </Item>
    </div>
  );
}