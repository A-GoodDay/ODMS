<template>
  <div class="admin-panel">
    <div class="admin-header">
      <h2>管理员面板</h2>
    </div>
    
    <div class="admin-content">
      <!-- 管理员信息 -->
      <div class="admin-info-section">
        <h3>管理员信息</h3>
        <div class="admin-info">
          <div class="info-item">
            <label>姓名：</label>
            <input v-model="adminInfo.name" type="text" />
          </div>
          <div class="info-item">
            <label>邮箱：</label>
            <input v-model="adminInfo.email" type="email" />
          </div>
          <div class="info-item">
            <label>部门：</label>
            <input v-model="adminInfo.department" type="text" />
          </div>
          <button @click="saveAdminInfo" class="save-button" :disabled="savingAdminInfo">
            {{ savingAdminInfo ? '保存中...' : '保存信息' }}
          </button>
          <div v-if="adminInfoMessage" class="message" :class="{ error: adminInfoError }">
            {{ adminInfoMessage }}
          </div>
        </div>
      </div>
      
      <!-- 公文录入 -->
      <div class="document-entry-section">
        <h3>{{ documentForm.id ? '编辑公文' : '录入新公文' }}</h3>
        <div class="document-form">
          <div class="form-group">
            <label>公文标题：</label>
            <input v-model="documentForm.title" type="text" class="form-control" />
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>发文部门：</label>
              <select v-model="documentForm.department" class="form-control">
                <option value="">请选择部门</option>
                <option value="办公厅">办公厅</option>
                <option value="人事部">人事部</option>
                <option value="财务部">财务部</option>
                <option value="法务部">法务部</option>
              </select>
            </div>
            
            <div class="form-group">
              <label>公文类型：</label>
              <select v-model="documentForm.type" class="form-control">
                <option value="">请选择类型</option>
                <option value="通知">通知</option>
                <option value="报告">报告</option>
                <option value="请示">请示</option>
                <option value="决定">决定</option>
              </select>
            </div>
          </div>
          
          <div class="form-group">
            <label>公文内容：</label>
            <div class="editor-toolbar">
              <button @click="formatText('bold')" title="粗体"><b>B</b></button>
              <button @click="formatText('italic')" title="斜体"><i>I</i></button>
              <button @click="formatText('underline')" title="下划线"><u>U</u></button>
              <button @click="insertHeading(1)" title="标题1">H1</button>
              <button @click="insertHeading(2)" title="标题2">H2</button>
              <button @click="insertHeading(3)" title="标题3">H3</button>
              <button @click="insertList('unordered')" title="无序列表">•</button>
              <button @click="insertList('ordered')" title="有序列表">1.</button>
            </div>
            <div 
              ref="editor" 
              contenteditable="true" 
              class="rich-editor"
              @input="updateContent"
              v-html="documentForm.content"
            ></div>
          </div>
          
          <div class="form-actions">
            <button @click="saveDocument" class="save-button" :disabled="savingDocument">
              {{ savingDocument ? '保存中...' : '保存公文' }}
            </button>
            <button @click="resetForm" class="reset-button">重置表单</button>
          </div>
          <div v-if="documentMessage" class="message" :class="{ error: documentError }">
            {{ documentMessage }}
          </div>
        </div>
      </div>
      
      <!-- 公文管理 -->
      <div class="document-management-section">
        <h3>公文管理</h3>
        <div class="management-controls">
          <input 
            v-model="searchTerm" 
            type="text" 
            placeholder="搜索公文..." 
            class="search-input"
            @keyup.enter="searchDocuments"
          />
          <button @click="searchDocuments" class="search-button">搜索</button>
        </div>
        
        <div v-if="loadingDocuments" class="loading">加载中...</div>
        <div v-else-if="documents.length === 0" class="no-documents">暂无公文</div>
        <div v-else class="documents-list">
          <div 
            v-for="doc in documents" 
            :key="doc.id" 
            class="document-item"
          >
            <div class="document-info">
              <h4>{{ doc.title }}</h4>
              <div class="doc-meta">
                <span>{{ getTypeName(doc.type) }} | {{ doc.department }} | {{ doc.updatedAt }}</span>
              </div>
            </div>
            <div class="document-actions">
              <button @click="editDocument(doc)" class="edit-button">编辑</button>
              <button @click="deleteDocument(doc.id)" class="delete-button">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import apiService from '../api.js'

// 管理员信息
const adminInfo = reactive({
  name: '',
  email: '',
  department: ''
})

// 公文表单
const documentForm = reactive({
  id: null,
  title: '',
  department: '办公厅',
  type: '通知',
  date: new Date().toISOString().substr(0, 10),
  content: ''
})

// 文档列表
const documents = ref([])

// 搜索词
const searchTerm = ref('')

// 富文本编辑器引用
const editor = ref(null)

