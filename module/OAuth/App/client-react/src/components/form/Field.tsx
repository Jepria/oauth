import styled from 'styled-components';


const Field = styled.div`
  padding: 0px;
  margin: 0px;
  margin-bottom: 5px;
  display: flex;
`

type LabelProps = {
  width?: string;
}

const Label = styled.div`
  text-align: right;
  max-width: ${(props: LabelProps) => props.width ? props.width : '200px'};
  width: 100%;
  min-height: 20px;
  padding: 2px;
  font-family: tahoma, arial, helvetica, sans-serif;
  font-size: 12px;
`;

type TextProps = {
  width?: string;
}


const Text = styled.div`
  display: inline;
  min-height: 20px;
  max-width: ${(props: TextProps) => props.width ? props.width : '200px'};
  width: 100%;
  padding: 2px;
  font-family: tahoma, arial, helvetica, sans-serif;
  font-size: 12px;
  word-wrap: break-word;
`;

export {Field as FormField, Label, Text}