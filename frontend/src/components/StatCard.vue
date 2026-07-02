<template>
  <section class="stat-card">
    <span>{{ label }}</span>
    <strong>{{ value }}</strong>
  </section>
</template>

<script setup>
defineProps({
  label: String,
  value: [String, Number]
})
</script>
