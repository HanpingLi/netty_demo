package cn.haniel.netty.server.session;

/**
 * @author hanping
 */
public abstract class SessionFactory {

    private static final Session session = new SessionMemoryImpl();

    public static Session getSession() {
        return session;
    }
}