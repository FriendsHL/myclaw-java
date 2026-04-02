package com.openclaw.app;

import com.openclaw.api.config.OpenClawConfig;
import com.openclaw.core.agent.ChatModelResolver;
import com.openclaw.core.plugin.PluginRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class OpenClawApplicationTest {

    @Autowired
    private OpenClawConfig config;

    @Autowired
    private PluginRegistry pluginRegistry;

    @Autowired
    private ChatModelResolver chatModelResolver;

    @Test
    void contextLoads() {
        // Spring 上下文能正常启动
    }

    @Test
    void configBindsFromYaml() {
        // 验证 @ConfigurationProperties 正确绑定
        assertNotNull(config);
        assertEquals(18789, config.getGateway().getPort());
        assertFalse(config.getAgents().isEmpty(), "应该有至少一个 agent 配置");
        assertTrue(config.getAgents().containsKey("main"), "应该有 main agent");
        assertEquals("gpt-4o", config.getAgents().get("main").getModel());
    }

    @Test
    void telegramPluginDiscovered() {
        // 验证 Telegram 插件被发现并注册
        assertTrue(pluginRegistry.get("telegram").isPresent(),
                "Telegram 插件应该被自动发现");
        assertEquals("Telegram",
                pluginRegistry.get("telegram").get().meta().getDisplayName());
    }

    @Test
    void chatModelResolverRegistered() {
        // 验证 ChatModelResolver bean 存在（测试环境下是默认实现或 OpenAI 实现均可）
        assertNotNull(chatModelResolver, "ChatModelResolver bean 应该存在");
    }
}
