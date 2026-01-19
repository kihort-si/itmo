package ru.itmo.se.is.cw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.se.is.cw.dto.ConversationParticipantResponseDto;
import ru.itmo.se.is.cw.dto.ConversationResponseDto;
import ru.itmo.se.is.cw.dto.MessageResponseDto;
import ru.itmo.se.is.cw.dto.SendMessageRequestDto;
import ru.itmo.se.is.cw.dto.filter.MessageFilter;
import ru.itmo.se.is.cw.dto.specification.MessageSpecification;
import ru.itmo.se.is.cw.exception.EntityNotFoundException;
import ru.itmo.se.is.cw.mapper.ConversationMapper;
import ru.itmo.se.is.cw.mapper.ConversationParticipantMapper;
import ru.itmo.se.is.cw.mapper.MessageMapper;
import ru.itmo.se.is.cw.model.ClientOrderEntity;
import ru.itmo.se.is.cw.model.ConversationEntity;
import ru.itmo.se.is.cw.model.ConversationParticipantEntity;
import ru.itmo.se.is.cw.model.FileEntity;
import ru.itmo.se.is.cw.model.MessageAttachmentEntity;
import ru.itmo.se.is.cw.model.MessageEntity;
import ru.itmo.se.is.cw.model.value.ConversationStatus;
import ru.itmo.se.is.cw.repository.ConversationParticipantRepository;
import ru.itmo.se.is.cw.repository.ConversationRepository;
import ru.itmo.se.is.cw.repository.FileRepository;
import ru.itmo.se.is.cw.repository.MessageAttachmentRepository;
import ru.itmo.se.is.cw.repository.MessageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationsService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository messageAttachmentRepository;
    private final FileRepository fileRepository;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final ConversationParticipantMapper conversationParticipantMapper;
    private final CurrentUserService currentUserService;

    @Transactional
    public ConversationEntity createConversationForOrder(ClientOrderEntity order) {
        ConversationEntity conversation = new ConversationEntity();
        conversation.setOrder(order);
        conversation.setStatus(ConversationStatus.ACTIVE);
        return conversationRepository.save(conversation);
    }

    @Transactional
    public void addParticipantToConversation(ConversationEntity conversation, Long userId) {
        if (participantRepository.existsByConversationIdAndUserId(conversation.getId(), userId)) {
            return;
        }
        ConversationParticipantEntity participant = new ConversationParticipantEntity();
        participant.setConversation(conversation);
        participant.setUserId(userId);
        participantRepository.save(participant);
    }

    @Transactional(readOnly = true)
    public ConversationResponseDto getConversationByOrderId(Long orderId) {
        ConversationEntity conversation = conversationRepository
                .findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation for order with id " + orderId + " not found"));
        assertCanAccess(conversation);
        return conversationMapper.toDto(conversation);
    }

    @Transactional(readOnly = true)
    public ConversationEntity getById(Long id) {
        return conversationRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conversation with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public ConversationEntity getConversationByOrderIdInternal(Long orderId) {
        return conversationRepository
                .findByOrderId(orderId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponseDto> getMessages(Long id, Pageable pageable, MessageFilter filter) {
        ConversationEntity conversation = getById(id);
        assertCanAccess(conversation);
        if (filter == null) filter = new MessageFilter();
        filter.setConversationId(id);
        return messageRepository
                .findAll(MessageSpecification.byFilter(filter), pageable)
                .map(messageMapper::toDto);
    }

    @Transactional
    public MessageResponseDto sendMessage(Long id, SendMessageRequestDto request) {
        ConversationEntity conversation = getById(id);
        assertCanAccess(conversation);

        ConversationParticipantEntity participant = participantRepository
                .findByConversationIdAndUserId(conversation.getId(), currentUserService.getAccountId())
                .orElseThrow(() -> new RuntimeException("Unexpected state"));

        MessageEntity message = new MessageEntity();
        message.setContent(request.getContent());
        message.setConversationParticipant(participant);

        MessageEntity savedMessage = messageRepository.save(message);

        if (request.getAttachmentFileIds() != null && !request.getAttachmentFileIds().isEmpty()) {
            for (Long fileId : request.getAttachmentFileIds()) {
                FileEntity file = fileRepository.findById(fileId)
                        .orElseThrow(() -> new EntityNotFoundException("File with id " + fileId + " not found"));
                MessageAttachmentEntity attachment = new MessageAttachmentEntity();
                attachment.setMessage(savedMessage);
                attachment.setFile(file);
                messageAttachmentRepository.save(attachment);
            }
        }

        return messageMapper.toDto(savedMessage);
    }

    @Transactional
    public MessageResponseDto sendMessageAsUser(Long conversationId, Long userId, String content) {
        ConversationEntity conversation = getById(conversationId);

        ConversationParticipantEntity participant = participantRepository
                .findByConversationIdAndUserId(conversation.getId(), userId)
                .orElseThrow(() -> new RuntimeException("User is not a participant of this conversation"));

        MessageEntity message = new MessageEntity();
        message.setContent(content);
        message.setConversationParticipant(participant);

        MessageEntity savedMessage = messageRepository.save(message);
        return messageMapper.toDto(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<ConversationParticipantResponseDto> getParticipants(Long conversationId) {
        ConversationEntity conversation = getById(conversationId);
        assertCanAccess(conversation);
        return participantRepository
                .findByConversationIdOrderByJoinedAtAsc(conversationId)
                .stream()
                .map(conversationParticipantMapper::toDto)
                .toList();
    }

    private void assertCanAccess(ConversationEntity conversation) {
        Long currentUserId = currentUserService.getAccountId();

        if (participantRepository.existsByConversationIdAndUserId(conversation.getId(), currentUserId)) {
            return;
        }
        
        if (conversation.getOrder() != null && conversation.getOrder().getProductDesign() != null) {
            var design = conversation.getOrder().getProductDesign();
            if (design.getConstructor() != null) {

                if (design.getConstructor().getAccountId().equals(currentUserId)) {
                    return;
                }
            }
        }
        
        throw new EntityNotFoundException("Conversation with id " + conversation.getId() + " not found");
    }
}
