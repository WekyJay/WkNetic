<script setup lang="ts">
import ProjectCard from './ProjectCard.vue'

// 模拟项目数据
const projectsRow1 = [
  { id: 1, name: 'Sodium', description: 'A modern rendering engine for Minecraft which greatly improves performance', icon: '🧪', downloads: '15.2M', category: 'Performance' },
  { id: 2, name: 'Iris Shaders', description: 'A modern shaders mod for Minecraft intended to be compatible with existing OptiFine shaders', icon: '🌈', downloads: '12.8M', category: 'Shaders' },
  { id: 3, name: 'Fabric API', description: 'Lightweight and modular API providing common hooks and interoperability measures', icon: '🧵', downloads: '18.5M', category: 'Library' },
  { id: 4, name: 'Lithium', description: 'No-compromises game logic/server optimization mod', icon: '⚡', downloads: '8.3M', category: 'Performance' },
  { id: 5, name: 'Create', description: 'Aesthetic Technology that empowers the Player', icon: '⚙️', downloads: '9.1M', category: 'Technology' },
  { id: 6, name: 'JEI', description: 'Just Enough Items - View Items and Recipes', icon: '📦', downloads: '22.4M', category: 'Utility' },
  { id: 7, name: 'Terralith', description: 'Terralith takes Minecraft terrain generation to the next level', icon: '🏔️', downloads: '5.6M', category: 'Worldgen' },
  { id: 8, name: 'Complementary', description: 'Complementary Shaders for beautiful visuals', icon: '✨', downloads: '4.2M', category: 'Shaders' },
]

const projectsRow2 = [
  { id: 9, name: 'ModMenu', description: 'Adds a mod menu to view the list of mods you have installed', icon: '📋', downloads: '14.7M', category: 'Utility' },
  { id: 10, name: 'Xaeros Minimap', description: 'Displays a map of the nearby world terrain', icon: '🗺️', downloads: '11.2M', category: 'Utility' },
  { id: 11, name: 'Origins', description: 'Choose an origin at the beginning of your Minecraft journey', icon: '🎭', downloads: '6.8M', category: 'Adventure' },
  { id: 12, name: 'Better Combat', description: 'Easy, spectacular and fun melee combat system', icon: '⚔️', downloads: '3.9M', category: 'Combat' },
  { id: 13, name: 'Biomes O Plenty', description: 'Adds over 90 unique biomes to enhance your Minecraft world', icon: '🌿', downloads: '7.5M', category: 'Worldgen' },
  { id: 14, name: 'Applied Energistics', description: 'A mod about matter, energy and using them to conquer the world', icon: '💎', downloads: '5.1M', category: 'Technology' },
  { id: 15, name: 'Botania', description: 'A tech mod themed around natural magic', icon: '🌸', downloads: '4.8M', category: 'Magic' },
  { id: 16, name: 'Farmers Delight', description: 'A mod that gently expands upon farming and cooking', icon: '🍳', downloads: '6.2M', category: 'Food' },
]

const projectsRow3 = [
  { id: 17, name: 'Starlight', description: 'Rewrites the light engine to fix lighting performance and bugs', icon: '⭐', downloads: '7.9M', category: 'Performance' },
  { id: 18, name: 'Distant Horizons', description: 'See farther without turning your computer into a heater', icon: '🔭', downloads: '3.4M', category: 'Performance' },
  { id: 19, name: 'Immersive Engineering', description: 'A tech mod with a certain flair', icon: '🔧', downloads: '4.3M', category: 'Technology' },
  { id: 20, name: 'Alex Mobs', description: 'Adds many new mobs with interesting behaviors', icon: '🦎', downloads: '5.7M', category: 'Mobs' },
  { id: 21, name: 'Waystones', description: 'Teleport to waypoints you discovered in the world', icon: '🪨', downloads: '8.9M', category: 'Utility' },
  { id: 22, name: 'Macaws Doors', description: 'A simple mod that adds vanilla doors variants', icon: '🚪', downloads: '2.8M', category: 'Decoration' },
  { id: 23, name: 'Supplementaries', description: 'Many small vanilla-like additions to make your world feel more alive', icon: '🎪', downloads: '4.1M', category: 'Decoration' },
  { id: 24, name: 'Curios API', description: 'A flexible and expandable accessory/equipment API for users and developers', icon: '💍', downloads: '9.6M', category: 'Library' },
]
</script>

<template>
  <section class="py-8 overflow-hidden">
    <!-- 第一行 - 向左滚动 -->
    <div class="relative mb-4">
      <div class="flex gap-4 animate-scroll" style="--direction: -1; --duration: 60s;">
        <ProjectCard v-for="project in [...projectsRow1, ...projectsRow1]" :key="project.id + '-1-' + Math.random()" :project="project" />
      </div>
    </div>

    <!-- 第二行 - 向右滚动 -->
    <div class="relative mb-4">
      <div class="flex gap-4 animate-scroll" style="--direction: 1; --duration: 55s;">
        <ProjectCard v-for="project in [...projectsRow2, ...projectsRow2]" :key="project.id + '-2-' + Math.random()" :project="project" />
      </div>
    </div>

    <!-- 第三行 - 向左滚动 -->
    <div class="relative">
      <div class="flex gap-4 animate-scroll" style="--direction: -1; --duration: 65s;">
        <ProjectCard v-for="project in [...projectsRow3, ...projectsRow3]" :key="project.id + '-3-' + Math.random()" :project="project" />
      </div>
    </div>

    <!-- 左右渐变遮罩 -->
    <div class="pointer-events-none absolute inset-y-0 left-0 w-32 bg-gradient-to-r from-bg to-transparent"></div>
    <div class="pointer-events-none absolute inset-y-0 right-0 w-32 bg-gradient-to-l from-bg to-transparent"></div>
  </section>
</template>

<style scoped>
@keyframes scroll {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(calc(var(--direction) * 50%));
  }
}

.animate-scroll {
  animation: scroll var(--duration, 60s) linear infinite;
  width: max-content;
}

.animate-scroll:hover {
  animation-play-state: paused;
}

section {
  position: relative;
}
</style>
