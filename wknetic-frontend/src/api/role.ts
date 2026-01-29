import request from './axios'

export interface Role {
  roleId: number
  roleCode: string
  roleName: string
  roleDesc?: string
  sortOrder: number
  isDefault: boolean
  status: number
  createTime: string
  updateTime: string
}

export interface RoleFormData {
  roleId?: number
  roleCode: string
  roleName: string
  roleDesc?: string
  sortOrder: number
  isDefault: boolean
  status: number
}

// 注意：axios 拦截器已经解包了响应，所以这里返回的是解包后的数据类型
export const roleApi = {
  getAllRoles: (): Promise<Role[]> => {
    return request.get('/api/v1/admin/roles/list')
  },

  getRoleById: (roleId: number): Promise<Role> => {
    return request.get(`/api/v1/admin/roles/${roleId}`)
  },

  createRole: (data: RoleFormData): Promise<void> => {
    return request.post('/api/v1/admin/roles/create', data)
  },

  updateRole: (data: RoleFormData): Promise<void> => {
    return request.put('/api/v1/admin/roles/update', data)
  },

  deleteRole: (roleId: number): Promise<void> => {
    return request.delete(`/api/v1/admin/roles/${roleId}`)
  },

  getDefaultRoleCode: (): Promise<string> => {
    return request.get('/api/v1/admin/roles/default')
  }
}
