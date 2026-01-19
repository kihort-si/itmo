import { apiClient, extractApiError } from './config';
import type {
  ConversationResponseDto,
  MessageResponseDto,
  SendMessageRequestDto,
  MessagesQueryParams,
} from './types';

export const conversationsService = {
  async getConversationByOrderId(orderId: number): Promise<ConversationResponseDto> {
    try {
      const response = await apiClient.get<ConversationResponseDto>(
        `/orders/${orderId}/conversation`
      );
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  async getMessages(
    conversationId: number,
    params?: MessagesQueryParams
  ): Promise<MessageResponseDto[]> {
    try {
      const response = await apiClient.get<{ content: MessageResponseDto[] } | MessageResponseDto[]>(
        `/conversations/${conversationId}/messages`,
        { params }
      );
      const data = response.data;
      if (data && typeof data === 'object' && 'content' in data && Array.isArray(data.content)) {
        return data.content;
      }
      return Array.isArray(data) ? data : [];
    } catch (error) {
      throw extractApiError(error);
    }
  },

  async sendMessage(
    conversationId: number,
    data: SendMessageRequestDto
  ): Promise<MessageResponseDto> {
    try {
      const response = await apiClient.post<MessageResponseDto>(
        `/conversations/${conversationId}/messages`,
        data
      );
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },
};

