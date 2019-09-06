package cn.yang.common.netty;

/**
 * @author Cool-Coding
 *         2018/8/3
 */
public interface INettyClient {
    /**
     * 连接服务器
     */
    void connect(String server) throws Exception;
}
