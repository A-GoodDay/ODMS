package ODMS.service.impl;

import ODMS.entity.Admin;
import ODMS.mapper.AdminMapper;
import ODMS.service.AdminService;
import ODMS.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.util.Map;

import static java.time.LocalDateTime.now;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Admin createAdmin(String username, String password, String name, String email, String department) {
        //加密
        String enpw =DigestUtils.md5DigestAsHex(password.getBytes());

        Admin newAdmin = new Admin();
        newAdmin.setUsername(username);
        newAdmin.setPassword(enpw);
        newAdmin.setName(name);
        newAdmin.setEmail(email);
        newAdmin.setDepartment(department);
        newAdmin.setCreatedAt(now());
        newAdmin.setUpdatedAt(now());

        adminMapper.insertAdmin(newAdmin); // 插入数据库
        return newAdmin;
    }

    @Override
    public Admin findByUsername(String username) {
        return adminMapper.findByUsername(username);
    }

    @Override
    public Integer check(String username, String password) {
        Admin login = findByUsername(username);
        if(login == null){
            return 0;
        }
        String enpw =DigestUtils.md5DigestAsHex(password.getBytes());
        if(enpw.equals(login.getPassword()))
            return 1;
        else
            return 2;
    }

    @Override
    public void update(Admin admin) {
        admin.setUpdatedAt(now());
        adminMapper.update(admin);
    }

    @Override
    public void changePwd(String newPwd) {
        Map<String,Object> map = ThreadLocalUtil.getThreadLocal();
        Integer id = (Integer) map.get("id");
        adminMapper.updatePwd(id, DigestUtils.md5DigestAsHex(newPwd.getBytes()));
    }
}
