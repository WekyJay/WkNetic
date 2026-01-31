/**
 * WkNetic Common Components
 * 统一导出所有通用组件
 */

// 基础UI组件
export { default as WkButton } from './WkButton.vue'
export { default as WkInput } from './WkInput.vue'
export { default as WkCard } from './WkCard.vue'
export { default as WkBadge } from './WkBadge.vue'

// 反馈组件
export { default as WkAlert } from './WkAlert.vue'
export { default as WkConfirmModal } from './WkConfirmModal.vue'
export { default as WkDialog } from './WkDialog.vue'
export { default as WkNotification } from './WkNotification.vue'
export { default as WkLoading } from './WkLoading.vue'
export { default as WkLegalModal } from './WkLegalModal.vue'

// Markdown组件
export { default as WkMarkdownEditor } from './WkMarkdownEditor.vue'
export { default as WkMarkdownRenderer } from './WkMarkdownRenderer.vue'

// 导出类型
export type { ButtonVariant, ButtonSize } from './WkButton.vue'
export type { InputType, InputSize } from './WkInput.vue'
export type { CardShadow, CardPadding } from './WkCard.vue'
export type { BadgeVariant, BadgeSize } from './WkBadge.vue'
export type { AlertType } from './WkAlert.vue'
export type { DialogSize } from './WkDialog.vue'
export type { NotificationType, NotificationPosition } from './WkNotification.vue'
export type { LoadingSize } from './WkLoading.vue'