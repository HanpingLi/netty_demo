package cn.haniel.netty.server.session;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author hanping
 */
public class GroupSessionMemoryImpl implements GroupSession {
    private final Map<String, Group> groupMap = new ConcurrentHashMap<>();

    @Override
    public Group createGroup(String name, Set<String> members) {
        Group group = new Group(name, members);
        // 没有则放入
        return groupMap.putIfAbsent(name, group);
    }

    @Override
    public Group joinMember(String name, String member) {
        return groupMap.computeIfPresent(name, (key, value) -> {
            // 指定 key 的值进行重新计算，前提是该 key 存在于 hashMap 中
            value.getMembers().add(member);
            return value;
        });
    }

    @Override
    public Group removeMember(String name, String member) {
        return groupMap.computeIfPresent(name, (key, value) -> {
            value.getMembers().remove(member);
            return value;
        });
    }

    @Override
    public Group removeGroup(String name) {
        return groupMap.remove(name);
    }

    @Override
    public Set<String> getMembers(String name) {
        return groupMap.getOrDefault(name, Group.EMPTY_GROUP).getMembers();
    }

    /**
     * 根据 【群聊名称】 -> 【用户名Set】 -> map遍历 -> 【用户名获取到 所有对应的 channel】 -> 【channel List】
     *
     * @param name 组名
     * @return
     */
    @Override
    public List<Channel> getMembersChannel(String name) {
        return getMembers(name).stream()
                // 根据成员名 获得Channel
                .map(member -> SessionFactory.getSession().getChannel(member))
                // 不是 null 才会 被下面收集
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}