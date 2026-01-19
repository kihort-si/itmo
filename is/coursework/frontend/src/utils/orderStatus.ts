import type { OrderStatus } from '../services/api/types';

export function getOrderStatusTranslationKey(status: OrderStatus): string {
  const statusMap: Record<OrderStatus, string> = {
    CREATED: 'order.status.created',
    IN_PROGRESS: 'order.status.inProgress',
    CONSTRUCTOR_PENDING_APPROVAL: 'order.status.pendingApproval',
    CLIENT_PENDING_APPROVAL: 'order.status.clientPendingApproval',
    REWORK: 'order.status.rework',
    CLIENT_REWORK: 'order.status.rework',
    APPROVED: 'order.status.approved',
    AWAITING_PAYMENT: 'order.status.awaitingPayment',
    PAID: 'order.status.paid',
    READY_FOR_PRODUCTION: 'order.status.readyForProduction',
    IN_PRODUCTION: 'order.status.inProduction',
    READY_FOR_PICKUP: 'order.status.readyForPickup',
    COMPLETED: 'order.status.completed',
  };
  return statusMap[status] || status;
}

export function getOrderStatusStyle(status: OrderStatus): string {
  const styleMap: Record<OrderStatus, string> = {
    CREATED: 'bg-sky-500/10 text-sky-300 ring-sky-500/40',
    IN_PROGRESS: 'bg-indigo-500/10 text-indigo-300 ring-indigo-500/40',
    CONSTRUCTOR_PENDING_APPROVAL: 'bg-amber-500/10 text-amber-300 ring-amber-500/40',
    CLIENT_PENDING_APPROVAL: 'bg-yellow-500/10 text-yellow-300 ring-yellow-500/40',
    REWORK: 'bg-orange-500/10 text-orange-300 ring-orange-500/40',
    CLIENT_REWORK: 'bg-orange-500/10 text-orange-300 ring-orange-500/40',
    APPROVED: 'bg-emerald-500/10 text-emerald-300 ring-emerald-500/40',
    AWAITING_PAYMENT: 'bg-yellow-500/10 text-yellow-300 ring-yellow-500/40',
    PAID: 'bg-green-500/10 text-green-300 ring-green-500/40',
    READY_FOR_PRODUCTION: 'bg-cyan-500/10 text-cyan-300 ring-cyan-500/40',
    IN_PRODUCTION: 'bg-purple-500/10 text-purple-300 ring-purple-500/40',
    READY_FOR_PICKUP: 'bg-blue-500/10 text-blue-300 ring-blue-500/40',
    COMPLETED: 'bg-emerald-500/10 text-emerald-300 ring-emerald-500/40',
  };
  return styleMap[status] || 'bg-gray-500/10 text-gray-300 ring-gray-500/40';
}

