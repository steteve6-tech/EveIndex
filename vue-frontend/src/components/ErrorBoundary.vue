<template>
  <div v-if="error" class="error-boundary">
    <a-result
      status="error"
      title="组件加载失败"
      :sub-title="error.message || '发生了未知错误'"
    >
      <template #extra>
        <a-button type="primary" @click="handleRetry">
          重试
        </a-button>
        <a-button @click="handleReset">
          重置
        </a-button>
      </template>
    </a-result>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue'
import { message } from 'ant-design-vue'

const error = ref<Error | null>(null)

const handleRetry = () => {
  error.value = null
  message.success('正在重试...')
}

const handleReset = () => {
  error.value = null
  window.location.reload()
}

onErrorCaptured((err, instance, info) => {
  console.error('错误边界捕获到错误:', err)
  console.error('错误信息:', info)
  error.value = err
  message.error('组件发生错误，已启用错误边界保护')
  return false
})
</script>

<style scoped>
.error-boundary {
  padding: 24px;
  text-align: center;
}
</style>
