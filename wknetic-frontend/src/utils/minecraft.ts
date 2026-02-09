/**
 * Minecraft UUID 验证和格式化工具
 */

/**
 * UUID 正则表达式（带连字符）
 */
const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

/**
 * UUID 正则表达式（无连字符）
 */
const UUID_NO_DASH_REGEX = /^[0-9a-f]{32}$/i

/**
 * 验证 UUID 格式是否正确
 */
export function validateUuid(uuid: string): boolean {
  if (!uuid) return false
  return UUID_REGEX.test(uuid) || UUID_NO_DASH_REGEX.test(uuid)
}

/**
 * 格式化 UUID（添加连字符）
 */
export function formatUuid(uuid: string): string {
  if (!uuid) return ''
  
  // 移除所有连字符
  const cleanUuid = uuid.replace(/-/g, '').toLowerCase()
  
  // 验证长度
  if (cleanUuid.length !== 32) {
    return uuid
  }
  
  // 添加连字符
  return `${cleanUuid.substring(0, 8)}-${cleanUuid.substring(8, 12)}-${cleanUuid.substring(12, 16)}-${cleanUuid.substring(16, 20)}-${cleanUuid.substring(20, 32)}`
}

/**
 * 移除 UUID 的连字符
 */
export function removeUuidDashes(uuid: string): string {
  if (!uuid) return ''
  return uuid.replace(/-/g, '').toLowerCase()
}

/**
 * 获取 Minecraft 头像 URL
 */
export function getMinecraftAvatarUrl(uuid: string, size: number = 64): string {
  if (!uuid) return ''
  const cleanUuid = removeUuidDashes(uuid)
  return `https://mc-heads.net/avatar/${cleanUuid}`
  
}

/**
 * 获取 Minecraft 皮肤 URL
 */
export function getMinecraftSkinUrl(uuid: string): string {
  if (!uuid) return ''
  const cleanUuid = removeUuidDashes(uuid)
  return `https://mc-heads.net/body/${cleanUuid}`
}

/**
 * UUID 错误提示
 */
export function getUuidErrorMessage(uuid: string): string {
  if (!uuid) {
    return 'UUID不能为空'
  }
  
  if (!validateUuid(uuid)) {
    return 'UUID格式不正确，应为32位十六进制或标准UUID格式'
  }
  
  return ''
}
