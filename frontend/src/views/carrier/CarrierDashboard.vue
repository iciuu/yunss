<template>
  <main class="app-shell">
    <header class="topbar">
      <div>
        <p class="eyebrow">承运商工作台</p>
        <h1>竞价大厅</h1>
      </div>
      <div style="display:flex;gap:12px;align-items:center">
        <button class="small ghost" @click="showMessages = !showMessages">
          消息{{ unreadCount > 0 ? ` (${unreadCount})` : '' }}
        </button>
        <button class="ghost" @click="logout">退出</button>
      </div>
    </header>

    <section class="stats">
      <StatCard label="报价次数" :value="dashboard.bidCount || 0" />
      <StatCard label="中标次数" :value="dashboard.winCount || 0" />
      <StatCard label="中标率" :value="`${Math.round((dashboard.winRate || 0) * 100)}%`" />
    </section>

    <!-- 消息面板 -->
    <section class="panel" v-if="showMessages" style="margin-bottom:24px">
      <h2>消息通知 <small class="muted">(点击标记已读)</small></h2>
      <article v-for="msg in messages" :key="msg.id" class="list-card"
               :style="!msg.read ? 'outline:2px solid #1769e0' : ''"
               @click="markRead(msg)">
        <b>{{ msg.title }}</b>
        <span>{{ msg.content }}</span>
        <small>{{ format(msg.createdAt) }}</small>
      </article>
      <p v-if="messages.length === 0" class="muted">暂无消息</p>
    </section>

    <section class="grid two">
      <section class="panel">
        <h2>可参与货源</h2>
        <article v-for="item in marketPage" :key="item.id" class="list-card" @click="selected = item">
          <b>{{ item.cargoName }} · {{ item.companyName }}</b>
          <span>{{ item.originAddress }} → {{ item.destAddress }}</span>
          <small>截止 {{ format(item.deadline) }} · 报价 {{ item.bidCount ?? '保密' }}</small>
        </article>
        <p v-if="market.length === 0" class="muted">暂无可用货源</p>
        <Pagination v-if="market.length > pageSize" v-model:page="marketPageNum" v-model:pageSize="pageSize" :total="market.length" />
      </section>

      <form class="panel" @submit.prevent="submitBid">
        <h2>提交报价</h2>
        <p v-if="selected">{{ selected.cargoName }}：{{ selected.originAddress }} → {{ selected.destAddress }}</p>
        <p v-else class="muted">先从左侧选择一个货源。</p>
        <label>报价金额<input v-model.number="bid.amount" type="number" /></label>
        <label>备注<textarea v-model="bid.remark" /></label>
        <button :disabled="!selected">提交暗标</button>
      </form>
    </section>

    <!-- 我的报价 -->
    <section class="panel" style="margin-bottom:24px">
      <h2>我的报价</h2>
      <table>
        <thead><tr><th>货源 ID</th><th>金额</th><th>状态</th><th>时间</th></tr></thead>
        <tbody>
          <tr v-for="item in myBidsPage" :key="item.id">
            <td>{{ item.freightId }}</td>
            <td>¥{{ item.amount }}</td>
            <td>{{ statusLabel(item.status) }}</td>
            <td>{{ format(item.bidTime) }}</td>
          </tr>
        </tbody>
      </table>
      <p v-if="myBids.length === 0" class="muted">暂无报价记录</p>
      <Pagination v-if="myBids.length > pageSize" v-model:page="myBidsPageNum" v-model:pageSize="pageSize" :total="myBids.length" />
    </section>

    <!-- 订单管理 -->
    <section class="panel">
      <h2>我的订单</h2>
      <table>
        <thead><tr><th>订单号</th><th>金额</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in ordersPage" :key="item.id" style="cursor:pointer" @click="showOrderDetail(item)">
            <td>{{ item.orderNo || item.id }}</td>
            <td>¥{{ item.price }}</td>
            <td>{{ orderStatusLabel(item.status) }}</td>
            <td>{{ format(item.createdAt) }}</td>
            <td @click.stop>
              <button v-if="item.status === 'PENDING_CONFIRM'" class="small" @click="confirmOrder(item)">确认接单</button>
              <button v-if="item.status === 'PENDING_CONFIRM'" class="small ghost" style="margin-left:8px" @click="cancelOrder(item)">取消</button>
              <span v-else-if="item.status === 'TRANSPORTING'" style="color:#1769e0">运输中</span>
              <span v-else>{{ orderStatusLabel(item.status) }}</span>
            </td>
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
          <div><b>关联货源</b><p>
            {{ orderFreight ? (orderFreight.cargoName + ' · ' + orderFreight.originAddress + ' → ' + orderFreight.destAddress) : orderDetail.sourceId }}
          </p></div>
          <div><b>状态</b><p>{{ orderStatusLabel(orderDetail.status) }}</p></div>
          <div><b>金额</b><p>¥{{ orderDetail.price }}</p></div>
          <div><b>承运商</b><p>{{ orderDetail.carrierId }}</p></div>
          <div><b>创建时间</b><p>{{ format(orderDetail.createdAt) }}</p></div>
          <div><b>确认时间</b><p>{{ orderDetail.confirmTime ? format(orderDetail.confirmTime) : '-' }}</p></div>
          <div><b>完成时间</b><p>{{ orderDetail.finishedTime ? format(orderDetail.finishedTime) : '-' }}</p></div>
        </div>
        <div v-if="orderFreight" style="margin-top:16px;padding-top:16px;border-top:1px solid #e6edf7">
          <h3>货源信息</h3>
          <div class="form-grid">
            <div><b>货物名称</b><p>{{ orderFreight.cargoName }}</p></div>
            <div><b>线路</b><p>{{ orderFreight.originAddress }} → {{ orderFreight.destAddress }}</p></div>
            <div><b>货物类型</b><p>{{ orderFreight.cargoType }}</p></div>
            <div v-if="orderFreight.companyName"><b>货主</b><p>{{ orderFreight.companyName }}</p></div>
          </div>
        </div>
        <div v-if="!orderFreight" class="muted" style="margin-top:12px">正在加载货源信息…</div>
      </section>
    </div>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import StatCard from '../../components/StatCard.vue'
