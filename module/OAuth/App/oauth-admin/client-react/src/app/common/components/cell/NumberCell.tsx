import styled from "styled-components";

export const NumberCell = styled.div`
  display: inline-block;
  text-align: right;
  @media only screen and (min-width: 761px) {
    width: 100%;
  }
  @media only screen and (max-width: 760px), (min-device-width: 768px) and (max-device-width: 1024px) {
    vertical-align: bottom;
  }
`;
