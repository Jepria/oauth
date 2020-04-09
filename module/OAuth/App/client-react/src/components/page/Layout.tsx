import styled from 'styled-components';

const Page = styled.div`
  height: 100%;
  width: 100%;
  margin: 0px;
  padding: 0px;
`;

const Content = styled.div`
  width: 100%;
  height: 100%;
  padding: 5px;
  overflow: auto;
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

export {Page, Content, VerticalLayout, HorizontalLayout}