<template>
  <main class="app-shell">
    <header class="topbar">
      <div>
        <p class="eyebrow">货主工作台</p>
        <h1>货源发布与开标</h1>
      </div>
      <button class="ghost" @click="logout">退出</button>
    </header>

    <section class="stats">
      <StatCard label="货源数量" :value="dashboard.totalFreight || 0" />
      <StatCard label="成交金额" :value="dashboard.totalCost || 0" />
      <StatCard label="平均运价" :value="dashboard.avgPrice || 0" />
    </section>

    <section class="grid two">
      <form class="panel" @submit.prevent="createFreight">
        <h2>发布货源</h2>
        <div class="form-grid">
          <label>货物名称<input v-model="freight.cargoName" /></label>
          <label>货物类型<select v-model="freight.cargoType"><option value="GENERAL">普通货物</option><option value="COLD_CHAIN">冷链</option><option value="DANGEROUS">危险品</option></select></label>
          <label>重量 (吨)<input v-model.number="freight.weight" type="number" /></label>
          <label>体积 (m³)<input v-model.number="freight.volume" type="number" /></label>
          <label>起点<input v-model="freight.originAddress" /></label>
          <label>终点<input v-model="freight.destAddress" /></label>
          <label>底价 (元)<input v-model.number="freight.floorPrice" type="number" /></label>
          <label>截止时间<input v-model="deadlineText" type="datetime-local" /></label>
        </div>
        <label>备注<textarea v-model="freight.remark" /></label>
        <button>发布</button>
      </form>

      <section class="panel">
        <h2>货源列表</h2>
        <div style="margin-bottom:12px">
          <select v-model="statusFilter" @change="loadFreights" style="width:auto">
            <option value="">全部状态</option>
            <option value="ACTIVE">竞价中</option>
            <option value="AWARDED">已开标</option>
            <option value="CLOSED">已关闭</option>
            <option value="FAILED">已流标</option>
          </select>
        </div>
        <article v-for="item in freightsPage" :key="item.id" class="list-card" @click="selectFreight(item)">
          <b>{{ item.publishNo }} · {{ item.cargoName }}</b>
          <span>{{ item.originAddress }} → {{ item.destAddress }}</span>
          <small>{{ statusLabel(item.status) }} · {{ item.bidCount }} 个报价 · 底价 ¥{{ item.floorPrice }}</small>
        </article>
        <p v-if="freights.length === 0" class="muted">暂无货源，请发布一条</p>
        <Pagination v-if="freights.length > pageSize" v-model:page="freightPageNum" v-model:pageSize="pageSize" :total="freights.length" />
      </section>
    </section>

    <!-- 开标面板：仅 ACTIVE 状态的货源可开标 -->
    <section class="panel" v-if="selected && selected.status === 'ACTIVE'">
      <h2>开标：{{ selected.publishNo }} · {{ selected.cargoName }}</h2>
      <table>
        <thead><tr><th>承运商</th><th>金额</th><th>评分</th><th>成交率</th><th>备注</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="bid in bids" :key="bid.id">
            <td>{{ bid.carrierName }}</td>
            <td>¥{{ bid.amount }}</td>
            <td>{{ bid.rating }}</td>
            <td>{{ Math.round((bid.dealRate || 0) * 100) }}%</td>
            <td>{{ bid.remark || '-' }}</td>
            <td><button class="small" @click="award(bid)">确认中标</button></td>
          </tr>
        </tbody>
      </table>
      <p v-if="bids.length === 0" class="muted">暂无承运商报价</p>
      <button v-if="bids.length > 0" class="danger" @click="failFreight">流标</button>
    </section>
    <!-- 已开标/已关闭的货源仅展示报价历史，不显示操作按钮 -->
    <section class="panel" v-else-if="selected">
      <h2>{{ selected.publishNo }} · {{ selected.cargoName }}
        <small class="muted">（{{ statusLabel(selected.status) }}）</small>
      </h2>
      <table>
        <thead><tr><th>承运商</th><th>金额</th><th>评分</th><th>备注</th></tr></thead>
        <tbody>
          <tr v-for="bid in bids" :key="bid.id">
            <td>{{ bid.carrierName }}</td>
            <td>¥{{ bid.amount }}</td>
            <td>{{ bid.rating }}</td>
            <td>{{ bid.remark || '-' }}</td>
          </tr>
        </tbody>
      </table>
      <p v-if="bids.length === 0" class="muted">暂无报价记录</p>
    </section>

    <!-- 订单管理 -->
    <section class="panel" style="margin-top:24px">
      <h2>订单管理</h2>
      <table>
        <thead><tr><th>订单号</th><th>货源</th><th>金额</th><th>承运商</th><th>状态</th><th>确认时间</th></tr></thead>
        <tbody>
          <tr v-for="item in ordersPage" :key="item.id" style="cursor:pointer" @click="showOrderDetail(item)">
            <td>{{ item.orderNo || item.id }}</td>
            <td>{{ freightMap[item.sourceId]?.publishNo || item.sourceId }}</td>
            <td>¥{{ item.price }}</td>
            <td>{{ carrierNames[item.carrierId] || item.carrierId }}</td>
            <td>{{ orderStatusLabel(item.status) }}</td>
            <td>{{ item.confirmTime ? format(item.confirmTime) : '-' }}</td>
          </tr>
        </tbody>
      </table>
      <p v-if="orders.length === 0" class="muted">暂无订单，开标后将自动生成</p>
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
          <div><b>关联货源</b><p>{{ freightMap[orderDetail.sourceId]?.publishNo || orderDetail.sourceId }}</p></div>
          <div><b>状态</b><p>{{ orderStatusLabel(orderDetail.status) }}</p></div>
          <div><b>金额</b><p>¥{{ orderDetail.price }}</p></div>
          <div><b>承运商</b><p>{{ orderDetail.carrierId }}</p></div>
          <div><b>创建时间</b><p>{{ format(orderDetail.createdAt) }}</p></div>
          <div><b>确认时间</b><p>{{ orderDetail.confirmTime ? format(orderDetail.confirmTime) : '-' }}</p></div>
          <div><b>完成时间</b><p>{{ orderDetail.finishedTime ? format(orderDetail.finishedTime) : '-' }}</p></div>
        </div>
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
const freights = ref([])
const bids = ref([])
const orders = ref([])
const carrierNames = reactive({})
const freightMap = reactive({})
const selected = ref(null)
const statusFilter = ref('')
const orderDetail = ref(null)
const deadlineText = ref(toDateTimeInput(new Date(Date.now() + 86400000)))
const freight = reactive({
  cargoName: '钢材',
  cargoType: 'GENERAL',
  weight: 10,
  volume: 12,
  originAddress: '上海市浦东新区',
  destAddress: '江苏省苏州市',
  loadingTime: toLocalDateTime(new Date(Date.now() + 3600000)),
  vehicleTypes: ['13M', '9.6M'],
  priceModel: 'TOTAL_PRICE',
  floorPrice: 5000,
  showBidCount: true,
  attachments: [],
  remark: '注意防雨'
})

