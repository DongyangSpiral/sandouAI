<template>
  <div class="page-container">
    <el-alert title="在线用户 - Sa-Token 内存存储模式" type="info" :closable="false" show-icon style="margin-bottom:16px" />
    <div class="search-bar">
      <el-input v-model="searchKey" placeholder="搜索用户名/IP" style="width:200px" clearable />
      <el-button type="primary" @click="load" style="margin-left:8px">搜索</el-button>
    </div>
    <el-table :data="filteredList" stripe border>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="loginIp" label="登录IP" width="140" />
      <el-table-column prop="loginTime" label="登录时间" width="170" :formatter="(r) => r.loginTime ? new Date(r.loginTime).toLocaleString() : ''" />
      <el-table-column label="操作" width="100"><template #default="{row}">
        <el-button size="small" type="danger">强退</el-button>
      </template></el-table-column>
    </el-table>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { getOnlineUsers } from '../../../api/monitor'
const list = ref([])
const searchKey = ref('')
const filteredList = computed(() => list.value.filter(u => !searchKey.value || u.username?.includes(searchKey.value) || u.loginIp?.includes(searchKey.value)))
async function load() { try { const r = await getOnlineUsers(); list.value = r.data.data || [] } catch(e) { list.value = [] } }
onMounted(load)
</script>
