package openjoe.smart.sso.server.manager.local;

import openjoe.smart.sso.server.entity.TicketGrantingTicketContent;
import openjoe.smart.sso.server.manager.AbstractTokenManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LocalTicketGrantingTicketManagerTest {

    private LocalTicketGrantingTicketManager tgtManager;

    @Mock
    private AbstractTokenManager tokenManager; // 尽管是Mock，但必须存在以满足构造函数

    @BeforeEach
    void setUp() {
        // 设置一个较短的超时时间方便测试，例如3秒
        tgtManager = new LocalTicketGrantingTicketManager(3, tokenManager);
    }

    @Test
    void testCreateAndGet_Success() {
        TicketGrantingTicketContent content = new TicketGrantingTicketContent(1L, "test-client");
        tgtManager.create("TGT-1", content);

        TicketGrantingTicketContent retrievedContent = tgtManager.get("TGT-1");

        assertNotNull(retrievedContent);
        assertEquals(1L, retrievedContent.getUserId());
    }

    @Test
    void testGet_NotFound() {
        assertNull(tgtManager.get("TGT-NON-EXISTENT"));
    }

    @Test
    void testRemove() {
        TicketGrantingTicketContent content = new TicketGrantingTicketContent(1L, "test-client");
        tgtManager.create("TGT-1", content);
        assertNotNull(tgtManager.get("TGT-1"));

        tgtManager.remove("TGT-1");
        assertNull(tgtManager.get("TGT-1"));
    }

    @Test
    void testExpiration() throws InterruptedException {
        TicketGrantingTicketContent content = new TicketGrantingTicketContent(1L, "test-client");
        tgtManager.create("TGT-EXPIRING", content);

        // 等待超过3秒，让TGT过期
        Thread.sleep(3100);

        assertNull(tgtManager.get("TGT-EXPIRING"));
    }

    @Test
    void testRefresh_SlidingWindow() throws InterruptedException {
        TicketGrantingTicketContent content = new TicketGrantingTicketContent(1L, "test-client");
        tgtManager.create("TGT-REFRESH", content);

        // 在过期前（例如2秒后）刷新它
        Thread.sleep(2000);
        tgtManager.refresh("TGT-REFRESH");

        // 再等待2秒，此时总时间已超过3秒，但因为刷新过，应该仍然有效
        Thread.sleep(2000);
        assertNotNull(tgtManager.get("TGT-REFRESH"));

        // 再等待2秒，此时距离上次刷新已超过3秒，应该过期了
        Thread.sleep(2000);
        assertNull(tgtManager.get("TGT-REFRESH"));
    }

    @Test
    void testMaxSessionsPerUser() {
        long userId = 99L;
        int maxSessions = 5; // 假设最大会话数为5

        // 创建5个会话，应该都成功
        for (int i = 1; i <= maxSessions; i++) {
            TicketGrantingTicketContent content = new TicketGrantingTicketContent(userId, "client-" + i);
            tgtManager.create("TGT-" + i, content);
            assertNotNull(tgtManager.get("TGT-" + i));
        }

        // 创建第6个会话
        TicketGrantingTicketContent content6 = new TicketGrantingTicketContent(userId, "client-6");
        tgtManager.create("TGT-6", content6);

        // 验证第6个会话是存在的
        assertNotNull(tgtManager.get("TGT-6"));

        // 验证第一个会话（最旧的）是否已被挤出
        // 注意：由于ConcurrentHashMap的实现细节，被移除的不一定是第一个，但肯定有一个被移除了。
        // 一个更可靠的测试是检查总数是否仍为5。
        long activeSessions = 0;
        for (int i = 1; i <= maxSessions + 1; i++) {
            if (tgtManager.get("TGT-" + i) != null) {
                activeSessions++;
            }
        }
        assertEquals(maxSessions, activeSessions, "Active sessions should not exceed the limit");
    }
}
