<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-select v-model="search.loginType" placeholder="登录方式" clearable style="width:140px">
          <el-option label="密码登录" value="password" />
          <el-option label="短信登录" value="sms" />
          <el-option label="企业登录" value="enterprise" />
        </el-select>
        <el-date-picker v-model="search.beginTime" type="datetime" placeholder="开始时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:200px" />
        <el-date-picker v-model="search.endTime" type="datetime" placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:200px" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="loginType" label="登录方式" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.loginType === 'password'" type="primary">密码登录</el-tag>
            <el-tag v-else-if="row.loginType === 'sms'" type="warning">短信登录</el-tag>
            <el-tag v-else-if="row.loginType === 'enterprise'" type="info">企业登录</el-tag>
            <el-tag v-else>{{ row.loginType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="username" label="登录名" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="错误信息" />
        <el-table-column label="登录时间" width="170">
          <template #default="{ row }">{{ row.createTime }}</template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { getLogList } from '../../../api/uas'

const search = reactive({ loginType: '', beginTime: '', endTime: '' })
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchData() {
  try {
    const res = await getLogList({ pageNum: pageNum.value, pageSize: pageSize.value, ...search })
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {}
}

fetchData()
</script>

<style scoped>
.page-container { height: 100%; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
</style>
