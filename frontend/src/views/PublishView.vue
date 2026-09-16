<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listBoards, publishPost, type BoardVo } from '../api/forum'
import { useNarrowScreen } from '../composables/useNarrowScreen'
import SvgIcon from '../components/SvgIcon.vue'

const route = useRoute()
const router = useRouter()

const narrow = useNarrowScreen()
const boards = ref<BoardVo[]>([])
const submitting = ref(false)
const mdInput = ref<{ textarea?: HTMLTextAreaElement; $el?: HTMLElement } | null>(null)
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

function textareaEl(): HTMLTextAreaElement | null {
  return mdInput.value?.textarea ?? mdInput.value?.$el?.querySelector('textarea') ?? null
}

/** 在光标处包裹 / 插入 Markdown 片段，并恢复焦点与选区 */
function wrap(before: string, after = '', placeholder = '') {
  const el = textareaEl()
  if (!el) {
    return
  }
  const { selectionStart: start, selectionEnd: end } = el
  const selected = form.contentMd.slice(start, end) || placeholder
  form.contentMd = form.contentMd.slice(0, start) + before + selected + after + form.contentMd.slice(end)
  nextTick(() => {
    el.focus()
    const caret = start + before.length + selected.length
    el.setSelectionRange(caret, caret)
  })
}

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
  <el-row :gutter="12">
    <el-col :xs="24" :md="16">
      <el-card shadow="never">
        <div class="flex gap-3 max-md:flex-col max-md:gap-2">
          <div class="w-[140px] shrink-0 max-md:w-full">
            <el-select v-model="form.boardCode" placeholder="选择版块">
              <el-option v-for="b in boards" :key="b.code" :label="b.name" :value="b.code" />
            </el-select>
          </div>
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="一句话说清问题或主题（1~100 字）" />
        </div>

        <div class="mt-3 flex flex-wrap items-center gap-1 rounded-md bg-code px-2 py-1" role="toolbar" aria-label="Markdown 工具条">
          <!-- EP 图标库无粗体 / 斜体 / 代码图标，沿用编辑器通行的字符写法（B / I / </>） -->
          <button type="button" class="cl-md-tool font-medium" title="加粗" aria-label="加粗" @click="wrap('**', '**', '加粗文字')">B</button>
          <button type="button" class="cl-md-tool italic" title="斜体" aria-label="斜体" @click="wrap('*', '*', '斜体文字')">I</button>
          <span class="mx-0.5 h-4 w-px bg-line" aria-hidden="true" />
          <button
            type="button"
            class="cl-md-tool font-mono text-caption"
            title="代码块"
            aria-label="代码块"
            @click="wrap('```\n', '\n```', '代码')"
          >
            &lt;/&gt;
          </button>
          <button type="button" class="cl-md-tool" title="无序列表" aria-label="无序列表" @click="wrap('- ', '', '列表项')">
            <SvgIcon name="list" :size="15" />
          </button>
          <button type="button" class="cl-md-tool" title="插入链接" aria-label="插入链接" @click="wrap('[', '](https://)', '链接文字')">
            <SvgIcon name="link" :size="15" />
          </button>
          <button
            type="button"
            class="cl-md-tool"
            disabled
            title="图片上传即将开放（暂无上传接口）"
            aria-label="图片（暂未开放）"
          >
            <SvgIcon name="photo" :size="15" />
          </button>
        </div>

        <el-input
          ref="mdInput"
          v-model="form.contentMd"
          type="textarea"
          :rows="narrow ? 8 : 14"
          maxlength="50000"
          show-word-limit
          resize="none"
          placeholder="支持 Markdown：标题 / 列表 / 代码块（```语言）/ 链接 / 引用"
        />

        <div class="mt-3 flex flex-wrap items-center justify-between gap-3">
          <span class="text-caption text-ink-meta">发布前将进行内容安全机审（F-SAFE-001）</span>
          <el-button type="primary" :loading="submitting" @click="submit">发布帖子</el-button>
        </div>
      </el-card>
    </el-col>

    <el-col :xs="24" :md="8">
      <el-card shadow="never">
        <template #header>
          <span class="font-medium">预览</span>
        </template>
        <p class="text-note text-ink-regular">
          暂无实时预览：按 ADR-005，Markdown 只在服务端渲染并做安全净化，前端不做二次渲染。
        </p>
        <p class="mt-2 text-note text-ink-meta">发布成功后可在帖子详情页查看渲染结果。</p>
      </el-card>
    </el-col>
  </el-row>
</template>
