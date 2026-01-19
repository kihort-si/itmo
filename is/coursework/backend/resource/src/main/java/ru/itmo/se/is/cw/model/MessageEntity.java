package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "message")
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "content", nullable = false, length = Integer.MAX_VALUE)
    private String content;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "conversation_participant_id", nullable = false)
    private ConversationParticipantEntity conversationParticipant;

    @Column(name = "sent_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime sentAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MessageAttachmentEntity> attachments = new ArrayList<>();

    public void addAttachment(MessageAttachmentEntity attachment) {
        attachments.add(attachment);
        attachment.setMessage(this);
    }
}