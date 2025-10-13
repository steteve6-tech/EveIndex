<template>
  <div class="cron-helper">
    <a-button size="small" @click="visible = true">
      <template #icon>
        <ClockCircleOutlined />
      </template>
      Cron助手
    </a-button>

    <a-modal v-model:open="visible" title="Cron表达式助手" width="600px">
      <a-space direction="vertical" style="width: 100%" :size="16">
        <!-- 常用表达式 -->
        <div>
          <div style="margin-bottom: 8px; font-weight: 500">常用Cron表达式</div>
          <a-space direction="vertical" style="width: 100%">
            <a-button
              v-for="preset in cronPresets"
              :key="preset.cron"
              block
              @click="selectCron(preset.cron)"
            >
              <div style="display: flex; justify-content: space-between; align-items: center; width: 100%">
                <span>{{ preset.label }}</span>
                <a-tag color="blue">{{ preset.cron }}</a-tag>
              </div>
            </a-button>
          </a-space>
        </div>

        <!-- 自定义输入 -->
        <div>
          <div style="margin-bottom: 8px; font-weight: 500">自定义Cron表达式</div>
          <a-input v-model:value="customCron" placeholder="输入自定义Cron表达式" />
          <div style="margin-top: 8px; color: #666; font-size: 12px">
            格式说明: 秒 分 时 日 月 周
            <br />
            例如: 0 0 2 * * ? (每天凌晨2点)
          </div>
        </div>

        <!-- Cron格式说明 -->
        <a-collapse>
          <a-collapse-panel key="1" header="Cron格式详细说明">
            <div style="font-size: 12px; line-height: 1.8">
              <p><strong>字段说明：</strong></p>
              <ul>
                <li>秒：0-59</li>
                <li>分：0-59</li>
                <li>时：0-23</li>
                <li>日：1-31</li>
                <li>月：1-12 或 JAN-DEC</li>
                <li>周：1-7 或 SUN-SAT (1=SUN)</li>
              </ul>
              <p><strong>特殊字符：</strong></p>
              <ul>
                <li>* 表示所有值</li>
                <li>? 用于日和周字段，表示不指定值</li>
                <li>- 表示范围，如 1-5</li>
                <li>/ 表示间隔，如 0/15 (每15分钟)</li>
                <li>, 表示列举，如 1,3,5</li>
              </ul>
            </div>
          </a-collapse-panel>
        </a-collapse>
      </a-space>

      <template #footer>
        <a-space>
          <a-button @click="visible = false">取消</a-button>
          <a-button type="primary" @click="confirmSelect">确定</a-button>
        </a-space>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ClockCircleOutlined } from '@ant-design/icons-vue';

const emit = defineEmits<{
  select: [cron: string]
}>();

const visible = ref(false);
const customCron = ref('');

// 常用Cron表达式预设
const cronPresets = [
  { label: '每天凌晨2点', cron: '0 0 2 * * ?' },
  { label: '每天凌晨3点', cron: '0 0 3 * * ?' },
  { label: '每天上午9点', cron: '0 0 9 * * ?' },
  { label: '每天中午12点', cron: '0 0 12 * * ?' },
  { label: '每天下午6点', cron: '0 0 18 * * ?' },
  { label: '每小时执行一次', cron: '0 0 * * * ?' },
  { label: '每30分钟执行一次', cron: '0 0/30 * * * ?' },
  { label: '每15分钟执行一次', cron: '0 0/15 * * * ?' },
  { label: '每周一凌晨2点', cron: '0 0 2 ? * MON' },
  { label: '每周一、三、五凌晨2点', cron: '0 0 2 ? * MON,WED,FRI' },
  { label: '每月1号凌晨2点', cron: '0 0 2 1 * ?' },
  { label: '每月最后一天凌晨2点', cron: '0 0 2 L * ?' }
];

const selectCron = (cron: string) => {
  customCron.value = cron;
};

const confirmSelect = () => {
  if (customCron.value) {
    emit('select', customCron.value);
    visible.value = false;
    customCron.value = '';
  }
};
</script>

<style scoped>
.cron-helper {
  display: inline-block;
}
</style>

