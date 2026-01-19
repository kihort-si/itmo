export interface PageMetadata {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

export interface PagedResponse<T> {
  content: T[];
  page: PageMetadata;
}

export interface ProductPhotoDto {
  id: number;
  fileId: number;
}

export interface ProductCatalogRequestDto {
  name: string;
  description?: string;
  productDesignId?: number;
  price: number;
  minimalAmount: number;
  category?: string;
  photoFileIds?: number[];
}

export interface ProductCatalogResponseDto {
  id: number;
  name: string;
  description?: string;
  productDesignId?: number;
  price: number;
  minimalAmount: number;
  category?: string;
  photos: ProductPhotoDto[];
}

export interface CatalogQueryParams {
  productDesignId?: number;
  name?: string;
  description?: string;
  category?: string;
  priceMin?: number;
  priceMax?: number;
  minimalAmountMin?: number;
  minimalAmountMax?: number;
  page?: number;
  size?: number;
  sort?: string[];
}

export interface FileMetadataResponseDto {
  id: number;
  filename: string;
  contentType: string;
  sizeBytes: number;
  ownerId: number;
  createdAt: string;
  updatedAt: string;
  deletedAt?: string;
}

export interface RequiredMaterialDto {
  materialId: number;
  amount: number;
}

export interface ProductDesignRequestDto {
  productName: string;
  fileIds?: number[];
  requiredMaterials?: RequiredMaterialDto[];
}

export interface ProductDesignResponseDto {
  id: number;
  constructorId?: number;
  productName: string;
  createdAt: string;
  updatedAt: string;
  files: FileMetadataResponseDto[];
  requiredMaterials: RequiredMaterialDto[];
}

export interface MaterialResponseDto {
  id: number;
  name: string;
  unitOfMeasure: string;
  orderPoint: number;
  currentBalance?: number;
}

export interface MaterialRequestDto {
  name: string;
  unitOfMeasure: string;
  orderPoint: number;
}

export interface ClientApplicationRequestDto {
  description: string;
  amount: number;
  templateProductDesignId?: number;
  catalogProductId?: number;
  attachmentFileIds: number[];
}

export interface ClientApplicationResponseDto {
  id: number;
  clientId: number;
  description: string;
  amount: number;
  templateProductDesignId?: number;
  catalogProductId?: number;
  createdAt: string;
}

export type OrderStatus =
  | "CREATED"
  | "IN_PROGRESS"
  | "CONSTRUCTOR_PENDING_APPROVAL"
  | "CLIENT_PENDING_APPROVAL"
  | "REWORK"
  | "CLIENT_REWORK"
  | "APPROVED"
  | "AWAITING_PAYMENT"
  | "PAID"
  | "READY_FOR_PRODUCTION"
  | "IN_PRODUCTION"
  | "READY_FOR_PICKUP"
  | "COMPLETED";

export interface CreateOrderRequestDto {
  clientApplicationId: number;
}

export interface ClientOrderResponseDto {
  id: number;
  clientApplicationId: number;
  managerId: number;
  productDesignId?: number;
  status: OrderStatus;
  price: number;
  createdAt: string;
  completedAt?: string;
}

export interface ConversationResponseDto {
  id: number;
  orderId: number;
  status: string;
  createdAt: string;
}

export interface MessageResponseDto {
  id: number;
  conversationId: number;
  authorId: number;
  content: string;
  sentAt: string;
  attachmentFileIds?: number[];
}


export interface MessagesQueryParams {
  conversationId?: number;
  authorId?: number;
  content?: string;
  sentFrom?: string;
  sentTo?: string;
  participantId?: number;
  page?: number;
  size?: number;
  sort?: string[];
}

export interface PersonResponseDto {
  id: number;
  firstName: string;
  lastName: string;
}

export interface ClientResponseDto {
  id: number;
  email: string;
  phoneNumber: string;
  person: PersonResponseDto;
  accountId: number;
}

import type { AccountRole } from './auth.service';

export interface EmployeeRequestDto {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  role: AccountRole;
}

export interface EmployeeResponseDto {
  id: number;
  accountId: number;
  person: PersonResponseDto;
  role: string;
  username?: string;
  enabled?: boolean;
}

export interface EmployeesQueryParams {
  role?: AccountRole;
  page?: number;
  size?: number;
  sort?: string[];
}

export interface SendMessageRequestDto {
  content: string;
  attachmentFileIds?: number[];
}

export interface MessageAttachmentDto {
  id: number;
  fileId: number;
  messageId: number;
}

