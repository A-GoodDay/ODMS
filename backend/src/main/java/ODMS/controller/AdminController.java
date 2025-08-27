package ODMS.controller;

import ODMS.common.Response;
import ODMS.entity.Admin;
import ODMS.service.AdminService;
import ODMS.utils.JwtUtil;
import ODMS.utils.ThreadLocalUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //账户登录
    @PostMapping("/login")
    public Response<String> login(@RequestParam("username")String username,@RequestParam("password")String password) {
        Admin login = adminService.findByUsername(username);
        int res = adminService.check(username, password);
        if (res == 0)
            return Response.error("The user does not exist");
        if (res == 2)
            return Response.error("The username or password is incorrect");

        Map<String, Object> map = new HashMap<>();
        map.put("id", login.getId());
        map.put("username", login.getUsername());

        //生成token并存储到redis中
        String token = JwtUtil.getToken(map);
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set(token,token,1, TimeUnit.HOURS);
        return Response.success(token);
    }

    //退出登录
    @PostMapping("/logout")
    public Response<String> logout(@RequestHeader("Authorization") String token) {
        //退出登录后删除redis中对应的token
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.getOperations().delete(token);
        return Response.success();
    }

    // 创建新管理账户
    @PostMapping("/admin")
    public Response<?> createAdmin(@RequestParam Map<String, String> req) {
        try {
            Admin newAdmin = adminService.findByUsername(req.get("username"));
            if (newAdmin != null) {
                return Response.error("The username already exists");
            }
            newAdmin = adminService.createAdmin(
                    req.get("username"),
                    req.get("password"),
                    req.get("name"),
                    req.get("email"),
                    req.get("department")
            );
            return Response.success();
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    //显示账户个人信息
    @GetMapping("/admin")
    public Response<Admin> getInfo() {
        //从线程中读取解析后的token数据
        Map<String,Object>map = ThreadLocalUtil.getThreadLocal();
        String username = map.get("username").toString();

        Admin admin = adminService.findByUsername(username);
        return Response.success(admin);
    }

    //更新个人信息
    @PutMapping("/admin")
    public Response<?> update(@RequestParam String username,
                              @RequestParam(required = false) String name,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String department) {
        //查找管理员信息
        Admin admin = adminService.findByUsername(username);
        
        //检查管理员是否存在
        if (admin == null) {
            return Response.error("管理员不存在");
        }
        
        //只更新提供了的字段
        if (name != null && !name.isEmpty()) {
            admin.setName(name);
        }
        if (email != null && !email.isEmpty()) {
            admin.setEmail(email);
        }
        if (department != null && !department.isEmpty()) {
            admin.setDepartment(department);
        }

        adminService.update(admin);
        return Response.success(admin);
    }

    //修改密码
    @PatchMapping("/admin")
    public Response<?> changePwd(@RequestBody Map<String, String> req, @RequestHeader("Authorization") String token) {
        //校验参数
        String oldPwd = req.get("old_pwd");
        String newPwd = req.get("new_pwd");
        String rePwd = req.get("re_pwd");
        if(StringUtils.isEmpty(oldPwd) || StringUtils.isEmpty(newPwd) || StringUtils.isEmpty(rePwd)){
            return Response.error("Required parameter missing");
        }
        //原密码是否正确
        Map<String,Object> map = ThreadLocalUtil.getThreadLocal();
        Admin admin = adminService.findByUsername(map.get("username").toString());
        if(!admin.getPassword().equals(DigestUtils.md5DigestAsHex(oldPwd.getBytes()))){
            return Response.error("The password doesn't match");
        }
        if(!rePwd.equals(newPwd)){
            return Response.error("New password doesn't match");
        }
        adminService.changePwd(newPwd);
        //修改成功后删除redis中对应的token
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.getOperations().delete(token);

        return Response.success();
    }