package ODMS.service;

import ODMS.entity.Admin;

public interface AdminService {

    // 创建新账户
    Admin createAdmin(String username, String password, String name, String email, String department);

    //根据用户名查找用户
    Admin findByUsername(String username);

    //验证密码
    Integer check(String username, String password);

    //更新信息
    void update(Admin admin);

    //修改密码
    void changePwd(String newPwd);
}
