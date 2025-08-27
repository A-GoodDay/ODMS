package ODMS.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.Map;

public class JwtUtil {
    private static final String KEY = "test";

    //接收数据，生成token并返回
    public static String getToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims",claims)
                .withExpiresAt(new Date(System.currentTimeMillis()+1000*3600*12))   //token有效期为12小时
                .sign(Algorithm.HMAC256(KEY));
    }

    //接收token，验证后返回数据
    public static Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(KEY))
                .build()
                .verify(token)
                .getClaim("claims").asMap();
    }
}
