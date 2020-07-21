import React, { useRef, useState, useEffect } from 'react';
import styled from 'styled-components';
import { useSelect, useMultiple, useFilter, useDual, UseDropdownInstance, UseFilterInstance, UseDualInstance, OptionInstance } from 'jfront-components';

export interface TagPickerProps {
  width?: string;
  height?: string;
}

const DualList = styled.div<TagPickerProps>`
  padding: 5px;
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  flex-direction: column;
  min-width: ${props => props.width ? `calc(${props.width} / 2)` : '200px'};
  max-width: ${props => props.width ? props.width : '400px'};
  height: ${props => props.height ? props.height : '200px'};
  width: 100%;
`;

const Container = styled.div`
  width: 100%;
  height: calc(100% - 30px);
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
`;

const ButtonBar = styled.div`
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
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

const Button = styled.button`
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

const Option = styled.option``;


export interface DualListFieldProps {
  id?: string;
  name?: string;
  initialValue?: Array<any>;
  touched?: boolean;
  error?: string;
  onChange?: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onChangeValue?: (field: string, value: any) => void;
  width?: string;
  options: Array<any>;
  isLoading?: boolean;
  getOptionName?: (option: any) => string;
  getOptionValue?: (option: any) => string;
  placeholder?: string;
  disabled?: boolean;
}

export const DualListField: React.FC<DualListFieldProps> = ({
  id,
  width,
  name = '',
  initialValue,
  touched,
  error,
  placeholder,
  disabled,
  onChange,
  onChangeValue,
  isLoading,
  options,
  getOptionName,
  getOptionValue }) => {

  const unselectedListRef = useRef<HTMLSelectElement>(null);
  const selectedListRef = useRef<HTMLSelectElement>(null)
  const [filter, setFilter] = useState("");
  const [_isLoading, setIsLoading] = useState(isLoading);

  useEffect(
    () => {
      setIsLoading(isLoading);
    }, [isLoading]
  );

  const {
    getOptions,
    getSelectedOptions,
    getSelectedOption,
    getInputProps,
    getRootProps,
    selectOption,
    getListProps
  } = useSelect({
    initialValue,
    options: options,
    onChange: (value) => {
      if (onChangeValue) {
        onChangeValue(name, value);
      }
    },
    getOptionName,
    getOptionValue
  }, useMultiple, useDual, useFilter
  ) as UseDropdownInstance & UseFilterInstance & UseDualInstance;

  const mapOptions = (values: Array<string>) => {
    const result: any[] = [];
    if (values.length === 0) return result;
    const options = getOptions().map(optionInstance => optionInstance.option);
    return values.map(value => {
      const target = options.find(option => option.value === value);
      if (target) {
        options.splice(options.indexOf(target), 1);
        return target;
      }
    })
  }

  const mapSelectedOptions = (values: Array<string>) => {
    if (values.length === 0) return getSelectedOption();
    return getSelectedOption().filter((option: any) => !values.find(value => option?.value === value));
  }

  const onInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (onChange) {
      selectOption([]);
      setFilter(e.target.value);
      onChange(e);
    } else {
      getInputProps().onChange(e)
    }
  }

  const onOptionClick = (event: React.MouseEvent, optionInstance: OptionInstance) => {
    if (disabled) return;
    event.stopPropagation();
    // In that case, event.ctrlKey does the trick.
    if (!event.ctrlKey && !event.shiftKey) {
      optionInstance.getOptionProps().onClick();
    }
  }

  const select = () => {
    if (unselectedListRef.current?.selectedOptions) {
      selectOption([...mapOptions(Array.from(unselectedListRef.current?.selectedOptions).map((selectedOption: any) => selectedOption.value)),
      ...getSelectedOptions().map(optionInstance => optionInstance.option)])
    }
  }

  const deselect = () => {
    if (selectedListRef.current?.selectedOptions) {
      selectOption(mapSelectedOptions(Array.from(selectedListRef.current?.selectedOptions).map((selectedOption: any) => selectedOption.value)))
    }
  }

  const selectAll = () => {
    selectOption([...getSelectedOptions().map(optionInstance => optionInstance.option),
    ...getOptions().map(optionInstance => optionInstance.option)]);
  }

  const deselectAll = () => {
    selectOption([])
  }

  return (
    <DualList id={id} {...getRootProps()} width={width}>
      <Input
        {...getInputProps()}
        onChange={onInputChange}
        placeholder={placeholder}
        disabled={disabled}
        value={onChange ? filter : getInputProps().value} />
      <Container>
        <List
          {...getListProps()}
          ref={unselectedListRef}
          disabled={disabled}
          error={touched && error ? true : false}>
          {getOptions().map(optionInstance => (
            <Option value={optionInstance.option.value} {...optionInstance.getOptionProps()}
              onClick={e => onOptionClick(e, optionInstance)}>
              {optionInstance.option.name}
            </Option>)
          )}
        </List>
        <ButtonBar>
          <Button onClick={selectAll} disabled={disabled}>↠</Button>
          <Button onClick={select} disabled={disabled}>→</Button>
          <Button onClick={deselect} disabled={disabled}>←</Button>
          <Button onClick={deselectAll} disabled={disabled}>↞</Button>
        </ButtonBar>
        <List
          {...getListProps()}
          ref={selectedListRef}
          disabled={disabled}
          error={touched && error ? true : false}>
          {getSelectedOptions().map(optionInstance => (
            <Option value={optionInstance.option.value} {...optionInstance.getOptionProps()}
              onClick={e => onOptionClick(e, optionInstance)}>
              {optionInstance.option.name}
            </Option>)
          )}
        </List>
      </Container>
    </DualList>
  );
}
