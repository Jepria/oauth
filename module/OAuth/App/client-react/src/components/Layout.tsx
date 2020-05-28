import React, { createContext, RefObject, useContext, useState, useRef, useLayoutEffect, HTMLAttributes } from 'react';
import styled from 'styled-components';

const Form = styled.div`
  padding: 5px;
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

const PageContainer = styled.div`
  box-sizing: border-box;
  height: 100%;
  width: auto;
`;

const HeaderPanel = styled.header`
  box-sizing: border-box;
  height: auto;
  width: auto;
`;

interface ContentPanelProps {
  height?: string;
}

const ContentPanel = styled.section<ContentPanelProps>`
  box-sizing: border-box;
  height: ${props => props.height ? props.height : '100%'};
  width: auto;
`;

const FooterPanel = styled.footer`
  box-sizing: border-box;
  height: auto;
  width: auto;
`;

type PageContextValues = {
  headerRef: RefObject<HTMLElement>,
  footerRef: RefObject<HTMLElement>
}

const PageContext = createContext<PageContextValues>(
  {
    headerRef: React.createRef<HTMLElement>(),
    footerRef: React.createRef<HTMLElement>()
  }
);

const Page = styled.div`
  width: 100%;
  height: 100%;
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-orient: vertical;
  -webkit-box-direction: normal;
      -ms-flex-direction: column;
          flex-direction: column;
`;

const Header = styled.header`
  box-sizing: border-box;
  height: auto;
  width: 100%;
`;

const Content = styled.section`
  width: 100%;
  overflow: auto;
  -webkit-box-flex: 1;
      -ms-flex: 1;
          flex: 1;
  -ms-flex-positive: 1;
      flex-grow: 1;
`;

const Footer = styled.footer`
  box-sizing: border-box;
  height: auto;
  width: 100%;
`;

export { Page, Content, Form, Header, Footer, PageContext, VerticalLayout, HorizontalLayout }