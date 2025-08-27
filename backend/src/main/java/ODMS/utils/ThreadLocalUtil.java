package ODMS.utils;

public class ThreadLocalUtil {
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    //根据键获取值
    public static <T> T getThreadLocal(){
        return (T)THREAD_LOCAL.get();
    }
    //存储键值对
    public static void setThreadLocal(Object value){
        THREAD_LOCAL.set(value);
    }
    //清除键值对
    public static void removeThreadLocal(){
        THREAD_LOCAL.remove();
    }
}
