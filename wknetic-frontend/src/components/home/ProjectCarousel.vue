<script setup lang="ts">
import ProjectCard from './ProjectCard.vue'

// User testimonials data
const projectsRow1 = [
  { id: 1, name: 'Alex Chen', description: 'This platform is absolutely amazing! Clean interface, powerful features, and it has significantly boosted my productivity 👍', icon: '😊', downloads: '2 hours ago', category: 'Review' },
  { id: 2, name: 'James Miller', description: 'Been using it for a while now, overall experience is excellent, especially the auto-sync feature - so convenient!', icon: '🌟', downloads: '5 hours ago', category: 'Recommend' },
  { id: 3, name: 'Sarah Wang', description: 'Amazing platform! The user interface is intuitive and the features are exactly what I needed.', icon: '💝', downloads: 'Yesterday', category: 'Review' },
  { id: 4, name: 'Robert Johnson', description: 'Customer support responds very quickly, any issues I had were resolved promptly. Thumbs up!', icon: '👏', downloads: '2 days ago', category: 'Service' },
  { id: 5, name: 'DevMaster', description: 'As a developer, I really appreciate the technical architecture and code quality of this project. Learned so much!', icon: '💻', downloads: '3 days ago', category: 'Technical' },
  { id: 6, name: 'Emily Davis', description: 'The interface design is beautiful, the color scheme and animations are so pleasant. Using it makes me happy!', icon: '🎨', downloads: '4 days ago', category: 'Design' },
  { id: 7, name: 'John Smith', description: 'Great work on the performance optimization! Everything runs smoothly now.', icon: '⚡', downloads: '5 days ago', category: 'Performance' },
  { id: 8, name: 'Michael Brown', description: 'Very comprehensive features, basically meets all my needs. Keep up the great work!', icon: '🔥', downloads: '1 week ago', category: 'Features' },
]

const projectsRow2 = [
  { id: 9, name: 'Emma Liu', description: 'Love the new features! Keep up the excellent work, team!', icon: '❤️', downloads: '1 week ago', category: 'Feedback' },
  { id: 10, name: 'Daniel Martinez', description: 'Mobile adaptation is excellent, runs smoothly on my phone too!', icon: '📱', downloads: '1 week ago', category: 'Experience' },
  { id: 11, name: 'CodeNinja', description: 'Documentation is very detailed, API design is well thought out. Integration was smooth!', icon: '📚', downloads: '2 weeks ago', category: 'Documentation' },
  { id: 12, name: 'Ryan Taylor', description: 'Fast, powerful, and beautiful interface. In three words: absolutely perfect!', icon: '🐵', downloads: '2 weeks ago', category: 'Review' },
  { id: 13, name: 'Lisa Park', description: 'The community here is so helpful and friendly. Thanks everyone!', icon: '🤝', downloads: '2 weeks ago', category: 'Community' },
  { id: 14, name: 'Kevin Anderson', description: 'Music playback feature is awesome, clear audio quality and fast loading speed!', icon: '🎵', downloads: '3 weeks ago', category: 'Music' },
  { id: 15, name: 'Chris Wilson', description: 'Been using it for three months now, very stable, never had any issues!', icon: '✅', downloads: '3 weeks ago', category: 'Stability' },
  { id: 16, name: 'Mike Zhang', description: 'Excellent product! Highly recommend it to all my friends and colleagues.', icon: '🌈', downloads: '3 weeks ago', category: 'Recommend' },
]

const projectsRow3 = [
  { id: 17, name: 'Brian Thomas', description: 'Team collaboration features are very practical. Using it with colleagues, productivity has improved significantly!', icon: '👥', downloads: '1 month ago', category: 'Collaboration' },
  { id: 18, name: 'Sophie Chen', description: 'The dark mode is perfect! My eyes are finally happy during late night coding sessions.', icon: '🌙', downloads: '1 month ago', category: 'Feature' },
  { id: 19, name: 'Jason Garcia', description: 'Data security is well implemented, can use it with complete confidence!', icon: '🔒', downloads: '1 month ago', category: 'Security' },
  { id: 20, name: 'Matthew Lee', description: 'Simple and easy to use, even beginners can master it quickly!', icon: '🎯', downloads: '1 month ago', category: 'Usability' },
  { id: 21, name: 'David Kim', description: 'Best decision I made this year! This tool has become essential for my daily workflow.', icon: '⭐', downloads: '1 month ago', category: 'Review' },
  { id: 22, name: 'Andrew White', description: 'Smart recommendation algorithm is very accurate, always finds exactly what I need!', icon: '🧠', downloads: '2 months ago', category: 'Intelligence' },
  { id: 23, name: 'Jessica Harris', description: 'The color scheme is gentle and comfortable, every detail is so well thought out!', icon: '🌸', downloads: '2 months ago', category: 'Design' },
  { id: 24, name: 'Tom Wilson', description: 'Outstanding support team! They solved my issue within minutes. Five stars!', icon: '⭐', downloads: '2 months ago', category: 'Support' },
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
