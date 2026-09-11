<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const code = computed(() => String(route.params.code || ''))

const names: Record<string, string> = {
  qna: '技术问答',
  resources: '学习资源',
  interview: '面经求职',
  contest: '竞赛交流',
  course: '课程交流',
  chat: '闲聊灌水',
}
</script>

<template>
  <el-card shadow="never">
    <h2>{{ names[code] || code }}</h2>
    <el-empty description="内容链路将在 Sprint 2 上线（发帖 / 列表 / 楼层回复），敬请期待" />
  </el-card>
</template>
