<template>
  <div class="pagination" v-if="total > pageSize">
    <button class="small ghost" :disabled="page <= 1" @click="$emit('update:page', page - 1)">上一页</button>
    <span class="page-info">{{ start }}-{{ end }} / 共 {{ total }} 条</span>
    <button class="small ghost" :disabled="page >= totalPages" @click="$emit('update:page', page + 1)">下一页</button>
    <select class="page-size" :value="pageSize" @change="$emit('update:pageSize', Number($event.target.value)); $emit('update:page', 1)">
      <option :value="10">10条/页</option>
      <option :value="20">20条/页</option>
      <option :value="50">50条/页</option>
    </select>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  page: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  total: { type: Number, required: true }
})

defineEmits(['update:page', 'update:pageSize'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))
const start = computed(() => (props.page - 1) * props.pageSize + 1)
const end = computed(() => Math.min(props.page * props.pageSize, props.total))
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  flex-wrap: wrap;
}
.page-info {
  color: #6c7a90;
  font-size: 14px;
}
.page-size {
  width: auto;
  margin-left: auto;
}
</style>
