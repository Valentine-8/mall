<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="订单号" prop="orderSn">
        <el-input v-model="queryParams.orderSn" placeholder="订单编号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="用户" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="用户账号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="订单状态" clearable style="width: 160px">
          <el-option v-for="dict in mall_order_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mall:order:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单号" align="center" prop="orderSn" width="160" />
      <el-table-column label="用户" align="center" prop="userName" width="100" />
      <el-table-column label="应付金额" align="center" prop="payAmount" width="100">
        <template #default="scope">￥{{ scope.row.payAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="mall_order_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="收货人" align="center" prop="receiverName" width="100" />
      <el-table-column label="电话" align="center" prop="receiverPhone" width="120" />
      <el-table-column label="下单时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="280" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['mall:order:query']">详情</el-button>
          <el-button link type="primary" v-if="scope.row.status === '1'" @click="handleShip(scope.row)" v-hasPermi="['mall:order:ship']">发货</el-button>
          <el-button link type="success" v-if="scope.row.status === '2'" @click="handleFinish(scope.row)" v-hasPermi="['mall:order:finish']">完成</el-button>
          <el-button link type="warning" v-if="scope.row.status === '0' || scope.row.status === '1'" @click="handleCancel(scope.row)" v-hasPermi="['mall:order:cancel']">取消</el-button>
          <el-button link type="danger" v-if="scope.row.status === '1' || scope.row.status === '2'" @click="handleRefund(scope.row)" v-hasPermi="['mall:order:cancel']">退款</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mall:order:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="订单详情" v-model="detailOpen" width="720px" append-to-body>
      <el-descriptions :column="2" border v-if="orderDetail">
        <el-descriptions-item label="订单号">{{ orderDetail.orderSn }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="mall_order_status" :value="orderDetail.status" />
        </el-descriptions-item>
        <el-descriptions-item label="用户">{{ orderDetail.userName }}</el-descriptions-item>
        <el-descriptions-item label="应付金额">￥{{ orderDetail.payAmount }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ orderDetail.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ orderDetail.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ orderDetail.receiverAddress }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="orderDetail.items || []" style="margin-top: 16px">
        <el-table-column label="商品" prop="productName" />
        <el-table-column label="单价" prop="productPrice" width="100">
          <template #default="scope">￥{{ scope.row.productPrice }}</template>
        </el-table-column>
        <el-table-column label="数量" prop="quantity" width="80" />
        <el-table-column label="小计" prop="totalAmount" width="100">
          <template #default="scope">￥{{ scope.row.totalAmount }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="MallOrder">
import { listOrder, getOrder, shipOrder, finishOrder, cancelOrder, refundOrder, delOrder } from '@/api/mall/order'

const { proxy } = getCurrentInstance()
const { mall_order_status } = useDict('mall_order_status')

const orderList = ref([])
const orderDetail = ref(null)
const detailOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  orderSn: undefined,
  userName: undefined,
  status: undefined
})

function getList() {
  loading.value = true
  listOrder(queryParams.value).then(res => {
    orderList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.orderId)
  multiple.value = !selection.length
}

function handleDetail(row) {
  getOrder(row.orderId).then(res => {
    orderDetail.value = res.data
    detailOpen.value = true
  })
}

function handleShip(row) {
  proxy.$modal.confirm('确认发货订单 ' + row.orderSn + '？').then(() => shipOrder(row.orderId)).then(() => {
    proxy.$modal.msgSuccess('发货成功')
    getList()
  }).catch(() => {})
}

function handleFinish(row) {
  proxy.$modal.confirm('确认完成订单 ' + row.orderSn + '？').then(() => finishOrder(row.orderId)).then(() => {
    proxy.$modal.msgSuccess('操作成功')
    getList()
  }).catch(() => {})
}

function handleCancel(row) {
  proxy.$modal.confirm('确认取消订单 ' + row.orderSn + '？').then(() => cancelOrder(row.orderId)).then(() => {
    proxy.$modal.msgSuccess('已取消')
    getList()
  }).catch(() => {})
}

function handleRefund(row) {
  proxy.$modal.confirm('确认为订单 ' + row.orderSn + ' 退款？将恢复库存并标记为已退款。').then(() => refundOrder(row.orderId)).then(() => {
    proxy.$modal.msgSuccess('已退款')
    getList()
  }).catch(() => {})
}

function handleDelete(row) {
  const orderIds = row.orderId || ids.value
  proxy.$modal.confirm('是否确认删除所选订单？').then(() => delOrder(orderIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>
