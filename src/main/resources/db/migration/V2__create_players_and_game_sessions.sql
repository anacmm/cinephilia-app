CREATE TABLE players (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_players_display_name_not_blank CHECK (display_name ~ '[^[:space:]]')
);

CREATE TABLE game_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id UUID NOT NULL,
    start_person_id BIGINT NOT NULL,
    target_person_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    move_count INTEGER,
    duration_ms BIGINT,
    path JSONB,
    CONSTRAINT fk_game_sessions_player FOREIGN KEY (player_id) REFERENCES players (id),
    CONSTRAINT ck_game_sessions_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'ABANDONED')),
    CONSTRAINT ck_game_sessions_start_person CHECK (start_person_id > 0),
    CONSTRAINT ck_game_sessions_target_person CHECK (target_person_id > 0),
    CONSTRAINT ck_game_sessions_move_count CHECK (move_count >= 0),
    CONSTRAINT ck_game_sessions_duration_ms CHECK (duration_ms >= 0),
    CONSTRAINT ck_game_sessions_completion_time CHECK (completed_at >= started_at)
);

CREATE INDEX idx_game_sessions_player_started_at ON game_sessions (player_id, started_at DESC);
