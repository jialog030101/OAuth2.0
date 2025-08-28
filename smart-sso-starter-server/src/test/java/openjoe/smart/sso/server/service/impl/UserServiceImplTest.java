package openjoe.smart.sso.server.service.impl;

import openjoe.smart.sso.base.entity.Result;
import openjoe.smart.sso.server.entity.User;
import openjoe.smart.sso.server.entity.TokenUser;
import openjoe.smart.sso.server.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserMapper userMapper;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setAccount("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setIsEnable(true);
        testUser.setPasswordLastModified(LocalDateTime.now().minusDays(30)); // 密码未过期

        // 让userService使用我们spy的passwordEncoder实例
        userService.setPasswordEncoder(passwordEncoder);
    }

    @Test
    void testValidate_Success() {
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        Result<User> result = userService.validate("testuser", "password123");

        assertTrue(result.isSuccess());
        assertEquals(testUser, result.getData());
    }

    @Test
    void testValidate_UserNotFound() {
        when(userMapper.selectByUsername("unknown")).thenReturn(null);

        Result<User> result = userService.validate("unknown", "password123");

        assertFalse(result.isSuccess());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    void testValidate_WrongPassword() {
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        Result<User> result = userService.validate("testuser", "wrongpassword");

        assertFalse(result.isSuccess());
        assertEquals("密码错误", result.getMessage());
    }

    @Test
    void testValidate_UserDisabled() {
        testUser.setIsEnable(false);
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        Result<User> result = userService.validate("testuser", "password123");

        assertFalse(result.isSuccess());
        assertEquals("用户已被禁用", result.getMessage());
    }

    @Test
    void testValidate_AccountLocked() {
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        // 模拟5次登录失败
        for (int i = 0; i < 5; i++) {
            userService.validate("testuser", "wrongpassword");
        }

        // 第6次尝试
        Result<User> result = userService.validate("testuser", "password123");

        assertFalse(result.isSuccess());
        assertEquals("账号已被锁定，请10分钟后再试", result.getMessage());
    }

    @Test
    void testValidate_PasswordExpired() {
        testUser.setPasswordLastModified(LocalDateTime.now().minusDays(91)); // 密码已过期
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        Result<User> result = userService.validate("testuser", "password123");

        assertFalse(result.isSuccess());
        assertEquals("密码已过期，请重置密码", result.getMessage());
    }

    @Test
    void testValidate_LoginSuccessClearsFailureCache() {
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        // 模拟2次失败
        userService.validate("testuser", "wrongpassword");
        userService.validate("testuser", "wrongpassword");

        // 第3次成功
        Result<User> successResult = userService.validate("testuser", "password123");
        assertTrue(successResult.isSuccess());

        // 第4次再失败，应该从0开始计数，不会被锁定
        Result<User> resultAfterSuccess = userService.validate("testuser", "wrongpassword");
        assertEquals("密码错误", resultAfterSuccess.getMessage());

        // 再尝试4次失败，凑够5次，第6次应该被锁定
        for (int i = 0; i < 4; i++) {
            userService.validate("testuser", "wrongpassword");
        }
        Result<User> lockedResult = userService.validate("testuser", "password123");
        assertEquals("账号已被锁定，请10分钟后再试", lockedResult.getMessage());
    }

    @Test
    void testGetTokenUser_Success() {
        when(userService.getById(1L)).thenReturn(testUser);

        TokenUser tokenUser = userService.getTokenUser(1L);

        assertNotNull(tokenUser);
        assertEquals(testUser.getId(), tokenUser.getId());
        assertEquals(testUser.getAccount(), tokenUser.getAccount());
        assertNull(tokenUser.getPassword()); // 确保密码未被包含
    }

    @Test
    void testGetTokenUser_NotFound() {
        when(userService.getById(2L)).thenReturn(null);

        TokenUser tokenUser = userService.getTokenUser(2L);

        assertNull(tokenUser);
    }
}
