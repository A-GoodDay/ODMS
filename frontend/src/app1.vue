<template>
  <div class="introduction">
    <h2>公文检索系统介绍</h2>
    <p>这是一个为大型单位设计的公文检索系统，方便单位内部人员查找下载历年公文。</p>
    
    <div class="features">
      <h3>系统功能</h3>
      <div class="feature-group">
        <h4>普通用户功能</h4>
        <ul>
          <li>设置文档筛选条件</li>
          <li>输入关键词进行标题或全文搜索</li>
          <li>查看和下载公文</li>
        </ul>
      </div>
      
      <div class="feature-group">
        <h4>管理员功能</h4>
        <ul>
          <li>管理个人信息</li>
          <li>录入各类公文（使用富文本编辑器）</li>
          <li>设置筛选条件（发文时间、部门、类型等）</li>
          <li>管理已有公文（编辑、删除）</li>
        </ul>
      </div>
    </div>
    
    <div class="instructions">
      <h3>使用说明</h3>
      <p>点击顶部导航按钮可在"用户检索"和"管理员入口"之间切换：</p>
      <ul>
        <li><strong>用户检索</strong>：用于搜索和查看公文</li>
        <li><strong>管理员入口</strong>：用于录入和管理公文</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
// 这里可以添加一些交互逻辑
</script>

<style scoped>
.introduction {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.features {
  margin: 30px 0;
}

.feature-group {
  margin-bottom: 20px;
}

.feature-group h4 {
  color: #007bff;
  border-bottom: 1px solid #eee;
  padding-bottom: 5px;
}

.instructions {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.instructions ul {
  margin: 10px 0;
}

.instructions li {
  margin-bottom: 8px;
}
</style>