// 状态标志
const savingAdminInfo = ref(false)
const savingDocument = ref(false)
const loadingDocuments = ref(false)
const adminInfoMessage = ref('')
const documentMessage = ref('')
const adminInfoError = ref(false)
const documentError = ref(false)

// 部门映射
const departmentMap = {
  '办公厅': '办公厅',
  '人事部': '人事部',
  '财务部': '财务部',
  '法务部': '法务部'
}

// 公文类型映射
const typeMap = {
  '通知': '通知',
  '报告': '报告',
  '请示': '请示',
  '决定': '决定'
}

// 获取部门名称
function getDepartmentName(departmentKey) {
  // 直接返回部门名称，因为现在直接使用中文值
  return departmentKey;
}

// 获取类型名称
function getTypeName(typeKey) {
  // 直接返回类型名称，因为现在直接使用中文值
  return typeKey;
}

// 保存管理员信息
async function saveAdminInfo() {
  adminInfoMessage.value = ''
  adminInfoError.value = false
  savingAdminInfo.value = true
  
  try {
    // 获取当前管理员信息以确保传递用户名
    const currentAdminInfo = await apiService.getAdminProfile();
    
    // 传递用户名和需要更新的字段
    const data = await apiService.updateAdminProfile({
      username: currentAdminInfo.username,
      name: adminInfo.name,
      email: adminInfo.email,
      department: adminInfo.department
    })
    
    // 检查返回的数据
    if (!data) {
      throw new Error('返回数据为空');
    }
    
    // 更新本地数据
    adminInfo.name = data.name || adminInfo.name;
    adminInfo.email = data.email || adminInfo.email;
    adminInfo.department = data.department || adminInfo.department;
    
    adminInfoMessage.value = '管理员信息保存成功';
  } catch (error) {
    adminInfoError.value = true;
    adminInfoMessage.value = error.message || '保存失败';
    console.error('保存管理员信息失败:', error);
  } finally {
    savingAdminInfo.value = false;
  }
}

// 格式化文本
function formatText(command) {
  document.execCommand(command, false, null)
  updateContent()
}

// 插入标题
function insertHeading(level) {
  document.execCommand('formatBlock', false, `h${level}`)
  updateContent()
}

// 插入列表
function insertList(type) {
  const command = type === 'unordered' ? 'insertUnorderedList' : 'insertOrderedList'
  document.execCommand(command, false, null)
  updateContent()
}

// 更新内容
function updateContent() {
  if (editor.value) {
    documentForm.content = editor.value.innerHTML
  }
}

// 保存公文
async function saveDocument() {
  if (!documentForm.title) {
    showMessage('请输入公文标题', true, 'document')
    return
  }
  
  // 检查标题是否已存在（仅在创建新公文时检查）
  if (!documentForm.id) {
    const isTitleExists = documents.value.some(doc => doc.title === documentForm.title)
    if (isTitleExists) {
      showMessage('公文标题已存在，请修改标题', true, 'document')
      return
    }
  }

  savingDocument.value = true
  documentMessage.value = ''
  documentError.value = false
  
  try {
    // 准备文档数据
    const docData = {
      title: documentForm.title,
      type: documentForm.type,
      department: documentForm.department, // 直接传递选择的部门值
      content: documentForm.content,
      excerpt: documentForm.content.substring(0, 200) + '...' || '暂无摘要'
    }
    
    let data
    if (documentForm.id) {
      // 更新现有公文
      docData.id = documentForm.id
      data = await apiService.updateDocument(docData)
      showMessage('公文更新成功', false, 'document')
    } else {
      // 添加新公文
      data = await apiService.createDocument(docData)
      showMessage('公文保存成功', false, 'document')
    }
    
    // 更新表单
    documentForm.id = data?.id || documentForm.id
    documentForm.title = data?.title || documentForm.title
    documentForm.type = data?.type || documentForm.type
    documentForm.department = data?.department || documentForm.department
    
    // 根据返回的部门中文名称查找对应的代码
    if (data?.department) {
      documentForm.department = Object.keys(departmentMap).find(
        key => departmentMap[key] === data.department
      ) || documentForm.department
    }
    
    documentForm.content = data?.content || documentForm.content
    
    // 更新文档列表
    await loadDocuments()
  } catch (error) {
    showMessage(error.message || '操作失败', true, 'document')
  } finally {
    savingDocument.value = false
  }
}

// 重置表单
function resetForm() {
  documentForm.id = null
  documentForm.title = ''
  documentForm.department = '办公厅'
  documentForm.type = '通知'
  documentForm.date = new Date().toISOString().substr(0, 10)
  documentForm.content = ''
  
  if (editor.value) {
    editor.value.innerHTML = ''
  }
  
  documentMessage.value = ''
  documentError.value = false
}

// 编辑公文
function editDocument(doc) {
  documentForm.id = doc.id
  documentForm.title = doc.title
  documentForm.type = doc.type
  documentForm.department = doc.department || '办公厅'
  documentForm.content = doc.content || ''
  
  if (editor.value) {
    editor.value.innerHTML = doc.content || ''
  }
  
  // 滚动到表单顶部
  document.querySelector('.document-entry-section').scrollIntoView({ behavior: 'smooth' })
}

