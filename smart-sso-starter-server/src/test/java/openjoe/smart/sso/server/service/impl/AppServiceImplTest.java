package openjoe.smart.sso.server.service.impl;

import openjoe.smart.sso.server.entity.App;
import openjoe.smart.sso.server.mapper.AppMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppServiceImplTest {

    @InjectMocks
    private AppServiceImpl appService;

    @Mock
    private AppMapper appMapper;

    private App testApp;

    @BeforeEach
    void setUp() {
        testApp = new App();
        testApp.setAppId("test-app");
        testApp.setAppSecret("test-secret");
        testApp.setIsEnable(true);
        // 预注册的回调地址，以逗号分隔
        testApp.setRedirectUri("http://localhost:8080/callback,https://client.com/oauth/callback");
    }

    @Test
    void testValidate_Success() {
        when(appMapper.selectByAppId("test-app")).thenReturn(testApp);

        Boolean isValid = appService.validate("test-app", "test-secret", "http://localhost:8080/callback");

        assertTrue(isValid);
    }

    @Test
    void testValidate_SuccessWithSecondRedirectUri() {
        when(appMapper.selectByAppId("test-app")).thenReturn(testApp);

        Boolean isValid = appService.validate("test-app", "test-secret", "https://client.com/oauth/callback");

        assertTrue(isValid);
    }

    @Test
    void testValidate_AppNotFound() {
        when(appMapper.selectByAppId("unknown-app")).thenReturn(null);

        Boolean isValid = appService.validate("unknown-app", "test-secret", "http://localhost:8080/callback");

        assertFalse(isValid);
    }

    @Test
    void testValidate_WrongSecret() {
        when(appMapper.selectByAppId("test-app")).thenReturn(testApp);

        Boolean isValid = appService.validate("test-app", "wrong-secret", "http://localhost:8080/callback");

        assertFalse(isValid);
    }

    @Test
    void testValidate_AppDisabled() {
        testApp.setIsEnable(false);
        when(appMapper.selectByAppId("test-app")).thenReturn(testApp);

        Boolean isValid = appService.validate("test-app", "test-secret", "http://localhost:8080/callback");

        assertFalse(isValid);
    }

    @Test
    void testValidate_InvalidRedirectUri() {
        when(appMapper.selectByAppId("test-app")).thenReturn(testApp);

        // 一个未注册的回调地址
        Boolean isValid = appService.validate("test-app", "test-secret", "http://malicious-site.com/callback");

        assertFalse(isValid);
    }

    @Test
    void testValidate_NullRedirectUri() {
        when(appMapper.selectByAppId("test-app")).thenReturn(testApp);

        // 传入的回调地址为null
        Boolean isValid = appService.validate("test-app", "test-secret", null);

        assertFalse(isValid);
    }

    @Test
    void testValidate_EmptyRedirectUri() {
        when(appMapper.selectByAppId("test-app")).thenReturn(testApp);

        // 传入的回调地址为空字符串
        Boolean isValid = appService.validate("test-app", "test-secret", "");

        assertFalse(isValid);
    }

    @Test
    void testValidate_AppHasNoRegisteredRedirectUri() {
        testApp.setRedirectUri(null); // 模拟应用未注册任何回调地址
        when(appMapper.selectByAppId("test-app")).thenReturn(testApp);

        Boolean isValid = appService.validate("test-app", "test-secret", "http://localhost:8080/callback");

        assertFalse(isValid);
    }
}
