-- Chat-memory table for Spring AI's JdbcChatMemoryRepository (H2 dialect).
-- One row per message, correlated by conversation_id and ordered by timestamp.
-- IF NOT EXISTS keeps this safe to re-run on every startup against the
-- file-based H2 database (spring.ai.chat.memory.repository.jdbc.initialize-schema=always).
CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    -- holds the raw `username` header (a freeform conversation id), not a UUID
    conversation_id VARCHAR(255) NOT NULL,
    content         LONGVARCHAR  NOT NULL,
    type            VARCHAR(10)  NOT NULL,
    "timestamp"     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT TYPE_CHECK CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL'))
);

-- The MessageWindowChatMemory sliding window reads the most recent N messages
-- for a conversation (ORDER BY "timestamp" DESC); this composite index serves
-- that lookup directly.
CREATE INDEX IF NOT EXISTS SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX
    ON SPRING_AI_CHAT_MEMORY (conversation_id, "timestamp" DESC);
