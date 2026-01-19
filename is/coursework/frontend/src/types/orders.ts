export type OrderStatus =
  | "REQUEST"
  | "CREATED"
  | "PROCESSING"
  | "ON_APPROVAL"
  | "REVISION"
  | "APPROVED"
  | "WAITING_PAYMENT"
  | "PAID"
  | "READY_FOR_PRODUCTION"
  | "IN_PRODUCTION"
  | "COMPLETED"
  | "CANCELLED";

export interface Order {
  id: string;
  name: string;
  clientName: string;
  createdAt: string;
  completedAt?: string;
  status: OrderStatus;
}

export interface Application {
  id: string;
  clientName: string;
  createdAt: string;
  status: OrderStatus;
}

export interface ManagerStats {
  newApplicationsCount: number;
  currentApplicationsCount: number;
  pendingApprovalCount: number;
}

export interface DesignerOrder {
  id: string;
  name: string;
  clientName: string;
  orderDate: string;
  expectedDate: string;
  status: OrderStatus;
  attachedFiles: string[];
}

export type TaskPriority = "HIGH" | "MEDIUM" | "LOW";

export type ProductionTaskStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED";

export interface MachineTask {
  id: string;
  name: string;
  upFile: string;
  material: string;
  quantity: number;
  priority: TaskPriority;
  status: ProductionTaskStatus;
}

export interface MaterialReceipt {
  invoiceNumber: string;
  author: string;
  expectedDate: string;
  status: string;
}

export interface ShipmentOrder {
  id: string;
  clientName: string;
  name: string;
  material: string;
  expectedDate: string;
  status: string;
}

