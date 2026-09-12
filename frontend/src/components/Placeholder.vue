<script setup lang="ts">
import { Document } from '@element-plus/icons-vue'

defineProps<{ title?: string; desc?: string }>()
</script>

<template>
  <!-- 不传 description：EP 的 description 固定排在默认插槽之上，会把页面名压到提示语下面 -->
  <el-empty>
    <template #image>
      <el-icon :size="60"><Document /></el-icon>
    </template>
    <h2 class="text-title font-medium text-ink">{{ title || '建设中' }}</h2>
    <p class="mt-1.5 text-note text-ink-meta">{{ desc || '该页面还在建设中，敬请期待' }}</p>
  </el-empty>
</template>
