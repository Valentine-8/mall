<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="文档标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="处理中" value="0" />
          <el-option label="已索引" value="1" />
          <el-option label="失败" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-upload
          :show-file-list="false"
          :http-request="handleUpload"
          accept=".txt,.md,.docx,.pdf"
          v-hasPermi="['mall:knowledge:upload']"
        >
          <el-button type="primary" plain icon="Upload" :loading="uploading">上传文档</el-button>
        </el-upload>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-alert type="info" show-icon :closable="false" class="mb8"
      title="轻量 RAG：文档切块存入 MySQL，向量化后供智能客服检索。支持 txt / md / docx / pdf，单文件 ≤10MB，单文档最多 200 块。" />

    <el-table v-loading="loading" :data="docList">
      <el-table-column label="标题" prop="title" min-width="160" show-overflow-tooltip />
      <el-table-column label="文件名" prop="fileName" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" prop="fileType" width="70" />
      <el-table-column label="块数" prop="chunkCount" width="70" />
      <el-table-column label="状态" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '1'" type="success">已索引</el-tag>
          <el-tag v-else-if="scope.row.status === '0'" type="warning">处理中</el-tag>
          <el-tag v-else type="danger">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="上传时间" prop="createTime" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleReindex(scope.row)" v-hasPermi="['mall:knowledge:reindex']">重新索引</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)" v-hasPermi="['mall:knowledge:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog v-model="failOpen" title="索引失败" width="520px" append-to-body>
      <p>{{ failMsg }}</p>
    </el-dialog>
  </div>
</template>

<script setup name="MallKnowledge">
import { getCurrentInstance, ref } from 'vue'
import { delKnowledgeDoc, listKnowledgeDocs, reindexKnowledgeDoc, uploadKnowledgeDoc } from '@/api/mall/knowledge'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const uploading = ref(false)
const showSearch = ref(true)
const docList = ref([])
const total = ref(0)
const failOpen = ref(false)
const failMsg = ref('')
const queryParams = ref({ pageNum: 1, pageSize: 10, title: '', status: '' })

function getList() {
  loading.value = true
  listKnowledgeDocs(queryParams.value).then(res => {
    docList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleUpload(option) {
  uploading.value = true
  uploadKnowledgeDoc(option.file).then(res => {
    uploading.value = false
    proxy.$modal.msgSuccess('上传成功')
    if (res.data?.status === '2') {
      failMsg.value = res.data.errorMsg || '索引失败'
      failOpen.value = true
    }
    getList()
  }).catch(() => { uploading.value = false })
}

function handleReindex(row) {
  proxy.$modal.confirm('确认重新索引「' + row.title + '」？').then(() => {
    return reindexKnowledgeDoc(row.docId)
  }).then(res => {
    proxy.$modal.msgSuccess('已重新索引')
    if (res.data?.status === '2') {
      failMsg.value = res.data.errorMsg || '索引失败'
      failOpen.value = true
    }
    getList()
  }).catch(() => {})
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除「' + row.title + '」？').then(() => {
    return delKnowledgeDoc(row.docId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
</style>
