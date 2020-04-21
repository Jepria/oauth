import React from 'react';
import styled from 'styled-components'
import loading from './loading.gif'

export const GlassMask = styled.div`
  background-color: #000;
  opacity: 0.2;
  position: absolute;
  top: 0px;
  left: 0px;
  height: 100%;
  width: 100%;
  z-index: 7100;
  text-align:
`;

export const MaskPanel = styled.div`
  position: absolute; 
  top: 50%;
  left: 50%;
  -webkit-transform: translateX(-50%) translateY(-50%);
  -moz-transform: translateX(-50%) translateY(-50%);
  -ms-transform: translateX(-50%) translateY(-50%);
  transform: translateX(-50%) translateY(-50%);
  border: 1px solid #ccc;
  padding: 8px;
  background: white;
  color: #444;
  z-index: 7200;
`;

const Image = styled.img`
  margin-right: 8px;
  float: left;
  height:100%;
`;

const Text = styled.p`
  font: normal 10px arial, tahoma, sans-serif;
  font-family: arial, helvetica, tahoma, sans-serif;
  margin: 0px;
  white-space: nowrap;
`;
const Header = styled.p`
  font: bold 13px tahoma, arial, helvetica;
  font-family: arial, helvetica, tahoma, sans-serif;
  margin: 0px;
  white-space: nowrap;
`;

const Block = styled.div`
  display: inline-block;
`;

export interface LoadingPanelProps {
  header?: string
  text?: string
}

export const LoadingPanel: React.FC<LoadingPanelProps> = ({ header, text }) => {
  return (
    <React.Fragment>
      <GlassMask />
      <MaskPanel>
        <Image src={loading} />
        <Block>
          <Header>{header}</Header>
          <Text>{text}</Text>
        </Block>
      </MaskPanel>
    </React.Fragment>
  );
} 