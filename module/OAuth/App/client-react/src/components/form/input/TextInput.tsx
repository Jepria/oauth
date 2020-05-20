import React from 'react';
import styled from 'styled-components';
import exclamation from './images/exclamation.gif'

interface TextInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  width?: string;
  height?: string;
  touched?: boolean;
  error?: string;
}

export const Input = styled.input`
  padding: 0px;
  margin: 0px;
  font-family: tahoma, arial, helvetica, sans-serif;
  font-size: 12px;
  height: ${(props: TextInputProps) => props.height ? props.height : '20px'};
  width: 100%;
  max-width: ${(props: TextInputProps) => props.width ? props.width : '200px'};
  ${(props: TextInputProps) => props.touched && props.error ? 'border: 1px solid red' : 'border: 1px solid #ccc; border-top: 1px solid #999;'}
`;

const Container = styled.div<TextInputProps>`
  width: 100%;
  max-width: ${(props: TextInputProps) => props.width ? `calc(${props.width} + 20px)` : '220px'};
  white-space: nowrap;
`;

const Icon = styled.img`
  margin-left: 4px;
`;

export const TextInput: React.FC<TextInputProps> = (props) => {
  return (
    <React.Fragment>
      <Container {...props}>
        <Input {...props} type='text' />
        {props.touched && props.error && <Icon src={exclamation} title={props.error} />}
      </Container>
    </React.Fragment>
  );
}