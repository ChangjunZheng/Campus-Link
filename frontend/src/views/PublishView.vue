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
const contentEl = ref<HTMLTextAreaElement | null>(null)
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

/** 在光标处包裹 / 插入 Markdown 片段，并恢复焦点与选区 */
function wrap(before: string, after = '', placeholder = '') {
  const el = contentEl.value
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
  <!-- 沉浸式编辑器（CR-073，参考 CSDN / 掘金）：单栏大 sheet，标题超大无边框、工具条通栏、正文大行距 -->
  <div class="overflow-hidden rounded-lg border-[0.5px] border-line bg-card">
    <!-- 顶栏：版块选择 + 字数 + 发布（掘金式极简头） -->
    <div class="flex items-center gap-3 border-b-[0.5px] border-line px-5 py-3">
      <el-select v-model="form.boardCode" placeholder="选择版块" class="w-[180px] max-md:w-full max-md:flex-1">
        <el-option v-for="b in boards" :key="b.code" :label="b.name" :value="b.code" />
      </el-select>
      <span class="ml-auto hidden shrink-0 text-caption text-ink-meta md:inline" aria-live="polite">
        标题 {{ form.title.length }}/100 · 正文 {{ form.contentMd.length }}/50000
      </span>
      <el-button type="primary" :loading="submitting" @click="submit">发 布</el-button>
    </div>

    <div class="mx-auto max-w-reading px-6 max-md:px-4">
      <!-- 标题：超大无边框输入（CSDN / 掘金式） -->
      <input
        v-model="form.title"
        maxlength="100"
        placeholder="输入标题（1~100 字）"
        class="block w-full border-0 bg-transparent py-6 text-[26px] leading-snug font-medium text-ink outline-none placeholder:text-ink-meta max-md:text-[22px]"
      />
    </div>

    <!-- 工具条：通栏（CSDN 式），样式来自全局 .cl-md-tool -->
    <div class="flex flex-wrap items-center gap-1 border-y-[0.5px] border-line bg-code px-5 py-1.5" role="toolbar" aria-label="Markdown 工具条">
      <button type="button" class="cl-md-tool text-caption font-medium" title="一级标题" aria-label="一级标题" @click="wrap('# ', '', '标题')">#</button>
      <button type="button" class="cl-md-tool text-caption font-medium" title="二级标题" aria-label="二级标题" @click="wrap('## ', '', '标题')">H2</button>
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
      <button type="button" class="cl-md-tool text-caption font-medium" title="引用" aria-label="引用" @click="wrap('> ', '', '引用内容')">❝</button>
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
      <span class="ml-auto text-caption text-ink-meta md:hidden">{{ form.title.length }}/100 · {{ form.contentMd.length }}/50000</span>
    </div>

    <div class="mx-auto max-w-reading px-6 max-md:px-4">
      <!-- 正文：无边框大行距编辑区（占位说明合并机审提示与 ADR-005 预览边界） -->
      <textarea
        ref="contentEl"
        v-model="form.contentMd"
        maxlength="50000"
        placeholder="正文支持 Markdown：标题 / 列表 / 代码块（```语言）/ 链接 / 引用。开始输入…"
        class="block w-full resize-none border-0 bg-transparent py-5 text-body leading-[1.9] text-ink outline-none placeholder:text-ink-meta min-h-[50vh] max-md:min-h-[36vh]"
      />
      <p class="pb-6 text-caption leading-body text-ink-meta">
        发布前将进行内容安全机审（F-SAFE-001）· 暂无实时预览：按 ADR-005，Markdown 只在服务端渲染并做安全净化，发布成功后可在帖子详情页查看渲染结果
      </p>
    </div>
  </div>
</template>
