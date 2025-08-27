<template>
  <div class="user-search">
    <div class="search-form">
      <h2>公文检索</h2>
      <div class="search-input-group">
        <input 
          v-model="searchKeyword" 
          type="text" 
          placeholder="请输入关键词搜索公文标题或全文" 
          class="search-input"
          @keyup.enter="performSearch"
        />
        <button @click="performSearch" class="search-button" :disabled="loading">
          {{ loading ? '搜索中...' : '搜索' }}
        </button>
      </div>
      
      <div class="filters">
        <div class="filter-group">
          <label>发文时间：</label>
          <select v-model="filters.time">
            <option value="">全部时间</option>
            <option value="lastWeek">最近一周</option>
            <option value="lastMonth">最近一月</option>
            <option value="lastYear">最近一年</option>
            <option value="custom">自定义</option>
          </select>
        </div>
        
        <!-- 自定义日期筛选 -->
        <div v-if="filters.time === 'custom'" class="date-range-filter">
          <div class="date-range-label">选择日期范围：</div>
          <div class="date-range-inputs">
            <div class="date-input-wrapper">
              <input 
                type="date" 
                v-model="filters.startDate" 
                class="date-input" 
                @change="handleStartDateChange"
              />
              <span class="date-label">起始日期</span>
            </div>
            <div class="date-separator">至</div>
            <div class="date-input-wrapper">
              <input 
                type="date" 
                v-model="filters.endDate" 
                class="date-input" 
                :min="filters.startDate"
                @change="handleEndDateChange"
              />
              <span class="date-label">结束日期</span>
            </div>
          </div>
        </div>
        
        <div class="filter-group">
          <label>发文部门：</label>
          <select v-model="filters.department">
            <option value="">全部部门</option>
            <option value="办公厅">办公厅</option>
            <option value="人事部">人事部</option>
            <option value="财务部">财务部</option>
            <option value="法务部">法务部</option>
          </select>
        </div>
        
        <div class="filter-group">
          <label>公文类型：</label>
          <select v-model="filters.type">
            <option value="">全部类型</option>
            <option value="通知">通知</option>
            <option value="报告">报告</option>
            <option value="请示">请示</option>
            <option value="决定">决定</option>
          </select>
        </div>
        
        <div class="filter-actions">
          <button @click="performSearch" class="apply-filters-button">应用筛选</button>
          <button @click="resetFilters" class="reset-filters-button">重置筛选</button>
        </div>
      </div>
    </div>
    
    <div class="search-results">
      <div v-if="!searched" class="no-search">
        <p>请输入关键词进行搜索</p>
      </div>
      <div v-else-if="loading" class="loading">
        搜索中...
      </div>
      <div v-else-if="searchResults.length === 0" class="no-results">
        <p>暂无搜索结果，请调整搜索条件</p>
      </div>
      <div v-else>
        <div class="results-info">
          共找到 {{ totalResults }} 条结果
        </div>
        <div 
          v-for="doc in searchResults" 
          :key="doc.id" 
          class="result-item"
        >
          <h3>
            <a href="#" @click.prevent="viewDocument(doc)">
              {{ doc.title }}
            </a>
          </h3>
          <div class="doc-meta">
            <span>公文类型：{{ getTypeName(doc.type) }}</span>
            <span>发文部门：{{ doc.department }}</span>
            <span>更新时间：{{ doc.updatedAt }}</span>
          </div>
          <div class="doc-actions">
            <button @click="viewDocument(doc)" class="view-button">查看</button>
            <button @click="downloadDocument(doc)" class="download-button">下载</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 文档详情弹窗 -->
    <div v-if="selectedDocument" class="modal-overlay" @click="closeDocument">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ selectedDocument.title }}</h2>
          <button @click="closeDocument" class="close-button">&times;</button>
        </div>
        <div class="modal-body">
          <div class="doc-meta">
            <span>公文类型：{{ getTypeName(selectedDocument.type) }}</span>
            <span>发文部门：{{ selectedDocument.department }}</span>
            <span>更新时间：{{ selectedDocument.updatedAt }}</span>
          </div>
          <div class="doc-content" v-html="selectedDocument.content"></div>
        </div>
        <div class="modal-footer">
          <button @click="downloadDocument(selectedDocument)" class="download-button">下载</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import apiService from '../api.js'

// 搜索关键词
const searchKeyword = ref('')

// 筛选条件
const filters = reactive({
  time: '',
  startDate: '',
  endDate: '',
  department: '',
  type: ''
})

