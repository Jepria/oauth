import React, { useReducer, createContext, useContext, useEffect } from 'react';
import styled from 'styled-components';
import { getRandomString } from '../../../security/Crypto';
import { isFunction } from '../../../utils';
import exclamation from './images/exclamation.gif'

interface ListBoxOptionBlockProps {
  width?: string;
}

const CheckBoxDiv = styled.div`
  display: inline-block;
  vertical-align: middle;
  width: 20px;
`;

const CheckBox = styled.input.attrs({ type: 'checkbox' })``;

const ListBoxOptionLabel = styled.label`
  vertical-align: middle;
  display: inline-block;
  overflow: hidden;
  cursor: pointer;
  white-space: nowrap;
  width: calc(100% - 20px);
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

interface ListBoxOptionListProps {
  error?: boolean
} 

const ListBoxOptionList = styled.ul`
  overflow: auto;
  background: white;
  margin: 0;
  padding: 0;
  border: 1px solid #B5B4B4;
  width: 200px;
  height: 100px;
  ${(props: ListBoxOptionListProps) => props.error ? 'border: 1px solid red' : 'border: 1px solid #ccc; border-top: 1px solid #999;'}
`;

const ListBox = styled.div`
  padding: 0px;
  margin: 0px;
  font-family: tahoma, arial, helvetica, sans-serif;
  font-size: 12px;
`;

const Icon = styled.img`
  margin-left: 5px;
  margin-top: 5px;
  height: 16px;
  width: 16px;
`;

type ListBoxState = {
  values: Array<any>;
  selected: Array<any>;
  isLoading: boolean;
}

type Action =
  | { type: "select", selected: Array<any> }
  | { type: "values", values: Array<any> }

const ListBoxReducer = (state: ListBoxState, action: Action) => {
  switch (action.type) {
    case 'select':
      return {
        isLoading: false,
        values: state.values,
        selected: action.selected ? action.selected : []
      };
    case 'values':
      return {
        isLoading: false,
        selected: state.selected,
        values: action.values
      };
  }
}

export interface ListBoxComponentProps {
  name?: string;
  value?: Array<any>;
  touched?: boolean;
  error?: string;
  onChange?(field: string, value: any): void;
  showSelectAll?: boolean;
  width?: string;
  height?: string;
}

const ListBoxComponent: React.FC<ListBoxComponentProps> = ({ name = '', value, touched, error, children, onChange }) => {

  const [{
    values,
    selected,
    isLoading
  }, dispatch] = useReducer(ListBoxReducer, {
    values: [],
    selected: [],
    isLoading: false
  });

  useEffect(() => {
    dispatch({
      type: 'select',
      selected: value ? value.slice() : []
    });
  }, [value, dispatch]);

  const changeSelection = (value: any) => {
    if (selected.includes(value)) {
      selected.splice(selected.indexOf(value), 1);
    } else {
      selected.push(value);
    }
    dispatch({
      type: 'select',
      selected: selected
    });
    if (onChange) {
      onChange(name, selected.slice());
    }
  }

  const selectAll = () => {
    if (values === selected || (values.length === selected.length && values.every(e => selected.includes(e)))) {
      dispatch({
        type: 'select',
        selected: []
      });
      if (onChange) {
        onChange(name, []);
      }
    } else {
      dispatch({
        type: 'select',
        selected: values.slice()
      });
      if (onChange) {
        onChange(name, values.slice());
      }
    }
  }

  const addValue = (value: any) => {
    if (!values.includes(value)) {
      values.push(value);
    }
  }

  return (
    <ListBoxContext.Provider value={{ name, selected, values, isLoading, touched, error, changeSelection, selectAll, addValue }}>
      <ListBox>{children}</ListBox>
      {touched && error && <Icon src={exclamation} title={error} />}
    </ListBoxContext.Provider>
  );
}


const ListBoxOptionListComponent: React.FC = ({ children }) => {
  const context = useContext(ListBoxContext);
  return (
    <ListBoxOptionList error={context.touched && context.error ? true : false}>{isFunction(children) ? children() : children}</ListBoxOptionList>
  );
}

export interface ListBoxOptionComponentProps {
  value: any;
  name?: string;
}

const ListBoxOptionComponent: React.FC<ListBoxOptionComponentProps> = ({ value, name, children }) => {
  const context = useContext(ListBoxContext);

  context.addValue(value);

  const id = getRandomString();

  if (children) {
    if (context.selected.includes(value)) {
      return (
        <ListBoxOptionSelected key={value} onClick={e => context.changeSelection(value)}>{children}</ListBoxOptionSelected>
      );
    } else {
      return (
        <ListBoxOption key={value} onClick={e => context.changeSelection(value)}>{children}</ListBoxOption>
      );
    }
  } else {
    return (
      <ListBoxOption key={value} >
        <CheckBoxDiv>
          <CheckBox id={id} onChange={() => context.changeSelection(value)} checked={context.selected ? context.selected.includes(value) : false} onDoubleClick={e => context.changeSelection(value)} />
        </CheckBoxDiv>
        <ListBoxOptionLabel htmlFor={id} onDoubleClick={e => context.changeSelection(value)}>{name}</ListBoxOptionLabel>
      </ListBoxOption>
    );
  }
}

const SelectAllCheckBoxComponent: React.FC = () => {

  const context = useContext(ListBoxContext);
  const id = getRandomString()

  return (
    <React.Fragment>
      <CheckBoxDiv>
        <CheckBox id={id}
          onChange={context.selectAll}
          checked={context.values === context.selected || (context.values.length === context.selected.length && context.values.every(e => context.selected.includes(e)))}
          onClick={context.selectAll} 
          disabled={context.values?.length === 0}/>
      </CheckBoxDiv>
      <ListBoxOptionLabel htmlFor={id}>Выделить всё</ListBoxOptionLabel>
    </React.Fragment>
  );
}

export interface IListBoxContext {
  name: string;
  selected: Array<any>;
  values: Array<any>;
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
  selected: [],
  values: [],
  changeSelection: (value: any) => { },
  selectAll: () => { },
  addValue: (value: any) => { },
});

export const useListBoxContext = () => {
  return useContext(ListBoxContext);
}

export {
  ListBoxComponent as ListBox,
  ListBoxOptionListComponent as ListBoxOptionList,
  ListBoxOptionComponent as ListBoxOption,
  SelectAllCheckBoxComponent as SelectAllCheckBox
}