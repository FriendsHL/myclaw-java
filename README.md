# OpenClaw Java

Multi-channel AI messaging gateway with extensible plugin system — Java port of [OpenClaw](https://github.com/openclaw/openclaw).

## Overview

OpenClaw Java routes messages between messaging channels (Telegram, Feishu, Discord, etc.) and AI providers (OpenAI, Anthropic, etc.), with session management, configurable routing, and a plugin architecture.

```
Telegram ──┐                              ┌── OpenAI (GPT-4o)
Feishu   ──┤    ┌───────────────────┐     ├── Anthropic (Claude)
Discord  ──┼───>│  OpenClaw Gateway │────>├── Ollama (local)
Slack    ──┤    └───────────────────┘     └── ...
Web      ──┘        ↕ Routing
                    ↕ Sessions
                    ↕ Allowlist
```

## Tech Stack

- **Java 17** + **Maven** multi-module
- **Spring Boot 3.3.6** — Web, WebSocket, auto-configuration
- **Spring AI 1.0.1** — Unified ChatModel/ChatClient for AI providers
- **Plugin System** — Java SPI + Spring Boot auto-configuration

## Module Structure

```
openclaw-java/
├── openclaw-api/                 # SPI interfaces & config types (pure Java, no Spring)
├── openclaw-core/                # Gateway engine, routing, sessions, agent runner
├── openclaw-app/                 # Spring Boot entry point (fat JAR)
└── extensions/
    ├── openclaw-channel-telegram/   # Telegram bot (long polling)
    ├── openclaw-channel-feishu/     # Feishu/Lark bot (webhook)
    ├── openclaw-provider-openai/    # OpenAI via Spring AI
    └── openclaw-provider-anthropic/ # Anthropic via Spring AI
```

## Quick Start

### Prerequisites

- Java 17 (e.g. Amazon Corretto 17)
- Maven 3.9+

### Build

```bash
mvn clean compile
```

### Run Tests

```bash
mvn test
```

### Run

```bash
# Set required environment variables
export OPENAI_API_KEY=sk-...
export TELEGRAM_BOT_TOKEN=123456:ABC-...

# Start the gateway
mvn spring-boot:run -pl openclaw-app
```

The gateway starts on port **18789** by default.

### API Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /api/status` | Gateway status |
| `GET /api/channels` | Registered channels |

## Configuration

Edit `openclaw-app/src/main/resources/application.yml`:

```yaml
openclaw:
  gateway:
    port: 18789
    host: 0.0.0.0

  agents:
    main:
      model: gpt-4o
      system-prompt: "You are a helpful assistant."
      max-tokens: 4096
      temperature: 0.7

  channels:
    telegram:
      accounts:
        my-bot:
          token: ${TELEGRAM_BOT_TOKEN:}
          allow-from: ["123456789"]

  models:
    gpt-4o:
      provider: openai
      api-key: ${OPENAI_API_KEY:}
      model-name: gpt-4o
    claude-sonnet:
      provider: anthropic
      api-key: ${ANTHROPIC_API_KEY:}
      model-name: claude-sonnet-4-6

  bindings:
    - channel: telegram
      account-id: my-bot
      agent-id: main
```

## Message Flow

```
Incoming message (Telegram/Feishu/...)
  → ChannelPlugin.inbound()        # Normalize to InboundMessage
  → AllowlistChecker               # Reject unauthorized senders
  → MessageRouter.resolve()        # Binding: peer → account → channel → default
  → SessionManager                 # Load/create session history
  → AgentRunner.process()          # Spring AI ChatClient call
  → ChannelPlugin.outbound()       # Send reply back to platform
```

## Plugin Development

### Channel Plugin

Implement the `ChannelPlugin` SPI interface:

```java
public class MyChannelPlugin implements ChannelPlugin {
    public String id() { return "my-channel"; }
    public ChannelMeta meta() { return new ChannelMeta("My Channel"); }
    // ... implement lifecycle, inbound, outbound
}
```

Register via:
- **SPI**: `META-INF/services/com.openclaw.api.channel.ChannelPlugin`
- **Spring**: `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

### AI Provider Plugin

Implement the `ChatModelResolver` interface:

```java
public class MyChatModelResolver implements ChatModelResolver {
    public boolean supports(String provider) { return "my-provider".equals(provider); }
    public ChatModel resolve(String modelId, ModelConfig config) {
        // Return a Spring AI ChatModel instance
    }
}
```

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed design documentation covering:
- Core interface design (ChannelPlugin SPI, ChatModelResolver)
- Spring AI integration architecture
- End-to-end message flow
- Config schema
- Plugin discovery & loading mechanism

## Roadmap

- [x] Core gateway engine (routing, sessions, agent runner)
- [x] Telegram channel plugin
- [x] OpenAI provider (Spring AI)
- [x] Anthropic provider (Spring AI)
- [x] Feishu channel plugin
- [ ] Discord channel plugin
- [ ] Slack channel plugin
- [ ] Ollama provider (local LLMs)
- [ ] CLI module (picocli)
- [ ] WebSocket control endpoint
- [ ] Docker image
- [ ] Media handling (images, files, voice)

## License

MIT
