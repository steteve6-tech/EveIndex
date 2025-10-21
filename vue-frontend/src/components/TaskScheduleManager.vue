<template>
  <div class="task-schedule-manager">
    <!-- 操作栏 -->
    <div class="action-bar">
      <a-button type="primary" @click="showCreateDialog">
        <template #icon><PlusOutlined /></template>
        新建任务
      </a-button>
      <a-button @click="refreshTasks">
        <template #icon><ReloadOutlined /></template>
        刷新
      </a-button>
    </div>

    <!-- 任务列表 -->
    <a-table
      :columns="columns"
      :data-source="tasks"
      :loading="loading"
      row-key="id"
      :pagination="{ pageSize: 10 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'enabled'">
          <a-switch
            v-model:checked="record.enabled"
            @change="toggleTask(record)"
            checked-children="启用"
            un-checked-children="停用"
          />
        </template>

        <template v-else-if="column.key === 'actions'">
          <a-space>
            <a-button size="small" type="primary" @click="executeTask(record)">
              执行
            </a-button>
            <a-button size="small" @click="editTask(record)">编辑</a-button>
            <a-popconfirm title="确定删除吗？" @confirm="deleteTask(record)">
              <a-button size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 创建/编辑对话框 -->
    <a-modal
      v-model:open="dialogVisible"
      :title="isEdit ? '编辑任务' : '新建任务'"
      @ok="handleSubmit"
      width="600px"
    >
      <a-form :model="formData" :label-col="{ span: 6 }">
        <a-form-item label="任务名称" required>
          <a-input v-model:value="formData.taskName" placeholder="请输入任务名称" />
        </a-form-item>

        <a-form-item label="爬虫" required>
          <a-select v-model:value="formData.crawlerName" placeholder="请选择爬虫">
            <a-select-option v-for="c in crawlers" :key="c.name" :value="c.name">
              {{ c.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="Cron表达式">
          <a-input
            v-model:value="formData.cronExpression"
            placeholder="例如: 0 0 2 * * ? (每天凌晨2点)"
          />
          <small style="color: #8c8c8c">
            留空表示不定时执行，格式: 秒 分 时 日 月 周
          </small>
        </a-form-item>

        <a-form-item label="描述">
          <a-textarea v-model:value="formData.description" placeholder="任务描述" :rows="3" />
        </a-form-item>

        <a-form-item label="启用">
          <a-switch v-model:checked="formData.enabled" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue';
import { getPresets, createPreset, updatePreset, deletePreset, triggerTask, getCrawlers } from '@/api/crawler';

// 数据
const loading = ref(false);
const tasks = ref<any[]>([]);
const crawlers = ref<any[]>([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formData = reactive({
  id: null as number | null,
  taskName: '',
  crawlerName: '',
  cronExpression: '',
  description: '',
  enabled: true,
});

// 表格列
const columns = [
  { title: '任务名称', dataIndex: 'taskName', key: 'taskName' },
  { title: '爬虫', dataIndex: 'crawlerName', key: 'crawlerName' },
  { title: 'Cron表达式', dataIndex: 'cronExpression', key: 'cronExpression' },
  { title: '状态', key: 'enabled', width: 120 },
  { title: '操作', key: 'actions', width: 200 },
];

// 加载任务列表
const loadTasks = async () => {
  loading.value = true;
  try {
    const res = await getPresets();
    if (res.success) {
      tasks.value = res.data;
    }
  } catch (error: any) {
    message.error('加载任务失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

// 加载爬虫列表
const loadCrawlers = async () => {
  try {
    const res = await getCrawlers();
    if (res.success) {
      crawlers.value = res.data;
    }
  } catch (error: any) {
    console.error('加载爬虫列表失败', error);
  }
};

// 刷新任务
const refreshTasks = () => {
  loadTasks();
};

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false;
  formData.id = null;
  formData.taskName = '';
  formData.crawlerName = '';
  formData.cronExpression = '';
  formData.description = '';
  formData.enabled = true;
  dialogVisible.value = true;
};

// 切换任务状态
const toggleTask = async (task: any) => {
  try {
    const res = await updatePreset(task.id, { enabled: task.enabled });
    if (res.success) {
      message.success(task.enabled ? '任务已启用' : '任务已停用');
    } else {
      task.enabled = !task.enabled;
      message.error(res.message);
    }
  } catch (error: any) {
    task.enabled = !task.enabled;
    message.error('操作失败: ' + error.message);
  }
};

// 执行任务
const executeTask = async (task: any) => {
  try {
    const res = await triggerTask(task.id);
    if (res.success) {
      message.success('任务已提交执行');
    } else {
      message.error(res.message);
    }
  } catch (error: any) {
    message.error('执行失败: ' + error.message);
  }
};

// 编辑任务
const editTask = (task: any) => {
  isEdit.value = true;
  formData.id = task.id;
  formData.taskName = task.taskName;
  formData.crawlerName = task.crawlerName;
  formData.cronExpression = task.cronExpression || '';
  formData.description = task.description || '';
  formData.enabled = task.enabled;
  dialogVisible.value = true;
};

// 删除任务
const deleteTask = async (task: any) => {
  try {
    const res = await deletePreset(task.id);
    if (res.success) {
      message.success('删除成功');
      await loadTasks();
    } else {
      message.error(res.message);
    }
  } catch (error: any) {
    message.error('删除失败: ' + error.message);
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!formData.taskName || !formData.crawlerName) {
    message.warning('请填写必填项');
    return;
  }

  try {
    const data = {
      taskName: formData.taskName,
      crawlerName: formData.crawlerName,
      cronExpression: formData.cronExpression || null,
      description: formData.description,
      enabled: formData.enabled,
      countryCode: 'US', // 可以从爬虫信息中获取
      parameters: JSON.stringify({}),
    };

    let res;
    if (isEdit.value && formData.id) {
      res = await updatePreset(formData.id, data);
    } else {
      res = await createPreset(data);
    }

    if (res.success) {
      message.success(isEdit.value ? '更新成功' : '创建成功');
      dialogVisible.value = false;
      await loadTasks();
    } else {
      message.error(res.message);
    }
  } catch (error: any) {
    message.error('操作失败: ' + error.message);
  }
};

// 初始化
onMounted(() => {
  loadTasks();
  loadCrawlers();
});
</script>

<style scoped lang="less">
.task-schedule-manager {
  .action-bar {
    margin-bottom: 16px;
    display: flex;
    gap: 8px;
  }
}
</style>
