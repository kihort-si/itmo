import { apiClient, extractApiError } from './config';

export type ProductionTaskStatus = 'QUEUED' | 'IN_PROGRESS' | 'COMPLETED' | 'PENDING';

export interface ProductionTaskResponseDto {
  id: number;
  clientOrderId: number;
  status: ProductionTaskStatus;
  cncOperatorId?: number;
  startedAt?: string;
  finishedAt?: string;
  createdAt: string;
}

export const productionService = {
  async getProductionTasks(params?: {
    clientOrderId?: number;
    currentStatusId?: number;
    cncOperatorId?: number;
    startedFrom?: string;
    startedTo?: string;
    finishedFrom?: string;
    finishedTo?: string;
    createdFrom?: string;
    createdTo?: string;
    page?: number;
    size?: number;
    sort?: string[];
  }): Promise<ProductionTaskResponseDto[]> {
    try {
      const response = await apiClient.get<{ content: ProductionTaskResponseDto[] } | ProductionTaskResponseDto[]>('/production-tasks', { params });
      const data = response.data;
      if (data && typeof data === 'object' && 'content' in data && Array.isArray(data.content)) {
        return data.content;
      }
      return Array.isArray(data) ? data : [];
    } catch (error) {
      throw extractApiError(error);
    }
  },

  async getCurrentProductionTask(): Promise<ProductionTaskResponseDto | null> {
    try {
      const response = await apiClient.get<ProductionTaskResponseDto>(`/production-tasks`);
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  async startTask(id: number): Promise<void> {
    try {
      await apiClient.post(`/production-tasks/${id}/start`);
    } catch (error) {
      throw extractApiError(error);
    }
  },

  async completeTask(id: number): Promise<void> {
    try {
      await apiClient.post(`/production-tasks/${id}/finish`);
    } catch (error) {
      throw extractApiError(error);
    }
  },
};

