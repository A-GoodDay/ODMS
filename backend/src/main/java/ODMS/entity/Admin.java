package ODMS.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Admin {
    private Integer id;      //主键、自动递增
    private String username; //非空、唯一
    @JsonIgnore //返回个人信息时忽略用户密码
    private String password; //非空
    private String name;     //非空
    private String email;
    private String department;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
