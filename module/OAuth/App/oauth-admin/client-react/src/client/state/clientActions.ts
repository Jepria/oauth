import { Option } from "../types"

export interface GetRolesAction {
  roleName?: string
}

export interface GetRolesSuccessAction {
  roles: Array<Option>
}