import Pagination from '../../components/Pagination.vue'
import { api, clearSession, post } from '../../api/client'

const router = useRouter()
const dashboard = reactive({})
const market = ref([])
const myBids = ref([])
const orders = ref([])
const messages = ref([])
const unreadCount = ref(0)
const showMessages = ref(false)
const selected = ref(null)
const bid = reactive({ amount: 4800, remark: '含9%增值税', attachment: '' })
const orderDetail = ref(null)
const orderFreight = ref(null)

// 分页状态
const pageSize = ref(10)
const marketPageNum = ref(1)
const myBidsPageNum = ref(1)
const ordersPageNum = ref(1)

const marketPage = computed(() => {
  const s = (marketPageNum.value - 1) * pageSize.value
  return market.value.slice(s, s + pageSize.value)
})
const myBidsPage = computed(() => {
  const s = (myBidsPageNum.value - 1) * pageSize.value
  return myBids.value.slice(s, s + pageSize.value)
})
const ordersPage = computed(() => {
  const s = (ordersPageNum.value - 1) * pageSize.value
  return orders.value.slice(s, s + pageSize.value)
})

async function loadDashboard() {
  Object.assign(dashboard, await api('/dashboard/carrier'))
}

async function loadMarket() {
  market.value = await api('/bid/market')
  marketPageNum.value = 1
}

async function loadMyBids() {
  myBids.value = await api('/bid/my')
  myBidsPageNum.value = 1
}

async function loadOrders() {
  orders.value = await api('/order/list')
  ordersPageNum.value = 1
}

async function loadMessages() {
  messages.value = await api('/message/list')
  const result = await api('/message/unread/count')
  unreadCount.value = result.count || 0
}

async function load() {
  await Promise.all([
    loadDashboard(),
    loadMarket(),
    loadMyBids(),
    loadOrders(),
    loadMessages()
  ])
}

async function submitBid() {
  await post('/bid', { freightId: selected.value.id, ...bid })
  selected.value = null
  await Promise.all([loadMarket(), loadMyBids()])
}

async function confirmOrder(item) {
  await post(`/order/${item.id}/confirm`)
  await loadOrders()
}

async function cancelOrder(item) {
  await post(`/order/${item.id}/cancel`)
  await loadOrders()
}

async function showOrderDetail(item) {
  orderDetail.value = await api(`/order/${item.id}`)
  orderFreight.value = null
  try {
    orderFreight.value = await api(`/bid/freight/${item.sourceId}`)
  } catch (_) { /* 可能已过期或无权限，忽略 */ }
}

async function markRead(msg) {
  if (!msg.read) {
    await post(`/message/${msg.id}/read`)
    await loadMessages()
  }
}

function statusLabel(status) {
  const map = { VALID: '有效', AWARDED: '已中标', INVALID: '无效' }
  return map[status] || status
}

function orderStatusLabel(status) {
  const map = { PENDING_CONFIRM: '待确认', TRANSPORTING: '运输中', CLOSED: '已关闭' }
  return map[status] || status
}

function format(value) {
  return value ? new Date(value).toLocaleString() : '-'
}

function logout() {
  clearSession()
  router.push('/login')
}

onMounted(load)
</script>
