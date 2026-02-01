<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useRouter, useRoute } from 'vue-router'

const { t } = useI18n()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const userId = computed(() => route.params.id as string)
const isOwnProfile = computed(() => {
  if (!authStore.user) return false
  return userId.value === authStore.user.userId?.toString() || userId.value === 'me'
})

const user = ref({
  id: '',
  username: '',
  nickname: '',
  email: '',
  avatar: '',
  bio: '',
  joinDate: '',
  location: '',
  website: '',
  posts: 0,
  followers: 0,
  following: 0
})

const isLoading = ref(false)
const isEditing = ref(false)
const editForm = ref({
  nickname: '',
  bio: '',
  location: '',
  website: '',
  avatar: ''
})

onMounted(() => {
  loadUserProfile()
})

const loadUserProfile = async () => {
  isLoading.value = true   
  try {
    if (isOwnProfile.value && authStore.user) {
      user.value = {
        ...authStore.user,
        id: authStore.user.userId?.toString() || '',
        posts: 0,
        followers: 0,
        following: 0
      }
    } else {
      // TODO: 从API加载其他用户的公开信息
      user.value = {
        id: userId.value,
        username: 'User' + userId.value,
        nickname: 'User ' + userId.value,
        email: '',
        avatar: '',
        bio: 'No bio yet',
        joinDate: new Date().toISOString(),
        location: '',
        website: '',
        posts: 42,
        followers: 123,
        following: 56
      }
    }
    editForm.value = {
      nickname: user.value.nickname,
      bio: user.value.bio,
      location: user.value.location,
      website: user.value.website,
      avatar: user.value.avatar
    }
  } finally {
    isLoading.value = false
  }
}

const startEdit = () => {
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  editForm.value = {
    nickname: user.value.nickname,
    bio: user.value.bio,
    location: user.value.location,
    website: user.value.website,
    avatar: user.value.avatar
  }
}

const saveProfile = async () => {
  try {
    // TODO: 调用API保存用户信息
    user.value = {
      ...user.value,
      nickname: editForm.value.nickname,
      bio: editForm.value.bio,
      location: editForm.value.location,
      website: editForm.value.website,
      avatar: editForm.value.avatar
    }
    isEditing.value = false
  } catch (error) {
    console.error('Failed to save profile:', error)
  }
}

const handleAvatarUpload = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      editForm.value.avatar = e.target?.result as string
    }
    reader.readAsDataURL(file)
  }
}
</script>

<template>
  <div class="min-h-screen bg-bg">
    <!-- 用户封面 -->
    <div class="h-32 bg-gradient-to-r from-brand/20 to-brand/5 border-b border-border"></div>

    <div class="container-main">
      <!-- 用户信息卡片 -->
      <div class="max-w-4xl mx-auto -mt-16 mb-6">
        <div class="card flex gap-6 items-start">
          <!-- 头像 -->
          <div class="flex-shrink-0">
            <img 
              v-if="user.avatar"
              :src="user.avatar"
              :alt="user.nickname"
              class="w-32 h-32 rounded-lg object-cover border-4 border-bg"
            />
            <div 
              v-else
              class="w-32 h-32 rounded-lg bg-brand/10 flex-center border-4 border-bg"
            >
              <span class="i-tabler-user text-4xl text-brand"></span>
            </div>
          </div>

          <!-- 用户信息 -->
          <div class="flex-1 pt-4">
            <div v-if="!isEditing" class="space-y-2">
              <h1 class="text-3xl font-bold text-text">{{ user.nickname }}</h1>
              <p class="text-text-secondary">@{{ user.username }}</p>
              <p v-if="user.bio" class="text-text-muted mt-4">{{ user.bio }}</p>
              
              <div class="flex flex-wrap gap-4 mt-4 text-sm text-text-muted">
                <div v-if="user.location" class="flex items-center gap-1">
                  <span class="i-tabler-map-pin"></span>
                  {{ user.location }}
                </div>
                <div v-if="user.website" class="flex items-center gap-1">
                  <span class="i-tabler-link"></span>
                  <a :href="user.website" target="_blank" class="text-brand hover:underline">
                    {{ user.website }}
                  </a>
                </div>
                <div class="flex items-center gap-1">
                  <span class="i-tabler-calendar"></span>
                  Joined {{ new Date(user.joinDate).toLocaleDateString() }}
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="flex gap-2 mt-6">
                <button 
                  v-if="isOwnProfile"
                  class="btn-primary"
                  @click="startEdit"
                >
                  <span class="i-tabler-edit"></span>
                  Edit Profile
                </button>
                <button v-else class="btn-secondary">
                  <span class="i-tabler-user-plus"></span>
                  Follow
                </button>
              </div>
            </div>

            <!-- 编辑表单 -->
            <div v-else class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-text mb-2">Nickname</label>
                <input 
                  v-model="editForm.nickname"
                  type="text"
                  class="w-full input-base"
                  placeholder="Your nickname"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-text mb-2">Bio</label>
                <textarea 
                  v-model="editForm.bio"
                  class="w-full input-base resize-none"
                  placeholder="Tell us about yourself"
                  rows="3"
                ></textarea>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-text mb-2">Location</label>
                  <input 
                    v-model="editForm.location"
                    type="text"
                    class="w-full input-base"
                    placeholder="Your location"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-text mb-2">Website</label>
                  <input 
                    v-model="editForm.website"
                    type="url"
                    class="w-full input-base"
                    placeholder="https://example.com"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-text mb-2">Avatar</label>
                <div class="flex gap-4 items-end">
                  <img 
                    v-if="editForm.avatar"
                    :src="editForm.avatar"
                    alt="preview"
                    class="w-16 h-16 rounded-lg object-cover border border-border"
                  />
                  <input 
                    type="file"
                    accept="image/*"
                    class="flex-1 input-base"
                    @change="handleAvatarUpload"
                  />
                </div>
              </div>

              <div class="flex gap-2 pt-4">
                <button class="btn-primary" @click="saveProfile">
                  <span class="i-tabler-check"></span>
                  Save
                </button>
                <button class="btn-secondary" @click="cancelEdit">
                  <span class="i-tabler-x"></span>
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 统计数据 -->
      <div class="max-w-4xl mx-auto grid grid-cols-3 gap-4 mb-8">
        <div class="card text-center py-4">
          <div class="text-2xl font-bold text-brand">{{ user.posts }}</div>
          <div class="text-sm text-text-muted">Posts</div>
        </div>
        <div class="card text-center py-4">
          <div class="text-2xl font-bold text-brand">{{ user.followers }}</div>
          <div class="text-sm text-text-muted">Followers</div>
        </div>
        <div class="card text-center py-4">
          <div class="text-2xl font-bold text-brand">{{ user.following }}</div>
          <div class="text-sm text-text-muted">Following</div>
        </div>
      </div>

      <!-- 用户活动（占位符） -->
      <div class="max-w-4xl mx-auto card py-8 text-center">
        <span class="i-tabler-inbox text-4xl text-text-muted mb-4 block"></span>
        <p class="text-text-muted">No activity yet</p>
      </div>
    </div>
  </div>
</template>
