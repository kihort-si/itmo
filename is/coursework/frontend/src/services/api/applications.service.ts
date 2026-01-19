import { apiClient, extractApiError } from './config';
import type {
  ClientApplicationRequestDto,
  ClientApplicationResponseDto,
  FileMetadataResponseDto,
  PageMetadata,
} from './types';

export const applicationsService = {
  createApplication: async (data: ClientApplicationRequestDto): Promise<ClientApplicationResponseDto> => {
    try {
      const response = await apiClient.post<ClientApplicationResponseDto>('/client-applications', data);
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  async getApplicationById(id: number): Promise<ClientApplicationResponseDto> {
    try {
      const response = await apiClient.get<ClientApplicationResponseDto>(`/client-applications/${id}`);
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  async getApplicationAttachments(applicationId: number): Promise<FileMetadataResponseDto[]> {
    try {
      const response = await apiClient.get<FileMetadataResponseDto[]>(
        `/client-applications/${applicationId}/attachments`
      );
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  async getApplications(params?: {
    clientId?: number;
    description?: string;
    templateProductDesignId?: number;
    createdFrom?: string;
    createdTo?: string;
    amountFrom?: number;
    amountTo?: number;
    page?: number;
    size?: number;
    sort?: string[];
  }): Promise<{ content: ClientApplicationResponseDto[]; page: PageMetadata }> {
    try {
      const response = await apiClient.get<{ content: ClientApplicationResponseDto[]; page: PageMetadata }>(
        '/client-applications',
        { params }
      );
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },
};