// 删除公文
async function deleteDocument(id) {
  if (confirm('确定要删除这篇公文吗？')) {
    try {
      await apiService.deleteDocument(id)
      showMessage('公文删除成功', false, 'document')
      await loadDocuments()
    } catch (error) {
      showMessage(error.message || '删除失败', true, 'document')
    }
  }
}

// 搜索公文
async function searchDocuments() {
  await loadDocuments()
}

// 显示消息
function showMessage(message, isError = false, type = 'admin') {
  if (type === 'admin') {
    adminInfoMessage.value = message
    adminInfoError.value = isError
  } else {
    documentMessage.value = message
    documentError.value = isError
  }
  
  // 3秒后自动清除消息
  setTimeout(() => {
    if (type === 'admin') {
      adminInfoMessage.value = ''
      adminInfoError.value = false
    } else {
      documentMessage.value = ''
      documentError.value = false
    }
  }, 3000)
}

// 加载文档列表
async function loadDocuments() {
  loadingDocuments.value = true
  try {
    // 使用适配后端API的搜索方法
    const data = await apiService.searchDocuments({
      keyword: searchTerm.value
    })
    
    // 确保数据格式正确
    let documentsData = []
    
    // 判断返回数据是否为数组
    if (Array.isArray(data)) {
      documentsData = data
    } 
    // 检查数据是否包含文档数组字段
    else if (data && data.documents && Array.isArray(data.documents)) {
      documentsData = data.documents
    } 
    // 默认情况
    else {
      documentsData = []
    }
    
    // 更新文档列表
    documents.value = documentsData.map(item => ({
      id: item.id,
      title: item.title,
      type: item.type,
      department: item.department || '未指定', // 添加默认值
      updatedAt: item.updatedAt || ''
    }))
  } catch (error) {
    showMessage(error.message || '加载失败', true, 'document')
    documents.value = []
  } finally {
    loadingDocuments.value = false
  }
}


// 组件挂载时初始化
onMounted(async () => {
  // 获取管理员信息
  try {
    const data = await apiService.getAdminProfile()
    adminInfo.name = data.name
    adminInfo.email = data.email
    adminInfo.department = data.department
  } catch (error) {
    showMessage(error.message || '获取管理员信息失败', true, 'admin')
  }
  
  // 初始化编辑器内容
  if (editor.value) {
    editor.value.innerHTML = documentForm.content
  }
  
  // 加载文档列表
  await loadDocuments()
})
</script>

<style scoped>
.admin-panel {
  padding: 20px;
}

.admin-header h2 {
  color: #333;
  margin-top: 0;
}

.admin-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.admin-info-section,
.document-entry-section,
.document-management-section {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.admin-info-section h3,
.document-entry-section h3,
.document-management-section h3 {
  margin-top: 0;
  color: #333;
  border-bottom: 1px solid #dee2e6;
  padding-bottom: 10px;
}

.admin-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-item label {
  width: 80px;
  font-weight: bold;
}

.info-item input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.save-button,
.reset-button,
.edit-button,
.delete-button,
.search-button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.save-button {
  background-color: #007bff;
  color: white;
}

.save-button:hover:not(:disabled) {
  background-color: #0056b3;
}

.save-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.reset-button,
.search-button {
  background-color: #6c757d;
  color: white;
}

.reset-button:hover,
.search-button:hover {
  background-color: #545b62;
}

.edit-button {
  background-color: #28a745;
  color: white;
  margin-right: 5px;
}

.edit-button:hover {
  background-color: #218838;
}

.delete-button {
  background-color: #dc3545;
  color: white;
}

.delete-button:hover {
  background-color: #c82333;
}

.document-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group label {
  font-weight: bold;
}

.form-control {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
}

.editor-toolbar {
  display: flex;
  gap: 5px;
  padding: 5px;
  background-color: #eee;
  border: 1px solid #ddd;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
}

.editor-toolbar button {
  padding: 5px 10px;
  background-color: #fff;
  border: 1px solid #ccc;
  border-radius: 3px;
  cursor: pointer;
}

.editor-toolbar button:hover {
  background-color: #f8f9fa;
}

.rich-editor {
  min-height: 200px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 0 0 4px 4px;
  background-color: #fff;
  outline: none;
}

.rich-editor:focus {
  border-color: #007bff;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.management-controls {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.loading,
.no-documents {
  text-align: center;
  padding: 20px;
  color: #6c757d;
}

.documents-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.document-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #fff;
  border: 1px solid #dee2e6;
  border-radius: 4px;
}

.document-info h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.doc-meta {
  font-size: 14px;
  color: #6c757d;
}

.message {
  padding: 10px;
  margin-top: 10px;
  border-radius: 4px;
  text-align: center;
}

.message:not(.error) {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}
</style>