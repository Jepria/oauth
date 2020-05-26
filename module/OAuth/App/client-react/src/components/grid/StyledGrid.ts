import { Grid as NonStyledGrid } from 'jfront-components';
import styled from 'styled-components';
import bg from './images/bg.gif';

export const Grid = styled(NonStyledGrid)`

`;
export const GridTable = styled(NonStyledGrid.Table)`
  
`;
export const GridHeader = styled(NonStyledGrid.Header)`
backround-colore:red;

`;
export const GridHeaderCell = styled(NonStyledGrid.HeaderCell)`
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
`;
export const GridBody = styled(NonStyledGrid.Body)`

`;
export const GridRow = styled(NonStyledGrid.Row)`
  padding: .35em;
  cursor: pointer;
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
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
export const GridRowCell = styled(NonStyledGrid.Cell)`
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
    font-size: .8em;
    &::before {    
      padding-right: 5px;
    }
  }
`;
export const GridPagingBar = styled(NonStyledGrid.PagingBar)`
  font: 11px arial, tahoma, helvetica, sans-serif;
  margin: 0;
  padding: 2px 2px 2px 2px;
  border-style: solid;
  border-color: #99BBE8;
  border-width: 1px;
  background-color: #D0DEF0;
  background-image: url(${bg});
`;