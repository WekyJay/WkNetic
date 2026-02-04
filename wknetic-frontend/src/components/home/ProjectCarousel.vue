<script setup lang="ts">
import ProjectCard from './ProjectCard.vue'

// Community highlights data
const projectsRow1 = [
  { id: 1, name: 'Skyblock Academy', description: 'Step-by-step island starter guide with layouts, farms, and economy tips.', icon: '🏝️', downloads: '2 hours ago', category: 'Build Guide' },
  { id: 2, name: 'Redstone Bootcamp', description: 'Compact redstone tutorials from basics to advanced clocks.', icon: '🔴', downloads: '5 hours ago', category: 'Tutorial' },
  { id: 3, name: 'Aurora SMP', description: 'Recruiting builders and storytellers for a seasonal SMP.', icon: '🌌', downloads: 'Yesterday', category: 'Server Recruiting' },
  { id: 4, name: 'Medieval City Pack', description: 'Shared schematics, palettes, and build styles for towns.', icon: '🏰', downloads: '2 days ago', category: 'Resource Share' },
  { id: 5, name: 'Weekend Build Jam', description: 'Theme: Floating villages. Submit before Sunday night!', icon: '🏗️', downloads: '3 days ago', category: 'Event' },
  { id: 6, name: 'Nether Hub Workshop', description: 'Community checklist for safe, stylish hub designs.', icon: '🔥', downloads: '4 days ago', category: 'Build Guide' },
  { id: 7, name: 'Survival Server Hub', description: 'Looking for traders, builders, and event hosts.', icon: '🧭', downloads: '5 days ago', category: 'Server Recruiting' },
  { id: 8, name: 'Shader-Friendly Builds', description: 'Lighting and material tips for cinematic screenshots.', icon: '✨', downloads: '1 week ago', category: 'Tutorial' },
]

const projectsRow2 = [
  { id: 9, name: 'Creative Trade Hub', description: 'Share trade hall layouts, villager presets, and signage packs.', icon: '📦', downloads: '1 week ago', category: 'Resource Share' },
  { id: 10, name: 'Parkour League', description: 'Weekly time-trial maps and community leaderboards.', icon: '⏱️', downloads: '1 week ago', category: 'Event' },
  { id: 11, name: 'Server Starter Kit', description: 'Rules templates, onboarding checklists, and spawn guides.', icon: '📚', downloads: '2 weeks ago', category: 'Server Recruiting' },
  { id: 12, name: 'Village Builder Tips', description: 'Compact homes, pathing patterns, and biome palettes.', icon: '🏘️', downloads: '2 weeks ago', category: 'Build Guide' },
  { id: 13, name: 'Adventure Map Collab', description: 'Looking for writers, builders, and redstone scripters.', icon: '🗺️', downloads: '2 weeks ago', category: 'Server Recruiting' },
  { id: 14, name: 'Resource Pack Swap', description: 'Post and review texture packs for themed seasons.', icon: '🎨', downloads: '3 weeks ago', category: 'Resource Share' },
  { id: 15, name: 'Circuit Sunday', description: 'Live redstone demos and Q&A with creators.', icon: '⚙️', downloads: '3 weeks ago', category: 'Event' },
  { id: 16, name: 'Castle Siege Arena', description: 'PvP build guide with schematic and materials list.', icon: '🛡️', downloads: '3 weeks ago', category: 'Build Guide' },
]

const projectsRow3 = [
  { id: 17, name: 'Season 4 Build Cup', description: 'Theme: Cliffside ports. Teams assemble and compete.', icon: '🏆', downloads: '1 month ago', category: 'Event' },
  { id: 18, name: 'Spawner Farm Blueprint', description: 'Step-by-step tutorial with rates and safety notes.', icon: '🧪', downloads: '1 month ago', category: 'Build Guide' },
  { id: 19, name: 'Server Showcase: Emberfall', description: 'Tour the spawn and apply to join the build team.', icon: '🌋', downloads: '1 month ago', category: 'Server Recruiting' },
  { id: 20, name: 'Biome Palette Library', description: 'Shared color palettes and block lists by biome.', icon: '🌿', downloads: '1 month ago', category: 'Resource Share' },
  { id: 21, name: 'Hardcore Event Night', description: 'Weekly co-op runs with prizes and highlights.', icon: '⚔️', downloads: '1 month ago', category: 'Event' },
  { id: 22, name: 'Marketplace Toolkit', description: 'Stall templates, pricing guides, and signage sets.', icon: '🛒', downloads: '2 months ago', category: 'Resource Share' },
  { id: 23, name: 'Skybridge Training', description: 'PvP movement drills and tutorial maps.', icon: '🧱', downloads: '2 months ago', category: 'Build Guide' },
  { id: 24, name: 'Community Notice Board', description: 'Pinned recruitment posts and event calendars.', icon: '📌', downloads: '2 months ago', category: 'Server Recruiting' },
]
</script>

<template>
  <section class="py-8 overflow-hidden">
    <!-- 第一行 - 向左滚动 -->
    <div class="relative mb-4 overflow-hidden">
      <div class="flex gap-4 animate-scroll-left" style="--duration: 60s;">
        <ProjectCard v-for="(project, index) in [...projectsRow1, ...projectsRow1, ...projectsRow1]" :key="`row1-${project.id}-${index}`" :project="project" />
      </div>
    </div>

    <!-- 第二行 - 向右滚动 -->
    <div class="relative mb-4 overflow-hidden">
      <div class="flex gap-4 animate-scroll-right" style="--duration: 55s;">
        <ProjectCard v-for="(project, index) in [...projectsRow2, ...projectsRow2, ...projectsRow2]" :key="`row2-${project.id}-${index}`" :project="project" />
      </div>
    </div>

    <!-- 第三行 - 向左滚动 -->
    <div class="relative overflow-hidden">
      <div class="flex gap-4 animate-scroll-left" style="--duration: 65s;">
        <ProjectCard v-for="(project, index) in [...projectsRow3, ...projectsRow3, ...projectsRow3]" :key="`row3-${project.id}-${index}`" :project="project" />
      </div>
    </div>

    <!-- 左右渐变遮罩 -->
    <div class="pointer-events-none absolute inset-y-0 left-0 w-32 bg-gradient-to-r from-bg to-transparent"></div>
    <div class="pointer-events-none absolute inset-y-0 right-0 w-32 bg-gradient-to-l from-bg to-transparent"></div>
  </section>
</template>

<style scoped>
@keyframes scroll-left {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-33.333%);
  }
}

@keyframes scroll-right {
  0% {
    transform: translateX(-33.333%);
  }
  100% {
    transform: translateX(0);
  }
}

.animate-scroll-left {
  animation: scroll-left var(--duration, 60s) linear infinite;
  width: max-content;
}

.animate-scroll-right {
  animation: scroll-right var(--duration, 60s) linear infinite;
  width: max-content;
}

.animate-scroll-left:hover,
.animate-scroll-right:hover {
  animation-play-state: paused;
}

section {
  position: relative;
}
</style>
