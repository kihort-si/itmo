import { useTranslation } from "react-i18next";
import { useState, useEffect } from "react";
import { purchaseOrdersService, ordersService, materialsService, applicationsService, clientsService, extractApiError } from "../../../services/api";
import type { PurchaseOrderResponseDto } from "../../../services/api/purchaseOrders.service";
import type { ClientOrderResponseDto, ClientResponseDto, ClientApplicationResponseDto } from "../../../services/api/types";
import DeleteIcon from "@mui/icons-material/Delete";

function WarehouseDashboard() {
  const { t } = useTranslation();
  const [purchaseOrders, setPurchaseOrders] = useState<PurchaseOrderResponseDto[]>([]);
  const [readyOrders, setReadyOrders] = useState<ClientOrderResponseDto[]>([]);
  const [clients, setClients] = useState<Map<number, ClientResponseDto>>(new Map());
  const [applications, setApplications] = useState<Map<number, ClientApplicationResponseDto>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showReceiptModal, setShowReceiptModal] = useState(false);
  const [selectedPurchaseOrder, setSelectedPurchaseOrder] = useState<PurchaseOrderResponseDto | null>(null);
  const [receiptInvoiceNumber, setReceiptInvoiceNumber] = useState("");
  const [receiptItems, setReceiptItems] = useState<Array<{ materialId: number; materialName: string; amount: number; originalAmount: number }>>([]);
  const [showShipmentModal, setShowShipmentModal] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<ClientOrderResponseDto | null>(null);

  const formatOrderName = (order: ClientOrderResponseDto): string => {
    const date = new Date(order.createdAt);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `ORD-${year}-${month}-${day}-${order.id}`;
  };

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        setError(null);

        try {
          const purchaseOrdersData = await purchaseOrdersService.getPurchaseOrders({
            status: 'CREATED',
          });
          setPurchaseOrders(Array.isArray(purchaseOrdersData) ? purchaseOrdersData : []);
        } catch (err) {
          const apiError = extractApiError(err);
          console.error('Failed to load purchase orders:', apiError);
          if (apiError.status !== 403) {
            setError(apiError.message || 'Ошибка загрузки заявок на закупку');
          }
        }

        try {
          const ordersData = await ordersService.getOrders({
            status: 'READY_FOR_PICKUP',
          });
          const ordersArray = Array.isArray(ordersData) ? ordersData : [];
          setReadyOrders(ordersArray);

          const clientsMap = new Map<number, ClientResponseDto>();
          const applicationsMap = new Map<number, ClientApplicationResponseDto>();
          const uniqueClientIds = new Set<number>();

          for (const order of ordersArray) {
            if (order.clientApplicationId) {
              try {
                const application = await applicationsService.getApplicationById(order.clientApplicationId);
                applicationsMap.set(order.clientApplicationId, application);
                if (application.clientId && !clientsMap.has(application.clientId)) {
                  uniqueClientIds.add(application.clientId);
                }
              } catch (err) {
                console.error(`Failed to load application ${order.clientApplicationId}:`, err);
              }
            }
          }

          for (const clientId of uniqueClientIds) {
            try {
              const client = await clientsService.getClientById(clientId);
              clientsMap.set(clientId, client);
            } catch (err) {
              console.error(`Failed to load client ${clientId}:`, err);
            }
          }

          setClients(clientsMap);
          setApplications(applicationsMap);
        } catch (err) {
          const apiError = extractApiError(err);
          console.error('Failed to load ready orders:', apiError);
          if (apiError.status !== 403 && apiError.code !== 'FORBIDDEN') {
            setError(apiError.message || apiError.detail || 'Ошибка загрузки заказов');
          }
        }
      } catch (err) {
        const apiError = extractApiError(err);
        setError(apiError.message || 'Ошибка загрузки данных');
      } finally {
        setLoading(false);
      }
    };
    
    loadData();
  }, []);

  const handleRegisterReceipt = async (purchaseOrderId: number) => {
    try {
      const order = purchaseOrders.find(po => po.id === purchaseOrderId);
      if (!order) return;

      setSelectedPurchaseOrder(order);
      setReceiptInvoiceNumber("");

      const itemsWithNames = await Promise.all(
        order.materials.map(async (m) => {
          try {
            const material = await materialsService.getMaterialById(m.materialId);
            return {
              materialId: m.materialId,
              materialName: material.name,
              amount: m.amount,
              originalAmount: m.amount,
            };
          } catch {
            return {
              materialId: m.materialId,
              materialName: `Материал #${m.materialId}`,
              amount: m.amount,
              originalAmount: m.amount,
            };
          }
        })
      );
      
      setReceiptItems(itemsWithNames);
      setShowReceiptModal(true);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка открытия формы регистрации поступления');
    }
  };

  const handleSaveReceipt = async () => {
    if (!selectedPurchaseOrder || !receiptInvoiceNumber.trim()) {
      setError('Заполните номер накладной');
      return;
    }

    if (receiptItems.length === 0) {
      setError('Добавьте хотя бы один материал');
      return;
    }

    try {
      setError(null);
      await purchaseOrdersService.registerReceipt(selectedPurchaseOrder.id, {
        invoiceNumber: receiptInvoiceNumber,
        receivedItems: receiptItems.map(item => ({
          materialId: item.materialId,
          amount: item.amount,
        })),
      });

      setShowReceiptModal(false);
      setSelectedPurchaseOrder(null);
      setReceiptInvoiceNumber("");
      setReceiptItems([]);

      const purchaseOrdersData = await purchaseOrdersService.getPurchaseOrders({
        status: 'CREATED',
      });
      setPurchaseOrders(purchaseOrdersData);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка регистрации поступления');
    }
  };

  const handleRemoveReceiptItem = (index: number) => {
    setReceiptItems(prev => prev.filter((_, i) => i !== index));
  };

  const handleProcessShipment = async (orderId: number) => {
    try {
      const order = readyOrders.find(o => o.id === orderId);
      if (!order) return;

      setSelectedOrder(order);
      setShowShipmentModal(true);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка открытия формы обработки отгрузки');
    }
  };

  const handleSaveShipment = async () => {
    if (!selectedOrder) return;

    try {
      setError(null);

      await ordersService.changeOrderStatus(selectedOrder.id, 'COMPLETED', 'Заказ отгружен');

      setShowShipmentModal(false);
      setSelectedOrder(null);

      const ordersData = await ordersService.getOrders({
        status: 'READY_FOR_PICKUP',
      });
      const ordersArray = Array.isArray(ordersData) ? ordersData : [];
      setReadyOrders(ordersArray);

      const clientsMap = new Map<number, ClientResponseDto>();
      const applicationsMap = new Map<number, ClientApplicationResponseDto>();
      const uniqueClientIds = new Set<number>();

      for (const order of ordersArray) {
        if (order.clientApplicationId) {
          try {
            const application = await applicationsService.getApplicationById(order.clientApplicationId);
            applicationsMap.set(order.clientApplicationId, application);
            if (application.clientId && !clientsMap.has(application.clientId)) {
              uniqueClientIds.add(application.clientId);
            }
          } catch (err) {
            console.error(`Failed to load application ${order.clientApplicationId}:`, err);
          }
        }
      }

      for (const clientId of uniqueClientIds) {
        try {
          const client = await clientsService.getClientById(clientId);
          clientsMap.set(clientId, client);
        } catch (err) {
          console.error(`Failed to load client ${clientId}:`, err);
        }
      }

      setClients(clientsMap);
      setApplications(applicationsMap);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка обработки отгрузки');
    }
  };

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10 flex items-center justify-center">
        <div className="text-gray-400">{t("catalog.loading")}</div>
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10 flex justify-center">
      <div className="w-full max-w-7xl space-y-8">
        <div>
          <h1 className="text-3xl font-semibold tracking-tight">
            {t("warehouse.dashboard")}
          </h1>
        </div>

        {error && (
          <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/40 text-red-400 text-sm">
            {error}
          </div>
        )}

        <section className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold">
              {t("warehouse.materialReceipts")}
            </h2>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full text-sm border-separate border-spacing-y-2">
              <thead>
                <tr className="text-xs uppercase text-gray-500">
                  <th className="text-left px-3 pb-2">
                    {t("warehouse.invoiceNumber")}
                  </th>
                  <th className="text-left px-3 pb-2">
                    {t("warehouse.author")}
                  </th>
                  <th className="text-left px-3 pb-2">
                    {t("warehouse.expectedDate")}
                  </th>
                  <th className="text-right px-3 pb-2">
                    {t("profile.orderActions")}
                  </th>
                </tr>
              </thead>
              <tbody>
                {purchaseOrders.length === 0 ? (
                  <tr>
                    <td
                      colSpan={4}
                      className="px-3 py-8 text-center text-gray-500"
                    >
                      {t("warehouse.noReceipts")}
                    </td>
                  </tr>
                ) : (
                  purchaseOrders.map((order) => (
                    <tr key={order.id}>
                      <td className="px-3 py-3">
                        <div className="text-sm font-medium">
                          Заявка #{order.id}
                        </div>
                      </td>
                      <td className="px-3 py-3 text-sm text-gray-300">
                        {order.supplyManagerFirstName && order.supplyManagerLastName
                          ? `${order.supplyManagerFirstName} ${order.supplyManagerLastName}`
                          : `Менеджер #${order.supplyManagerId}`}
                      </td>
                      <td className="px-3 py-3 text-sm text-gray-300">
                        {new Date(order.createdAt).toLocaleDateString('ru-RU')}
                      </td>
                      <td className="px-3 py-3 text-right">
                        <button
                          onClick={() => handleRegisterReceipt(order.id)}
                          className="text-xs rounded-full border border-gray-700 px-3 py-1 hover:bg-gray-800 transition-colors"
                        >
                          {t("warehouse.registerReceipt")}
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold">
              {t("warehouse.ordersReadyForShipment")}
            </h2>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full text-sm border-separate border-spacing-y-2">
              <thead>
                <tr className="text-xs uppercase text-gray-500">
                  <th className="text-left px-3 pb-2">
                    {t("warehouse.order")}
                  </th>
                  <th className="text-left px-3 pb-2">
                    {t("warehouse.client")}
                  </th>
                  <th className="text-left px-3 pb-2">
                    {t("order.price")}
                  </th>
                  <th className="text-left px-3 pb-2">
                    {t("warehouse.expectedDate")}
                  </th>
                  <th className="text-right px-3 pb-2">
                    {t("profile.orderActions")}
                  </th>
                </tr>
              </thead>
              <tbody>
                {readyOrders.length === 0 ? (
                  <tr>
                    <td
                      colSpan={5}
                      className="px-3 py-8 text-center text-gray-500"
                    >
                      {t("warehouse.noOrders")}
                    </td>
                  </tr>
                ) : (
                  readyOrders.map((order) => {
                    const application = order.clientApplicationId ? applications.get(order.clientApplicationId) : null;
                    const client = application ? clients.get(application.clientId) : null;
                    return (
                      <tr key={order.id}>
                        <td className="px-3 py-3">
                          <div className="text-sm font-medium">{formatOrderName(order)}</div>
                          <div className="text-xs text-gray-500">ID: {order.id}</div>
                        </td>
                        <td className="px-3 py-3 text-sm text-gray-300">
                          {client 
                            ? `${client.person.firstName} ${client.person.lastName}`
                            : application 
                              ? `Клиент #${application.clientId}`
                              : `Заявка #${order.clientApplicationId || 'N/A'}`}
                        </td>
                        <td className="px-3 py-3 text-sm text-gray-300">
                          {order.price ? `${order.price.toLocaleString("ru-RU")} ₽` : "—"}
                        </td>
                        <td className="px-3 py-3 text-sm text-gray-300">
                          {new Date(order.createdAt).toLocaleDateString('ru-RU')}
                        </td>
                        <td className="px-3 py-3 text-right">
                          <button
                            onClick={() => handleProcessShipment(order.id)}
                            className="text-xs rounded-full border border-gray-700 px-3 py-1 hover:bg-gray-800 transition-colors"
                          >
                            {t("warehouse.processShipment")}
                          </button>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>

      {showReceiptModal && selectedPurchaseOrder && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-stone-900 rounded-2xl border border-gray-800 p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <h2 className="text-xl font-semibold mb-4">
              {t("warehouse.registerReceipt")} - Заявка #{selectedPurchaseOrder.id}
            </h2>

            <div className="space-y-4">
              <div>
                <label className="block text-sm text-gray-400 mb-1">
                  Номер накладной *
                </label>
                <input
                  type="text"
                  value={receiptInvoiceNumber}
                  onChange={(e) => setReceiptInvoiceNumber(e.target.value)}
                  className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                  placeholder="INV-2023-12345"
                />
              </div>

              <div>
                <label className="block text-sm text-gray-400 mb-2">
                  Полученные материалы
                </label>
                <div className="space-y-2">
                  {receiptItems.map((item, index) => (
                    <div
                      key={index}
                      className="flex items-center gap-3 bg-stone-800/50 rounded-lg px-3 py-2 border border-gray-700"
                    >
                      <div className="flex-1">
                        <div className="text-sm text-white">
                          {item.materialName || `Материал #${item.materialId}`}
                        </div>
                        <div className="text-xs text-gray-400">
                          Ожидалось: {item.originalAmount}
                        </div>
                      </div>
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        value={item.amount}
                        onChange={(e) => {
                          const newItems = [...receiptItems];
                          newItems[index] = { ...newItems[index], amount: parseFloat(e.target.value) || 0 };
                          setReceiptItems(newItems);
                        }}
                        className="w-24 rounded-lg bg-stone-950/70 border border-gray-700 px-3 py-1.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-emerald-400"
                      />
                      <button
                        onClick={() => handleRemoveReceiptItem(index)}
                        className="text-red-400 hover:text-red-300 transition-colors"
                      >
                        <DeleteIcon fontSize="small" />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="flex gap-4 mt-6">
              <button
                onClick={handleSaveReceipt}
                className="flex-1 rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors"
              >
                Сохранить
              </button>
              <button
                onClick={() => {
                  setShowReceiptModal(false);
                  setSelectedPurchaseOrder(null);
                  setReceiptInvoiceNumber("");
                  setReceiptItems([]);
                }}
                className="flex-1 rounded-full bg-stone-950 text-white border border-gray-700 text-sm font-medium py-2.5 hover:bg-gray-900 transition-colors"
              >
                Отмена
              </button>
            </div>
          </div>
        </div>
      )}

      {showShipmentModal && selectedOrder && (() => {
        const application = selectedOrder.clientApplicationId ? applications.get(selectedOrder.clientApplicationId) : null;
        const client = application ? clients.get(application.clientId) : null;
        return (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-stone-900 rounded-2xl border border-gray-800 p-6 max-w-2xl w-full">
              <h2 className="text-xl font-semibold mb-4">
                {t("warehouse.processShipment")} - {formatOrderName(selectedOrder)}
              </h2>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm text-gray-400 mb-1">
                    {t("warehouse.client")}
                  </label>
                  <div className="text-sm text-white">
                    {client 
                      ? `${client.person.firstName} ${client.person.lastName}`
                      : application 
                        ? `Клиент #${application.clientId}`
                        : `Заявка #${selectedOrder.clientApplicationId || 'N/A'}`}
                  </div>
                </div>

                <div>
                  <label className="block text-sm text-gray-400 mb-1">
                    {t("order.price")}
                  </label>
                  <div className="text-sm text-white font-semibold">
                    {selectedOrder.price ? `${selectedOrder.price.toLocaleString("ru-RU")} ₽` : "—"}
                  </div>
                </div>
              </div>

              <div className="flex gap-4 mt-6">
                <button
                  onClick={handleSaveShipment}
                  className="flex-1 rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors"
                >
                  Сохранить и завершить заказ
                </button>
                <button
                  onClick={() => {
                    setShowShipmentModal(false);
                    setSelectedOrder(null);
                  }}
                  className="flex-1 rounded-full bg-stone-950 text-white border border-gray-700 text-sm font-medium py-2.5 hover:bg-gray-900 transition-colors"
                >
                  Отмена
                </button>
              </div>
            </div>
          </div>
        );
      })()}
    </div>
  );
}

export default WarehouseDashboard;