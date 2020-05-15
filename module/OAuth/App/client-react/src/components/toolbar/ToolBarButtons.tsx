import React from 'react';
import { ToolBarButton } from './';
import add from './images/add.png';
import save from './images/save.png';
import edit from './images/edit.png';
import view from './images/view.png';
import del from './images/delete.png';
import search from './images/search.png';
import split from './images/split.gif';
import styled from 'styled-components';

const Splitter = styled.span`
  display: inline-block;
  background-position: center;
  background-repeat: repeat;
  height: 22px;
  width: 2px;
  margin-left: 2px;
  margin-right: 2px;
  background-image: url(${split});
`;

interface CreateButtonProps {
  onCreate(): any;
  disabled?: boolean;
}

const CreateButton: React.FC<CreateButtonProps> = ({onCreate, disabled}) => {
  return (
    <ToolBarButton onClick={onCreate} disabled={disabled ? true : false} tooltip='Создание записи'>
      <img src={add} alt='Создание записи'/>
    </ToolBarButton>
  );
}

interface EditButtonProps {
  onEdit(): any;
  disabled?: boolean;
}

const EditButton: React.FC<EditButtonProps> = ({onEdit, disabled}) => {
  return (
    <ToolBarButton onClick={onEdit} disabled={disabled ? true : false} tooltip='Редактирование записи'>
      <img src={edit} alt='Редактирование записи'/>
    </ToolBarButton>
  );
}

interface SaveButtonProps {
  onSave(): any;
  disabled?: boolean;
}

const SaveButton: React.FC<SaveButtonProps> = ({onSave, disabled}) => {
  return (
    <ToolBarButton onClick={onSave} disabled={disabled ? true : false} tooltip='Сохранить запись'>
      <img src={save} alt='Сохранить запись'/>
    </ToolBarButton>
  );
}

interface ViewButtonProps {
  onView(): any;
  disabled?: boolean;
}

const ViewButton: React.FC<ViewButtonProps> = ({onView, disabled}) => {
  return (
    <ToolBarButton onClick={onView} disabled={disabled ? true : false} tooltip='Просмотр записи'>
      <img src={view} alt='Просмотр записи'/>
    </ToolBarButton>
  );
}

interface DeleteButtonProps {
  onDelete(): any;
  disabled?: boolean;
}

const DeleteButton: React.FC<DeleteButtonProps> = ({onDelete, disabled}) => {
  return (
    <ToolBarButton onClick={onDelete} disabled={disabled ? true : false} tooltip='Удалить запись'>
      <img src={del} alt='Удалить запись'/>
    </ToolBarButton>
  );
}

interface ListButtonProps {
  onList(): any;
  disabled?: boolean;
}

const ListButton: React.FC<ListButtonProps> = ({onList, disabled}) => {
  return (
    <ToolBarButton onClick={onList} disabled={disabled ? true : false} tooltip='Список'>
      Список
    </ToolBarButton>
  );
}

interface SearchButtonProps {
  onSearch(): any;
  disabled?: boolean;
}

const SearchButton: React.FC<SearchButtonProps> = ({onSearch, disabled}) => {
  return (
    <ToolBarButton onClick={onSearch} disabled={disabled ? true : false} tooltip='Поиск'>
      <img src={search} alt='Поиск'/>
    </ToolBarButton>
  );
}

interface DoSearchButtonProps {
  onDoSearch(): any;
  disabled?: boolean;
}

const DoSearchButton: React.FC<DoSearchButtonProps> = ({onDoSearch, disabled}) => {
  return (
    <ToolBarButton onClick={onDoSearch} disabled={disabled ? true : false} tooltip='Найти'>
      Найти
    </ToolBarButton>
  );
}

export { CreateButton, EditButton, SaveButton, DeleteButton, ViewButton, Splitter, ListButton, SearchButton, DoSearchButton};