// 搜索结果
const searchResults = ref([])
const totalResults = ref(0)
const loading = ref(false)
const searched = ref(false)

// 选中的文档
const selectedDocument = ref(null)


// 部门映射
const departmentMap = {
  'office': '办公厅',
  'personnel': '人事部',
  'finance': '财务部',
  'legal': '法务部'
}

// 公文类型映射
const typeMap = {
  'notice': '通知',
  'report': '报告',
  'request': '请示',
  'decision': '决定'
}

// 获取部门名称
function getDepartmentName(departmentKey) {
  // 如果是空值或未指定，返回默认值
  if (!departmentKey || departmentKey === '未指定') {
    return '未指定';
  }
  // 如果已经是中文则直接返回
  if (['办公厅', '人事部', '财务部', '法务部'].includes(departmentKey)) {
    return departmentKey;
  }
  // 否则使用映射表转换
  return departmentMap[departmentKey] || departmentKey;
}

// 获取类型名称
function getTypeName(typeKey) {
  // 后端可能直接返回中文类型名，如果已经是中文则直接返回
  if (['通知', '报告', '请示', '决定'].includes(typeKey)) {
    return typeKey;
  }
  // 后端也可能返回英文类型名，使用映射表转换
  return typeMap[typeKey] || typeKey;
}

