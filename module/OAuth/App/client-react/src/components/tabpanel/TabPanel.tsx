import styled from 'styled-components';
import bg from './images/bg.png'

const TabPanel = styled.div`
  height:22px;
  border-bottom: 1px solid #99BBE8;
  font-family: tahoma, arial, helvetica, sans-serif;
  color: rgb(21, 66, 139);
  font-size: 11px;
  border-bottom: 1px solid #99BBE8;
`;

const Tab = styled.div`
  display: inline-block;
  margin-left: 2px;
  padding: 3px 6px 3px 6px;
  text-align: center;
  border: 1px solid #8DB2E3;
  background-image: url(${bg});
  background-color: white;
  background-repeat: repeat;
  background-position: 0 100%;
  cursor: pointer;
  &:hover{
    opacity: 0.8
  }
`;

const SelectedTab = styled.div`
  display: inline-block;
  height:22px;
  background-color: #D7E4F3;
  text-align:center;
  margin-left: 2px;
  padding: 3px 6px 3px 6px;
  min-width: 20px;
  border: 1px solid #8DB2E3;
  border-bottom-color: #D7E4F3;
  background-color: #D7E4F3;
  font-weight: bold;
  cursor: default;
`;

export {TabPanel, Tab, SelectedTab};