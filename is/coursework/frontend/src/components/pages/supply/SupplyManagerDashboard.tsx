import { useTranslation } from "react-i18next";
import { useState, useEffect } from "react";
import { Formik, Form, Field, ErrorMessage, FieldArray } from "formik";
import * as Yup from "yup";
import { purchaseOrdersService, materialsService, extractApiError } from "../../../services/api";
import type { PurchaseOrderResponseDto, PurchaseOrderRequestDto, PurchaseOrderMaterialDto } from "../../../services/api/purchaseOrders.service";
import type { MaterialResponseDto } from "../../../services/api/types";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
// import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import VisibilityIcon from "@mui/icons-material/Visibility";

function SupplyManagerDashboard() {
  const { t } = useTranslation();
  const [purchaseOrders, setPurchaseOrders] = useState<PurchaseOrderResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [showCreateMaterialModal, setShowCreateMaterialModal] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<PurchaseOrderResponseDto | null>(null);
  const [allMaterials, setAllMaterials] = useState<MaterialResponseDto[]>([]);
  const [, setLoadingMaterials] = useState(false);
  const [creatingMaterial, setCreatingMaterial] = useState(false);
  const [receiptInfo, setReceiptInfo] = useState<{ [key: number]: { invoiceNumber: string; receivedItems: Array<{ materialId: number; amount: number }> } }>({});
  const [lowBalanceMaterials, setLowBalanceMaterials] = useState<MaterialResponseDto[]>([]);

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    const loadMaterials = async () => {
      try {
        setLoadingMaterials(true);
        const materials = await materialsService.getMaterials({ size: 1000 });
        setAllMaterials(materials);

        const lowBalance = await materialsService.getMaterials({ 
          belowOrderPoint: true,
          size: 1000 
        });
        setLowBalanceMaterials(lowBalance);
      } catch (err) {
        console.error('Failed to load materials:', err);
      } finally {
        setLoadingMaterials(false);
      }
    };
    loadMaterials();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      const ordersData = await purchaseOrdersService.getPurchaseOrders();
      setPurchaseOrders(Array.isArray(ordersData) ? ordersData : []);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка загрузки данных');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateOrder = () => {
    setShowCreateModal(true);
    setError(null);
  };

  // const handleEditOrder = (order: PurchaseOrderResponseDto) => {
  //   setSelectedOrder(order);
  //   setShowEditModal(true);
  //   setError(null);
  // };

  const handleViewDetails = async (order: PurchaseOrderResponseDto) => {
    setSelectedOrder(order);
    
    try {
      const receipt = await purchaseOrdersService.getPurchaseOrderReceipt(order.id);
      if (receipt) {
        setReceiptInfo(prev => ({
          ...prev,
          [order.id]: {
            invoiceNumber: receipt.invoiceNumber,
            receivedItems: receipt.receivedItems || []
          }
        }));
      } else {
        setReceiptInfo(prev => {
          const newInfo = { ...prev };
          delete newInfo[order.id];
          return newInfo;
        });
      }
    } catch (err) {
      const apiError = extractApiError(err);
      if (apiError.status !== 404) {
        console.error('Error loading receipt:', err);
      }
      setReceiptInfo(prev => {
        const newInfo = { ...prev };
        delete newInfo[order.id];
        return newInfo;
      });
    }

    setShowDetailsModal(true);
  };

  const handleSubmitCreate = async (values: PurchaseOrderRequestDto) => {
    try {
      setError(null);
      await purchaseOrdersService.createPurchaseOrder(values);
      setShowCreateModal(false);
      await loadData();
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка создания заявки');
    }
  };

  const handleSubmitEdit = async (values: { materials: PurchaseOrderMaterialDto[] }) => {
    if (!selectedOrder) return;

    try {
      setError(null);
      await purchaseOrdersService.updateMaterialsInPurchaseOrder(selectedOrder.id, values.materials);
      setShowEditModal(false);
      setSelectedOrder(null);
      await loadData();
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка обновления заявки');
    }
  };

  const handleCreateMaterial = async (values: { name: string; unitOfMeasure: string; orderPoint: number }) => {
    try {
      setCreatingMaterial(true);
      setError(null);
      const newMaterial = await materialsService.createMaterial(values);
      setAllMaterials(prev => [...prev, newMaterial]);
      setShowCreateMaterialModal(false);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка создания материала');
    } finally {
      setCreatingMaterial(false);
    }
  };

  const materialValidationSchema = Yup.object({
    materialId: Yup.number().min(1, t("supply.materialRequired")).required(t("supply.materialRequired")),
    amount: Yup.number().required(t("supply.amountRequired")).min(0.01, t("supply.amountMin")),
    priceForUnit: Yup.number().required(t("supply.priceRequired")).min(0, t("supply.priceMin")),
    supplier: Yup.string().required(t("supply.supplierRequired")),
  });

  const createOrderValidationSchema = Yup.object({
    materials: Yup.array()
      .of(materialValidationSchema)
      .min(1, t("supply.atLeastOneMaterial"))
      .required(),
  });

  const initialMaterialValues: PurchaseOrderMaterialDto = {
    materialId: 0,
    amount: 0,
    priceForUnit: 0,
    supplier: "",
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-stone-950 text-white flex items-center justify-center">
        <div>{t("catalog.loading")}</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-stone-950 text-white p-8">
      <div className="max-w-7xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-semibold mb-2">{t("supply.dashboard")}</h1>
            <p className="text-gray-400">{t("supply.dashboardDescription")}</p>
          </div>
          <button
            onClick={handleCreateOrder}
            className="flex items-center gap-2 px-6 py-3 rounded-full bg-white text-black font-medium hover:bg-gray-200 transition-colors"
          >
            <AddIcon />
            {t("supply.createOrder")}
          </button>
        </div>

        {error && (
          <div className="mb-6 p-4 rounded-xl bg-red-500/10 border border-red-500/40 text-red-400 text-sm">
            {error}
          </div>
        )}

        {lowBalanceMaterials.length > 0 && (
          <div className="mb-6 p-4 rounded-xl bg-amber-500/10 border border-amber-500/40 text-amber-400">
            <div className="flex items-center justify-between mb-2">
              <div className="font-semibold text-sm">
                {t("supply.lowBalanceAlert") || "Требуется дозаказ материалов"}
              </div>
            </div>
            <div className="text-sm space-y-1">
              {lowBalanceMaterials.map((material) => (
                <div key={material.id} className="flex items-center justify-between">
                  <span>
                    {material.name} - {t("supply.balance") || "Баланс"}: {material.currentBalance || 0} {material.unitOfMeasure}
                  </span>
                  <span className="text-amber-300">
                    {t("supply.orderPoint") || "Порог заказа"}: {material.orderPoint} {material.unitOfMeasure}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="mb-6 rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold">{t("supply.materialBalances") || "Остатки материалов"}</h2>
            <button
              onClick={() => setShowCreateMaterialModal(true)}
              className="flex items-center gap-2 px-4 py-2 rounded-full bg-emerald-500 text-white text-sm font-medium hover:bg-emerald-600 transition-colors"
            >
              <AddIcon fontSize="small" />
              {t("supply.createMaterial") || "Создать материал"}
            </button>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="text-xs uppercase text-gray-500 border-b border-gray-700">
                  <th className="text-left px-3 pb-2">{t("supply.materialName") || "Материал"}</th>
                  <th className="text-center px-3 pb-2">{t("supply.currentBalance") || "Текущий остаток"}</th>
                  <th className="text-center px-3 pb-2">{t("supply.orderPoint") || "Необходимый остаток"}</th>
                  <th className="text-center px-3 pb-2">{t("supply.unitOfMeasure") || "Ед. изм."}</th>
                  <th className="text-center px-3 pb-2">{t("supply.status") || "Статус"}</th>
                </tr>
              </thead>
              <tbody>
                {allMaterials.map((material) => {
                  const currentBalance = material.currentBalance || 0;
                  const orderPoint = material.orderPoint || 0;
                  const threshold = orderPoint * 1.15;
                  const isLow = currentBalance < threshold;
                  const percentage = orderPoint > 0 ? ((currentBalance - orderPoint) / orderPoint) * 100 : 100;

                  return (
                    <tr
                      key={material.id}
                      className={`border-b border-gray-800 hover:bg-stone-800/50 ${isLow ? 'bg-red-900/20' : ''}`}
                    >
                      <td className="px-3 py-3">
                        <div className="font-medium">{material.name}</div>
                        <div className="text-xs text-gray-500">ID: {material.id}</div>
                      </td>
                      <td className="px-3 py-3 text-center">
                        <span className={`font-semibold ${isLow ? 'text-red-400' : 'text-white'}`}>
                          {currentBalance.toFixed(2)}
                        </span>
                      </td>
                      <td className="px-3 py-3 text-center text-gray-300">
                        {orderPoint.toFixed(2)}
                      </td>
                      <td className="px-3 py-3 text-center text-gray-400">
                        {material.unitOfMeasure}
                      </td>
                      <td className="px-3 py-3 text-center">
                        {isLow ? (
                          <span className="inline-flex items-center gap-1 text-xs text-red-400">
                            {t("supply.lowStock") || "Низкий остаток"}
                            {percentage >= 0 && (
                              <span className="text-gray-500">
                                (+{percentage.toFixed(0)}%)
                              </span>
                            )}
                            {percentage < 0 && (
                              <span className="text-red-500 font-bold">
                                ({percentage.toFixed(0)}%)
                              </span>
                            )}
                          </span>
                        ) : (
                          <span className="text-xs text-emerald-400">
                            ✓ {t("supply.normal") || "Норма"}
                            {percentage >= 0 && (
                              <span className="text-gray-500 ml-1">
                                (+{percentage.toFixed(0)}%)
                              </span>
                            )}
                          </span>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>

        <div className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] overflow-hidden">
          <table className="min-w-full">
            <thead className="bg-stone-800/50">
              <tr>
                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-300">{t("supply.orderId")}</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-300">{t("supply.status")}</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-300">{t("supply.createdAt")}</th>
                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-300">{t("supply.materialsCount")}</th>
                <th className="px-6 py-4 text-right text-sm font-semibold text-gray-300">{t("supply.actions")}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800">
              {purchaseOrders.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-gray-500">
                    {t("supply.noOrders")}
                  </td>
                </tr>
              ) : (
                purchaseOrders.map((order) => (
                  <tr key={order.id} className="hover:bg-stone-800/30 transition-colors">
                    <td className="px-6 py-4 text-sm">#{order.id}</td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ${
                          order.status === "COMPLETED"
                            ? "bg-emerald-500/10 text-emerald-300 ring-emerald-500/40"
                            : "bg-sky-500/10 text-sky-300 ring-sky-500/40"
                        }`}
                      >
                        {order.status === "COMPLETED" ? t("supply.completed") : t("supply.created")}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-300">
                      {new Date(order.createdAt).toLocaleDateString('ru-RU')}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-300">
                      {order.materials?.length || 0}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => handleViewDetails(order)}
                          className="p-2 rounded-lg bg-stone-800/50 hover:bg-stone-700/50 transition-colors"
                          title={t("supply.viewDetails")}
                        >
                          <VisibilityIcon fontSize="small" />
                        </button>
                        {/*{order.status === "CREATED" && (*/}
                        {/*  <button*/}
                        {/*    onClick={() => handleEditOrder(order)}*/}
                        {/*    className="p-2 rounded-lg bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 transition-colors"*/}
                        {/*    title={t("supply.approveOrder")}*/}
                        {/*  >*/}
                        {/*    <CheckCircleIcon fontSize="small" />*/}
                        {/*  </button>*/}
                        {/*)}*/}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {showCreateModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-stone-900 rounded-3xl border border-gray-800 p-8 max-w-4xl w-full max-h-[90vh] overflow-y-auto">
              <h2 className="text-2xl font-semibold mb-6">{t("supply.createOrder")}</h2>
              <Formik
                initialValues={{ materials: [initialMaterialValues] }}
                validationSchema={createOrderValidationSchema}
                onSubmit={handleSubmitCreate}
              >
                {({ values, isSubmitting }) => (
                  <Form>
                    <FieldArray name="materials">
                      {({ push, remove }) => (
                        <div className="space-y-4">
                          {values.materials.map((_, index) => (
                            <div key={index} className="p-4 rounded-xl bg-stone-800/50 border border-gray-700">
                              <div className="flex justify-between items-center mb-4">
                                <h3 className="text-lg font-medium">{t("supply.material")} {index + 1}</h3>
                                {values.materials.length > 1 && (
                                  <button
                                    type="button"
                                    onClick={() => remove(index)}
                                    className="p-2 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 transition-colors"
                                  >
                                    <DeleteIcon fontSize="small" />
                                  </button>
                                )}
                              </div>
                              <div className="grid grid-cols-2 gap-4">
                                <div>
                                  <label className="block text-sm text-gray-300 mb-1">
                                    {t("supply.material")} *
                                  </label>
                                  <div className="flex gap-2">
                                    <Field
                                      name={`materials.${index}.materialId`}
                                      as="select"
                                      className="flex-1 rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white"
                                    >
                                      <option value={0}>{t("supply.selectMaterial")}</option>
                                      {allMaterials.map((mat) => (
                                        <option key={mat.id} value={mat.id}>
                                          {mat.name} ({mat.unitOfMeasure})
                                        </option>
                                      ))}
                                    </Field>
                                    <button
                                      type="button"
                                      onClick={() => setShowCreateMaterialModal(true)}
                                      className="px-4 py-2.5 rounded-xl bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 text-sm font-medium transition-colors"
                                      title={t("supply.createMaterial")}
                                    >
                                      <AddIcon fontSize="small" />
                                    </button>
                                  </div>
                                  <ErrorMessage
                                    name={`materials.${index}.materialId`}
                                    component="div"
                                    className="text-xs text-red-400 mt-1"
                                  />
                                  {values.materials[index]?.materialId > 0 && (() => {
                                    const selectedMaterial = allMaterials.find(m => m.id === values.materials[index].materialId);
                                    return selectedMaterial ? (
                                      <div className="text-xs text-gray-500 mt-1">
                                        {t("supply.unitOfMeasure")}: {selectedMaterial.unitOfMeasure}
                                      </div>
                                    ) : null;
                                  })()}
                                </div>
                                <div>
                                  <label className="block text-sm text-gray-300 mb-1">
                                    {t("supply.amount")} {values.materials[index]?.materialId > 0 && (() => {
                                      const selectedMaterial = allMaterials.find(m => m.id === values.materials[index].materialId);
                                      return selectedMaterial ? `(${selectedMaterial.unitOfMeasure})` : '';
                                    })()} *
                                  </label>
                                  <Field
                                    name={`materials.${index}.amount`}
                                    type="number"
                                    step="0.01"
                                    className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white"
                                  />
                                  <ErrorMessage
                                    name={`materials.${index}.amount`}
                                    component="div"
                                    className="text-xs text-red-400 mt-1"
                                  />
                                </div>
                                <div>
                                  <label className="block text-sm text-gray-300 mb-1">
                                    {t("supply.priceForUnit")} *
                                  </label>
                                  <Field
                                    name={`materials.${index}.priceForUnit`}
                                    type="number"
                                    step="0.01"
                                    className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white"
                                  />
                                  <ErrorMessage
                                    name={`materials.${index}.priceForUnit`}
                                    component="div"
                                    className="text-xs text-red-400 mt-1"
                                  />
                                </div>
                                <div>
                                  <label className="block text-sm text-gray-300 mb-1">
                                    {t("supply.supplier")} *
                                  </label>
                                  <Field
                                    name={`materials.${index}.supplier`}
                                    className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white"
                                  />
                                  <ErrorMessage
                                    name={`materials.${index}.supplier`}
                                    component="div"
                                    className="text-xs text-red-400 mt-1"
                                  />
                                </div>
                              </div>
                            </div>
                          ))}
                          <button
                            type="button"
                            onClick={() => push(initialMaterialValues)}
                            className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-xl border border-gray-700 hover:bg-stone-800/50 transition-colors"
                          >
                            <AddIcon />
                            {t("supply.addMaterial")}
                          </button>
                        </div>
                      )}
                    </FieldArray>
                    <div className="flex gap-4 mt-6">
                      <button
                        type="button"
                        onClick={() => setShowCreateModal(false)}
                        className="flex-1 rounded-full border border-gray-700 text-white text-sm font-medium py-2.5 hover:bg-gray-800 transition-colors"
                      >
                        {t("application.cancel")}
                      </button>
                      <button
                        type="submit"
                        disabled={isSubmitting}
                        className="flex-1 rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors disabled:opacity-50"
                      >
                        {isSubmitting ? t("application.submitting") : t("supply.create")}
                      </button>
                    </div>
                  </Form>
                )}
              </Formik>
            </div>
          </div>
        )}

        {showEditModal && selectedOrder && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-stone-900 rounded-3xl border border-gray-800 p-8 max-w-4xl w-full max-h-[90vh] overflow-y-auto">
              <h2 className="text-2xl font-semibold mb-6">{t("supply.approveOrder")} #{selectedOrder.id}</h2>
              <Formik
                initialValues={{ materials: selectedOrder.materials || [] }}
                validationSchema={createOrderValidationSchema}
                onSubmit={handleSubmitEdit}
              >
                {({ values, isSubmitting }) => (
                  <Form>
                    <FieldArray name="materials">
                      {({ push, remove }) => (
                        <div className="space-y-4">
                          {values.materials.map((_, index) => (
                            <div key={index} className="p-4 rounded-xl bg-stone-800/50 border border-gray-700">
                              <div className="flex justify-between items-center mb-4">
                                <h3 className="text-lg font-medium">{t("supply.material")} {index + 1}</h3>
                                {values.materials.length > 1 && (
                                  <button
                                    type="button"
                                    onClick={() => remove(index)}
                                    className="p-2 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 transition-colors"
                                  >
                                    <DeleteIcon fontSize="small" />
                                  </button>
                                )}
                              </div>
                              <div className="grid grid-cols-2 gap-4">
                                <div>
                                  <label className="block text-sm text-gray-300 mb-1">
                                    {t("supply.material")} *
                                  </label>
                                  <div className="flex gap-2">
                                    <Field
                                      name={`materials.${index}.materialId`}
                                      as="select"
                                      className="flex-1 rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white"
                                    >
                                      <option value={0}>{t("supply.selectMaterial")}</option>
                                      {allMaterials.map((mat) => (
                                        <option key={mat.id} value={mat.id}>
                                          {mat.name} ({mat.unitOfMeasure})
                                        </option>
                                      ))}
                                    </Field>
                                    <button
                                      type="button"
                                      onClick={() => setShowCreateMaterialModal(true)}
                                      className="px-4 py-2.5 rounded-xl bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 text-sm font-medium transition-colors"
                                      title={t("supply.createMaterial")}
                                    >
                                      <AddIcon fontSize="small" />
                                    </button>
                                  </div>
                                  <ErrorMessage
                                    name={`materials.${index}.materialId`}
                                    component="div"
                                    className="text-xs text-red-400 mt-1"
                                  />
                                  {values.materials[index]?.materialId > 0 && (() => {
                                    const selectedMaterial = allMaterials.find(m => m.id === values.materials[index].materialId);
                                    return selectedMaterial ? (
                                      <div className="text-xs text-gray-500 mt-1">
                                        {t("supply.unitOfMeasure")}: {selectedMaterial.unitOfMeasure}
                                      </div>
                                    ) : null;
                                  })()}
                                </div>
                                <div>
                                  <label className="block text-sm text-gray-300 mb-1">
                                    {t("supply.amount")} {values.materials[index]?.materialId > 0 && (() => {
                                      const selectedMaterial = allMaterials.find(m => m.id === values.materials[index].materialId);
                                      return selectedMaterial ? `(${selectedMaterial.unitOfMeasure})` : '';
                                    })()} *
                                  </label>
                                  <Field
                                    name={`materials.${index}.amount`}
                                    type="number"
                                    step="0.01"
                                    className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white"
                                  />
                                  <ErrorMessage
                                    name={`materials.${index}.amount`}
                                    component="div"
                                    className="text-xs text-red-400 mt-1"
                                  />
                                </div>
                                <div>
                                  <label className="block text-sm text-gray-300 mb-1">
                                    {t("supply.priceForUnit")} *
                                  </label>
                                  <Field
                                    name={`materials.${index}.priceForUnit`}
                                    type="number"
                                    step="0.01"
                                    className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white"
                                  />
                                  <ErrorMessage
                                    name={`materials.${index}.priceForUnit`}
                                    component="div"
                                    className="text-xs text-red-400 mt-1"
                                  />
                                </div>
                                <div>
                                  <label className="block text-sm text-gray-300 mb-1">
                                    {t("supply.supplier")} *
                                  </label>
                                  <Field
                                    name={`materials.${index}.supplier`}
                                    className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white"
                                  />
                                  <ErrorMessage
                                    name={`materials.${index}.supplier`}
                                    component="div"
                                    className="text-xs text-red-400 mt-1"
                                  />
                                </div>
                              </div>
                            </div>
                          ))}
                          <button
                            type="button"
                            onClick={() => push(initialMaterialValues)}
                            className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-xl border border-gray-700 hover:bg-stone-800/50 transition-colors"
                          >
                            <AddIcon />
                            {t("supply.addMaterial")}
                          </button>
                        </div>
                      )}
                    </FieldArray>
                    <div className="flex gap-4 mt-6">
                      <button
                        type="button"
                        onClick={() => {
                          setShowEditModal(false);
                          setSelectedOrder(null);
                        }}
                        className="flex-1 rounded-full border border-gray-700 text-white text-sm font-medium py-2.5 hover:bg-gray-800 transition-colors"
                      >
                        {t("application.cancel")}
                      </button>
                      <button
                        type="submit"
                        disabled={isSubmitting}
                        className="flex-1 rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors disabled:opacity-50"
                      >
                        {isSubmitting ? t("application.submitting") : t("supply.approve")}
                      </button>
                    </div>
                  </Form>
                )}
              </Formik>
            </div>
          </div>
        )}

        {showDetailsModal && selectedOrder && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-stone-900 rounded-3xl border border-gray-800 p-8 max-w-4xl w-full max-h-[90vh] overflow-y-auto">
              <h2 className="text-2xl font-semibold mb-6">
                {receiptInfo[selectedOrder.id]?.invoiceNumber 
                  ? `${t("supply.invoice")} ${receiptInfo[selectedOrder.id].invoiceNumber}`
                  : `${t("supply.orderDetails")} #${selectedOrder.id}`}
              </h2>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm text-gray-400 mb-1">{t("supply.status")}</label>
                  <span
                    className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ${
                      selectedOrder.status === "COMPLETED"
                        ? "bg-emerald-500/10 text-emerald-300 ring-emerald-500/40"
                        : "bg-sky-500/10 text-sky-300 ring-sky-500/40"
                    }`}
                  >
                    {selectedOrder.status === "COMPLETED" ? t("supply.completed") : t("supply.created")}
                  </span>
                </div>
                <div>
                  <label className="block text-sm text-gray-400 mb-1">{t("supply.createdAt")}</label>
                  <p className="text-white">
                    {new Date(selectedOrder.createdAt).toLocaleString('ru-RU')}
                  </p>
                </div>
                <div>
                  <label className="block text-sm text-gray-400 mb-2">{t("supply.materials")}</label>
                  <div className="space-y-2">
                    {selectedOrder.materials && selectedOrder.materials.length > 0 ? (
                      selectedOrder.materials.map((material, index) => {
                        const materialInfo = allMaterials.find(m => m.id === material.materialId);
                        const receipt = receiptInfo[selectedOrder.id];
                        const receivedAmount = material.realAmount || 0;
                        
                        return (
                          <div key={index} className="p-4 rounded-xl bg-stone-800/50 border border-gray-700">
                            <div className="grid grid-cols-2 gap-4">
                              <div>
                                <span className="text-xs text-gray-400">{t("supply.material")}:</span>
                                <p className="text-white">
                                  {materialInfo ? `${materialInfo.name} (${materialInfo.unitOfMeasure})` : `ID: ${material.materialId}`}
                                </p>
                              </div>
                              <div>
                                <span className="text-xs text-gray-400">{t("supply.amount")}:</span>
                                <p className="text-white">{material.amount} {materialInfo?.unitOfMeasure || ''}</p>
                              </div>
                              {receipt && (
                                <div>
                                  <span className="text-xs text-gray-400">{t("supply.receivedAmount")}:</span>
                                  <p className={`text-white ${receivedAmount < material.amount ? 'text-amber-400' : receivedAmount > material.amount ? 'text-orange-400' : ''}`}>
                                    {receivedAmount} {materialInfo?.unitOfMeasure || ''}
                                  </p>
                                </div>
                              )}
                              <div>
                                <span className="text-xs text-gray-400">{t("supply.priceForUnit")}:</span>
                                <p className="text-white">{material.priceForUnit}</p>
                              </div>
                              <div>
                                <span className="text-xs text-gray-400">{t("supply.supplier")}:</span>
                                <p className="text-white">{material.supplier}</p>
                              </div>
                            </div>
                          </div>
                        );
                      })
                    ) : (
                      <p className="text-gray-500">{t("supply.noMaterials")}</p>
                    )}
                  </div>
                </div>
              </div>
              <div className="flex justify-end mt-6">
                <button
                  onClick={() => {
                    setShowDetailsModal(false);
                    setSelectedOrder(null);
                  }}
                  className="px-6 py-2 rounded-full bg-white text-black text-sm font-medium hover:bg-gray-200 transition-colors"
                >
                  {t("close")}
                </button>
              </div>
            </div>
          </div>
        )}

        {showCreateMaterialModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-stone-900 rounded-3xl border border-gray-800 p-8 max-w-2xl w-full">
              <h2 className="text-2xl font-semibold mb-6">{t("supply.createMaterial")}</h2>
              <Formik
                initialValues={{
                  name: "",
                  unitOfMeasure: "",
                  orderPoint: 0,
                }}
                validationSchema={Yup.object({
                  name: Yup.string().required(t("supply.materialNameRequired")).min(1, t("supply.materialNameMin")),
                  unitOfMeasure: Yup.string().required(t("supply.unitOfMeasureRequired")),
                  orderPoint: Yup.number().required(t("supply.orderPointRequired")).min(0, t("supply.orderPointMin")),
                })}
                onSubmit={handleCreateMaterial}
              >
                {({ isSubmitting }) => (
                  <Form className="space-y-4">
                    <div>
                      <label className="block text-sm text-gray-300 mb-1">
                        {t("supply.materialName")} *
                      </label>
                      <Field
                        name="name"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white"
                        placeholder={t("supply.materialNamePlaceholder")}
                      />
                      <ErrorMessage
                        name="name"
                        component="div"
                        className="text-xs text-red-400 mt-1"
                      />
                    </div>
                    <div>
                      <label className="block text-sm text-gray-300 mb-1">
                        {t("supply.unitOfMeasure")} *
                      </label>
                      <Field
                        name="unitOfMeasure"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white"
                        placeholder={t("supply.unitOfMeasurePlaceholder")}
                      />
                      <ErrorMessage
                        name="unitOfMeasure"
                        component="div"
                        className="text-xs text-red-400 mt-1"
                      />
                    </div>
                    <div>
                      <label className="block text-sm text-gray-300 mb-1">
                        {t("supply.orderPoint")} *
                      </label>
                      <Field
                        name="orderPoint"
                        type="number"
                        step="0.01"
                        min="0"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white"
                        placeholder={t("supply.orderPointPlaceholder")}
                      />
                      <ErrorMessage
                        name="orderPoint"
                        component="div"
                        className="text-xs text-red-400 mt-1"
                      />
                    </div>
                    <div className="flex gap-4 mt-6">
                      <button
                        type="button"
                        onClick={() => setShowCreateMaterialModal(false)}
                        className="flex-1 rounded-full border border-gray-700 text-white text-sm font-medium py-2.5 hover:bg-gray-800 transition-colors"
                      >
                        {t("cancel")}
                      </button>
                      <button
                        type="submit"
                        disabled={isSubmitting || creatingMaterial}
                        className="flex-1 rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors disabled:opacity-50"
                      >
                        {creatingMaterial ? t("supply.creating") : t("supply.create")}
                      </button>
                    </div>
                  </Form>
                )}
              </Formik>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default SupplyManagerDashboard;

