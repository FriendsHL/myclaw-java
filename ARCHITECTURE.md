# OpenClaw Java - Architecture Design

## 1. Overview

OpenClaw Java is the Java port of [OpenClaw](https://github.com/openclaw/openclaw) — a multi-channel AI messaging gateway with extensible plugin system. It routes messages between messaging channels (Telegram, Discord, Slack, etc.) and AI providers (OpenAI, Anthropic, etc.), with session management, authentication, and hot-reloadable configuration.

### Core Principles
- **Spring AI native**: AI provider layer directly uses Spring AI's `ChatModel` / `ChatClient` abstraction
- **Plugin-first**: channels and AI providers are all plugins, loaded via Java SPI + Spring auto-configuration
- **Account-scoped isolation**: each bot instance runs as an independent account
- **Deterministic routing**: message -> route -> session -> agent -> reply
- **Hot-reloadable config**: YAML config changes applied without restart

---

## 2. Technology Stack

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Language | Java 17 | LTS, records, sealed classes, text blocks |
| Framework | Spring Boot 3.3+ | Mature ecosystem, WebSocket/HTTP built-in |
| AI Framework | Spring AI 1.0+ | Unified ChatModel/ChatClient, multi-provider |
| WebSocket | Spring WebSocket | Gateway client connections |
| HTTP Client | Spring RestClient / WebClient | Channel API calls |
| JSON | Jackson | Ubiquitous, fast, extensible |
| Config | Spring Boot YAML + custom loader | YAML config, env var interpolation |
| Validation | Jakarta Validation (Hibernate Validator) | Config schema validation |
| Plugin System | Java SPI + Spring Boot auto-config | Extension discovery and loading |
| Build | Maven (multi-module) | Enterprise standard, dependency management |
| Test | JUnit 5 + Mockito + Spring Boot Test | Standard testing |
| Logging | SLF4J + Logback | Spring Boot default |
| DB (optional) | H2 / SQLite (via JDBC) | Session persistence |

---

## 3. Module Structure

```
openclaw-java/
├── pom.xml                              # Parent POM (dependency management)
│
├── openclaw-api/                        # [Module] Public API & SPI interfaces
│   ├── pom.xml                          # Zero Spring dependency, pure Java
│   └── src/main/java/com/openclaw/api/
│       ├── channel/                     # Channel plugin SPI
│       │   ├── ChannelPlugin.java
│       │   ├── ChannelMeta.java
│       │   ├── ChannelCapabilities.java
│       │   ├── ChannelLifecycle.java
│       │   ├── ChannelOutbound.java
│       │   ├── ChannelInbound.java
│       │   ├── ChannelConfigAdapter.java
│       │   └── ChannelSetup.java
│       ├── message/                     # Message types
│       │   ├── InboundMessage.java
│       │   ├── OutboundPayload.java
│       │   ├── Attachment.java
│       │   ├── ChatType.java
│       │   └── DeliveryResult.java
│       ├── routing/                     # Routing types
│       │   ├── RouteContext.java
│       │   ├── ResolvedRoute.java
│       │   └── RoutingBinding.java
│       ├── session/                     # Session types
│       │   ├── SessionKey.java
│       │   ├── SessionEntry.java
│       │   └── SessionStore.java
│       ├── config/                      # Config types
│       │   ├── OpenClawConfig.java
│       │   ├── GatewayConfig.java
│       │   ├── AgentConfig.java
│       │   ├── ChannelConfig.java
│       │   ├── ModelConfig.java
│       │   └── BindingConfig.java
│       ├── agent/                       # Agent types
│       │   ├── AgentContext.java
│       │   └── AgentResult.java
│       └── event/                       # Event types
│           ├── GatewayEvent.java
│           ├── MessageReceivedEvent.java
│           ├── MessageSentEvent.java
│           └── ChannelStatusEvent.java
│
├── openclaw-core/                       # [Module] Core gateway engine
│   ├── pom.xml                          # Depends on: openclaw-api, spring-ai-core
│   └── src/main/java/com/openclaw/core/
│       ├── gateway/                     # Gateway server
│       │   ├── GatewayServer.java
│       │   ├── GatewayWebSocketHandler.java
│       │   ├── GatewayRestController.java
│       │   └── GatewayAutoConfiguration.java
│       ├── routing/                     # Message routing engine
│       │   ├── MessageRouter.java
│       │   ├── DefaultMessageRouter.java
│       │   ├── BindingResolver.java
│       │   └── AllowlistChecker.java
│       ├── session/                     # Session management
│       │   ├── SessionManager.java
│       │   ├── FileSessionStore.java
│       │   └── TranscriptManager.java
│       ├── agent/                       # Agent execution (Spring AI integration)
│       │   ├── AgentRunner.java
│       │   ├── AgentRegistry.java
│       │   ├── ChatModelResolver.java   # Resolve Spring AI ChatModel by config
│       │   └── AgentPromptBuilder.java
│       ├── plugin/                      # Plugin loading
│       │   ├── PluginDiscovery.java
│       │   ├── PluginRegistry.java
│       │   └── PluginLifecycleManager.java
│       ├── config/                      # Config management
│       │   ├── ConfigLoader.java
│       │   ├── ConfigWatcher.java
│       │   └── EnvInterpolator.java
│       └── event/                       # Spring event bus integration
│           └── SpringEventBridge.java
│
├── openclaw-app/                        # [Module] Spring Boot application
│   ├── pom.xml                          # Depends on: openclaw-core, spring-boot-starter-web
│   └── src/
│       ├── main/java/com/openclaw/app/
│       │   ├── OpenClawApplication.java
│       │   ├── WebSocketConfig.java
│       │   └── SecurityConfig.java
│       └── main/resources/
│           ├── application.yml
│           └── banner.txt
│
├── openclaw-cli/                        # [Module] CLI interface (optional)
│   ├── pom.xml
│   └── src/main/java/com/openclaw/cli/
│       ├── OpenClawCli.java             # Entry point (picocli)
│       └── commands/
│           ├── GatewayRunCommand.java
│           ├── ConfigCommand.java
│           └── StatusCommand.java
│
└── extensions/                          # [Plugins] Channel & provider plugins
    ├── openclaw-channel-telegram/
    │   ├── pom.xml
    │   ├── src/main/java/com/openclaw/channel/telegram/
    │   │   ├── TelegramChannelPlugin.java
    │   │   ├── TelegramLifecycle.java
    │   │   ├── TelegramOutbound.java
    │   │   ├── TelegramInbound.java
    │   │   ├── TelegramConfigAdapter.java
    │   │   ├── TelegramConfig.java
    │   │   └── TelegramAutoConfiguration.java
    │   └── src/main/resources/
    │       ├── META-INF/services/
    │       │   └── com.openclaw.api.channel.ChannelPlugin
    │       └── META-INF/spring/
    │           └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
    │
    ├── openclaw-channel-discord/
    ├── openclaw-channel-slack/
    ├── openclaw-provider-openai/        # Wraps spring-ai-openai
    ├── openclaw-provider-anthropic/     # Wraps spring-ai-anthropic
    └── openclaw-provider-ollama/        # Wraps spring-ai-ollama
```

---

## 4. Core Interface Design

### 4.1 ChannelPlugin (SPI)

```java
/**
 * Core contract for all messaging channel plugins.
 * Register via Java SPI (META-INF/services) or Spring auto-configuration.
 */
public interface ChannelPlugin {

    /** Unique channel identifier, e.g. "telegram", "discord" */
    String id();

    /** Human-readable metadata */
    ChannelMeta meta();

    /** Feature flags */
    ChannelCapabilities capabilities();

    /** Account config resolution */
    ChannelConfigAdapter configAdapter();

    /** Lifecycle (init / connect / disconnect) */
    default ChannelLifecycle lifecycle() { return ChannelLifecycle.NOOP; }

    /** Outbound message sending */
    ChannelOutbound outbound();

    /** Inbound message listener registration */
    ChannelInbound inbound();

    /** Interactive setup wizard (optional) */
    default ChannelSetup setup() { return null; }
}
```

### 4.2 ChannelLifecycle

```java
public interface ChannelLifecycle {

    ChannelLifecycle NOOP = new ChannelLifecycle() {};

    /** Called once when plugin is loaded */
    default void init(PluginContext context) {}

    /** Called per-account when channel connects */
    default void connect(AccountContext ctx) throws Exception {}

    /** Called per-account when channel disconnects */
    default void disconnect(AccountContext ctx) throws Exception {}

    /** Called on config hot-reload */
    default void onConfigReload(OpenClawConfig newConfig) {}
}
```

### 4.3 ChannelInbound

```java
@FunctionalInterface
public interface ChannelInbound {

    /**
     * Register a listener that receives normalized inbound messages.
     * The channel plugin calls listener.accept() when a message arrives.
     */
    void register(InboundMessageListener listener);
}

@FunctionalInterface
public interface InboundMessageListener {
    void onMessage(InboundMessage message);
}
```

### 4.4 ChannelOutbound

```java
public interface ChannelOutbound {

    /**
     * Send a message to the channel.
     * @param context  target account + peer + thread info
     * @param payload  text, media, or structured message
     * @return delivery result with platform message ID
     */
    DeliveryResult send(OutboundContext context, OutboundPayload payload);
}
```

### 4.5 Spring AI Integration - ChatModelResolver

The key bridge between OpenClaw config and Spring AI:

```java
/**
 * Resolves a Spring AI ChatModel instance based on agent/model config.
 * This is the central integration point with Spring AI.
 *
 * Instead of defining our own AiProvider SPI, we leverage Spring AI's
 * ChatModel interface directly. Each AI provider is a Spring AI starter
 * (spring-ai-openai, spring-ai-anthropic, etc.) with auto-configured
 * ChatModel beans.
 */
public interface ChatModelResolver {

    /**
     * Resolve the ChatModel for a given model config.
     *
     * Example: modelId="gpt-4o" -> looks up ModelConfig -> provider="openai"
     *          -> returns the OpenAI ChatModel bean with overridden options.
     */
    ChatModel resolve(String modelId, ModelConfig modelConfig);

    /** List all available model IDs from registered providers */
    List<String> availableModels();
}
```

### 4.6 AgentRunner (Spring AI ChatClient)

```java
/**
 * Executes agent logic using Spring AI ChatClient.
 *
 * ChatClient wraps ChatModel with fluent API for:
 * - System prompts
 * - Conversation history (advisors)
 * - Function/tool calling
 * - Output parsing
 */
public class AgentRunner {

    private final ChatModelResolver chatModelResolver;
    private final SessionManager sessionManager;
    private final AgentRegistry agentRegistry;

    /**
     * Process an inbound message through the agent pipeline.
     *
     * Flow:
     * 1. Load agent config (system prompt, model, tools)
     * 2. Load session history
     * 3. Build ChatClient with Spring AI
     * 4. Call ChatModel via ChatClient
     * 5. Return AgentResult with response text
     */
    public AgentResult process(ResolvedRoute route, InboundMessage message) {
        AgentConfig agent = agentRegistry.get(route.agentId());
        ChatModel chatModel = chatModelResolver.resolve(agent.model(), ...);

        // Use Spring AI ChatClient fluent API
        ChatClient chatClient = ChatClient.builder(chatModel)
            .defaultSystem(agent.systemPrompt())
            .defaultAdvisors(new MessageChatMemoryAdvisor(/* session history */))
            .build();

        String response = chatClient.prompt()
            .user(message.text())
            .call()
            .content();

        return new AgentResult(response, ...);
    }
}
```

### 4.7 Message & Config Types

```java
// --- Message types (in openclaw-api) ---

public record InboundMessage(
    String channelId,
    String accountId,
    String peerId,
    ChatType chatType,
    String threadId,        // nullable
    String senderId,
    String senderName,
    String text,
    List<Attachment> attachments,
    Instant timestamp,
    Map<String, Object> extra
) {}

public record OutboundPayload(
    String text,
    List<Attachment> attachments,
    Map<String, Object> extra
) {}

public enum ChatType { DM, GROUP, CHANNEL }

public record DeliveryResult(
    boolean success,
    String platformMessageId,
    String errorMessage
) {}

// --- Config types (in openclaw-api) ---

public class OpenClawConfig {
    private GatewayConfig gateway;
    private Map<String, AgentConfig> agents;
    private Map<String, ChannelConfig> channels;
    private Map<String, ModelConfig> models;
    private List<BindingConfig> bindings;
    // getters, setters (Jackson deserialization)
}

public class AgentConfig {
    private String model;           // Reference to models.* key
    private String systemPrompt;
    private Integer maxTokens;
    private Double temperature;
}

public class ModelConfig {
    private String provider;        // "openai", "anthropic", "ollama"
    private String apiKey;
    private String baseUrl;         // Optional override
    private String modelName;       // Provider-specific model name
}

public class BindingConfig {
    private String channel;
    private String accountId;
    private String peer;            // nullable - specific user/group
    private String agentId;         // Target agent
}

// --- Session types ---

public record SessionKey(
    String agentId,
    String channel,
    String accountId,
    String peerId
) {
    public String toKeyString() {
        return String.format("agent:%s:%s/%s/%s", agentId, channel, accountId, peerId);
    }

    public static SessionKey parse(String key) { /* ... */ }
}

public record SessionEntry(
    String role,            // "user" | "assistant" | "system"
    String content,
    Instant timestamp,
    Map<String, Object> metadata
) {}
```

---

## 5. Spring AI Integration Architecture

The core design decision: **use Spring AI as the AI provider layer, not a custom AiProvider SPI**.

```
┌──────────────────────────────────────────────────────────────────┐
│                        OpenClaw Java                             │
│                                                                  │
│  ┌─────────────┐    ┌──────────────┐    ┌─────────────────────┐ │
│  │ Channel      │    │ Message      │    │ Agent               │ │
│  │ Plugins      │───>│ Router       │───>│ Runner              │ │
│  │ (SPI)        │    │              │    │                     │ │
│  └─────────────┘    └──────────────┘    └────────┬────────────┘ │
│                                                   │              │
│                                          ┌────────▼────────┐    │
│                                          │ ChatModelResolver│    │
│                                          │ (bridge layer)   │    │
│                                          └────────┬────────┘    │
│  ┌────────────────────────────────────────────────┼────────────┐│
│  │                    Spring AI                    │            ││
│  │  ┌──────────┐  ┌──────────┐  ┌──────────────┐ │            ││
│  │  │ OpenAI   │  │Anthropic │  │   Ollama     │ │            ││
│  │  │ChatModel │  │ChatModel │  │  ChatModel   │ │ ...        ││
│  │  └──────────┘  └──────────┘  └──────────────┘ │            ││
│  └────────────────────────────────────────────────┴────────────┘│
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌─────────────────────┐│
│  │ Session      │    │ Config       │    │ Event Bus           ││
│  │ Manager      │    │ Loader       │    │ (Spring Events)     ││
│  └──────────────┘    └──────────────┘    └─────────────────────┘│
└──────────────────────────────────────────────────────────────────┘
```

### Why Spring AI instead of custom AiProvider?

| Aspect | Custom AiProvider SPI | Spring AI ChatModel |
|--------|----------------------|---------------------|
| Provider support | Build each from scratch | 20+ providers out of box |
| Streaming | Custom callback | Reactor Flux (standard) |
| Function calling | Custom impl | Built-in tool support |
| Memory/history | Custom impl | ChatMemory advisors |
| RAG | Not included | VectorStore + advisors |
| Maintenance | All on us | Spring team maintains |

### Spring AI Dependencies per Provider

```xml
<!-- OpenAI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>

<!-- Anthropic -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-anthropic-spring-boot-starter</artifactId>
</dependency>

<!-- Ollama -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
</dependency>
```

---

## 6. Message Flow (End-to-End)

```
┌─────────────────────────────────────────────────────────────┐
│  External Messaging Platform (Telegram, Discord, Slack...) │
└──────────────────────────┬──────────────────────────────────┘
                           │ Webhook / Long Polling
                           ▼
┌──────────────────────────────────────────────────────────────┐
│  ChannelPlugin.inbound()                                     │
│  - Receive raw platform message                              │
│  - Normalize -> InboundMessage                               │
│  - Call InboundMessageListener.onMessage()                   │
└──────────────────────────┬───────────────────────────────────┘
                           │ InboundMessage
                           ▼
┌──────────────────────────────────────────────────────────────┐
│  GatewayServer (message dispatch)                            │
│  - Publish Spring ApplicationEvent: MessageReceivedEvent     │
│  - AllowlistChecker: reject if sender not allowed            │
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│  MessageRouter.resolve(RouteContext)                          │
│  - BindingResolver: peer -> account -> channel -> default    │
│  - Return ResolvedRoute { agentId, sessionKey }              │
└──────────────────────────┬───────────────────────────────────┘
                           │ ResolvedRoute
                           ▼
┌──────────────────────────────────────────────────────────────┐
│  SessionManager                                              │
│  - Load or create session by SessionKey                      │
│  - Append user message to transcript                         │
│  - Build conversation history (List<SessionEntry>)           │
└──────────────────────────┬───────────────────────────────────┘
                           │ Session + History
                           ▼
┌──────────────────────────────────────────────────────────────┐
│  AgentRunner.process()                                       │
│  1. Load AgentConfig (system prompt, model ref)              │
│  2. ChatModelResolver.resolve(modelId) -> Spring AI ChatModel│
│  3. Build ChatClient with system prompt + memory advisor     │
│  4. chatClient.prompt().user(text).call().content()          │
│  5. Append assistant message to session                      │
│  6. Return AgentResult                                       │
└──────────────────────────┬───────────────────────────────────┘
                           │ AgentResult (response text)
                           ▼
┌──────────────────────────────────────────────────────────────┐
│  ChannelPlugin.outbound().send()                             │
│  - Convert to platform-specific format                       │
│  - Call platform API (e.g. Telegram sendMessage)             │
│  - Return DeliveryResult                                     │
│  - Publish Spring Event: MessageSentEvent                    │
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  User sees reply on Telegram / Discord / Slack / ...        │
└─────────────────────────────────────────────────────────────┘
```

---

## 7. Config Schema (YAML)

```yaml
# ~/.openclaw/config.yaml

gateway:
  port: 18789
  host: "0.0.0.0"
  auth:
    mode: "token"               # token | none
    token: "${OPENCLAW_TOKEN}"

agents:
  main:
    model: "gpt-4o"
    system-prompt: "You are a helpful assistant."
    max-tokens: 4096
    temperature: 0.7
  support:
    model: "claude-sonnet"
    system-prompt: "You are a customer support agent."

channels:
  telegram:
    accounts:
      my-bot:
        token: "${TELEGRAM_BOT_TOKEN}"
        allow-from:
          - "123456789"
          - "group:-100123"
        dm-policy: "allowlist"    # open | allowlist | none
  discord:
    accounts:
      my-server:
        token: "${DISCORD_BOT_TOKEN}"
        guild-id: "123456789"

bindings:
  - channel: "telegram"
    account-id: "my-bot"
    peer: "123456789"
    agent-id: "support"           # Route this user -> support agent
  - channel: "discord"
    agent-id: "main"              # Default all Discord -> main agent

models:
  gpt-4o:
    provider: "openai"
    api-key: "${OPENAI_API_KEY}"
    model-name: "gpt-4o"
  claude-sonnet:
    provider: "anthropic"
    api-key: "${ANTHROPIC_API_KEY}"
    model-name: "claude-sonnet-4-6"
  local-llama:
    provider: "ollama"
    base-url: "http://localhost:11434"
    model-name: "llama3"
```

---

## 8. Plugin Discovery & Loading

### Dual discovery: SPI + Spring Auto-Configuration

```
JAR: openclaw-channel-telegram.jar
│
├── META-INF/services/com.openclaw.api.channel.ChannelPlugin
│   └── com.openclaw.channel.telegram.TelegramChannelPlugin
│
└── META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
    └── com.openclaw.channel.telegram.TelegramAutoConfiguration
```

**SPI** provides framework-agnostic discovery (for CLI, testing).
**Spring auto-config** provides full DI integration (for Spring Boot app).

### Loading Flow

```
Spring Boot Application Start
    │
    ├── Auto-configuration scans classpath
    │   └── TelegramAutoConfiguration registers TelegramChannelPlugin bean
    │
    ├── PluginDiscovery collects all ChannelPlugin beans
    │   └── Also falls back to ServiceLoader for non-Spring plugins
    │
    ├── PluginRegistry.register(plugin)
    │   └── Validates id uniqueness, stores in Map
    │
    ├── ConfigLoader.load("~/.openclaw/config.yaml")
    │   └── Returns OpenClawConfig with env var interpolation
    │
    ├── PluginLifecycleManager.connectAll(config)
    │   └── For each channel + each account:
    │       plugin.lifecycle().connect(accountCtx)
    │
    ├── ConfigWatcher starts (WatchService on config file)
    │
    └── GatewayServer.start()
        └── HTTP + WebSocket listening on configured port
```

---

## 9. Implementation Roadmap

### Phase 1: Foundation
- [ ] Maven multi-module setup (parent POM + all child POMs)
- [ ] `openclaw-api`: SPI interfaces (ChannelPlugin, message types, config types, session types)
- [ ] `openclaw-core`: ConfigLoader (YAML -> OpenClawConfig), PluginDiscovery, EventBus
- [ ] `openclaw-core`: SessionKey + FileSessionStore
- [ ] `openclaw-app`: Spring Boot shell (starts, loads config, discovers plugins)

### Phase 2: Routing & Agent Pipeline
- [ ] `openclaw-core`: MessageRouter + BindingResolver
- [ ] `openclaw-core`: AgentRunner + ChatModelResolver (Spring AI integration)
- [ ] `openclaw-core`: SessionManager with transcript
- [ ] `openclaw-core`: AllowlistChecker
- [ ] `openclaw-app`: REST API (`/api/status`, `/api/channels`)

### Phase 3: First Channel (Telegram)
- [ ] `openclaw-channel-telegram`: TelegramChannelPlugin
- [ ] Telegram Bot API client (long polling via TelegramBots library)
- [ ] Inbound normalization + outbound send
- [ ] SPI registration + Spring auto-config

### Phase 4: End-to-End
- [ ] Full flow: Telegram -> Router -> Agent -> Spring AI OpenAI -> Telegram reply
- [ ] Config hot-reload (WatchService)
- [ ] WebSocket endpoint for control clients
- [ ] Docker image

### Phase 5: Expand
- [ ] Discord, Slack channel plugins
- [ ] Anthropic, Ollama provider wrappers
- [ ] CLI module (picocli)
- [ ] Rate limiting, webhook support
- [ ] Media handling (images, files, voice)

---

## 10. Key Design Decisions

### Why Spring AI instead of custom AiProvider SPI?
Spring AI provides `ChatModel` / `ChatClient` with 20+ provider implementations, streaming (Flux), function calling, memory advisors, and RAG support. Building this from scratch would duplicate effort. We use Spring AI as the AI layer and focus on the channel/routing/session layer that Spring AI doesn't cover.

### Why Maven?
Enterprise standard, well-understood multi-module support, consistent with Spring ecosystem conventions. BOM-based dependency management works well for Spring AI version alignment.

### Why Java 17 (not 21)?
Broader deployment compatibility. Java 17 is the current LTS with widest adoption. Records (Java 16+) and sealed classes (Java 17) are available. Thread pools with `@Async` / `CompletableFuture` handle concurrency well enough; virtual threads can be adopted later when upgrading to 21.

### Why SPI + Spring auto-config (dual)?
SPI keeps `openclaw-api` framework-agnostic (pure Java interfaces, no Spring dependency). Spring auto-config provides seamless DI integration in the main app. Plugins work both as standalone JARs (SPI) and as Spring-managed beans.

### Why file-based sessions initially?
Zero infrastructure dependency, easy to inspect/debug, matches the TypeScript version. The `SessionStore` interface allows swapping in H2/Redis/PostgreSQL later.

### Why Spring ApplicationEvents for the event bus?
No need for a custom event bus when Spring's `ApplicationEventPublisher` provides type-safe pub/sub with `@EventListener` / `@TransactionalEventListener` support. Keeps the core framework-integrated without external dependencies.
