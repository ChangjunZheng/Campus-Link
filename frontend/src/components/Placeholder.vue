<script setup lang="ts">
import { Document } from '@element-plus/icons-vue'

defineProps<{ title?: string; desc?: string }>()
</script>

<template>
  <el-empty :description="desc || '该页面将在后续 Sprint 实现，当前为占位（见 docs/development/sprint-1.md）'">
    <template #image>
      <el-icon :size="60"><Document /></el-icon>
    </template>
    <h2>{{ title || '建设中' }}</h2>
  </el-empty>
</template>
