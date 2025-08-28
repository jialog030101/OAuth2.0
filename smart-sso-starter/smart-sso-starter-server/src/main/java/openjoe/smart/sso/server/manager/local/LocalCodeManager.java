package openjoe.smart.sso.server.manager.local;

import openjoe.smart.sso.base.entity.ExpirationPolicy;
import openjoe.smart.sso.base.entity.ExpirationWrapper;
import openjoe.smart.sso.server.entity.CodeContent;
import openjoe.smart.sso.server.manager.AbstractCodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地授权码管理
 *
 * @author Joe
 */
public class LocalCodeManager extends AbstractCodeManager implements ExpirationPolicy {

    protected final Logger logger = LoggerFactory.getLogger(LocalCodeManager.class);
    private Map<String, ExpirationWrapper<CodeContent>> codeMap = new ConcurrentHashMap<>();

    public LocalCodeManager(int timeout) {
        super(timeout);
    }

    @Override
    public void create(String code, CodeContent codeContent) {
      // TODO: 请同学们在此处实现创建授权码的逻辑
        //
        // 1. 将授权码内容(codeContent)包装成一个带过期时间的对象
        //    提示: new ExpirationWrapper<>(codeContent, getTimeout())
        //
        // 2. 将包装后的对象存入 codeMap 中，键为授权码(code)，值为包装对象
        //
        // 3. (可选) 添加日志记录，表示授权码创建成功
        //    提示: logger.debug("授权码创建成功, code:{}", code);
    }

    @Override
    public CodeContent get(String code) {
       // TODO: 请同学们在此处实现获取授权码的逻辑
        //
        // 1. 从 codeMap 中根据授权码(code)获取包装对象
        //
        // 2. 判断包装对象是否存在，或者是否已经过期
        //    提示: wrapper == null || wrapper.checkExpired()
        //    如果不存在或已过期，返回 null
        //
        // 3. 如果存在且未过期，返回包装对象中的授权码内容
        //    提示: wrapper.getObject()
        
        return null; // 请替换为正确的返回值
    }

    @Override
    public void remove(String code) {
       
    }

    @Override
    public void verifyExpired() {
        codeMap.forEach((code, wrapper) -> {
            if (wrapper.checkExpired()) {
                remove(code);
                logger.debug("授权码已失效, code:{}", code);
            }
        });
    }
}
