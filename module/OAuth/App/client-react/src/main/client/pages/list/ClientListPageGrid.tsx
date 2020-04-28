import React from 'react';
import styled from 'styled-components';

const Collection = styled.ol`
  @media screen and (max-width: 736px) {
    @supports(display: grid) {
      display: grid;
      grid-template-columns: 1fr 1fr;
      grid-gap: 20px;
    }
    & > li:first-child {
      display: none;
    }
  }
  @media screen and (max-width:580px) {
    @supports(display: grid) {
      display: grid;
      grid-template-columns: 1fr;
    }
  }
  & > li {
    list-style-type: none;
  }
`;

const Row = styled.li`
  @media screen and (min-width: 737px) {
    @supports(display: grid) {
      display: grid;
      grid-template-columns: 2em 10fr 2fr 2fr 2fr 2fr 5em 5em;
    }
  }
`;

interface ColumnProps {
  width?: number
}

const Column = styled.div<ColumnProps>`
  @media screen and (min-width: 737px) {
    @supports(display: grid) {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(${props => props.width ? `${props.width}em` : '10em'}, 1fr));
    }
  }
`;


interface CellProps {
  name?: string
}

const Cell = styled.div<CellProps>`
  @media screen and (min-width: 737px) {
    @supports(display: grid) {
      display: grid;
      grid-template-columns: minmax(9em, 30%) 1fr;
    }
    border: 1px solid black;
  }
  @media screen and (max-width: 736px) {
    &::before {
      content: "${props => props.name ? `${props.name}: ` : ''}";
    }
  }
`;

export const ClientListPage: React.FC = () => {
  return (
    <section>
      <Collection>
        <Row>
          <Column width={2}>#</Column>
          <Column width={10}>
            <Column width={10}>
              <Cell>Part Number</Cell>
              <Cell>Part Description</Cell>
            </Column>
            <Column width={8}>
              <Cell>Vendor Number</Cell>
              <Cell>Vendor Name</Cell>
            </Column>
          </Column>
          <Column width={8}>
            <Cell>Order Qty</Cell>
            <Cell>Receive</Cell>
          </Column>
          <Column width={8}>
            <Cell>Cost</Cell>
            <Cell>Extended Cost</Cell>
          </Column>
          <Column width={5}>
            <Cell>Duty %</Cell>
            <Cell>Duty</Cell>
          </Column>
          <Column width={8}>
            <Cell>Freight %</Cell>
            <Cell>Freight</Cell>
          </Column>
          <Column width={5}>
            <Cell>UOM</Cell>
          </Column>
          <Column width={8}>
            <Cell>Vendor Part Number</Cell>
          </Column>
        </Row>
        <Row>
          <Column width={2}>1</Column>
            <Column width={10}>
            <Column width={10}>
              <Cell name="Part Number">100-10001</Cell>
              <Cell name="Part Description">Description of part</Cell>
            </Column>
            <Column width={8}>
              <Cell name="Vendor Number">001</Cell>
              <Cell name="Vendor Name">Vendor Name A</Cell>
            </Column>
          </Column>
          <Column width={8}>
            <Cell name="Order Qty">10</Cell>
            <Cell name="Receive">20</Cell>
          </Column>
          <Column width={8}>
            <Cell name="Cost">5.00</Cell>
            <Cell name="Extended Cost">2.00</Cell>
          </Column>
          <Column width={5}>
            <Cell name="Duty %">3.0%</Cell>
            <Cell name="Duty">0.15</Cell>
          </Column>
          <Column width={8}>
            <Cell name="Freight %">3.0%</Cell>
            <Cell name="Freight">0.15</Cell>
          </Column>
          <Column width={5}>
            <Cell name="UOM">EA</Cell>
          </Column>
          <Column width={8}>
            <Cell name="Vendor Part Number">10001</Cell>
          </Column>
        </Row>
        <Row>
          <Column width={2}>2</Column>
            <Column width={10}>
            <Column width={10}>
              <Cell name="Part Number">100-10001</Cell>
              <Cell name="Part Description">Description of part</Cell>
            </Column>
            <Column width={8}>
              <Cell name="Vendor Number">001</Cell>
              <Cell name="Vendor Name">Vendor Name A</Cell>
            </Column>
          </Column>
          <Column width={8}>
            <Cell name="Order Qty">10</Cell>
            <Cell name="Receive">20</Cell>
          </Column>
          <Column width={8}>
            <Cell name="Cost">5.00</Cell>
            <Cell name="Extended Cost">2.00</Cell>
          </Column>
          <Column width={5}>
            <Cell name="Duty %">3.0%</Cell>
            <Cell name="Duty">0.15</Cell>
          </Column>
          <Column width={8}>
            <Cell name="Freight %">3.0%</Cell>
            <Cell name="Freight">0.15</Cell>
          </Column>
          <Column width={5}>
            <Cell name="UOM">EA</Cell>
          </Column>
          <Column width={8}>
            <Cell name="Vendor Part Number">10001</Cell>
          </Column>
        </Row>
      </Collection>
    </section>
  );
}