package cn.haniel.netty.server.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hanping
 */
@Slf4j
public class ServiceFactory {

    private static final Map<Class<?>, Object> MAP = new ConcurrentHashMap<>();

    static {
        try {
            Class<?> interfaceClass = Class.forName("cn.haniel.netty.server.service.HelloService");
            Class<?> instanceClass = Class.forName("cn.haniel.netty.server.service.HelloServiceImpl");
            MAP.put(interfaceClass, instanceClass.newInstance());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据 接口类 获取 实现类
     *
     * @param interfaceClass 接口类
     * @param <T>            实现类
     * @return 实现类
     */
    public static <T> T getService(Class<T> interfaceClass) {
        return (T) MAP.get(interfaceClass);
    }
}