package openjoe.smart.sso.server;

import openjoe.smart.sso.server.manager.AbstractCodeManager;
import openjoe.smart.sso.server.manager.AbstractTicketGrantingTicketManager;
import openjoe.smart.sso.server.manager.AbstractTokenManager;
import openjoe.smart.sso.server.manager.local.DummyTokenManager;
import openjoe.smart.sso.server.manager.local.LocalCodeManager;
import openjoe.smart.sso.server.manager.local.LocalTicketGrantingTicketManager;
import openjoe.smart.sso.server.manager.local.LocalTokenManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ServerProperties.class})
public class ServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AbstractCodeManager.class)
    public AbstractCodeManager codeManager(ServerProperties properties) {
        return new LocalCodeManager(properties.getCodeTimeout());
    }

    @Bean
    @ConditionalOnMissingBean(AbstractTokenManager.class)
    public AbstractTokenManager tokenManager(ServerProperties properties) {
       // return new LocalTokenManager(properties.getAccessTokenTimeout(), properties.getTimeout(), properties.getThreadPoolSize());
        // ========================= 课程指导 (第一至四周) =========================
        // 使用下面的 DummyTokenManager，让应用在早期阶段可以正常启动。
        
        return new DummyTokenManager();
        
        // =======================================================================


        // ========================= 课程指导 (第五周开始) =========================
        // 当你开始实现 LocalTokenManager 后，请注释掉上面的 return 语句，
        // 并取消下面这段代码的注释，以启用你自己的完整实现。
        /*
        return new LocalTokenManager(
            properties.getAccessTokenTimeout(), 
            properties.getTimeout(), // 注意：这里可能是 getRefreshTokenTimeout()，请根据你的 ServerProperties 类确认
            properties.getThreadPoolSize()
        );
        */
        // =======================================================================
    }

    @Bean
    @ConditionalOnMissingBean(AbstractTicketGrantingTicketManager.class)
    public AbstractTicketGrantingTicketManager tgtManager(ServerProperties properties, AbstractTokenManager tokenManager) {
        return new LocalTicketGrantingTicketManager(properties.getTimeout(), properties.getCookieName(), tokenManager);
    }
}