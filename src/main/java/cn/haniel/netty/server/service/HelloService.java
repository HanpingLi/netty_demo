package cn.haniel.netty.server.service;

/**
 * RPC 调用的服务
 *
 * @author hanping
 */
public interface HelloService {

    String sayHello(String msg);
}