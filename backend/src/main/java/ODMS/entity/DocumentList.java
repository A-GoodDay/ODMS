package ODMS.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentList {
    private Integer id;
    private String title;
    private String type;
    private String department;
    private LocalDateTime updatedAt;
}
