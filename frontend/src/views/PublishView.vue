<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listBoards, publishPost, type BoardVo } from '../api/forum'

const route = useRoute()
const router = useRouter()

const boards = ref<BoardVo[]>([])
const submitting = ref(false)
const form = reactive({
  boardCode: '',
  title: '',
  contentMd: '',
})

onMounted(async () => {
  try {
    boards.value = await listBoards()
    const fromQuery = String(route.query.board || '')
    form.boardCode = boards.value.some((b) => b.code === fromQuery) ? fromQuery : boards.value[0]?.code || ''
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '版块加载失败')
  }
})

async function submit() {
  if (!form.boardCode) {
    ElMessage.warning('请选择版块')
    return
  }
  if (!form.title.trim()) {
    ElMessage.warning('请填写标题')
    return
  }
  if (!form.contentMd.trim()) {
    ElMessage.warning('请填写正文')
    return
  }
  submitting.value = true
  try {
    const res = await publishPost({
      boardCode: form.boardCode,
      title: form.title.trim(),
      contentMd: form.contentMd,
    })
    ElMessage.success('发布成功')
    router.push(`/post/${res.id}`)
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '发布失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-card shadow="never" class="mx-auto max-w-[860px]">
    <template #header>
      <span class="font-medium">发布帖子</span>
    </template>
    <el-form label-position="top" @submit.prevent>
      <el-form-item label="版块">
        <div class="w-[260px]">
          <el-select v-model="form.boardCode" placeholder="选择版块">
            <el-option v-for="b in boards" :key="b.code" :label="b.name" :value="b.code">
              <span>{{ b.name }}</span>
              <span class="float-right ml-4 text-caption text-ink-meta">{{ b.description }}</span>
            </el-option>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="一句话说清问题或主题（1~100 字）" />
      </el-form-item>
      <el-form-item label="正文">
        <el-input
          v-model="form.contentMd"
          type="textarea"
          :rows="14"
          maxlength="50000"
          show-word-limit
          placeholder="支持 Markdown：标题 / 列表 / 代码块（```语言）/ 链接 / 引用"
        />
      </el-form-item>
      <el-button type="primary" :loading="submitting" @click="submit">发布</el-button>
      <span class="ml-3 text-caption text-ink-meta">发布后正文会被渲染为 HTML 并做安全净化，XSS 防线在服务端。</span>
    </el-form>
  </el-card>
</template>
