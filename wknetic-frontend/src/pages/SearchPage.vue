<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <div class="container-main py-8">
    <h1 class="text-2xl font-bold text-text mb-6">{{ t('pages.search_mods') }}</h1>
    <p class="text-text-secondary">{{ t('pages.search_content_coming') }}</p>
  </div>
</template>
