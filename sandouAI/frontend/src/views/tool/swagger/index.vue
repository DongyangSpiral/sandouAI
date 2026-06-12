<template>
  <div class="page-container">
    <el-alert title="系统接口文档 - Swagger / Knife4j" type="success" :closable="false" show-icon style="margin-bottom:16px">
      <template #default>
        <div style="margin-top:8px">点击下方按钮打开 API 接口文档（需后端集成 Knife4j 配置）</div>
      </template>
    </el-alert>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" @click="openDoc" style="cursor:pointer;text-align:center">
          <el-icon :size="48" color="#409eff"><Document /></el-icon>
          <div style="margin-top:12px;font-weight:600;font-size:16px">Knife4j 接口文档</div>
          <div style="color:#909399;margin-top:4px">http://localhost:8080/doc.html</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" style="text-align:center">
          <el-icon :size="48" color="#67c23a"><Connection /></el-icon>
          <div style="margin-top:12px;font-weight:600;font-size:16px">Swagger JSON</div>
          <div style="color:#909399;margin-top:4px">/v3/api-docs</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" style="text-align:center">
          <el-icon :size="48" color="#e6a23c"><DataAnalysis /></el-icon>
          <div style="margin-top:12px;font-weight:600;font-size:16px">API 统计</div>
          <div style="color:#909399;margin-top:4px">{{ apiCount }} 个接口</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:20px">
      <template #header><span style="font-weight:600">接口列表</span></template>
      <el-table :data="apiList" stripe border size="small">
        <el-table-column prop="group" label="模块" width="120" />
        <el-table-column prop="method" label="方法" width="80"><template #default="{row}"><el-tag :type="methodType(row.method)" size="small">{{row.method}}</el-tag></template></el-table-column>
        <el-table-column prop="path" label="路径" />
        <el-table-column prop="desc" label="说明" />
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
function openDoc() { window.open('http://localhost:8080/doc.html', '_blank') }
function methodType(m) { return m==='GET'?'success':m==='POST'?'primary':m==='PUT'?'warning':m==='DELETE'?'danger':'info' }
const apiCount = ref(42)
const apiList = ref([
  { group:'系统管理', method:'POST', path:'/api/system/login', desc:'管理员登录' },
  { group:'系统管理', method:'GET', path:'/api/system/user/list', desc:'用户列表' },
  { group:'系统管理', method:'POST', path:'/api/system/user', desc:'新增用户' },
  { group:'系统管理', method:'GET', path:'/api/system/dept/list', desc:'部门列表' },
  { group:'系统管理', method:'GET', path:'/api/system/post/list', desc:'岗位列表' },
  { group:'系统管理', method:'GET', path:'/api/system/dict/type/list', desc:'字典类型列表' },
  { group:'系统管理', method:'GET', path:'/api/system/dict/data/list', desc:'字典数据列表' },
  { group:'系统管理', method:'GET', path:'/api/system/config/list', desc:'参数列表' },
  { group:'系统管理', method:'GET', path:'/api/system/notice/list', desc:'公告列表' },
  { group:'系统监测', method:'GET', path:'/api/monitor/server', desc:'服务监控' },
  { group:'系统监测', method:'GET', path:'/api/monitor/cache', desc:'缓存监控' },
  { group:'系统工具', method:'GET', path:'/api/tool/gen/tables', desc:'代码生成-表列表' },
  { group:'系统工具', method:'POST', path:'/api/tool/gen/generate', desc:'代码生成' },
  { group:'首页', method:'GET', path:'/api/dashboard/stats', desc:'统计卡片' },
  { group:'首页', method:'GET', path:'/api/dashboard/chart', desc:'图表数据' }
])
</script>
