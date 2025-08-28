package openjoe.smart.sso.server.manager.local;

import openjoe.smart.sso.base.entity.ExpirationPolicy;
import openjoe.smart.sso.base.entity.ExpirationWrapper;
import openjoe.smart.sso.server.entity.TicketGrantingTicketContent;
import openjoe.smart.sso.server.manager.AbstractTicketGrantingTicketManager;
import openjoe.smart.sso.server.manager.AbstractTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地登录凭证管理
 *
 * @author Joe
 */
public class LocalTicketGrantingTicketManager extends AbstractTicketGrantingTicketManager implements ExpirationPolicy {

    protected final Logger logger = LoggerFactory.getLogger(LocalTicketGrantingTicketManager.class);
    private Map<String, ExpirationWrapper<TicketGrantingTicketContent>> tgtMap = new ConcurrentHashMap<>();

    public LocalTicketGrantingTicketManager(int timeout, String cookieName, AbstractTokenManager tokenManager) {
        super(timeout, cookieName, tokenManager);
    }

    @Override
    public void create(String tgt, TicketGrantingTicketContent tgtContent) {
        // TODO: 请同学们在此处实现创建TGT的逻辑
        //
        // 1. 将 TGT 内容(tgtContent)包装成一个带过期时间的对象
        //    提示: new ExpirationWrapper<>(tgtContent, getTimeout())
        //
        // 2. 将包装后的对象存入 tgtMap 中，键为 TGT(tgt)，值为包装对象
        //
        // 3. (可选) 添加日志记录，表示凭证创建成功
        //    提示: logger.debug("登录凭证创建成功, tgt:{}", tgt);
    }

    @Override
    public TicketGrantingTicketContent get(String tgt) {
       // TODO: 请同学们在此处实现获取TGT的逻辑
        //
        // 1. 从 tgtMap 中根据 TGT(tgt) 获取包装对象
        //
        // 2. 判断包装对象是否存在，或者是否已经过期
        //    提示: wrapper == null || wrapper.checkExpired()
        //    如果不存在或已过期，返回 null
        //
        // 3. 如果存在且未过期，返回包装对象中的 TGT 内容
        //    提示: wrapper.getObject()
        
        return null; // 请替换为正确的返回值
    }

    @Override
    public void remove(String tgt) {
        // TODO: 请同学们在此处实现移除TGT的逻辑
        //
        // 1. 从 tgtMap 中根据 TGT(tgt) 移除对应的条目
        //
        // 2. (可选) 添加日志记录，表示凭证删除成功
        //    提示: logger.debug("登录凭证删除成功, tgt:{}", tgt);
    }

    @Override
    public void refresh(String tgt) {
        ExpirationWrapper<TicketGrantingTicketContent> wrapper = tgtMap.get(tgt);
        if (wrapper != null) {
            wrapper.setExpired(System.currentTimeMillis() + getTimeout() * 1000);
        }
    }

    @Override
    public Map<String, TicketGrantingTicketContent> getTgtMap(Set<Long> userIds, Long current, Long size) {
        Map<String, TicketGrantingTicketContent> map = new LinkedHashMap<>();
        // 计算分页起始位置
        long start = (current - 1) * size;
        long end = start + size;
        long count = 0;

        for (Map.Entry<String, ExpirationWrapper<TicketGrantingTicketContent>> entry : tgtMap.entrySet()) {
            TicketGrantingTicketContent tgtContent = entry.getValue().getObject();
            if (CollectionUtils.isEmpty(userIds) || userIds.contains(tgtContent.getUserId())) {
                // 只有当count在分页范围内时才添加到结果中
                if (count >= start && count < end) {
                    map.put(entry.getKey(), tgtContent);
                }
                count++;

                // 如果已经收集到足够的数据，提前退出循环
                if (count >= end) {
                    break;
                }
            }
        }
        return map;
    }

    @Override
    public void verifyExpired() {
        tgtMap.forEach((tgt, wrapper) -> {
            if (wrapper.checkExpired()) {
                remove(tgt);
            }
        });
    }
}
