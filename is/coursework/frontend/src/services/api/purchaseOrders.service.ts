import { apiClient, extractApiError } from './config';

export type PurchaseOrderStatus = 'CREATED' | 'COMPLETED';

export interface PurchaseOrderMaterialDto {
  materialId: number;
  amount: number;
  priceForUnit: number;
  supplier: string;
  realAmount?: number;
}

export interface PurchaseOrderResponseDto {
  id: number;
  supplyManagerId: number;
  supplyManagerFirstName?: string;
  supplyManagerLastName?: string;
  status: PurchaseOrderStatus;
  createdAt: string;
  materials: PurchaseOrderMaterialDto[];
}

export interface PurchaseOrderRequestDto {
  materials: PurchaseOrderMaterialDto[];
}

export const purchaseOrdersService = {
  createPurchaseOrder: async (data: PurchaseOrderRequestDto): Promise<PurchaseOrderResponseDto> => {
    try {
      const response = await apiClient.post<PurchaseOrderResponseDto>('/purchase-orders', data);
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  getPurchaseOrders: async (params?: {
    supplyManagerId?: number;
    currentStatusId?: number;
    status?: PurchaseOrderStatus;
    createdFrom?: string;
    createdTo?: string;
    withoutCurrentStatus?: boolean;
    page?: number;
    size?: number;
    sort?: string[];
  }): Promise<PurchaseOrderResponseDto[]> => {
    try {
      const response = await apiClient.get<{ content: PurchaseOrderResponseDto[] }>('/purchase-orders', { params });
      return response.data.content || [];
    } catch (error) {
      throw extractApiError(error);
    }
  },

  getPurchaseOrderById: async (id: number): Promise<PurchaseOrderResponseDto> => {
    try {
      const response = await apiClient.get<PurchaseOrderResponseDto>(`/purchase-orders/${id}`);
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  updateMaterialsInPurchaseOrder: async (
    id: number,
    materials: PurchaseOrderMaterialDto[]
  ): Promise<PurchaseOrderResponseDto> => {
    try {
      const response = await apiClient.post<PurchaseOrderResponseDto>(
        `/purchase-orders/${id}/materials`,
        materials
      );
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  registerReceipt: async (
    purchaseOrderId: number,
    data: {
      invoiceNumber: string;
      receivedItems: Array<{
        materialId: number;
        amount: number;
      }>;
    }
  ): Promise<PurchaseOrderReceiptResponseDto> => {
    try {
      const response = await apiClient.post<PurchaseOrderReceiptResponseDto>(
        `/purchase-orders/${purchaseOrderId}/receipt`,
        data
      );
      return response.data;
    } catch (error) {
      throw extractApiError(error);
    }
  },

  getPurchaseOrderReceipt: async (purchaseOrderId: number): Promise<PurchaseOrderReceiptDetailDto | null> => {
    try {
      const response = await apiClient.get<PurchaseOrderReceiptDetailDto>(
        `/purchase-orders/${purchaseOrderId}/receipt`
      );
      return response.data;
    } catch (error) {
      const apiError = extractApiError(error);
      if (apiError.status === 404) {
        return null;
      }
      throw apiError;
    }
  },
};

export interface PurchaseOrderReceiptResponseDto {
  id: number;
  purchaseOrderId: number;
  warehouseWorkerId: number;
  invoiceNumber: string;
  receiptedAt: string;
  receivedItems?: Array<{
    materialId: number;
    amount: number;
  }>;
}

export interface PurchaseOrderReceiptDetailDto {
  id: number;
  purchaseOrderId: number;
  warehouseWorkerId: number;
  invoiceNumber: string;
  receiptedAt: string;
  receivedItems: Array<{
    materialId: number;
    amount: number;
  }>;
}

