import React, { useRef, useState, useEffect } from 'react';
import styled from 'styled-components';
import { TextInput } from '@jfront/ui-core';

const DualList = styled.div`
  padding: 5px;
  display: inline-flex;
  flex-grow: 1;
  flex-direction: column;
  width: 100%;
`;

const Container = styled.div`
  width: 100%;
  flex-grow: 1;
  display: flex;
`;

const ButtonBar = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 10%;
  min-width: 32px;
`;

const Input = styled.input.attrs({ type: 'text' })`
  padding: 0;
  margin: 0;
  margin-bottom: 5px;
  height: 20px;
  border: 1px solid #ccc;
  border-top: 1px solid #999;
  max-width: 45%;
`;

interface ListProps {
  error?: boolean;
}

const List = styled.select.attrs({ multiple: true }) <ListProps>`
  padding: 0;
  overflow: auto;
  height: 100%;
  width: 45%;
  ${(props: ListProps) => props.error ? 'border: 1px solid red' : 'border: 1px solid #ccc; border-top: 1px solid #999;'}
`;

const Button = styled.button.attrs({ type: "button" })`
  margin: 0;
  padding: 0;
  text-decoration: none;
  font-size: 20px;
  font-weight: bold;
  height: 25px;
  cursor: pointer;
  border: 1px outset #ccc;
  width: 32px;
`;

const Option = styled.option`
  padding-left: 3px;
`;


export interface DualListFieldProps {
  id?: string;
  name?: string;
  style?: React.CSSProperties;
  className?: string;
  initialValues?: Array<any>;
  touched?: boolean;
  error?: string;
  onInputChange?: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onSelectionChange?: (field: string, values: Array<any>) => void;
  options: Array<any>;
  isLoading?: boolean;
  getOptionName?: (option: any) => string;
  getOptionValue?: (option: any) => string;
  placeholder?: string;
  disabled?: boolean;
}

export const DualListField: React.FC<DualListFieldProps> = ({
  id,
  name = '',
  initialValues,
  error,
  placeholder,
  disabled,
  isLoading,
  options,
  style,
  className,
  onInputChange,
  onSelectionChange,
  getOptionName,
  getOptionValue }) => {

  const unselectedListRef = useRef<HTMLSelectElement>(null);
  const selectedListRef = useRef<HTMLSelectElement>(null)
  const [filter, setFilter] = useState("");
  const [values, setValues] = useState<Array<any>>(initialValues ? [...initialValues] : []);

  useEffect(() => {
    if (initialValues) {
      setValues(initialValues)
    }
  }, [initialValues])

  const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (onInputChange) {
      onInputChange(e);
    } else {
      setFilter(e.target.value);
    }
  }

  const select = (newValues?: Array<any>) => {
    if (newValues && newValues.length > 0) {
      if (onSelectionChange) {
        onSelectionChange(name,
          [...values.map(option => getOptionValue ? getOptionValue(option) : option.value),
          ...newValues]);
      }
      setValues([...values, ...options.filter(option => newValues.includes(String(getOptionValue ? getOptionValue(option) : option.value)))]);
    }
  }

  const deselect = (newValues?: Array<any>) => {
    console.log(newValues)
    if (newValues && newValues.length > 0 && values.length > 0) {
      const nextValues = values.filter(valueOption => !newValues.find(newValue => (getOptionValue ?
        getOptionValue(valueOption) : valueOption.value) === newValue ));
      if (onSelectionChange) {
        onSelectionChange(name, nextValues);
      }
      setValues(nextValues);
    }
  }

  const selectAll = () => {
    if (onSelectionChange) {
      onSelectionChange(name, options.map(option => getOptionValue ? getOptionValue(option) : option.value));
    }
    setValues(options);
  }

  const deselectAll = () => {
    if (onSelectionChange) {
      onSelectionChange(name, []);
    }
    setValues([]);
  }

  const renderOptions = () => {
    const notSelectedOptions = options.filter(option => !values.find(valueOption => getOptionValue ?
      getOptionValue(option) === getOptionValue(valueOption) : option.value === valueOption.value))
    if (onInputChange) {
      return notSelectedOptions.map(option => <Option
        onClick={(e) => {
          if (!e.ctrlKey && !e.shiftKey) {
            select([getOptionValue ? getOptionValue(option) : option.value])
          }
        }}
        key={getOptionValue ? getOptionValue(option) : option.value}
        value={getOptionValue ? getOptionValue(option) : option.value}
        title={getOptionName ? getOptionName(option) : option.name}>
        {getOptionName ? getOptionName(option) : option.name}
      </Option>)
    } else {
      return notSelectedOptions.filter(option => getOptionName ? getOptionName(option).startsWith(filter) : option.name.startsWith(filter))
        .map(option => <Option
          onClick={(e) => {
            if (!e.ctrlKey && !e.shiftKey) {
              select([getOptionValue ? getOptionValue(option) : option.value])
            }
          }}
          key={getOptionValue ? getOptionValue(option) : option.value}
          value={getOptionValue ? getOptionValue(option) : option.value}
          title={getOptionName ? getOptionName(option) : option.name}>
          {getOptionName ? getOptionName(option) : option.name}
        </Option>);
    }
  }

  return (
    <DualList id={id} style={style} className={className}>
      <TextInput
        onChange={onChange}
        value={onInputChange ? undefined : filter}
        placeholder={placeholder}
        style={{maxWidth: "45%", paddingBottom: "5px"}}
        error={error}
        isLoading={isLoading}
        disabled={disabled} />
      <Container>
        <List
          ref={unselectedListRef}
          disabled={disabled}
          error={error ? true : false}>
          {renderOptions()}
        </List>
        <ButtonBar>
          <Button onClick={selectAll} disabled={disabled}>↠</Button>
          <Button onClick={() =>
            select(unselectedListRef.current?.selectedOptions ?
              Array.from(unselectedListRef.current?.selectedOptions).map((selectedOption: any) => selectedOption.value) : [])}
            disabled={disabled}>→</Button>
          <Button onClick={() =>
            deselect(selectedListRef.current?.selectedOptions ?
              Array.from(selectedListRef.current?.selectedOptions).map((selectedOption: any) => selectedOption.value) : [])}
            disabled={disabled}>←</Button>
          <Button onClick={deselectAll} disabled={disabled}>↞</Button>
        </ButtonBar>
        <List
          ref={selectedListRef}
          disabled={disabled}
          error={error ? true : false}>
          {values.map(option => <Option
            onClick={(e) => {
              if (!e.ctrlKey && !e.shiftKey) {
                deselect([getOptionValue ? getOptionValue(option) : option.value])
              }
            }}
            key={getOptionValue ? getOptionValue(option) : option.value}
            value={getOptionValue ? getOptionValue(option) : option.value}
            title={getOptionName ? getOptionName(option) : option.name}>
            {getOptionName ? getOptionName(option) : option.name}
          </Option>)}
        </List>
      </Container>
    </DualList>
  );
}
