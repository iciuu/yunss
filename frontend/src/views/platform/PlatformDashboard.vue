<template>
  <main class="app-shell">
    <header class="topbar">
      <div>
        <p class="eyebrow">平台后台</p>
        <h1>运营总览</h1>
      </div>
      <button class="ghost" @click="logout">退出</button>
    </header>

    <section class="stats">
      <StatCard label="租户数" :value="dashboard.tenantCount || 0" />
      <StatCard label="承运商数" :value="dashboard.carrierCount || 0" />
      <StatCard label="货源数" :value="dashboard.freightCount || 0" />
      <StatCard label="订单数" :value="dashboard.orderCount || 0" />
    </section>

    <section class="grid two">
      <section class="panel">
        <h2>租户列表</h2>
        <table>
          <thead><tr><th>ID</th><th>名称</th><th>状态</th></tr></thead>
          <tbody>
            <tr v-for="item in tenants" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.tenantName }}</td>
              <td>{{ item.status }}</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="panel">
        <h2>承运商列表</h2>
        <table>
          <thead><tr><th>ID</th><th>公司名称</th><th>联系人</th><th>评分</th></tr></thead>
          <tbody>
            <tr v-for="item in carriers" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.companyName }}</td>
              <td>{{ item.contact }}</td>
              <td>{{ item.score }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </section>

    <section class="panel" style="margin-top:24px">
      <h2>全部订单</h2>
      <table>
        <thead><tr><th>订单号</th><th>租户</th><th>金额</th><th>状态</th><th>创建时间</th></tr></thead>
        <tbody>
          <tr v-for="item in ordersPage" :key="item.id" style="cursor:pointer" @click="showOrderDetail(item)">
            <td>{{ item.orderNo || item.id }}</td>
            <td>{{ item.tenantId }}</td>
            <td>¥{{ item.price }}</td>
            <td>{{ orderStatusLabel(item.status) }}</td>
            <td>{{ format(item.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
      <p v-if="orders.length === 0" class="muted">暂无订单</p>
      <Pagination v-if="orders.length > pageSize" v-model:page="ordersPageNum" v-model:pageSize="pageSize" :total="orders.length" />
    </section>

    <!-- 订单详情弹窗 -->
    <div v-if="orderDetail" class="modal-overlay" @click.self="orderDetail = null">
      <section class="panel modal-content">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
          <h2 style="margin:0">订单详情 — {{ orderDetail.orderNo || orderDetail.id }}</h2>
          <button class="small ghost" @click="orderDetail = null">✕ 关闭</button>
        </div>
        <div class="form-grid">
          <div><b>订单号</b><p>{{ orderDetail.orderNo || orderDetail.id }}</p></div>
          <div><b>货主租户</b><p>{{ orderDetail.tenantId }}</p></div>
          <div><b>关联货源</b><p>{{ orderDetail.sourceId }}</p></div>
          <div><b>状态</b><p>{{ orderStatusLabel(orderDetail.status) }}</p></div>
          <div><b>金额</b><p>¥{{ orderDetail.price }}</p></div>
          <div><b>承运商</b><p>{{ orderDetail.carrierId }}</p></div>
          <div><b>创建时间</b><p>{{ format(orderDetail.createdAt) }}</p></div>
          <div><b>确认时间</b><p>{{ orderDetail.confirmTime ? format(orderDetail.confirmTime) : '-' }}</p></div>
          <div><b>完成时间</b><p>{{ orderDetail.finishedTime ? format(orderDetail.finishedTime) : '-' }}</p></div>
        </div>
      </section>
    </div>

    <section class="panel" style="margin-top:24px">
      <h2>管理入口</h2>
      <p>租户管理、企业审核、用户管理、系统字典与审计日志等管理功能将在后续版本中完善。</p>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import StatCard from '../../components/StatCard.vue'
import Pagination from '../../components/Pagination.vue'
import { api, clearSession } from '../../api/client'

const router = useRouter()
const dashboard = reactive({})
const tenants = ref([])
const carriers = ref([])
const orders = ref([])
const orderDetail = ref(null)

// 分页状态
const pageSize = ref(10)
const ordersPageNum = ref(1)
const ordersPage = computed(() => {
  const s = (ordersPageNum.value - 1) * pageSize.value
  return orders.value.slice(s, s + pageSize.value)
})

onMounted(async () => {
  Object.assign(dashboard, await api('/dashboard/platform'))
  tenants.value = await api('/platform/tenants')
  carriers.value = await api('/platform/carriers')
  orders.value = await api('/order/list')
})

function orderStatusLabel(status) {
  const map = { PENDING_CONFIRM: '待确认', TRANSPORTING: '运输中', CLOSED: '已关闭' }
  return map[status] || status
}

function format(value) {
  return value ? new Date(value).toLocaleString() : '-'
}

async function showOrderDetail(item) {
  orderDetail.value = await api(`/order/${item.id}`)
}

function logout() {
  clearSession()
  router.push('/login')
}
</script>
