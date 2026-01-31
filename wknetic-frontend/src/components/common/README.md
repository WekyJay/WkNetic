# WkNetic UI Components 使用文档

## 📦 组件列表

### 基础组件
1. [WkButton](#1-wkbutton---按钮组件) - 按钮组件
2. [WkInput](#2-wkinput---输入框组件) - 输入框组件
3. [WkCard](#3-wkcard---卡片组件) - 卡片组件
4. [WkBadge](#4-wkbadge---徽章组件) - 徽章组件

### 反馈组件
5. [WkAlert](#5-wkalert---提示组件) - 提示组件
6. [WkDialog](#6-wkdialog---对话框组件) - 对话框组件
7. [WkNotification](#7-wknotification---通知组件) - 通知组件
8. [WkLoading](#8-wkloading---加载组件) - 加载组件
9. [WkConfirmModal](#9-wkconfirmmodal---确认对话框) - 确认对话框

### 数据组件
10. [WkDataTable](#10-wkdatatable---数据表格) - 数据表格

### Markdown组件
11. [WkMarkdownEditor](#11-wkmarkdowneditor---markdown编辑器) - Markdown编辑器
12. [WkMarkdownRenderer](#12-wkmarkdownrenderer---markdown渲染器) - Markdown渲染器

---

## 基础组件

### 1. WkButton - 按钮组件

统一的按钮组件，支持多种样式和状态。

**Props:**
- `variant`: 按钮类型 (`primary` | `secondary` | `danger` | `success` | `warning` | `ghost` | `text`)
- `size`: 按钮大小 (`sm` | `md` | `lg`)
- `disabled`: 是否禁用
- `loading`: 是否加载中
- `block`: 是否块级元素
- `icon`: 图标类名
- `iconPosition`: 图标位置 (`left` | `right`)

**使用示例:**
```vue
<script setup>
import { WkButton } from '@/components/common'
</script>

<template>
  <!-- 基础用法 -->
  <WkButton variant="primary">Primary Button</WkButton>
  <WkButton variant="secondary">Secondary Button</WkButton>
  <WkButton variant="danger">Danger Button</WkButton>
  
  <!-- 带图标 -->
  <WkButton icon="i-tabler-plus" @click="handleCreate">
    Create
  </WkButton>
  
  <!-- 加载状态 -->
  <WkButton :loading="isSubmitting" @click="handleSubmit">
    Submit
  </WkButton>
  
  <!-- 不同尺寸 -->
  <WkButton size="sm">Small</WkButton>
  <WkButton size="md">Medium</WkButton>
  <WkButton size="lg">Large</WkButton>
</template>
```

---

### 2. WkInput - 输入框组件

功能丰富的输入框组件。

**Props:**
- `modelValue`: v-model绑定值
- `type`: 输入类型 (`text` | `password` | `email` | `number` | `tel` | `url` | `search`)
- `size`: 输入框大小 (`sm` | `md` | `lg`)
- `placeholder`: 占位文本
- `disabled`: 是否禁用
- `error`: 错误信息
- `prefixIcon`: 前缀图标
- `suffixIcon`: 后缀图标
- `clearable`: 是否显示清空按钮
- `maxlength`: 最大长度
- `showCount`: 是否显示字符计数

**事件:**
- `@update:modelValue`: 值变化
- `@focus`: 获得焦点
- `@blur`: 失去焦点
- `@enter`: 按下回车键
- `@clear`: 点击清空按钮

**使用示例:**
```vue
<script setup>
import { ref } from 'vue'
import { WkInput } from '@/components/common'

const username = ref('')
const password = ref('')
const email = ref('')
const emailError = ref('')
</script>

<template>
  <!-- 基础用法 -->
  <WkInput 
    v-model="username" 
    placeholder="Enter username"
    prefix-icon="i-tabler-user"
  />
  
  <!-- 密码输入 -->
  <WkInput 
    v-model="password" 
    type="password"
    prefix-icon="i-tabler-lock"
  />
  
  <!-- 带错误提示 -->
  <WkInput 
    v-model="email" 
    type="email"
    :error="emailError"
    suffix-icon="i-tabler-mail"
  />
  
  <!-- 可清空，带字符计数 -->
  <WkInput 
    v-model="description" 
    placeholder="Enter description"
    :clearable="true"
    :maxlength="200"
    :show-count="true"
  />
</template>
```

---

### 3. WkCard - 卡片组件

通用的卡片容器组件。

**Props:**
- `title`: 卡片标题
- `shadow`: 阴影大小 (`none` | `sm` | `md` | `lg`)
- `padding`: 内边距 (`none` | `sm` | `md` | `lg`)
- `hoverable`: 是否可悬停
- `bordered`: 是否显示边框
- `loading`: 是否加载中

**插槽:**
- `header`: 自定义头部
- `default`: 卡片内容
- `footer`: 自定义底部

**使用示例:**
```vue
<script setup>
import { WkCard } from '@/components/common'
</script>

<template>
  <!-- 基础用法 -->
  <WkCard title="Card Title">
    <p>Card content goes here</p>
  </WkCard>
  
  <!-- 可悬停卡片 -->
  <WkCard :hoverable="true" shadow="lg">
    <p>Hover me!</p>
  </WkCard>
  
  <!-- 自定义头部和底部 -->
  <WkCard>
    <template #header>
      <div class="flex justify-between items-center">
        <h3>Custom Header</h3>
        <button>Action</button>
      </div>
    </template>
    
    <p>Content</p>
    
    <template #footer>
      <div class="text-right">
        <button>Save</button>
      </div>
    </template>
  </WkCard>
  
  <!-- 加载状态 -->
  <WkCard :loading="isLoading" />
</template>
```

---

### 4. WkBadge - 徽章组件

用于状态显示的徽章/标签组件。

**Props:**
- `variant`: 徽章类型 (`default` | `primary` | `success` | `warning` | `danger` | `info`)
- `size`: 徽章大小 (`sm` | `md` | `lg`)
- `dot`: 是否显示为圆点
- `count`: 数字徽章的值
- `max`: 最大显示数字
- `show`: 是否显示

**使用示例:**
```vue
<script setup>
import { WkBadge } from '@/components/common'
</script>

<template>
  <!-- 文本徽章 -->
  <WkBadge variant="success">Active</WkBadge>
  <WkBadge variant="warning">Pending</WkBadge>
  <WkBadge variant="danger">Error</WkBadge>
  
  <!-- 数字徽章 -->
  <WkBadge :count="5">
    <button>Messages</button>
  </WkBadge>
  
  <!-- 带最大值限制 -->
  <WkBadge :count="100" :max="99">
    <button>Notifications</button>
  </WkBadge>
  
  <!-- 圆点徽章 -->
  <WkBadge variant="danger" dot>
    <span class="i-tabler-bell text-2xl" />
  </WkBadge>
  
  <!-- 条件显示 -->
  <WkBadge :count="unreadCount" :show="unreadCount > 0">
    <button>Inbox</button>
  </WkBadge>
</template>
```

---

### 5. WkDataTable - 数据表格

功能丰富的数据表格组件，支持排序和分页。

**Props:**
- `columns`: 列定义数组
- `data`: 表格数据
- `loading`: 是否加载中
- `pagination`: 是否启用分页
- `pageSize`: 每页数量
- `rowKey`: 行的唯一标识

**插槽:**
- `column-{key}`: 自定义列内容
- `actions`: 操作列内容
- `empty-icon`: 空状态图标

**使用示例:**
```vue
<script setup>
import { WkDataTable, type Column } from '@/components/common'

const columns: Column[] = [
  { key: 'name', label: 'Name', sortable: true },
  { key: 'email', label: 'Email' },
  { key: 'status', label: 'Status' }
]

const users = ref([
  { id: 1, name: 'John', email: 'john@example.com', status: 'active' },
  { id: 2, name: 'Jane', email: 'jane@example.com', status: 'inactive' }
])
</script>

<template>
  <WkDataTable 
    :columns="columns" 
    :data="users"
    :loading="loading"
  >
    <!-- 自定义状态列 -->
    <template #column-status="{ row }">
      <WkBadge :variant="row.status === 'active' ? 'success' : 'default'">
        {{ row.status }}
      </WkBadge>
    </template>
    
    <!-- 操作列 -->
    <template #actions="{ row }">
      <WkButton size="sm" @click="handleEdit(row)">Edit</WkButton>
      <WkButton size="sm" variant="danger" @click="handleDelete(row)">
        Delete
      </WkButton>
    </template>
  </WkDataTable>
</template>
```

---

### 6. WkConfirmModal - 确认对话框

用于重要操作确认的模态框组件。

**Props:**
- `visible`: 是否显示
- `title`: 标题
- `content`: 内容文本
- `type`: 类型 (`info` | `warning` | `danger` | `success`)
- `buttonType`: 按钮配置 (`cancel-only` | `confirm-only` | `both`)
- `loading`: 确认按钮加载状态

**事件:**
- `@update:visible`: 显示状态变化
- `@confirm`: 点击确认
- `@cancel`: 点击取消

---

### 7. WkMarkdownEditor - Markdown编辑器

富文本Markdown编辑器，支持实时预览。

**Props:**
- `modelValue`: v-model绑定的Markdown内容
- `placeholder`: 占位文本
- `minHeight`: 最小高度
- `maxHeight`: 最大高度

---

## 反馈组件

### 5. WkAlert - 提示组件

提示/警告信息展示组件，支持多种类型。

**Props:**
- `type`: 警告类型 (`info` | `success` | `warning` | `error`)
- `title`: 标题
- `message`: 描述文本
- `closable`: 是否可关闭
- `showIcon`: 是否显示图标
- `center`: 是否居中
- `icon`: 自定义图标

**使用示例:**
```vue
<script setup>
import { ref } from 'vue'
import { WkAlert } from '@/components/common'

const visible = ref(true)
</script>

<template>
  <!-- 基础用法 -->
  <WkAlert type="info" title="Info" message="This is an info alert" />
  <WkAlert type="success" title="Success" message="Operation completed!" />
  <WkAlert type="warning" title="Warning" message="Please be careful" />
  <WkAlert type="error" title="Error" message="Something went wrong" />
  
  <!-- 可关闭 -->
  <WkAlert 
    v-model:visible="visible"
    type="info" 
    title="Closable Alert" 
    :closable="true"
  />
  
  <!-- 自定义内容 -->
  <WkAlert type="success" :closable="true">
    <h4 class="font-bold">Custom Content</h4>
    <p>You can use custom HTML content here.</p>
  </WkAlert>
</template>
```

---

### 6. WkDialog - 对话框组件

通用对话框组件，支持自定义头部、底部和内容。

**Props:**
- `modelValue` / `v-model`: 是否显示
- `title`: 对话框标题
- `size`: 对话框大小 (`sm` | `md` | `lg` | `xl` | `full`)
- `width`: 自定义宽度
- `closable`: 是否显示关闭按钮
- `modal`: 是否显示遮罩层
- `closeOnClickModal`: 点击遮罩层是否关闭
- `closeOnPressEscape`: 按ESC是否关闭
- `showFooter`: 是否显示底部
- `center`: 是否居中显示
- `fullscreen`: 是否全屏

**事件:**
- `@open`: 打开时触发
- `@opened`: 打开动画结束后触发
- `@close`: 关闭时触发
- `@closed`: 关闭动画结束后触发

**插槽:**
- `header`: 自定义头部
- `default`: 对话框内容
- `footer`: 自定义底部

**使用示例:**
```vue
<script setup>
import { ref } from 'vue'
import { WkDialog, WkButton } from '@/components/common'

const visible = ref(false)
</script>

<template>
  <WkButton @click="visible = true">Open Dialog</WkButton>
  
  <!-- 基础用法 -->
  <WkDialog v-model="visible" title="Dialog Title">
    <p>This is the dialog content.</p>
  </WkDialog>
  
  <!-- 带底部按钮 -->
  <WkDialog v-model="visible" title="Confirm Action">
    <p>Are you sure you want to continue?</p>
    
    <template #footer>
      <WkButton variant="ghost" @click="visible = false">Cancel</WkButton>
      <WkButton variant="primary" @click="handleConfirm">Confirm</WkButton>
    </template>
  </WkDialog>
  
  <!-- 全屏对话框 -->
  <WkDialog v-model="visible" :fullscreen="true">
    <div class="h-full">Fullscreen content</div>
  </WkDialog>
</template>
```

---

### 7. WkNotification - 通知组件

通知消息组件，支持不同位置和自动关闭。

**Props:**
- `type`: 通知类型 (`info` | `success` | `warning` | `error`)
- `title`: 标题
- `message`: 消息内容
- `duration`: 显示时长（毫秒），0表示不自动关闭
- `closable`: 是否可关闭
- `position`: 位置 (`top-right` | `top-left` | `bottom-right` | `bottom-left`)
- `showIcon`: 是否显示图标
- `offset`: 偏移距离

**事件:**
- `@close`: 关闭时触发
- `@click`: 点击时触发

**使用示例:**
```vue
<script setup>
import { ref } from 'vue'
import { WkNotification, WkButton } from '@/components/common'

const showNotif = ref(false)

const notify = () => {
  showNotif.value = true
}
</script>

<template>
  <WkButton @click="notify">Show Notification</WkButton>
  
  <!-- 基础用法 -->
  <WkNotification
    v-if="showNotif"
    type="success"
    title="Success"
    message="Operation completed successfully!"
    :duration="3000"
    @close="showNotif = false"
  />
  
  <!-- 不同位置 -->
  <WkNotification
    type="info"
    title="Info"
    message="This appears at top-left"
    position="top-left"
  />
  
  <!-- 不自动关闭 -->
  <WkNotification
    type="warning"
    title="Warning"
    message="This stays until manually closed"
    :duration="0"
  />
</template>
```

---

### 8. WkLoading - 加载组件

加载状态组件，支持全局和局部加载。

**Props:**
- `loading`: 是否显示加载
- `text`: 加载文本
- `size`: 加载器大小 (`sm` | `md` | `lg`)
- `fullscreen`: 是否全屏
- `background`: 背景色
- `spinner`: 自定义spinner图标

**使用示例:**
```vue
<script setup>
import { ref } from 'vue'
import { WkLoading, WkCard } from '@/components/common'

const loading = ref(true)
</script>

<template>
  <!-- 局部加载 -->
  <WkCard class="relative min-h-[200px]">
    <WkLoading :loading="loading" text="Loading data..." />
    <div v-if="!loading">Content here</div>
  </WkCard>
  
  <!-- 全屏加载 -->
  <WkLoading :loading="loading" fullscreen text="Please wait..." />
  
  <!-- 不同大小 -->
  <WkLoading :loading="true" size="sm" />
  <WkLoading :loading="true" size="md" />
  <WkLoading :loading="true" size="lg" />
  
  <!-- 自定义spinner -->
  <WkLoading 
    :loading="true" 
    spinner="i-tabler-refresh" 
    text="Refreshing..."
  />
</template>
```

---

### 9. WkConfirmModal - 确认对话框

渲染Markdown内容为HTML，支持语法高亮。

**Props:**
- `content`: Markdown内容
- `class`: 额外的CSS类名

---

## 🎨 主题变量

所有组件都使用CSS变量以支持主题切换：

```css
--brand-default: 主品牌色
--bg-raised: 卡片背景色
--bg-surface: 表面背景色
--bg-hover: 悬停背景色
--text-default: 默认文本色
--text-secondary: 次要文本色
--text-muted: 弱化文本色
--border-default: 默认边框色
```

## 📝 命名规范

所有组件统一使用 `Wk` 前缀，避免与原生HTML元素或第三方库冲突。

## 🔧 导入方式

```vue
<script setup>
// 单个导入
import { WkButton, WkInput } from '@/components/common'

// 或从具体文件导入
import WkButton from '@/components/common/WkButton.vue'
</script>
```
