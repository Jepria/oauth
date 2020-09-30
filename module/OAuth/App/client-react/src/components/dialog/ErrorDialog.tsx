import React, { useState } from 'react';
import styled from 'styled-components';
import error from './images/error.gif'

const GlassMask = styled.div`
  position: absolute;
  height: 100vh;
  width: 100vw;
  z-index: 1100;
  opacity: 0.2;
  background-color: black;
  font-family: tahoma, arial, helvetica, sans-serif;
`;

const Dialog = styled.section`
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

const Header = styled.header`
  background: linear-gradient(rgb(255, 255, 255), rgb(208, 222, 240));
  padding: 5px 10px;
  color: rgb(21, 66, 139);
`;

const Body = styled.div`
  display: flex;
`;

const Controls = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
`;

const Button = styled.button.attrs({type: 'button'})`
  width: 30%;
  background: linear-gradient(rgb(255, 255, 255), rgb(208, 222, 240));
  margin: 5px;
  padding: 3px 5px;
  border: 1px outset #ccc;
`;

const FieldSet = styled.fieldset`
  border: 1px solid #99BBE8;
  margin: 5px;
  padding: .625em;
`;

const Legend = styled.legend`
  color: #15428B;
  font-size: 11px;
  font-weight: bold;
`;

const TextArea = styled.textarea`
  font-size: 11px;
  resize: none;
  box-sizing: border-box;
  height: 100%;
  width: 100%;
  overflow: auto;
`;

export interface ErrorDialogProps {
  errorId?: string
  errorCode?: number
  errorDescription?: string
  errorMessage?: string
  onClose?: () => void
}

export const ErrorDialog = ({ errorId, errorCode, errorDescription, errorMessage, onClose }: ErrorDialogProps) => {

  const [showDetails, setShowDetails] = useState(false);

  return (
    <>
      <GlassMask />
      <Container>
        <Dialog>
          <div>
            <Header>
              <h5>Ошибка</h5>
            </Header>
            <Body>
              <div style={{ padding: "5px" }}>
                <img style={{ display: 'inline-block' }} src={error} alt="Error" />
              </div>
              <div>
                {errorId && <div>
                  ID ошибки: <span style={{ fontSize: "11px" }}>{errorId}</span>
                </div>}
                {errorCode && <div>
                  Код ошибки: <span style={{ fontSize: "11px" }}>{errorCode}</span>
                </div>}
                {errorDescription && <div>{errorDescription}</div>}
              </div>
            </Body>
          </div>
          <Controls>
            <Button onClick={onClose}>OK</Button>
            {errorMessage && <Button onClick={() => setShowDetails(!showDetails)}>Показать детали</Button>}
          </Controls>
          {errorMessage && showDetails && <FieldSet>
            <Legend>Детали</Legend>
            {errorMessage && <TextArea>
              {errorMessage}
            </TextArea>}
          </FieldSet>}
        </Dialog>
      </Container>
    </>
  )
}
