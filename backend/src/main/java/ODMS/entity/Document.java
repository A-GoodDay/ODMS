package ODMS.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Document {
    private Integer id;     //主键、自动递增
    private String title;   //非空、唯一
    private String type;    //非空
    private String content; //非空、富文本
    private String excerpt; //非空
    private String department;
    @JsonIgnore
    private Integer creator;//非空
    @JsonIgnore
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
