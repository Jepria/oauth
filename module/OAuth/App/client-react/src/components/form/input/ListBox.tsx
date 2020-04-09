import React, { useReducer, createContext, useContext } from 'react';
import styled from 'styled-components';
import { getRandomString } from '../../../security/Crypto';

interface ListBoxOptionBlockProps {
  width?: string;
}

const CheckBoxDiv = styled.div`
  display: inline-block;
  vertical-align: middle;
  width: 20px;
`;

const CheckBox = styled.input.attrs({ type: 'checkbox' })`
`;

const ListBoxOptionLabel = styled.label`
  vertical-align: middle;
  display: inline-block;
  overflow: hidden;
  cursor: pointer;
  white-space: nowrap;
  width: calc(100% - 20px);
  -webkit-user-select:none;
  -khtml-user-select:none;
  -moz-user-select:none;
  -o-user-select:none;
  user-select:none;
`;

const ListBoxOptionSelected = styled.li`
  height: 20px;
  width: 100%;
  background: #ccddf3;
`;

const ListBoxOption = styled.li`
  height: 20px;
  width: 100%;
  &:hover{
    background: #eee
  }
`;

const ListBoxOptionList = styled.ul`
  overflow: auto;
  background: white;
  margin: 0;
  padding: 0;
  border: 1px solid #B5B4B4;
  width: 200px;
  height: 100px;
`;

const ListBox = styled.div`
  padding: 0px;
  margin: 0px;
  font-family: tahoma, arial, helvetica, sans-serif;
  font-size: 12px;
`;

type ListBoxState = {
  values: Array<any>;
  selected?: Array<any>;
  selectAll: boolean;
  isLoading: boolean;
}

type Action =
  | { type: "select", selected: Array<any> }
  | { type: "selectAll" }
  | { type: "values", values: Array<any> }

const ListBoxReducer = (state: ListBoxState, action: Action) => {
  switch (action.type) {
    case 'select':
      return { isLoading: false, selected: action.selected, selectAll: state.values === action.selected && state.values.every((value, index) => value === action.selected[index])};
    case 'selectAll':
      return { isLoading: false, selected: state.selectAll ? [] : state.values, selectAll: !state.selectAll };
    case 'values':
      return { isLoading: false, selected: state.selected, values: action.values, selectAll: state.selected === action.values && state.selected.every((value, index) => value === action.values[index])};
  }
}

interface ListBoxComponentProps {
  /** Имя поля */
  name: string;
  /** Изначальное значение */
  value?: Array<any>;
  touched?: boolean;
  error?: string;
  /**Handler, который принимает либо значение с типом string, либо React.ChangeEvent от изменения значения input, вызывается при смене значения*/
  onChange?<T = string | React.ChangeEvent<any>>(field: T): T extends React.ChangeEvent<any> ? void : (e: string | React.ChangeEvent<any>) => void;
  showSelectAll?: boolean;
  width?: string;
  height?: string;
}

const ListBoxComponent: React.FC = ({ children }) => {
  return (
    <ListBox>{children}</ListBox>
  );
}

const ListBoxOptionListComponent: React.FC = ({ children }) => {
  return (
    <ListBoxOptionList>{children}</ListBoxOptionList>
  );
}

interface ListBoxOptionComponentProps {
  value: any;
  name?: string;
}

const ListBoxOptionComponent: React.FC<ListBoxOptionComponentProps> = ({ value, name, children }) => {
  const context = useContext(ListBoxContext);

  context.addValue(value);

  const id = getRandomString();

  if (children) {
    return (
      <ListBoxOption>{children}</ListBoxOption>
    );
  } else {
    return (
      // <ListBoxOption>
      //   <CheckBoxDiv>
      //     <CheckBox id={id} onChange={e => context.changeSelection(value)}/>
      //   </CheckBoxDiv>
      //   <ListBoxOptionLabel htmlFor={id}>{name}</ListBoxOptionLabel>
      // </ListBoxOption>
      <ListBoxOption>
      <CheckBoxDiv>
        <CheckBox id={id}/>
      </CheckBoxDiv>
      <ListBoxOptionLabel htmlFor={id}>{name}</ListBoxOptionLabel>
    </ListBoxOption>
    );
  }
}

interface IListBoxContext {
  name: string;
  selected?: Array<any>;
  isLoading: boolean;
  touched?: boolean;
  error?: string;
  changeSelection(value: any): void;
  selectAll(): void;
  addValue(value: any): void;
}

const ListBoxContext = createContext<IListBoxContext>({
  name: '',
  isLoading: false,
  changeSelection: (value: any) => { },
  selectAll: () => { },
  addValue: (value: any) => { },
});

export {
  ListBoxComponent as ListBox,
  ListBoxOptionListComponent as ListBoxOptionList,
  ListBoxOptionComponent as ListBoxOption
}