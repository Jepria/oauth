import React from 'react';
import styled from 'styled-components';
import bg from './images/bg.gif'

const ToolBar = styled.div`
  font: 11px arial,tahoma,helvetica,sans-serif;
  vertical-align: middle;
  horizontal-align: left;
  margin: 0;
  padding: 2px;
  min-height: 20px;
  border-style: solid;
  border-color: #99BBE8;
  border-width: 0 1px 1px 1px;
  overflow: hidden;
  background-color: #D0DEF0;
  background-image: url(${bg});
  background-position: 0 5%;
`;

const ToolBarItem = styled.div`
  float: left;
  vertical-align: top;
  height: 100%;
`;

const Button = styled.button`
  font: 11px arial,tahoma,verdana,helvetica;
  height: 100%;
  padding: 1px 1px;
  background-color: transparent;
  background-image: none;
  border: solid 1px transparent;
  &:hover:enabled {
    border: solid 1px #99BBE8;
    background: #DDEFFF;
  }
  &:active:enabled {
    border-top: solid 1px #99BBE8;
    border-bottom: solid 1px white;
    border-left: solid 1px #99BBE8;
    border-right: solid 1px white;
    background: #B6CBE4;
  }
  &:disabled {
    opacity:0.5;
    cursor: default;
  }
`


type ToolBarButtonComponentProps = {
  onClick(): any;
  tooltip: string;
  disabled: boolean;
}

const ToolBarButtonComponent: React.FC<ToolBarButtonComponentProps> = ({onClick, tooltip, disabled, children}) => {
  return (
    <ToolBarItem>
      <Button onClick={onClick} title={tooltip} disabled={disabled}>{children}</Button>
    </ToolBarItem>
  );
}

const ToolBarComponent: React.FC = ({children}) => {
  return (
    <ToolBar>
      {children}
    </ToolBar>
  );
}

export {ToolBarComponent as ToolBar, ToolBarButtonComponent as ToolBarButton, ToolBarItem}