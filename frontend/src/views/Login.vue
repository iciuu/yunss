<template>
  <main class="login-shell">
    <section class="hero">
      <p class="eyebrow">运输竞价 SaaS</p>
      <h1>运省省</h1>
      <p>货主发货、承运商暗标报价、开标生成订单，一条链路清清爽爽。</p>
      <div class="demo-box">
        <b>演示账号</b>
        <span>货主：13800000000 / 123456</span>
        <span>承运商：13900000000 / 123456</span>
        <span>平台：13000000000 / 123456</span>
      </div>
    </section>
    <form class="panel login-card" @submit.prevent="login">
      <h2>登录工作台</h2>
      <label>手机号<input v-model="form.phone" placeholder="请输入手机号" /></label>
      <label>密码<input v-model="form.password" type="password" placeholder="请输入密码" /></label>
      <button>进入系统</button>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { post, setSession } from '../api/client'

const router = useRouter()
const error = ref('')
const form = reactive({ phone: '13800000000', password: '123456', loginType: 'PASSWORD' })

async function login() {
  error.value = ''
  try {
    const session = await post('/auth/login', form)
    setSession(session)
    if (session.role.startsWith('SHIPPER')) router.push('/shipper')
    else if (session.role.startsWith('CARRIER')) router.push('/carrier')
    else router.push('/platform')
  } catch (err) {
    error.value = err.message
  }
}
</script>
