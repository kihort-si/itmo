import { apiClient, extractApiError } from './config';
import type { FileMetadataResponseDto } from './types';

export const filesService = {
  uploadFile: async (file: File): Promise<FileMetadataResponseDto> => {
    try {
      const formData = new FormData();
      formData.append('file', file);
      const response = await apiClient.post<FileMetadataResponseDto>('/files', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  downloadFile: async (id: number, filename?: string): Promise<void> => {
    try {
      const response = await apiClient.get(`/files/${id}/download`, {
        responseType: 'blob',
      });
      const blob = response.data;

      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename || `file-${id}`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      throw extractApiError(error);
    }
  },

  getFileUrl: async (id: number): Promise<string | null> => {
    try {
      const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/resource';
      const token = localStorage.getItem('accessToken');
      return `${baseURL}/files/${id}/download${token ? `?token=${token}` : ''}`;
    } catch (error) {
      console.error('Failed to get file URL:', error);
      return null;
    }
  },

  getFileBlob: async (id: number): Promise<Blob> => {
    try {
      const response = await apiClient.get(`/files/${id}/download`, {
        responseType: 'blob',
      });
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  getFileMetadata: async (id: number): Promise<FileMetadataResponseDto> => {
    try {
      const response = await apiClient.get<FileMetadataResponseDto>(`/files/${id}`);
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },
};

