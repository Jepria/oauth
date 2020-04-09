import React from 'react';
import { ToolBarButton } from '../toolbar/ToolBar';
import add from './images/add.png';
import save from './images/save.png';
import edit from './images/edit.png';
import view from './images/view.png';
import del from './images/delete.png';

type CreateButtonProps = {
  onCreate(): any;
  disabled: boolean;
}

const CreateButton: React.FC<CreateButtonProps> = ({onCreate, disabled}) => {
  return (
    <ToolBarButton onClick={onCreate} disabled={disabled} tooltip='Создание записи'>
      <img src={add} alt='Создание записи'/>
    </ToolBarButton>
  );
}

type EditButtonProps = {
  onEdit(): any;
  disabled: boolean;
}

const EditButton: React.FC<EditButtonProps> = ({onEdit, disabled}) => {
  return (
    <ToolBarButton onClick={onEdit} disabled={disabled} tooltip='Редактирование записи'>
      <img src={edit} alt='Редактирование записи'/>
    </ToolBarButton>
  );
}

type SaveButtonProps = {
  onSave(): any;
  disabled: boolean;
}

const SaveButton: React.FC<SaveButtonProps> = ({onSave, disabled}) => {
  return (
    <ToolBarButton onClick={onSave} disabled={disabled} tooltip='Сохранить запись'>
      <img src={save} alt='Сохранить запись'/>
    </ToolBarButton>
  );
}

type ViewButtonProps = {
  onView(): any;
  disabled: boolean;
}

const ViewButton: React.FC<ViewButtonProps> = ({onView, disabled}) => {
  return (
    <ToolBarButton onClick={onView} disabled={disabled} tooltip='Просмотр записи'>
      <img src={view} alt='Просмотр записи'/>
    </ToolBarButton>
  );
}

type DeleteButtonProps = {
  onDelete(): any;
  disabled: boolean;
}

const DeleteButton: React.FC<DeleteButtonProps> = ({onDelete, disabled}) => {
  return (
    <ToolBarButton onClick={onDelete} disabled={disabled} tooltip='Удалить запись'>
      <img src={del} alt='Удалить запись'/>
    </ToolBarButton>
  );
}

export { CreateButton, EditButton, SaveButton, DeleteButton, ViewButton};