// 分页状态
const pageSize = ref(10)
const freightPageNum = ref(1)
const ordersPageNum = ref(1)

const freightsPage = computed(() => {
  const s = (freightPageNum.value - 1) * pageSize.value
  return freights.value.slice(s, s + pageSize.value)
})
const ordersPage = computed(() => {
  const s = (ordersPageNum.value - 1) * pageSize.value
  return orders.value.slice(s, s + pageSize.value)
})

async function loadDashboard() {
  Object.assign(dashboard, await api('/dashboard/shipper'))
}

async function loadFreights() {
  const params = statusFilter.value ? `?status=${statusFilter.value}` : ''
  freights.value = await api(`/freight/list${params}`)
  freightPageNum.value = 1
  for (const f of freights.value) {
    freightMap[f.id] = { publishNo: f.publishNo, cargoName: f.cargoName }
  }
}

async function loadOrders() {
  orders.value = await api('/order/list')
  ordersPageNum.value = 1
  for (const order of orders.value) {
    if (order.carrierId && !carrierNames[order.carrierId]) {
      try {
        carrierNames[order.carrierId] = order.carrierId ? `承运商 #${order.carrierId}` : '-'
      } catch (_) {}
    }
  }
}

async function load() {
  await Promise.all([loadDashboard(), loadFreights(), loadOrders()])
}

async function createFreight() {
  await post('/freight', { ...freight, deadline: toLocalDateTime(new Date(deadlineText.value)) })
  await Promise.all([loadDashboard(), loadFreights()])
}

async function selectFreight(item) {
  selected.value = item
  bids.value = await api(`/freight/${item.id}/bids`)
}

async function award(bid) {
  await post(`/freight/${selected.value.id}/award`, { bidId: bid.id, remark: '确认承运' })
  selected.value = null
  bids.value = []
  await Promise.all([loadDashboard(), loadFreights(), loadOrders()])
}

async function failFreight() {
  await post(`/freight/${selected.value.id}/fail`, { reason: '价格过高' })
  selected.value = null
  bids.value = []
  await load()
}

function statusLabel(status) {
  const map = { ACTIVE: '竞价中', AWARDED: '已开标', CLOSED: '已关闭', FAILED: '已流标' }
  return map[status] || status
}

function orderStatusLabel(status) {
  const map = { PENDING_CONFIRM: '待承运商确认', TRANSPORTING: '运输中', CLOSED: '已关闭' }
  return map[status] || status
}

function logout() {
  clearSession()
  router.push('/login')
}

function toDateTimeInput(date) {
  return toLocalDateTime(date).slice(0, 16)
}
function toLocalDateTime(date) {
  const pad = value => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}
function format(value) {
  return value ? new Date(value).toLocaleString() : '-'
}

async function showOrderDetail(item) {
  orderDetail.value = await api(`/order/${item.id}`)
}

onMounted(load)
</script>
