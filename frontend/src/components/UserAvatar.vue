<script setup lang="ts">
/**
 * 用户头像（CR-055 批次 2）
 *
 * 后端事实：`users.avatar_url` 字段存在，但注册链路从不写入、论坛 5 个 VO 也都不带 avatar，
 * 故 `src` 当前恒为空——本组件的主体是**按昵称派生的占位头像**，`src` 一旦有值即优先用之。
 * 补 `authorAvatarUrl` 属接口契约变更，另开 CR（见方案 §2 不做清单）。
 *
 * 派生规则：昵称 → FNV-1a 稳定哈希 → tokens.css 的 --cl-avatar-1~8 之一；首字取昵称真实首字。
 * 同一昵称永远同一底色（跨会话、跨设备、与登录态无关）。
 */
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    /** 真实头像 URL；见上方后端事实，当前恒为空 */
    src?: string
    /** 昵称，用于派生底色与首字 */
    name?: string
    size?: number
  }>(),
  { src: '', name: '', size: 32 },
)

/** 与 tokens.css 的 --cl-avatar-1~8 一一对应，改档位数须同步改令牌 */
const PALETTE_SIZE = 8

/** FNV-1a 32 位。按码点而非 UTF-16 单元遍历：昵称允许 emoji / 增补平面汉字，截半会撞档 */
function bucketOf(name: string): number {
  let hash = 0x811c9dc5
  for (const char of name) {
    hash ^= char.codePointAt(0) ?? 0
    hash = Math.imul(hash, 0x01000193)
  }
  return (hash >>> 0) % PALETTE_SIZE
}

const trimmed = computed(() => props.name.trim())
/** 首字同样按码点取，`slice(0, 1)` 对 emoji 会截出半个代理对 */
const initial = computed(() => Array.from(trimmed.value)[0] ?? '')
/**
 * EP 的 `--el-avatar-text-size` 恒为 14px，塞进 20px 小头像会溢出；缩到 10px 又不在
 * 本项目字号阶梯（最小 12px）上。故按尺寸在阶梯内二选一，不引入阶梯外字号。
 * 底色按实例覆写 EP 变量（内联优先级高于 tokens.css 的 .el-avatar.el-avatar 兜底）；
 * 昵称为空时不覆写底色，落回兜底。
 */
const avatarStyle = computed(() => {
  const style: Record<string, string> = {
    '--el-avatar-text-size':
      props.size >= 28 ? 'var(--cl-font-body)' : 'var(--cl-font-caption)',
  }
  if (trimmed.value) {
    style['--el-avatar-bg-color'] = `var(--cl-avatar-${bucketOf(trimmed.value) + 1})`
  }
  return style
})
</script>

<template>
  <el-avatar :size="size" :src="src || undefined" :alt="name" :style="avatarStyle">
    {{ initial }}
  </el-avatar>
</template>
