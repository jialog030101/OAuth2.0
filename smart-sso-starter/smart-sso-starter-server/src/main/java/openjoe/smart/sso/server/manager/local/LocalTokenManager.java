package openjoe.smart.sso.server.manager.local;

import openjoe.smart.sso.base.entity.ExpirationPolicy;
import openjoe.smart.sso.base.entity.ExpirationWrapper;
import openjoe.smart.sso.server.entity.TokenContent;
import openjoe.smart.sso.server.manager.AbstractTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地调用凭证管理
 *
 * @author Joe
 */
public class LocalTokenManager extends AbstractTokenManager implements ExpirationPolicy {

    private final Logger logger = LoggerFactory.getLogger(LocalTokenManager.class);
    private Map<String, ExpirationWrapper<String>> accessTokenMap = new ConcurrentHashMap<>();
    private Map<String, ExpirationWrapper<TokenContent>> refreshTokenMap = new ConcurrentHashMap<>();
    private Map<String, Set<String>> tgtMap = new ConcurrentHashMap<>();

    public LocalTokenManager(int accessTokenTimeout, int refreshTokenTimeout, int threadPoolSize) {
        super(accessTokenTimeout, refreshTokenTimeout, threadPoolSize);
    }

    @Override
    public void create(String refreshToken, TokenContent tokenContent) {
        // TODO: (第五周) 请同学们在此处实现创建AccessToken和RefreshToken的逻辑
        //
        // 1. 存储 Access Token -> Refresh Token 的映射
        //    - Key: tokenContent.getAccessToken()
        //    - Value: refreshToken
        //    - 过期时间: getAccessTokenTimeout()
        //    - 提示: new ExpirationWrapper<>(refreshToken, getAccessTokenTimeout())
        //
        // 2. 存储 Refresh Token -> TokenContent 的映射
        //    - Key: refreshToken
        //    - Value: tokenContent
        //    - 过期时间: getRefreshTokenTimeout()
        //    - 提示: new ExpirationWrapper<>(tokenContent, getRefreshTokenTimeout())
        //
        // 3. 存储 TGT -> Refresh Token 的映射关系，用于单点登出
        //    - Key: tokenContent.getTgt()
        //    - Value: 一个包含 refreshToken 的 Set 集合
        //    - 提示: 使用 computeIfAbsent(key, k -> new HashSet<>()).add(value) 来优雅地处理Set不存在的情况
        //
        // 4. (可选) 添加日志
    }

    @Override
    public TokenContent get(String refreshToken) {
        ExpirationWrapper<TokenContent> wrapper = refreshTokenMap.get(refreshToken);
        if (wrapper == null || wrapper.checkExpired()) {
            return null;
        } else {
            return wrapper.getObject();
        }
    }

    @Override
    public TokenContent getByAccessToken(String accessToken) {
       // TODO: (第五周) 请同学们在此处实现通过AccessToken获取Token内容的逻辑
        //
        // 1. 从 accessTokenMap 中根据 accessToken 获取包装对象
        //
        // 2. 检查包装对象是否存在或已过期，如果无效则返回 null
        //
        // 3. 如果有效，从包装对象中获取 refreshToken
        //
        // 4. 调用 get(refreshToken) 方法，获取并返回完整的 TokenContent
        
        return null; // 请替换为正确的返回值
    }

    @Override
    public void remove(String refreshToken) {
        // 删除refreshToken
        ExpirationWrapper<TokenContent> wrapper = refreshTokenMap.remove(refreshToken);
        if (wrapper == null) {
            return;
        }

        // 删除accessToken
        accessTokenMap.remove(wrapper.getObject().getAccessToken());

        // 删除tgt映射中的refreshToken
        Set<String> refreshTokenSet = tgtMap.get(wrapper.getObject().getTgt());
        if (CollectionUtils.isEmpty(refreshTokenSet)) {
            return;
        }
        refreshTokenSet.remove(refreshToken);
    }

    @Override
    public void removeByTgt(String tgt) {
        // 删除tgt映射中的refreshToken集合
       // TODO: (第六周) 请同学们在此处实现通过TGT吊销所有相关令牌的逻辑
        //
        // 1. 从 tgtMap 中根据 TGT(tgt) 移除并获取其关联的所有 refreshToken 集合
        //
        // 2. 检查集合是否为空，如果为空则直接返回
        //
        // 3. 如果集合不为空，调用 submitRemoveToken(refreshTokenSet) 方法，
        //    将吊销任务提交到线程池进行异步处理。
    }

    @Override
    public void processRemoveToken(String refreshToken) {
        // TODO: (第六周) 请同学们在此处实现真正处理令牌吊销的逻辑
        //
        // 1. 从 refreshTokenMap 中移除并获取对应的包装对象，如果不存在则直接返回
        //
        // 2. 从包装对象中获取 TokenContent
        //
        // 3. 从 accessTokenMap 中根据 TokenContent 中的 accessToken 移除对应的条目
        //
        // 4. 调用 sendLogoutRequest(logoutUri, accessToken) 方法，通知客户端应用下线
        //    提示: tokenContent.getLogoutUri(), tokenContent.getAccessToken()
    }

    @Override
    public Map<String, Set<String>> getClientIdMapByTgt(Set<String> tgtSet) {
        Map<String, Set<String>> clientIdMap = new HashMap<>();
        tgtSet.forEach(tgt -> {
            Set<String> refreshTokenSet = tgtMap.get(tgt);
            Set<String> clientIdSet = new HashSet<>();
            refreshTokenSet.forEach(refreshToken -> {
                ExpirationWrapper<TokenContent> wrapper = refreshTokenMap.get(refreshToken);
                if (wrapper == null) {
                    return;
                }
                TokenContent tokenContent = wrapper.getObject();
                if (tokenContent == null) {
                    return;
                }
                clientIdSet.add(tokenContent.getClientId());
            });
            clientIdMap.put(tgt, clientIdSet);
        });
        return clientIdMap;
    }

    @Override
    public void verifyExpired() {
        accessTokenMap.forEach((accessToken, wrapper) -> {
            if (wrapper.checkExpired()) {
                accessTokenMap.remove(accessToken);
                logger.debug("调用凭证已失效, accessToken:{}", accessToken);
            }
        });

        refreshTokenMap.forEach((refreshToken, wrapper) -> {
            if (wrapper.checkExpired()) {
                remove(refreshToken);
                logger.debug("刷新凭证已失效, accessToken:{}, refreshToken:{}", wrapper.getObject().getAccessToken(), refreshToken);
            }
        });
    }
}
