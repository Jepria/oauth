import React from 'react'
import styled from 'styled-components'


const Header = styled.header`
  background: linear-gradient(rgb(255, 255, 255), rgb(208, 222, 240));
  padding: 5px 10px;
  color: rgb(21, 66, 139);
`;

const GlassMask = styled.div`
position: absolute;
height: 100vh;
width: 100vw;
z-index: 1100;
opacity: 0.2;
background-color: black;
font-family: tahoma, arial, helvetica, sans-serif;
`;

const StyledDialog = styled.section`
z-index: 5100;
width: 400px;
background-color: white;
border: 1px solid #99BBE8;
`;

const Container = styled.div`
position: absolute;
width: 100vw;
height: 100vh;
display: flex;
justify-content: center;
align-items: center;
`

export interface DialogProps extends React.HTMLAttributes<HTMLDivElement> {
  header?: string
}

export const Dialog = React.forwardRef<HTMLDivElement, DialogProps>((props, ref) => {
  return (
    <>
      <GlassMask />
      <Container>
        <StyledDialog {...props} ref={ref}>
          {props.header && <Header>
            <h5>{props.header}</h5>
          </Header>}
          {props.children}
        </StyledDialog>
      </Container>
    </>
  )
})