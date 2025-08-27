// API服务模块
const API_BASE_URL = '/api'; // 使用相对路径，通过代理转发到后端

class ApiService {
  constructor() {
    this.baseUrl = API_BASE_URL;
    this.token = localStorage.getItem('adminToken');
  }

  // 设置认证令牌
  setToken(token) {
    this.token = token;
    localStorage.setItem('adminToken', token);
  }

  // 清除认证令牌
  clearToken() {
    this.token = null;
    localStorage.removeItem('adminToken');
  }

  // 通用请求方法
  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    
    const config = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      },
      ...options
    };

    // 如果有令牌，添加到请求头
    if (this.token) {
      config.headers.Authorization = `${this.token}`; // 不使用Bearer前缀
    }

    try {
      console.log('发送请求:', url, config); // 添加请求日志
      const response = await fetch(url, config);
      console.log('收到响应:', response.status, response.statusText); // 添加响应日志
      
      // 检查响应状态
      if (!response.ok) {
        let errorMessage = '请求失败';
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || `HTTP错误: ${response.status}`;
        } catch (e) {
          errorMessage = `HTTP错误: ${response.status}`;
        }
        throw new Error(errorMessage);
      }
      
      // 尝试解析JSON响应
      let data;
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
        console.log('解析后的数据:', data); // 添加数据日志
      } else {
        // 如果响应不是JSON格式，返回一个默认结构
        data = {
          code: response.status === 200 ? 200 : 500,
          message: response.status === 200 ? 'success' : '请求失败',
          data: null
        };
      }
      
      return data;
    } catch (error) {
      console.error('API请求错误:', error);
      throw error;
    }
  }

  // 认证相关API
  async login(username, password) {
    // 使用表单数据格式发送登录请求
    const formData = new FormData();
    formData.append('username', username);
    formData.append('password', password);
    
    const data = await this.request('/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({username, password}).toString()
    });
    
    if (data.code === 200) {
      this.setToken(data.data);
      return data.data;
    }
    
    throw new Error(data.message || '登录失败');
  }

  async logout() {
    try {
      const data = await this.request('/logout', {
        method: 'POST'
      });
      
      this.clearToken();
      return data;
    } catch (error) {
      // 即使服务器登出失败，也要清除本地令牌
      this.clearToken();
      throw error;
    }
  }

  // 管理员相关API
  async getAdminProfile() {
    const data = await this.request('/admin');
    return data.data;
  }

  async updateAdminProfile(profileData) {
    // 获取当前管理员信息以获得用户名
    const currentAdmin = await this.getAdminProfile();
    
    // 合并用户名和要更新的字段数据
    const requestData = {
      username: currentAdmin.username,
      ...profileData
    };
    
    // 使用x-www-form-urlencoded格式传递数据
    const data = await this.request('/admin', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams(requestData).toString()
    });
    
    if (data && data.code === 200) {
      return data.data;
    }
    
    throw new Error(data.message || '更新失败');
  }

  async changePassword(passwordData) {
    const data = await this.request('/admin', {
      method: 'PATCH',
      body: JSON.stringify(passwordData)
    });
    return data.data;
  }

  async registerAdmin(adminData) {
    const formData = new FormData();
    formData.append('username', adminData.username);
    formData.append('password', adminData.password);
    formData.append('name', adminData.name);
    formData.append('email', adminData.email);
    formData.append('department', adminData.department);
    
    const data = await this.request('/admin', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams(adminData).toString()
    });
    return data.data;
  }

  // 公文相关API
  async searchDocuments(params = {}) {
    // 构建查询参数
    const queryParams = new URLSearchParams();
    Object.keys(params).forEach(key => {
      if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
        // 支持数组参数（如日期范围）
        if (Array.isArray(params[key])) {
          params[key].forEach(value => {
            queryParams.append(key, value);
          });
        } else {
          queryParams.append(key, params[key]);
        }
      }
    });
    
    const queryString = queryParams.toString();
    const endpoint = `/document${queryString ? `?${queryString}` : ''}`;
    
    try {
      const response = await this.request(endpoint, {
        method: 'GET'
      });
      
      // 检查响应数据格式
      if (response && response.code === 200) {
        return response.data || [];
      } else {
        console.error('搜索接口返回异常:', response);
        return [];
      }
    } catch (error) {
      console.error('搜索文档时出错:', error);
      return [];
    }
  }

  async getDocument(id) {
    const response = await this.request(`/document/${id}`, {
      method: 'GET'
    });
    
    if (response && response.code === 200) {
      return response.data;
    }
    
    throw new Error(response.message || '获取文档失败');
  }

  async createDocument(documentData) {
    // 确保excerpt字段不为空
    const docData = {
      ...documentData,
      excerpt: documentData.excerpt || documentData.content?.substring(0, 200) || '暂无摘要'
    };
    
    const response = await this.request('/admin/doc', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(docData)
    });
    
    if (response && response.code === 200) {
      return response.data;
    }
    
    // 特别处理标题重复的情况
    if (response && response.message && response.message.includes('Duplicate entry')) {
      throw new Error('公文标题已存在，请修改标题');
    }
    
    throw new Error(response.message || '创建文档失败');
  }

  async updateDocument(documentData) {
    // 根据后端接口文档，更新文档需要提供id字段
    const response = await this.request('/admin/doc', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        id: documentData.id.toString(),
        title: documentData.title,
        type: documentData.type,
        department: documentData.department,
        content: documentData.content,
        excerpt: documentData.excerpt || documentData.content?.substring(0, 200) || '暂无摘要'
      }).toString()
    });
    
    if (response && response.code === 200) {
      return response.data;
    }
    
    throw new Error(response.message || '更新文档失败');
  }

  async deleteDocument(id) {
    const response = await this.request('/admin/doc', {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({id: id.toString()}).toString()
    });
    
    if (response && response.code === 200) {
      return response.data;
    }
    
    throw new Error(response.message || '删除文档失败');
  }

  // 下载文档
  async downloadDocument(id) {
    try {
      const response = await fetch(`${this.baseUrl}/download/${id}`, {
        method: 'GET',
        headers: {
          'Authorization': this.token ? `${this.token}` : '',
        },
      });

      if (!response.ok) {
        let errorMessage = `HTTP错误: ${response.status}`;
        // 特别处理401错误
        if (response.status === 401) {
          errorMessage = '认证失败，请重新登录';
          // 清除本地存储的令牌
          this.clearToken();
        }
        
        try {
          const contentType = response.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            const errorData = await response.json();
            errorMessage = errorData.message || errorMessage;
          } else {
            const errorText = await response.text();
            errorMessage = errorText || errorMessage;
          }
        } catch (e) {
          console.error('解析错误响应失败:', e);
        }
        throw new Error(errorMessage);
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = window.document.createElement('a');
      link.href = url;
      
      // 尝试从Content-Disposition头中提取文件名
      const contentDisposition = response.headers.get('content-disposition');
      let filename = `document_${id}.docx`;
      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        if (filenameMatch && filenameMatch[1]) {
          filename = filenameMatch[1].replace(/['"]/g, '');
        }
      }
      
      link.download = filename;
      window.document.body.appendChild(link);
      link.click();
      window.document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('下载文档失败:', error);
      throw error;
    }
  }

  // 下载文档（备选方案：前端生成文本文件）
  async downloadDocumentAsText(id) {
    try {
      // 直接使用fetch获取文档信息，不使用认证（支持公开访问）
      const response = await fetch(`${this.baseUrl}/document/${id}`);
      
      if (!response.ok) {
        throw new Error(`HTTP错误: ${response.status}`);
      }
      
      const result = await response.json();
      
      if (result.code !== 200) {
        throw new Error(result.message || '获取文档失败');
      }
      
      const document = result.data;
      
      // 创建文档内容
      let content = '';
      content += `标题：${document.title}\n`;
      content += `类型：${document.type}\n`;
      content += `部门：${document.department}\n`;
      content += `更新时间：${document.updatedAt}\n`;
      content += `========================\n\n`;
      content += `摘要：\n${document.excerpt}\n\n`;
      content += `========================\n\n`;
      content += `正文：\n${document.content}`;
      
      // 创建Blob对象
      const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
      
      // 创建下载链接
      const url = window.URL.createObjectURL(blob);
      const link = window.document.createElement('a');
      link.href = url;
      link.download = `${document.title}.txt`;
      
      // 触发下载
      window.document.body.appendChild(link);
      link.click();
      window.document.body.removeChild(link);
      
      // 清理URL对象
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('生成文档失败:', error);
      throw error;
    }
  }
}

// 创建API服务实例
const apiService = new ApiService();

export default apiService;