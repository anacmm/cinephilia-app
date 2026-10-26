package br.com.maestrello.cinephilia.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "game_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameSession {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "player_id", nullable = false)
  private Player player;

  @Column(name = "start_person_id", nullable = false)
  private Long startPersonId;

  @Column(name = "target_person_id", nullable = false)
  private Long targetPersonId;

  @Setter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private GameSessionStatus status = GameSessionStatus.IN_PROGRESS;

  @CreationTimestamp(source = SourceType.DB)
  @Column(name = "started_at", nullable = false, updatable = false)
  private Instant startedAt;

  @Setter
  @Column(name = "completed_at")
  private Instant completedAt;

  @Setter
  @Column(name = "move_count")
  private Integer moveCount;

  @Setter
  @Column(name = "duration_ms")
  private Long durationMs;

  // Ordered navigation nodes; the API path contract will be defined in a later step.
  @Setter
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private List<Map<String, Object>> path;

  public GameSession(Player player, Long startPersonId, Long targetPersonId) {
    this.player = player;
    this.startPersonId = startPersonId;
    this.targetPersonId = targetPersonId;
  }
}
