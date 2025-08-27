package ODMS.mapper;

import ODMS.entity.Admin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminMapper {

    @Select("select * from admins where username=#{username}")
    Admin findByUsername(String username);

    @Insert("INSERT INTO admins (username, password, name, email, department, created_at, updated_at)" +
            " VALUES (#{username}, #{password}, #{name}, #{email}, #{department}, now(), now())")
    void insertAdmin(Admin admin);

    @Update("update admins set email=#{email},department=#{department},updated_at=now() where id=#{id}")
    void update(Admin admin);

    @Update("update admins set password=#{pwd},updated_at=now() where id=#{id}")
    void updatePwd(Integer id, String pwd);
}