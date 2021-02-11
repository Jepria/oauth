import styled from "styled-components";

export interface TextCellProps {
  wrapText?: boolean;
}

export const TextCell = styled.div<TextCellProps>`
  display: inline-block;
  text-align: left;
  ${props => props.wrapText ? 'white-space: normal;' : `
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  `}
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    vertical-align: bottom;
  }
`;