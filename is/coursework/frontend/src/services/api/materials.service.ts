import { apiClient, extractApiError } from './config';
import type { MaterialResponseDto, MaterialRequestDto, PagedResponse } from './types';

export const materialsService = {
  getMaterialById: async (id: number): Promise<MaterialResponseDto> => {
    try {
      const response = await apiClient.get<MaterialResponseDto>(`/materials/${id}`);
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  getMaterials: async (params?: {
    name?: string;
    unitOfMeasure?: string;
    orderPointFrom?: number;
    orderPointTo?: number;
    currentBalanceFrom?: number;
    currentBalanceTo?: number;
    belowOrderPoint?: boolean;
    withoutCurrentBalance?: boolean;
    page?: number;
    size?: number;
    sort?: string[];
  }): Promise<MaterialResponseDto[]> => {
    try {
      const response = await apiClient.get<PagedResponse<MaterialResponseDto> | MaterialResponseDto[]>('/materials', { params });
      const data = response.data;
      if (data && typeof data === 'object' && 'content' in data && Array.isArray(data.content)) {
        return data.content;
      }
      return Array.isArray(data) ? data : [];
    } catch (error) {
      throw extractApiError(error);
    }
  },

  createMaterial: async (data: MaterialRequestDto): Promise<MaterialResponseDto> => {
    try {
      const response = await apiClient.post<MaterialResponseDto>('/materials', data);
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },
};

