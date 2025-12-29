import {API_URL} from "./api.ts";

export interface FileResponseDto {
  id: number;
  filename: string;
  size: number;
  creationDate: string;
  success: boolean;
  objectsCount: number;
}

class UploadFileService {
  private baseUrl = `${API_URL}/upload-file`;

  async sendFile(file: File): Promise<{ message: string}> {
    if (!file) {
      throw new Error('Файл не выбран');
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileName', file.name);
    formData.append('size', file.size.toString());

    const response = await fetch(`${this.baseUrl}`, {
      method: 'POST',
      body: formData,
    });

    const contentType = response.headers.get('content-type') || '';

    if (!response.ok) {
      if (contentType.includes('text/plain')) {
        const text = (await response.text()).trim();
        throw new Error(text || `Ошибка при импорте: HTTP ${response.status}`);
      }

      const fallback = (await response.text().catch(() => '')).trim();
      throw new Error(fallback || `Ошибка при импорте: HTTP ${response.status}`);
    }

    return { message: 'Файл успешно импортирован' };
  }

  async getImportHistory(): Promise<FileResponseDto[]> {
    const response = await fetch(`${this.baseUrl}`, {
      method: 'GET',
    });

    if (!response.ok) {
      throw new Error(`Ошибка при получении истории импортов: HTTP ${response.status}`);
    }

    return response.json();
  }

  async downloadFile(id: number): Promise<Blob> {
    const response = await fetch(`${this.baseUrl}/${id}/download`, {
      method: 'GET',
    });

    if (!response.ok) {
      throw new Error(`Ошибка при скачивании файла: HTTP ${response.status}`);
    }

    return response.blob();
  }
}

export default new UploadFileService();