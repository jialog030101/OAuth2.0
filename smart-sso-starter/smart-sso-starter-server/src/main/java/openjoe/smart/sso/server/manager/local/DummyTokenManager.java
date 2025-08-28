package openjoe.smart.sso.server.manager.local;

import openjoe.smart.sso.server.entity.TokenContent;
import openjoe.smart.sso.server.manager.AbstractTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 一个临时的、无操作的TokenManager实现.
 * 仅用于课程前四周，以确保应用可以正常启动。
 * 在第五周时，学生将用 LocalTokenManager 替换掉它。
 */
public class DummyTokenManager extends AbstractTokenManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyTokenManager.class);

    public DummyTokenManager() {
        // 使用默认的超时和线程池大小
        super(3600, 7200, 1);
        LOGGER.warn("警告：正在使用 DummyTokenManager。这是一个临时的空实现，用于课程早期阶段。");
    }

    @Override
    public TokenContent getByAccessToken(String accessToken) {
        return null;
    }

    @Override
    public void removeByTgt(String tgt) {
        // 空实现
    }

    @Override
    public void processRemoveToken(String refreshToken) {
        // 空实现
    }

    @Override
    public Map<String, Set<String>> getClientIdMapByTgt(Set<String> tgtSet) {
        return Collections.emptyMap();
    }

    @Override
    public void create(String key, TokenContent value) {
        // 空实现
    }

    @Override
    public TokenContent get(String key) {
        return null;
    }

    @Override
    public void remove(String key) {
        // 空实现
    }
}