package openjoe.smart.sso.server.manager.local;

import openjoe.smart.sso.server.entity.CodeContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class LocalCodeManagerTest {

    private LocalCodeManager codeManager;

    @BeforeEach
    void setUp() {
        // 设置超时时间为2秒用于测试
        codeManager = new LocalCodeManager(2);
    }

    @Test
    void testCreateAndGetAndRemove_Success() {
        CodeContent content = new CodeContent(1L, "test-client", "http://localhost/callback");
        codeManager.create("CODE-123", content);

        // 使用 getAndRemove 获取并移除
        CodeContent retrievedContent = codeManager.getAndRemove("CODE-123");

        assertNotNull(retrievedContent);
        assertEquals(1L, retrievedContent.getUserId());

        // 验证已被移除
        assertNull(codeManager.getAndRemove("CODE-123"));
    }

    @Test
    void testExpiration() throws InterruptedException {
        CodeContent content = new CodeContent(1L, "test-client", "http://localhost/callback");
        codeManager.create("CODE-EXPIRING", content);

        // 等待超过2秒使其过期
        Thread.sleep(2100);

        assertNull(codeManager.getAndRemove("CODE-EXPIRING"));
    }

    @Test
    void testPkceValidation_S256_Success() throws NoSuchAlgorithmException {
        String codeVerifier = "this-is-a-long-and-random-string-for-pkce";
        
        // 生成 code_challenge
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes());
        String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

        CodeContent content = new CodeContent(1L, "test-client", "http://localhost/callback");
        content.setCodeChallenge(codeChallenge);
        content.setCodeChallengeMethod("S256");

        assertTrue(codeManager.validateCodeVerifier(codeVerifier, content));
    }

    @Test
    void testPkceValidation_S256_Failure() throws NoSuchAlgorithmException {
        String codeVerifier = "this-is-a-long-and-random-string-for-pkce";
        String wrongVerifier = "this-is-a-DIFFERENT-string";

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes());
        String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

        CodeContent content = new CodeContent(1L, "test-client", "http://localhost/callback");
        content.setCodeChallenge(codeChallenge);
        content.setCodeChallengeMethod("S256");

        // 使用错误的 verifier 进行验证
        assertFalse(codeManager.validateCodeVerifier(wrongVerifier, content));
    }

    @Test
    void testPkceValidation_NoPkce_ShouldPass() {
        // 没有设置PKCE信息，应该直接通过验证
        CodeContent content = new CodeContent(1L, "test-client", "http://localhost/callback");
        assertTrue(codeManager.validateCodeVerifier("any-verifier", content));
    }

    @Test
    void testPkceValidation_UnsupportedMethod_ShouldFail() {
        CodeContent content = new CodeContent(1L, "test-client", "http://localhost/callback");
        content.setCodeChallenge("some-challenge");
        content.setCodeChallengeMethod("MD5"); // 不支持的方法

        // 假设 validateCodeVerifier 对不支持的方法返回 false
        assertFalse(codeManager.validateCodeVerifier("any-verifier", content));
    }
}
