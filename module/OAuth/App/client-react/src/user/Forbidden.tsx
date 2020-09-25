import React from 'react';
import styled from 'styled-components';

const Background = styled.div`
  width: 100vw;
  height: 100vh;
  background: #f0f0f0;
  display: flex;
  justify-content: center;
  align-items: center;
`;

const Dialog = styled.div`
  padding: 15px;
  background: #fff;
  border-radius: 3px;
  border: 1px solid #ccc;
  box-shadow: 0 1px 2px rgba(0, 0, 0, .1);
  justify-content: center;
  align-items: center;
`

export interface ForbiddenProps {
  text?: string
}

export const Forbidden = ({ text }: ForbiddenProps) => {
  return (
    <Background>
      <Dialog>
        {text ? text : "Недостаточно прав доступа"}
      </Dialog>
    </Background>
  )
}