<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { sendCaptcha, verifyStudent } from '../api/auth'
import { useCountdown } from '../composables/useCountdown'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const tab = ref<'login' | 'register'>('login')

const loginForm = reactive({ email: '', code: '' })
const reg = reactive({
  step: 1 as 1 | 2,
  ticket: '',
  studentId: '',
  name: '',
  email: '',
  code: '',
  nickname: '',
})

const loginCaptcha = useCountdown()
const regCaptcha = useCountdown()

function errorMessage(e: unknown, fallback: string) {
  return e instanceof Error ? e.message : fallback
}

async function doLogin() {
  try {
    await auth.login(loginForm.email, loginForm.code)
    ElMessage.success('登录成功')
    router.push(String(route.query.redirect || '/'))
  } catch (e) {
    ElMessage.error(errorMessage(e, '登录失败'))
  }
}

async function doVerify() {
  try {
    const res = await verifyStudent(reg.studentId, reg.name)
    reg.ticket = res.ticket
    reg.step = 2
    ElMessage.success('学籍核验通过，请继续完成注册')
  } catch (e) {
    // 后端对学号不存在 / 姓名不匹配 / 已注册统一返回同一提示（防名册枚举）
    ElMessage.error(errorMessage(e, '学籍信息校验未通过'))
  }
}

async function doRegister() {
  try {
    await auth.register({ ticket: reg.ticket, email: reg.email, code: reg.code, nickname: reg.nickname })
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(errorMessage(e, '注册失败'))
  }
}
</script>

<template>
  <el-card shadow="never" class="mx-auto mt-6 max-w-[480px]">
    <el-tabs v-model="tab">
      <el-tab-pane label="登录" name="login">
        <el-form label-position="top" @submit.prevent>
          <el-form-item label="邮箱">
            <el-input v-model="loginForm.email" placeholder="注册时使用的邮箱" />
          </el-form-item>
          <el-form-item label="验证码">
            <div class="flex w-full gap-2">
              <el-input v-model="loginForm.code" placeholder="6 位验证码" maxlength="6" />
              <el-button
                :disabled="loginCaptcha.remaining.value > 0 || loginCaptcha.sending.value"
                @click="loginCaptcha.run(() => sendCaptcha(loginForm.email))"
              >
                {{ loginCaptcha.remaining.value > 0 ? `${loginCaptcha.remaining.value}s` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-button type="primary" class="w-full" @click="doLogin">登录</el-button>
          <p class="mt-2 text-caption text-ink-meta">未注册？切换到"注册"，先用学号 + 姓名完成学籍核验。</p>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="注册" name="register">
        <el-steps :active="reg.step - 1" align-center finish-status="success">
          <el-step title="学籍核验" />
          <el-step title="邮箱验证" />
        </el-steps>

        <el-form v-if="reg.step === 1" label-position="top" @submit.prevent>
          <el-form-item label="学号">
            <el-input v-model="reg.studentId" placeholder="请输入学号" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="reg.name" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-button type="primary" class="w-full" @click="doVerify">核验学籍</el-button>
          <p class="mt-2 text-caption text-ink-meta">学号仅用于与学校名册做哈希比对，不会展示在个人主页。</p>
        </el-form>

        <el-form v-else label-position="top" @submit.prevent>
          <el-form-item label="邮箱">
            <el-input v-model="reg.email" placeholder="用于登录与找回，仅本人可见" />
          </el-form-item>
          <el-form-item label="验证码">
            <div class="flex w-full gap-2">
              <el-input v-model="reg.code" placeholder="6 位验证码" maxlength="6" />
              <el-button
                :disabled="regCaptcha.remaining.value > 0 || regCaptcha.sending.value"
                @click="regCaptcha.run(() => sendCaptcha(reg.email))"
              >
                {{ regCaptcha.remaining.value > 0 ? `${regCaptcha.remaining.value}s` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="reg.nickname" placeholder="社区展示昵称（2~32 字符）" maxlength="32" />
          </el-form-item>
          <el-button type="primary" class="w-full" @click="doRegister">完成注册</el-button>
          <p class="mt-2 text-caption text-ink-meta">核验票据 5 分钟内有效，过期请返回上一步重新核验。</p>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>