// 执行搜索
async function performSearch() {
  loading.value = true
  searched.value = true
  
  try {
    // 构建搜索参数
    const params = {}
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    if (filters.department) {
      params.department = filters.department
    }
    
    if (filters.type) {
      params.type = filters.type
    }
    
    // 处理时间筛选
    if (filters.time === 'custom') {
      if (filters.startDate && filters.endDate) {
        // 确保开始日期不晚于结束日期
        if (filters.startDate > filters.endDate) {
          alert('开始日期不能晚于结束日期');
          loading.value = false;
          return;
        }
        params.startDate = filters.startDate;
        params.endDate = filters.endDate;
      } else {
        alert('请选择完整的日期范围');
        loading.value = false;
        return;
      }
    } else if (filters.time && filters.time !== 'custom') {
      // 处理预设时间范围
      const now = new Date();
      let startDate = new Date();
      
      switch (filters.time) {
        case 'lastWeek':
          startDate.setDate(now.getDate() - 7);
          break;
        case 'lastMonth':
          startDate.setMonth(now.getMonth() - 1);
          break;
        case 'lastYear':
          startDate.setFullYear(now.getFullYear() - 1);
          break;
      }
      
      if (filters.time !== '') {
        params.startDate = startDate.toISOString().split('T')[0];
        params.endDate = now.toISOString().split('T')[0];
      }
    }
    
    // 调用后端API进行搜索
    const data = await apiService.searchDocuments(params)
    
    // 根据后端接口格式化数据
    // 后端搜索接口直接返回DocumentList数组，包含id, title, type, department, updatedAt字段
    if (Array.isArray(data)) {
      searchResults.value = data.map(item => ({
        id: item.id,
        title: item.title,
        type: item.type,
        department: item.department || '未指定', // 添加默认值处理
        updatedAt: item.updatedAt || '' // 使用后端返回的updatedAt字段
      }))
      totalResults.value = data.length
    } else {
      searchResults.value = []
      totalResults.value = 0
    }
  } catch (error) {
    console.error('搜索失败:', error)
    searchResults.value = []
    totalResults.value = 0
    searched.value = true
    alert('搜索失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 处理开始日期变化
function handleStartDateChange() {
  if (filters.startDate && filters.endDate && filters.startDate > filters.endDate) {
    filters.endDate = filters.startDate;
  }
}

// 处理结束日期变化
function handleEndDateChange() {
  if (filters.endDate && filters.startDate && filters.endDate < filters.startDate) {
    filters.startDate = filters.endDate;
  }
}

// 重置筛选条件
function resetFilters() {
  filters.time = ''
  filters.startDate = ''
  filters.endDate = ''
  filters.department = ''
  filters.type = ''
  searchKeyword.value = ''
  searchResults.value = []
  totalResults.value = 0
  searched.value = false
}

// 查看文档详情
async function viewDocument(doc) {
  try {
    // 获取完整文档内容
    const fullDoc = await apiService.getDocument(doc.id)
    
    // 根据后端接口文档格式化数据
    selectedDocument.value = {
      id: fullDoc.id,
      title: fullDoc.title,
      type: fullDoc.type,
      department: fullDoc.department,
      content: fullDoc.content,
      updatedAt: fullDoc.updatedAt || fullDoc.createdAt || ''
    }
  } catch (error) {
    console.error('获取文档详情失败:', error)
    alert('获取文档详情失败: ' + (error.message || '未知错误'))
  }
}

// 关闭文档详情
function closeDocument() {
  selectedDocument.value = null
}

// 下载文档
async function downloadDocument(doc) {
  try {
    // 首先尝试正常的下载方式（需要认证）
    await apiService.downloadDocument(doc.id);
  } catch (error) {
    console.error('使用标准方式下载文档失败:', error);
    let errorMessage = error.message || '未知错误';
    
    // 处理401错误（未授权）
    if (errorMessage.includes('401') || errorMessage.includes('认证失败')) {
      // 对于认证错误，提供备选方案
      if (window.confirm('需要登录才能使用标准下载功能，是否要以文本格式下载文档（无需登录）？')) {
        try {
          await apiService.downloadDocumentAsText(doc.id);
        } catch (textError) {
          console.error('备选下载方案也失败:', textError);
          window.alert('下载文档失败: ' + (textError.message || '未知错误'));
        }
        return;
      } else {
        window.alert('请登录后重新尝试下载');
        // 跳转到登录页面
        window.location.href = '/login';
        return;
      }
    }
    
    // 如果是HTTP 500错误，提供更具体的提示并尝试备选方案
    if (errorMessage.includes('500')) {
      if (window.confirm('下载服务暂时不可用，是否要以文本格式下载文档？')) {
        try {
          await apiService.downloadDocumentAsText(doc.id);
        } catch (textError) {
          console.error('备选下载方案也失败:', textError);
          window.alert('下载文档失败: ' + (textError.message || '未知错误'));
        }
        return;
      }
    }
    
    window.alert('下载文档失败: ' + errorMessage);
  }
}

// 组件挂载时初始化
onMounted(() => {
  // 页面加载时可以执行初始搜索
})
</script>

<style scoped>
.user-search {
  padding: 20px;
}

.search-form {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.search-form h2 {
  margin-top: 0;
  color: #333;
}

.search-input-group {
  display: flex;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px 0 0 4px;
  box-sizing: border-box;
}

.search-button {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: 1px solid #007bff;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
}

.search-button:hover:not(:disabled) {
  background-color: #0056b3;
}

.search-button:disabled {
  background-color: #ccc;
  border-color: #ccc;
  cursor: not-allowed;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.filter-group label {
  font-weight: bold;
}

.filter-group select,
.filter-group input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.date-input {
  width: 140px;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.apply-filters-button,
.reset-filters-button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  height: fit-content;
  align-self: flex-end;
}

.apply-filters-button {
  background-color: #007bff;
  color: white;
}

.apply-filters-button:hover {
  background-color: #0056b3;
}

.reset-filters-button {
  background-color: #6c757d;
  color: white;
}

.reset-filters-button:hover {
  background-color: #545b62;
}

.search-results {
  min-height: 200px;
}

.loading,
.no-results {
  text-align: center;
  padding: 40px 20px;
  color: #6c757d;
}

.results-info {
  padding: 10px 0;
  border-bottom: 1px solid #dee2e6;
  margin-bottom: 20px;
  color: #495057;
}

.result-item {
  padding: 20px;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  margin-bottom: 20px;
  background-color: #fff;
}

.result-item h3 {
  margin-top: 0;
}

.result-item h3 a {
  color: #007bff;
  text-decoration: none;
}

.result-item h3 a:hover {
  text-decoration: underline;
}

.doc-meta {
  display: flex;
  gap: 15px;
  margin: 10px 0;
  font-size: 14px;
  color: #6c757d;
}

.doc-excerpt {
  color: #495057;
  line-height: 1.5;
}

.doc-actions {
  margin-top: 15px;
}

.view-button,
.download-button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
}

.view-button {
  background-color: #007bff;
  color: white;
}

.view-button:hover {
  background-color: #0056b3;
}

.download-button {
  background-color: #28a745;
  color: white;
}

.download-button:hover {
  background-color: #218838;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-width: 800px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  margin: 0;
  color: #333;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6c757d;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.close-button:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.modal-body .doc-meta {
  background-color: #f8f9fa;
  padding: 10px 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.doc-content {
  line-height: 1.6;
}

.doc-content h1,
.doc-content h2,
.doc-content h3 {
  color: #333;
  margin-top: 20px;
  margin-bottom: 10px;
}

.doc-content p {
  margin: 10px 0;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #dee2e6;
  text-align: right;
}
</style>