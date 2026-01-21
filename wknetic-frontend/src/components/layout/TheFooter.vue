<script setup lang="ts">
const footerLinks = {
  product: [
    { name: 'WkNetic App', href: '#' },
    { name: 'Servers', href: '#' },
    { name: 'Blog', href: '#' },
    { name: 'Changelog', href: '#' },
  ],
  resources: [
    { name: 'Support', href: '#' },
    { name: 'Documentation', href: '#' },
    { name: 'API', href: '#' },
    { name: 'Status', href: '#' },
  ],
  company: [
    { name: 'About', href: '#' },
    { name: 'Careers', href: '#' },
    { name: 'Terms', href: '#' },
    { name: 'Privacy', href: '#' },
  ],
}

const socialLinks = [
  { name: 'GitHub', icon: 'i-tabler-brand-github', href: 'https://github.com/wekyjay/WkNetic' },
  { name: 'Discord', icon: 'i-tabler-brand-discord', href: '#' },
  { name: 'Twitter', icon: 'i-tabler-brand-twitter', href: '#' },
  { name: 'Mastodon', icon: 'i-tabler-brand-mastodon', href: '#' },
]
</script>

<template>
  <footer class="bg-bg-raised border-t border-border mt-auto">
    <div class="container-main py-12 lg:py-16">
      <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-8 lg:gap-12">
        <!-- Logo & 描述 -->
        <div class="col-span-2 md:col-span-4 lg:col-span-1">
          <div class="flex items-center gap-2 mb-4">
            <div class="w-8 h-8 bg-brand rounded-lg flex-center">
              <span class="i-tabler-cube text-bg text-lg"></span>
            </div>
            <span class="text-xl font-bold">WkNetic</span>
          </div>
          <p class="text-text-secondary text-sm mb-6 max-w-xs">
            Discover and share Minecraft content through our open-source platform built for the community.
          </p>
          <!-- 社交链接 -->
          <div class="flex items-center gap-3">
            <a
                v-for="social in socialLinks"
                :key="social.name"
                :href="social.href"
                :title="social.name"
                class="w-9 h-9 rounded-lg bg-bg-surface hover:bg-bg-hover flex-center text-text-secondary hover:text-text transition-colors"
                target="_blank"
                rel="noopener"
            >
              <span :class="social.icon" class="text-lg"></span>
            </a>
          </div>
        </div>

        <!-- 链接组 -->
        <div>
          <h4 class="font-semibold text-text mb-4">Product</h4>
          <ul class="space-y-3">
            <li v-for="link in footerLinks.product" :key="link.name">
              <a :href="link.href" class="text-text-secondary hover:text-brand text-sm transition-colors">
                {{ link.name }}
              </a>
            </li>
          </ul>
        </div>

        <div>
          <h4 class="font-semibold text-text mb-4">Resources</h4>
          <ul class="space-y-3">
            <li v-for="link in footerLinks.resources" :key="link.name">
              <a :href="link.href" class="text-text-secondary hover:text-brand text-sm transition-colors">
                {{ link.name }}
              </a>
            </li>
          </ul>
        </div>

        <div>
          <h4 class="font-semibold text-text mb-4">Company</h4>
          <ul class="space-y-3">
            <li v-for="link in footerLinks.company" :key="link.name">
              <a :href="link.href" class="text-text-secondary hover:text-brand text-sm transition-colors">
                {{ link.name }}
              </a>
            </li>
          </ul>
        </div>
      </div>

      <!-- 底部版权 -->
      <div class="mt-12 pt-8 border-t border-border flex flex-col sm:flex-row items-center justify-between gap-4">
        <p class="text-text-muted text-sm">
          &copy; {{ new Date().getFullYear() }} WkNetic. Open source under the AGPL-3.0 license.
        </p>
        <div class="flex items-center gap-4 text-sm text-text-muted">
          <a href="#" class="hover:text-text transition-colors">Terms</a>
          <a href="#" class="hover:text-text transition-colors">Privacy</a>
          <a href="#" class="hover:text-text transition-colors">Cookies</a>
        </div>
      </div>
    </div>
  </footer>
</template>
