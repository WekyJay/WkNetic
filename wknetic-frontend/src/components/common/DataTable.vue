<template>
  <div class="data-table-wrapper">
    <!-- 表格容器 -->
    <div class="table-container bg-[var(--bg-raised)] rounded-lg shadow-sm overflow-hidden border border-[var(--border-default)]">
      <table class="w-full">
        <!-- 表头 -->
        <thead class="bg-[var(--bg-surface)] border-b border-[var(--border-default)]">
          <tr>
            <th
              v-for="column in columns"
              :key="column.key"
              class="px-6 py-3 text-left text-xs font-medium text-[var(--text-secondary)] uppercase tracking-wider"
              :class="{ 'cursor-pointer hover:bg-[var(--bg-hover)]': column.sortable }"
              @click="column.sortable && handleSort(column.key)"
            >
              <div class="flex items-center gap-2">
                {{ column.label }}
                <span v-if="column.sortable && sortKey === column.key" class="text-[var(--brand-default)]">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </div>
            </th>
            <th
              v-if="$slots.actions"
              class="px-6 py-3 text-right text-xs font-medium text-[var(--text-secondary)] uppercase tracking-wider"
            >
              {{ actionsLabel || 'Actions' }}
            </th>
          </tr>
        </thead>

        <!-- 表体 -->
        <tbody v-if="!loading && sortedData.length > 0" class="divide-y divide-[var(--border-default)]">
          <tr
            v-for="(row, index) in paginatedData"
            :key="getRowKey(row, index)"
            class="hover:bg-[var(--bg-hover)] transition-colors"
          >
            <td
              v-for="column in columns"
              :key="column.key"
              class="px-6 py-4 whitespace-nowrap text-sm text-[var(--text-default)]"
            >
              <!-- 自定义列插槽 -->
              <slot v-if="$slots[`column-${column.key}`]" :name="`column-${column.key}`" :row="row" :value="row[column.key]">
              </slot>
              <!-- 默认显示 -->
              <span v-else>{{ formatValue(row[column.key], column) }}</span>
            </td>
            <!-- 操作列 -->
            <td v-if="$slots.actions" class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <slot name="actions" :row="row"></slot>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex items-center justify-center py-16">
        <div class="flex flex-col items-center gap-3">
          <div class="w-8 h-8 border-4 border-[var(--brand-default)] border-t-transparent rounded-full animate-spin"></div>
          <span class="text-sm text-[var(--text-secondary)]">{{ loadingText || 'Loading...' }}</span>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && sortedData.length === 0" class="flex flex-col items-center justify-center py-16">
        <div class="text-6xl text-[var(--text-muted)] mb-4">
          <slot name="empty-icon">
            <i class="i-tabler-database-off"></i>
          </slot>
        </div>
        <p class="text-[var(--text-secondary)] text-sm">{{ emptyText || 'No data available' }}</p>
      </div>
    </div>

    <!-- 分页器 -->
    <div
      v-if="pagination && sortedData.length > 0"
      class="flex items-center justify-between mt-4 px-2"
    >
      <div class="text-sm text-[var(--text-secondary)]">
        Showing {{ (currentPage - 1) * pageSize + 1 }} to {{ Math.min(currentPage * pageSize, sortedData.length) }} of {{ sortedData.length }} entries
      </div>

      <div class="flex items-center gap-2">
        <button
          class="px-3 py-1 rounded border border-[var(--border-default)] bg-[var(--bg-raised)] text-[var(--text-default)] hover:bg-[var(--bg-hover)] disabled:opacity-50 disabled:cursor-not-allowed text-sm"
          :disabled="currentPage === 1"
          @click="changePage(currentPage - 1)"
        >
          Previous
        </button>

        <div class="flex gap-1">
          <button
            v-for="page in displayedPages"
            :key="page"
            class="w-8 h-8 rounded border text-sm transition-colors"
            :class="
              page === currentPage
                ? 'bg-[var(--brand-default)] text-white border-[var(--brand-default)]'
                : 'border-[var(--border-default)] bg-[var(--bg-raised)] text-[var(--text-default)] hover:bg-[var(--bg-hover)]'
            "
            @click="changePage(page)"
          >
            {{ page }}
          </button>
        </div>

        <button
          class="px-3 py-1 rounded border border-[var(--border-default)] bg-[var(--bg-raised)] text-[var(--text-default)] hover:bg-[var(--bg-hover)] disabled:opacity-50 disabled:cursor-not-allowed text-sm"
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
        >
          Next
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

export interface Column {
  key: string
  label: string
  sortable?: boolean
  formatter?: (value: any) => string
}

interface Props {
  columns: Column[]
  data: any[]
  loading?: boolean
  emptyText?: string
  loadingText?: string
  actionsLabel?: string
  pagination?: boolean
  pageSize?: number
  rowKey?: string | ((row: any) => string | number)
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  emptyText: 'No data available',
  loadingText: 'Loading...',
  actionsLabel: 'Actions',
  pagination: true,
  pageSize: 10,
  rowKey: 'id'
})

// 排序
const sortKey = ref<string>('')
const sortOrder = ref<'asc' | 'desc'>('asc')

const handleSort = (key: string) => {
  if (sortKey.value === key) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortOrder.value = 'asc'
  }
}

const sortedData = computed(() => {
  if (!sortKey.value) return props.data

  return [...props.data].sort((a, b) => {
    const aVal = a[sortKey.value]
    const bVal = b[sortKey.value]

    if (aVal === bVal) return 0

    const comparison = aVal > bVal ? 1 : -1
    return sortOrder.value === 'asc' ? comparison : -comparison
  })
})

// 分页
const currentPage = ref(1)

const totalPages = computed(() => {
  return Math.ceil(sortedData.value.length / props.pageSize)
})

const paginatedData = computed(() => {
  if (!props.pagination) return sortedData.value

  const start = (currentPage.value - 1) * props.pageSize
  const end = start + props.pageSize
  return sortedData.value.slice(start, end)
})

const displayedPages = computed(() => {
  const pages = []
  const maxDisplay = 5
  const half = Math.floor(maxDisplay / 2)

  let start = Math.max(1, currentPage.value - half)
  let end = Math.min(totalPages.value, start + maxDisplay - 1)

  if (end - start < maxDisplay - 1) {
    start = Math.max(1, end - maxDisplay + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

const changePage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

// 工具函数
const getRowKey = (row: any, index: number): string | number => {
  if (typeof props.rowKey === 'function') {
    return props.rowKey(row)
  }
  return row[props.rowKey] ?? index
}

const formatValue = (value: any, column: Column): string => {
  if (column.formatter) {
    return column.formatter(value)
  }
  if (value === null || value === undefined) {
    return '-'
  }
  return String(value)
}
</script>

<style scoped>
.data-table-wrapper {
  width: 100%;
}

.table-container {
  overflow-x: auto;
}

table {
  border-collapse: collapse;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
