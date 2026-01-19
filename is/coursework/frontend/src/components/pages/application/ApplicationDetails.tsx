import { useTranslation } from "react-i18next";
import { useState, useEffect } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import {
  applicationsService,
  ordersService,
  clientsService,
  filesService,
  extractApiError,
} from "../../../services/api";
import type {
  ClientApplicationResponseDto,
  ClientOrderResponseDto,
  ClientResponseDto,
  FileMetadataResponseDto,
} from "../../../services/api/types";
import { useUserRole } from "../../../hooks/useUserRole";
import DownloadIcon from "@mui/icons-material/Download";

function ApplicationDetails() {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { role, isClient } = useUserRole();
  const applicationId = id ? Number(id) : null;
  
  const isSalesManager = role === 'SALES_MANAGER';

  const [application, setApplication] = useState<ClientApplicationResponseDto | null>(null);
  const [order, setOrder] = useState<ClientOrderResponseDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [clientInfo, setClientInfo] = useState<ClientResponseDto | null>(null);
  const [applicationFiles, setApplicationFiles] = useState<FileMetadataResponseDto[]>([]);
  const [loadingFiles, setLoadingFiles] = useState(false);
  const [creatingOrder, setCreatingOrder] = useState(false);

  useEffect(() => {
    if (!applicationId) {
      setError("Application ID is required");
      setLoading(false);
      return;
    }

    loadApplicationData();
  }, [applicationId, isSalesManager]);


  const loadApplicationData = async () => {
    if (!applicationId) return;

    try {
      setLoading(true);
      setError(null);

      const applicationData = await applicationsService.getApplicationById(applicationId);
      setApplication(applicationData);

      if (isSalesManager) {
        try {
          const client = await clientsService.getClientById(applicationData.clientId);
          setClientInfo(client);
        } catch (err) {
          console.error("Failed to load client info:", err);
        }
      }

      try {
        const allOrders = await ordersService.getOrders();
        const ordersArray = Array.isArray(allOrders) ? allOrders : [];
        const relatedOrder = ordersArray.find(o => o.clientApplicationId === applicationId);
        if (relatedOrder) {
          setOrder(relatedOrder);
        }
      } catch (err) {
        console.error("Failed to load related order:", err);
      }

      loadApplicationFiles(applicationId);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || apiError.detail || "Failed to load application");
    } finally {
      setLoading(false);
    }
  };

  const loadApplicationFiles = async (appId: number) => {
    if (loadingFiles || applicationFiles.length > 0) return;

    try {
      setLoadingFiles(true);
      const files = await applicationsService.getApplicationAttachments(appId);
      setApplicationFiles(files);
    } catch (err) {
      console.error("Failed to load application files:", err);
    } finally {
      setLoadingFiles(false);
    }
  };

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleString("ru-RU", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const handleCreateOrder = async () => {
    if (!application || creatingOrder) return;

    if (order) {
      setError(t("manager.orderAlreadyExists") || "Order already exists for this application");
      return;
    }

    if (!window.confirm(t("manager.confirmCreateOrder") || "Create an order from this application?")) {
      return;
    }

    try {
      setCreatingOrder(true);
      setError(null);

      const createdOrder = await ordersService.createOrder({
        clientApplicationId: application.id,
      });

      setOrder(createdOrder);
    } catch (err) {
      const apiError = extractApiError(err);
      if (apiError.detail?.includes("duplicate key") || apiError.detail?.includes("already exists")) {
        setError(t("manager.orderAlreadyExists") || "Order already exists for this application. Please refresh the page.");
        try {
          const allOrders = await ordersService.getOrders();
          const ordersArray = Array.isArray(allOrders) ? allOrders : [];
          const existingOrder = ordersArray.find(o => o.clientApplicationId === application.id);
          if (existingOrder) {
            setOrder(existingOrder);
          }
        } catch (loadErr) {
          console.error("Failed to load existing order:", loadErr);
        }
      } else {
        setError(apiError.message || apiError.detail || "Failed to create order");
      }
    } finally {
      setCreatingOrder(false);
    }
  };


  if (loading) {
    return (
      <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10 flex items-center justify-center">
        <div className="text-gray-400">{t("catalog.loading")}</div>
      </div>
    );
  }

  if (error && !application) {
    return (
      <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10 flex items-center justify-center">
        <div className="text-red-400">{error}</div>
      </div>
    );
  }

  if (!application) return null;

  return (
    <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10">
      <div className="container mx-auto max-w-4xl">
        <div className="bg-stone-900/80 rounded-xl border border-gray-800 p-6">
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-2xl font-bold text-white">
              {t("manager.application")} #{application.id}
            </h1>
            <button
              onClick={() => navigate(-1)}
              className="text-sm text-gray-400 hover:text-white transition-colors"
            >
              ← {t("back")}
            </button>
          </div>

          {error && (
            <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/40 text-red-400 text-sm">
              {error}
            </div>
          )}

          <div className="space-y-4">
              {isSalesManager && (
                <div>
                  <label className="text-xs text-gray-500 uppercase mb-1 block">
                    {t("manager.client")}
                  </label>
                  <p className="text-white">
                    {clientInfo ? (
                      `${clientInfo.person.firstName} ${clientInfo.person.lastName}`
                    ) : (
                      <span className="text-gray-500">{t("catalog.loading")}</span>
                    )}
                  </p>
                </div>
              )}

              <div>
                <label className="text-xs text-gray-500 uppercase mb-1 block">
                  {t("manager.applicationDate")}
                </label>
                <p className="text-gray-300">{formatDate(application.createdAt)}</p>
              </div>

              <div>
                <label className="text-xs text-gray-500 uppercase mb-1 block">
                  {t("application.description")}
                </label>
                <p className="text-gray-300 whitespace-pre-wrap">{application.description}</p>
              </div>

              <div>
                <label className="text-xs text-gray-500 uppercase mb-1 block">
                  {t("application.amount")}
                </label>
                <p className="text-white font-semibold">{application.amount}</p>
              </div>

              {application.catalogProductId && (
                <div>
                  <label className="text-xs text-gray-500 uppercase mb-1 block">
                    {t("application.catalogProduct") || "Товар из каталога"}
                  </label>
                  <Link
                    to={`/catalog/${application.catalogProductId}`}
                    className="text-emerald-400 hover:text-emerald-300"
                  >
                    {t("application.viewInCatalog") || "Посмотреть в каталоге"} #{application.catalogProductId}
                  </Link>
                </div>
              )}

              {application.templateProductDesignId && (
                <div>
                  <label className="text-xs text-gray-500 uppercase mb-1 block">
                    {t("application.templateDesign") || "Шаблон дизайна"}
                  </label>
                  <p className="text-gray-300">
                    {t("application.templateDesignId") || "ID шаблона"}: {application.templateProductDesignId}
                  </p>
                </div>
              )}

              {isSalesManager && (
                <>
                  {order ? (
                    <div>
                      <label className="text-xs text-gray-500 uppercase mb-4 block">
                        {t("order.title")}
                      </label>
                      <Link
                        to={`/orders/${order.id}`}
                        className="w-full rounded-full bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 disabled:cursor-not-allowed text-white text-sm font-medium py-2.5 px-10 transition-colors"
                      >
                        Перейти к заказу
                      </Link>
                    </div>
                  ) : (
                    <div>
                      <button
                        onClick={handleCreateOrder}
                        disabled={creatingOrder}
                        className="rounded-full bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 disabled:cursor-not-allowed text-white text-sm font-medium py-2.5 px-10 transition-colors"
                      >
                        {creatingOrder ? (t("manager.creating") || "Creating...") : (t("manager.createOrder") || "Create Order")}
                      </button>
                    </div>
                  )}
                </>
              )}

              {isClient && order && (
                <div>
                  <label className="text-xs text-gray-500 uppercase mb-1 block">
                    {t("order.title")}
                  </label>
                  <Link
                    to={`/orders/${order.id}`}
                    className="text-emerald-400 hover:text-emerald-300"
                  >
                    #{order.id}
                  </Link>
                </div>
              )}

              {applicationFiles.length > 0 && (
                <div>
                  <label className="text-xs text-gray-500 uppercase mb-2 block">
                    {t("order.files") || "Files"}
                  </label>
                  <div className="space-y-2">
                    {applicationFiles.map((file) => (
                      <div
                        key={file.id}
                        className="flex items-center justify-between p-2 bg-stone-800/50 rounded-lg"
                      >
                        <span className="text-sm text-gray-300 truncate flex-1">
                          {file.filename || `File #${file.id}`}
                        </span>
                        <div className="flex gap-2">
                          <button
                            onClick={async () => {
                              try {
                                await filesService.downloadFile(file.id, file.filename || `file-${file.id}`);
                              } catch (err) {
                                console.error("Failed to download file:", err);
                              }
                            }}
                            className="p-1 text-emerald-400 hover:text-emerald-300"
                            title={t("order.download") || "Download"}
                          >
                            <DownloadIcon fontSize="small" />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {loadingFiles && (
                <div className="text-xs text-gray-500">
                  {t("order.loadingFiles") || "Загрузка файлов..."}
                </div>
              )}
            </div>
        </div>
      </div>
    </div>
  );
}

export default ApplicationDetails;

