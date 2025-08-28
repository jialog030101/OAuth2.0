package openjoe.smart.sso.server.manager.local;

import openjoe.smart.sso.server.entity.TokenContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class LocalTokenManagerTest {

    private LocalTokenManager tokenManager;

    @BeforeEach
    void setUp() {
        // AT超时2秒, RT超时4秒
        tokenManager = new LocalTokenManager(2, 4, 1);
    }

    private TokenContent createDummyTokenContent(String accessToken, String tgt) {
        TokenContent content = new TokenContent();
        content.setAccessToken(accessToken);
        content.setTgt(tgt);
        content.setUserId(1L);
        content.setClientId("test-client");
        content.setLogoutUri("http://client.com/logout");
        return content;
    }

    @Test
    void testCreateAndGetByAccessToken_Success() {
        TokenContent content = createDummyTokenContent("AT-1", "TGT-1");
        tokenManager.create("RT-1", content);

        TokenContent retrievedContent = tokenManager.getByAccessToken("AT-1");
        assertNotNull(retrievedContent);
        assertEquals("TGT-1", retrievedContent.getTgt());
    }

    @Test
    void testAccessTokenExpiration() throws InterruptedException {
        TokenContent content = createDummyTokenContent("AT-EXP", "TGT-1");
        tokenManager.create("RT-EXP", content);

        // 等待超过AT超时时间
        Thread.sleep(2100);

        assertNull(tokenManager.getByAccessToken("AT-EXP"));
        // 但RT应该仍然有效
        assertNotNull(tokenManager.get("RT-EXP"));
    }

    @Test
    void testRefreshTokenExpiration() throws InterruptedException {
        TokenContent content = createDummyTokenContent("AT-RT-EXP", "TGT-1");
        tokenManager.create("RT-RT-EXP", content);

        // 等待超过RT超时时间
        Thread.sleep(4100);

        assertNull(tokenManager.getByAccessToken("AT-RT-EXP"));
        assertNull(tokenManager.get("RT-RT-EXP"));
    }

    @Test
    void testRemoveByTgt_SingleSignOut() {
        // 用户在两个客户端登录，获取了两个令牌
        TokenContent content1 = createDummyTokenContent("AT-SLO-1", "TGT-SLO");
        tokenManager.create("RT-SLO-1", content1);

        TokenContent content2 = createDummyTokenContent("AT-SLO-2", "TGT-SLO");
        tokenManager.create("RT-SLO-2", content2);

        assertNotNull(tokenManager.getByAccessToken("AT-SLO-1"));
        assertNotNull(tokenManager.getByAccessToken("AT-SLO-2"));

        // 用户在一个客户端登出，触发TGT吊销
        tokenManager.removeByTgt("TGT-SLO");

        // 验证所有令牌都被吊销
        assertNull(tokenManager.getByAccessToken("AT-SLO-1"));
        assertNull(tokenManager.getByAccessToken("AT-SLO-2"));
        assertNull(tokenManager.get("RT-SLO-1"));
        assertNull(tokenManager.get("RT-SLO-2"));
    }

    @Test
    void testRefreshTokenRotationAndTheftDetection() {
        TokenContent originalContent = createDummyTokenContent("AT-ROTATE-1", "TGT-ROTATE");
        tokenManager.create("RT-ROTATE-1", originalContent);

        // 第一次刷新，应该成功，并返回新的AT和RT
        TokenContent refreshedContent = tokenManager.refresh("RT-ROTATE-1");
        assertNotNull(refreshedContent);
        assertNotEquals("AT-ROTATE-1", refreshedContent.getAccessToken());
        
        String newRefreshToken = refreshedContent.getRefreshToken();
        assertNotEquals("RT-ROTATE-1", newRefreshToken);

        // 验证旧令牌已失效
        assertNull(tokenManager.getByAccessToken("AT-ROTATE-1"));
        assertNull(tokenManager.get("RT-ROTATE-1"));

        // 此时，如果攻击者使用旧的RT("RT-ROTATE-1")来刷新
        // 这是一个模拟盗用检测的测试
        TokenContent stolenAttemptResult = tokenManager.refresh("RT-ROTATE-1");
        assertNull(stolenAttemptResult, "Stolen refresh token usage should be detected and fail");

        // [重要] 盗用检测的副作用是，整个令牌家族都应该被吊销
        // 验证新的RT也已失效
        assertNull(tokenManager.get(newRefreshToken), "Token family should be revoked upon theft detection");
    }

    @Test
    void testProcessRemoveToken_SendsLogoutRequest() {
        // 这个测试需要更复杂的Mocking来验证异步方法和HTTP调用
        // 这里我们用一个简化的方式来验证核心逻辑
        final AtomicBoolean logoutSent = new AtomicBoolean(false);

        // 重写 sendLogoutRequest 方法用于测试
        LocalTokenManager testManager = new LocalTokenManager(2, 4, 1) {
            @Override
            protected void sendLogoutRequest(String logoutUri, String accessToken) {
                assertEquals("http://client.com/logout", logoutUri);
                assertEquals("AT-PROC-REM", accessToken);
                logoutSent.set(true);
            }
        };

        TokenContent content = createDummyTokenContent("AT-PROC-REM", "TGT-PROC-REM");
        testManager.create("RT-PROC-REM", content);

        // 直接调用 processRemoveToken 来模拟异步执行
        testManager.processRemoveToken("RT-PROC-REM");

        assertTrue(logoutSent.get(), "Logout request should have been sent");
        // 验证令牌已被清除
        assertNull(testManager.get("RT-PROC-REM"));
        assertNull(testManager.getByAccessToken("AT-PROC-REM"));
    }
}
