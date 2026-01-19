import { apiClient, extractApiError } from './config';
import type { ClientResponseDto, PagedResponse } from './types';

export const clientsService = {
  async getClientById(id: number): Promise<ClientResponseDto> {
    try {
      const response = await apiClient.get<ClientResponseDto>(`/clients/${id}`);
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  async getClients(params?: { email?: string; page?: number; size?: number }): Promise<PagedResponse<ClientResponseDto>> {
    try {
      const response = await apiClient.get<PagedResponse<ClientResponseDto>>('/clients', { params });
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  async getClientByAccountId(accountId: number): Promise<ClientResponseDto> {
    try {
      const response = await apiClient.get<ClientResponseDto>(`/clients/by-account/${accountId}`);
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },
};

