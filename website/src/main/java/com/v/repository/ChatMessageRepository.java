package com.v.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.v.model.ChatMessageEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

  @Query("""
          SELECT m FROM ChatMessageEntity m
          WHERE (m.sender = :a AND m.recipient = :b)
             OR (m.sender = :b AND m.recipient = :a)
          ORDER BY m.sentAt ASC
      """)
  List<ChatMessageEntity> findByParticipants(@Param("a") String a, @Param("b") String b);

  // ========== Unread ==========

  // map: sender -> số tin nhắn họ gửi cho "me" mà "me" chưa đọc
  @Query("""
          SELECT m.sender AS sender, COUNT(m) AS cnt
          FROM ChatMessageEntity m
          WHERE m.recipient = :me AND m.readAt IS NULL
          GROUP BY m.sender
      """)
  List<UnreadCountRow> unreadCountBySender(@Param("me") String me);

  interface UnreadCountRow {
    String getSender();

    long getCnt();
  }

  @Modifying
  @Query("""
          UPDATE ChatMessageEntity m
          SET m.readAt = :now
          WHERE m.recipient = :me AND m.sender = :other AND m.readAt IS NULL
      """)
  int markReadForThread(@Param("me") String me,
      @Param("other") String other,
      @Param("now") LocalDateTime now);

  @Modifying
  @Query("""
          DELETE FROM ChatMessageEntity m
          WHERE (m.sender = :userA AND m.recipient = :userB)
             OR (m.sender = :userB AND m.recipient = :userA)
      """)
  int deleteAllByParticipants(@Param("userA") String userA, @Param("userB") String userB);

  // ========== Threads mới nhất để đổ sidebar ==========

  @Query(value = """
      SELECT id, sender, recipient, content, sent_at, sender_role, read_at
      FROM (
        SELECT
          id,
          sender,
          recipient,
          content,
          sent_at,
          sender_role,
          read_at,
          ROW_NUMBER() OVER (
            PARTITION BY CASE WHEN sender = :me THEN recipient ELSE sender END
            ORDER BY sent_at DESC
          ) AS rn
        FROM chat_messages
        WHERE sender = :me OR recipient = :me
      ) t
      WHERE t.rn = 1
      ORDER BY t.sent_at DESC
      """, nativeQuery = true)
  List<ChatMessageEntity> findLatestThreads(@Param("me") String me);
}
