package ODMS.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response<T> {
    private Integer code;       // 响应状态码
    private  String message;    // 提示信息
    private T data;             //响应数据

    public static <E> Response<E> success() {
        return new Response<E>(200, "success", null);
    }

    public static <E> Response<E> success(E data) {
        return new Response<E>(200, "success", data);
    }

    public static <E> Response<E> error(String message) {
        return new Response<E>(500, message, null);
    }

}
