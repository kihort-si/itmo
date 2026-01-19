import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { useState, useMemo, useEffect } from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import {
  ordersService, applicationsService, clientsService, catalogService, designsService, filesService, extractApiError,
  authService
} from "../../../services/api";
import type { ClientOrderResponseDto, ClientApplicationResponseDto, ClientResponseDto, ProductDesignResponseDto, ProductCatalogRequestDto } from "../../../services/api/types";
import { getOrderStatusTranslationKey, getOrderStatusStyle } from "../../../utils/orderStatus";

interface ManagerStats {
  newApplicationsCount: number;
  currentApplicationsCount: number;
  pendingApprovalCount: number;
}

function ManagerDashboard() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [orders, setOrders] = useState<ClientOrderResponseDto[]>([]);
  const [myOrders, setMyOrders] = useState<ClientOrderResponseDto[]>([]);
  const [applications, setApplications] = useState<ClientApplicationResponseDto[]>([]);
  const [clients, setClients] = useState<Map<number, ClientResponseDto>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeFilter, setActiveFilter] = useState<string | null>(null);

  const [isCreateProductOpen, setIsCreateProductOpen] = useState(false);
  const [creatingProduct, setCreatingProduct] = useState(false);
  const [productError, setProductError] = useState<string | null>(null);
  const [productSuccess, setProductSuccess] = useState(false);
  const [designs, setDesigns] = useState<ProductDesignResponseDto[]>([]);
  const [loadingDesigns, setLoadingDesigns] = useState(false);
  const [uploadedPhotoFiles, setUploadedPhotoFiles] = useState<number[]>([]);

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

        const [ordersData, applicationsData] = await Promise.all([
          ordersService.getOrders(),
          applicationsService.getApplications()
        ]);
        
        const ordersArray = Array.isArray(ordersData) ? ordersData : [];
        const me = await authService.getCurrentUser();
        setOrders(ordersArray);
        setMyOrders(ordersArray.filter(order => order.managerId === me.employee?.id));
        setApplications(applicationsData.content || []);

        const clientsMap = new Map<number, ClientResponseDto>();
        const uniqueClientIds = new Set<number>();

        applicationsData.content?.forEach(app => {
          if (app.clientId) {
            uniqueClientIds.add(app.clientId);
          }
        });

        for (const clientId of uniqueClientIds) {
          try {
            const client = await clientsService.getClientById(clientId);
            clientsMap.set(clientId, client);
          } catch (err) {
            console.error(`Failed to load client ${clientId}:`, err);
          }
        }
        
        setClients(clientsMap);
      } catch (err) {
        const apiError = extractApiError(err);
        setError(apiError.message || 'Ошибка загрузки данных');
      } finally {
        setLoading(false);
      }
    };
    
    loadData();
  }, []);

  const stats = useMemo<ManagerStats>(() => {
    const ordersArray = Array.isArray(orders) ? orders : [];
    const applicationsArray = Array.isArray(applications) ? applications : [];
    
    const newApplicationsCount = applicationsArray.filter(
      (app) => !app.id || !ordersArray.some(order => order.clientApplicationId === app.id)
    ).length;

    const currentApplicationsCount = ordersArray.filter(
      (order) => order.status === "CREATED" || order.status === "IN_PROGRESS"
    ).length;

    const pendingApprovalCount = ordersArray.filter(
      (order) => order.status === "CONSTRUCTOR_PENDING_APPROVAL" || order.status === "REWORK"
    ).length;
    
    return {
      newApplicationsCount,
      currentApplicationsCount,
      pendingApprovalCount,
    };
  }, [orders, applications]);

  const filteredOrders = useMemo(() => {
    if (!activeFilter) {
      return myOrders;
    }

    switch (activeFilter) {
      case "current":
        return orders.filter(
          (order) => order.status === "CREATED" || order.status === "IN_PROGRESS"
        );
      case "approval":
        return orders.filter(
          (order) =>
            order.status === "IN_PROGRESS" ||
            order.status === "CONSTRUCTOR_PENDING_APPROVAL" ||
            order.status === "REWORK"
        );
      default:
        return myOrders;
    }
  }, [activeFilter, orders]);

  const handleNewApplicationsClick = () => {
    navigate("/applications?filter=new");
  };

  const handleCurrentApplicationsClick = () => {
    setActiveFilter("current");
  };

  const handlePendingApprovalClick = () => {
    setActiveFilter("approval");
  };

  const handleClearFilter = () => {
    setActiveFilter(null);
  };

  const loadDesigns = async () => {
    try {
      setLoadingDesigns(true);

      const designsResponse = await designsService.getDesigns({ page: 0, size: 1000 });
      const allDesigns = designsResponse.content || [];

      const catalogResponse = await catalogService.getProducts({ page: 0, size: 1000 });
      const allProducts = catalogResponse.content || [];

      const usedDesignIds = new Set(
        allProducts
          .map(product => product.productDesignId)
          .filter((id): id is number => id !== undefined && id !== null)
      );

      const availableDesigns = allDesigns.filter(
        design => !usedDesignIds.has(design.id)
      );

      setDesigns(availableDesigns);
    } catch (error) {
      console.error('Failed to load designs:', error);
    } finally {
      setLoadingDesigns(false);
    }
  };

  useEffect(() => {
    if (isCreateProductOpen) {
      loadDesigns();
    }
  }, [isCreateProductOpen]);

  const handleFileUpload = async (file: File): Promise<number> => {
    try {
      const response = await filesService.uploadFile(file);
      return response.id;
    } catch (err) {
      const apiError = extractApiError(err);
      throw new Error(apiError.message || 'Ошибка загрузки файла');
    }
  };

  const handleCreateProduct = async (values: {
    name: string;
    description?: string;
    productDesignId?: number;
    price: number;
    minimalAmount: number;
    category?: string;
  }) => {
    try {
      setCreatingProduct(true);
      setProductError(null);
      setProductSuccess(false);

      const productData: ProductCatalogRequestDto = {
        name: values.name,
        description: values.description,
        productDesignId: values.productDesignId || undefined,
        price: values.price,
        minimalAmount: values.minimalAmount,
        category: values.category,
        photoFileIds: uploadedPhotoFiles.length > 0 ? uploadedPhotoFiles : undefined,
      };

      await catalogService.createProduct(productData);

      setProductSuccess(true);
      setUploadedPhotoFiles([]);
      setTimeout(() => {
        setIsCreateProductOpen(false);
        setProductSuccess(false);
      }, 2000);
    } catch (err) {
      const apiError = extractApiError(err);
      setProductError(apiError.message || 'Ошибка создания товара');
    } finally {
      setCreatingProduct(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10 flex items-center justify-center">
        <div className="text-gray-400">{t("catalog.loading")}</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10 flex items-center justify-center">
        <div className="text-red-400">{error}</div>
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10 flex justify-center">
      <div className="w-full max-w-7xl space-y-8">
        <div>
          <h1 className="text-3xl font-semibold tracking-tight">
            {t("manager.dashboard")}
          </h1>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <button
            onClick={handleNewApplicationsClick}
            className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6 hover:bg-stone-800 transition-colors text-left"
          >
            <div className="text-xs uppercase tracking-wide text-gray-500 mb-2">
              {t("manager.newApplications")}
            </div>
            <div className="text-4xl font-bold text-white mb-2">
              {stats.newApplicationsCount}
            </div>
            <div className="text-sm text-gray-400">
              {t("manager.newApplications")}
            </div>
          </button>

          <button
            onClick={handleCurrentApplicationsClick}
            className={`rounded-3xl border shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6 transition-colors text-left ${
              activeFilter === "current"
                ? "border-emerald-500 bg-stone-800"
                : "border-gray-800 bg-stone-900/80 hover:bg-stone-800"
            }`}
          >
            <div className="text-xs uppercase tracking-wide text-gray-500 mb-2">
              {t("manager.currentApplications")}
            </div>
            <div className="text-4xl font-bold text-white mb-2">
              {stats.currentApplicationsCount}
            </div>
            <div className="text-sm text-gray-400">
              {t("manager.currentApplications")}
            </div>
          </button>

          <button
            onClick={handlePendingApprovalClick}
            className={`rounded-3xl border shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6 transition-colors text-left ${
              activeFilter === "approval"
                ? "border-emerald-500 bg-stone-800"
                : "border-gray-800 bg-stone-900/80 hover:bg-stone-800"
            }`}
          >
            <div className="text-xs uppercase tracking-wide text-gray-500 mb-2">
              {t("manager.pendingApproval")}
            </div>
            <div className="text-4xl font-bold text-white mb-2">
              {stats.pendingApprovalCount}
            </div>
            <div className="text-sm text-gray-400">
              {t("manager.pendingApproval")}
            </div>
          </button>
        </div>

        <section className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-lg font-semibold">
                {t("manager.catalogManagement")}
              </h2>
              <p className="text-xs text-gray-400 mt-1">
                {t("manager.catalogManagementInfo")}
              </p>
            </div>
            <button
              onClick={() => setIsCreateProductOpen(true)}
              className="rounded-full bg-white text-black text-sm font-medium px-4 py-2 hover:bg-gray-200 transition-colors"
            >
              {t("manager.createProduct")}
            </button>
          </div>
        </section>

        <section className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-lg font-semibold">
                {t("manager.ordersList")}
              </h2>
              {activeFilter && (
                <button
                  onClick={handleClearFilter}
                  className="text-xs text-gray-400 hover:text-white mt-1"
                >
                  Сбросить фильтр
                </button>
              )}
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full text-sm border-separate border-spacing-y-2">
              <thead>
                <tr className="text-xs uppercase text-gray-500">
                  <th className="text-left px-3 pb-2">{t("manager.order")}</th>
                  <th className="text-left px-3 pb-2">{t("manager.client")}</th>
                  <th className="text-left px-3 pb-2">{t("manager.status")}</th>
                  <th className="text-left px-3 pb-2">
                    {t("manager.orderDate")}
                  </th>
                  <th className="text-right px-3 pb-2">
                    {t("profile.orderActions")}
                  </th>
                </tr>
              </thead>
              <tbody>
                {filteredOrders.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-3 py-8 text-center text-gray-500">
                      {t("manager.noOrders")}
                    </td>
                  </tr>
                ) : (
                  filteredOrders.map((order) => {
                    const application = applications.find(app => app.id === order.clientApplicationId);
                    const client = application ? clients.get(application.clientId) : null;
                    
                    return (
                      <tr key={order.id}>
                        <td className="px-3 py-3">
                          <div className="text-sm font-medium">{formatOrderName(order)}</div>
                          <div className="text-xs text-gray-500">ID: {order.id}</div>
                          {application && application.catalogProductId && (
                            <div className="mt-1 inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/40">
                              <span className="text-emerald-400 text-xs">
                                {t("manager.fromCatalog") || "Из каталога"}
                              </span>
                            </div>
                          )}
                        </td>
                        <td className="px-3 py-3 text-sm text-gray-300">
                          {client ? (
                            `${client.person.firstName} ${client.person.lastName}`
                          ) : (
                            <span className="text-gray-500">{t("catalog.loading")}</span>
                          )}
                        </td>
                      <td className="px-3 py-3">
                        <span
                          className={[
                            "inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ring-1",
                            getOrderStatusStyle(order.status),
                          ].join(" ")}
                        >
                          {t(getOrderStatusTranslationKey(order.status))}
                        </span>
                      </td>
                      <td className="px-3 py-3 text-xs text-gray-300">
                        {new Date(order.createdAt).toLocaleDateString('ru-RU')}
                      </td>
                      <td className="px-3 py-3 text-right">
                        <Link
                          to={`/orders/${order.id}`}
                          className="text-xs rounded-full border border-gray-700 px-3 py-1 hover:bg-gray-800 transition-colors"
                        >
                          {t("manager.openOrder")}
                        </Link>
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

      {isCreateProductOpen && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-stone-900 rounded-3xl border border-gray-800 p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold">
                {t("manager.createProduct") || "Создать товар каталога"}
              </h2>
              <button
                onClick={() => {
                  setIsCreateProductOpen(false);
                  setProductError(null);
                  setProductSuccess(false);
                  setUploadedPhotoFiles([]);
                }}
                className="text-gray-400 hover:text-white transition-colors"
              >
                ✕
              </button>
            </div>

            {productError && (
              <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/40 text-red-400 text-sm">
                {productError}
              </div>
            )}

            {productSuccess && (
              <div className="mb-4 p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/40 text-emerald-400 text-sm">
                {t("manager.productCreated")}
              </div>
            )}

            <Formik
              initialValues={{
                name: '',
                description: '',
                productDesignId: undefined,
                price: 0,
                minimalAmount: 1,
                category: '',
              }}
              validationSchema={Yup.object({
                name: Yup.string().required(t("manager.productNameRequired")),
                price: Yup.number().required().min(0, t("manager.priceMustBePositive")),
                minimalAmount: Yup.number().required().min(1, t("manager.minAmountMustBePositive")),
              })}
              onSubmit={handleCreateProduct}
            >
              {({ isSubmitting }) => (
                <Form className="space-y-4">
                  <div>
                    <label className="block text-xs text-gray-400 mb-1">
                      {t("manager.productName")}
                    </label>
                    <Field
                      name="name"
                      type="text"
                      className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                      placeholder={t("manager.productNamePlaceholder")}
                    />
                    <ErrorMessage
                      name="name"
                      component="div"
                      className="mt-1 text-xs text-red-400"
                    />
                  </div>

                  <div>
                    <label className="block text-xs text-gray-400 mb-1">
                      {t("manager.productDescription")}
                    </label>
                    <Field
                      name="description"
                      as="textarea"
                      rows={3}
                      className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                      placeholder={t("manager.productDescriptionPlaceholder")}
                    />
                  </div>

                  <div>
                    <label className="block text-xs text-gray-400 mb-1">
                      {t("manager.selectDesign")}
                    </label>
                    {loadingDesigns ? (
                      <div className="text-sm text-gray-400">{t("catalog.loading")}...</div>
                    ) : (
                      <Field
                        name="productDesignId"
                        as="select"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                      >
                        <option value="">{t("manager.noDesign") || "Без дизайна"}</option>
                        {designs.map((design) => (
                          <option key={design.id} value={design.id}>
                            {design.productName} (ID: {design.id})
                          </option>
                        ))}
                      </Field>
                    )}
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs text-gray-400 mb-1">
                        {t("manager.price") || "Цена"}
                      </label>
                      <Field
                        name="price"
                        type="number"
                        min="0"
                        step="0.01"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder="0.00"
                      />
                      <ErrorMessage
                        name="price"
                        component="div"
                        className="mt-1 text-xs text-red-400"
                      />
                    </div>

                    <div>
                      <label className="block text-xs text-gray-400 mb-1">
                        {t("manager.minimalAmount")}
                      </label>
                      <Field
                        name="minimalAmount"
                        type="number"
                        min="1"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder="1"
                      />
                      <ErrorMessage
                        name="minimalAmount"
                        component="div"
                        className="mt-1 text-xs text-red-400"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs text-gray-400 mb-1">
                      {t("manager.category")}
                    </label>
                    <Field
                      name="category"
                      type="text"
                      className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                      placeholder={t("manager.categoryPlaceholder")}
                    />
                  </div>

                  <div>
                    <label className="block text-xs text-gray-400 mb-1">
                      {t("manager.productPhotos") || "Фотографии товара"}
                    </label>
                    <input
                      type="file"
                      multiple
                      accept="image/*"
                      onChange={async (e) => {
                        const files = Array.from(e.target.files || []);
                        try {
                          const fileIds = await Promise.all(files.map(file => handleFileUpload(file)));
                          setUploadedPhotoFiles(prev => [...prev, ...fileIds]);
                        } catch (error) {
                          const errorMessage = error instanceof Error ? error.message : 'Ошибка загрузки фотографий';
                          setProductError(errorMessage);
                        }
                      }}
                      className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white"
                    />
                    {uploadedPhotoFiles.length > 0 && (
                      <div className="mt-2 text-xs text-gray-400">
                        {t("manager.photosUploaded")}: {uploadedPhotoFiles.length}
                      </div>
                    )}
                  </div>

                  <div className="flex gap-3 pt-2">
                    <button
                      type="submit"
                      disabled={creatingProduct || isSubmitting}
                      className="flex-1 rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors disabled:opacity-50"
                    >
                      {creatingProduct ? t("catalog.loading") : (t("manager.create") || "Создать")}
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setIsCreateProductOpen(false);
                        setProductError(null);
                        setProductSuccess(false);
                        setUploadedPhotoFiles([]);
                      }}
                      className="flex-1 rounded-full bg-stone-800 text-white text-sm font-medium py-2.5 hover:bg-stone-700 transition-colors"
                    >
                      {t("cancel")}
                    </button>
                  </div>
                </Form>
              )}
            </Formik>
          </div>
        </div>
      )}
    </div>
  );
}

export default ManagerDashboard;

