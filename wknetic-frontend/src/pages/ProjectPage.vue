<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const { t } = useI18n()
</script>

<template>
  <div class="container-main py-8">
    <h1 class="text-2xl font-bold text-text mb-6">{{ t('pages.project') }}: {{ route.params.id }}</h1>
    <p class="text-text-secondary">{{ t('pages.project_coming_soon') }}</p>
  </div>
</template>
