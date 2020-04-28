import React from 'react';
import styled from 'styled-components';
import { isFunction } from '../../utils';

const Page = styled.div`
  height: 100%;
  width: 100%;
  margin: 0px;
  padding: 0px;
  display: table;
`;

const ContentContainer = styled.div`
  height: 100%;
  display: table-row;
`;

const ContentOverflow = styled.div`
  height: inherit;
  overflow:auto;
`;

const ContentComponents: React.FC<React.HTMLAttributes<HTMLDivElement>> = (props) => {
  return (
    <ContentContainer {...props}>
      <ContentOverflow>
        {isFunction(props.children) ? props.children() : props.children}
      </ContentOverflow>
    </ContentContainer>
  );
}

const Header = styled.div`
  display: table-header;
`;

const Footer = styled.div`
  display: table-footer;
`;

const VerticalLayout = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
`;

const HorizontalLayout = styled.div`
  width: 100%;
  display: flex;
`;

export {Page, ContentComponents as Content, Header, Footer, VerticalLayout, HorizontalLayout}