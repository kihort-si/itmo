import { useTranslation } from "react-i18next";
import { useState, useEffect, useRef } from "react";
import { useParams, Link } from "react-router-dom";
import {
  ordersService,
  conversationsService,
  clientsService,
  employeesService,
  filesService,
  applicationsService,
  designsService,
  extractApiError,
  type OrderStatus,
} from "../../../services/api";
import type {
  ClientOrderResponseDto,
  ClientApplicationResponseDto,
  ConversationResponseDto,
  MessageResponseDto,
  ClientResponseDto,
  EmployeeResponseDto,
  FileMetadataResponseDto,
} from "../../../services/api/types";
import { useUserRole } from "../../../hooks/useUserRole.ts";
import { getOrderStatusTranslationKey, getOrderStatusStyle } from "../../../utils/orderStatus";
import SendIcon from "@mui/icons-material/Send";
import UploadFileIcon from "@mui/icons-material/UploadFile";
import DeleteIcon from "@mui/icons-material/Delete";
import AttachFileIcon from "@mui/icons-material/AttachFile";
import DownloadIcon from "@mui/icons-material/Download";
import VisibilityIcon from "@mui/icons-material/Visibility";

function OrderDetails() {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const orderId = id ? Number(id) : null;
  const { isClient, isStaff, role } = useUserRole();

  const [order, setOrder] = useState<ClientOrderResponseDto | null>(null);
  const [application, setApplication] = useState<ClientApplicationResponseDto | null>(null);
  const [conversation, setConversation] = useState<ConversationResponseDto | null>(null);
  const [messages, setMessages] = useState<MessageResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [sending, setSending] = useState(false);
  const [messageText, setMessageText] = useState("");
  const [uploadedFiles, setUploadedFiles] = useState<File[]>([]);
  const [uploadedFileIds, setUploadedFileIds] = useState<number[]>([]);
  const [uploading, setUploading] = useState(false);
  const [clientInfo, setClientInfo] = useState<ClientResponseDto | null>(null);
  const [managerInfo, setManagerInfo] = useState<EmployeeResponseDto | null>(null);
  const [authorNames, setAuthorNames] = useState<Record<number, string>>({});
  const [orderFiles, setOrderFiles] = useState<FileMetadataResponseDto[]>([]);
  const [loadingFiles, setLoadingFiles] = useState(false);
  const [designFiles, setDesignFiles] = useState<FileMetadataResponseDto[]>([]);
  const [loadingDesignFiles, setLoadingDesignFiles] = useState(false);
  const [messageAttachments, setMessageAttachments] = useState<Map<number, FileMetadataResponseDto[]>>(new Map());
  const [changingStatus, setChangingStatus] = useState(false);
  const [showDenyModal, setShowDenyModal] = useState(false);
  const [denyComment, setDenyComment] = useState("");
  const [processingApproval, setProcessingApproval] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!orderId) {
      setError("Order ID is required");
      setLoading(false);
      return;
    }

    loadOrderData();
  }, [orderId]);

  useEffect(() => {
    if (conversation) {
      loadMessages();
    }
  }, [conversation]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (!Array.isArray(messages)) return;
    const authorIds = new Set(messages.map((m) => m.authorId));
    authorIds.forEach((authorId) => {
      if (!authorNames[authorId]) {
        loadAuthorName(authorId).catch(err => {
          console.error(`Failed to load author name for ${authorId}:`, err);
        });
      }
    });
  }, [messages]);

  const loadOrderData = async () => {
    if (!orderId) return;

    try {
      setLoading(true);
      setError(null);

      const [orderData, conversationData] = await Promise.all([
        ordersService.getOrderById(orderId),
        conversationsService.getConversationByOrderId(orderId).catch(() => null),
      ]);

      setOrder(orderData);
      setConversation(conversationData);

      if (orderData.clientApplicationId) {
        try {
          const applicationData = await applicationsService.getApplicationById(orderData.clientApplicationId);
          setApplication(applicationData);
        } catch (err) {
          console.error("Failed to load application:", err);
        }
        loadOrderFiles(orderData.clientApplicationId);
      }

      if (orderData.productDesignId && orderData.status === "CLIENT_PENDING_APPROVAL") {
        loadDesignFiles(orderData.productDesignId);
      }

      if (isClient) {
        if (orderData.managerId) {
          try {
            const manager = await employeesService.getEmployeeById(orderData.managerId);
            setManagerInfo(manager);
          } catch (err) {
            console.error("Failed to load manager info:", err);
          }
        }
      } else if (isStaff) {
        try {
          const applicationToClientMap: Record<number, number> = {
            801: 456,
            802: 457,
            803: 458,
            804: 459,
            805: 460,
          };
          const clientId = applicationToClientMap[orderData.clientApplicationId];
          if (clientId) {
            const client = await clientsService.getClientById(clientId);
            setClientInfo(client);
          }
        } catch (err) {
          console.error("Failed to load client info:", err);
        }
      }
    } catch (err) {
      console.error("Failed to load order data:", err);
      const apiError = extractApiError(err);
      setError(apiError.message || "Failed to load order");
    } finally {
      setLoading(false);
    }
  };

  const formatOrderName = (order: ClientOrderResponseDto): string => {
    const date = new Date(order.createdAt);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `ORD-${year}-${month}-${day}-${order.id}`;
  };

  const loadOrderFiles = async (applicationId: number) => {
    try {
      setLoadingFiles(true);
      const files = await applicationsService.getApplicationAttachments(applicationId);
      setOrderFiles(files || []);
    } catch (err) {
      console.error("Failed to load order files:", err);
      setOrderFiles([]);
    } finally {
      setLoadingFiles(false);
    }
  };

  const loadDesignFiles = async (designId: number) => {
    try {
      setLoadingDesignFiles(true);
      const design = await designsService.getDesignById(designId);
      setDesignFiles(design.files || []);
    } catch (err) {
      console.error("Failed to load design files:", err);
      setDesignFiles([]);
    } finally {
      setLoadingDesignFiles(false);
    }
  };

  const handleDownloadFile = async (fileId: number, filename: string) => {
    try {
      await filesService.downloadFile(fileId, filename);
    } catch (err) {
      console.error("Failed to download file:", err);
      const apiError = extractApiError(err);
      setError(apiError.message || "Failed to download file");
    }
  };

  const handleViewFile = async (fileId: number, filename: string, contentType: string) => {
    try {
      if (contentType.startsWith('image/')) {
        const url = await filesService.getFileUrl(fileId);
        if (url) {
          window.open(url, '_blank');
        } else {
          await handleDownloadFile(fileId, filename);
        }
      } else {
        await handleDownloadFile(fileId, filename);
      }
    } catch (err) {
      console.error("Failed to view file:", err);
      const apiError = extractApiError(err);
      setError(apiError.message || "Failed to view file");
    }
  };

  const loadAuthorName = async (authorId: number) => {
    if (authorNames[authorId]) return;

    try {
      try {
        const client = await clientsService.getClientByAccountId(authorId);
        if (client) {
          setAuthorNames((prev) => ({
            ...prev,
            [authorId]: `${client.person.firstName} ${client.person.lastName}`,
          }));
          return;
        }
      } catch (err) {
        const apiError = extractApiError(err);
        if (apiError.status !== 404) {
          console.error(`Failed to load client for accountId ${authorId}:`, apiError);
        }
      }

      try {
        const employee = await employeesService.getEmployeeByAccountId(authorId);
        if (employee) {
          setAuthorNames((prev) => ({
            ...prev,
            [authorId]: `${employee.person.firstName} ${employee.person.lastName}`,
          }));
          return;
        }
      } catch (err) {
        const apiError = extractApiError(err);
        if (apiError.status !== 404) {
          console.error(`Failed to load employee for accountId ${authorId}:`, apiError);
        }
      }
    } catch (err) {
      console.error("Failed to load author name:", err);
    }
  };

  const loadMessageAttachments = async (messageId: number, fileIds: number[]) => {
    if (fileIds.length === 0) return;
    
    try {
      const files: FileMetadataResponseDto[] = [];
      for (const fileId of fileIds) {
        try {
          const file = await filesService.getFileMetadata(fileId);
          files.push(file);
        } catch (err) {
          console.error(`Failed to load file ${fileId}:`, err);
        }
      }
      setMessageAttachments(prev => new Map(prev).set(messageId, files));
    } catch (err) {
      console.error(`Failed to load attachments for message ${messageId}:`, err);
    }
  };

  const loadMessages = async () => {
    if (!conversation) return;

    try {
      const messagesData = await conversationsService.getMessages(conversation.id, {
        sort: ["sentAt,ASC"],
      });
      const messagesArray = Array.isArray(messagesData) ? messagesData : [];
      setMessages(messagesArray);

      for (const message of messagesArray) {
        if (message.attachmentFileIds && message.attachmentFileIds.length > 0) {
          await loadMessageAttachments(message.id, message.attachmentFileIds);
        }
      }
    } catch (err) {
      console.error("Failed to load messages:", err);
      setMessages([]);
    }
  };

  const handleChangeStatusToInProgress = async () => {
    if (!order) return;

    try {
      setChangingStatus(true);
      setError(null);
      await ordersService.changeOrderStatus(order.id, "IN_PROGRESS");
      setOrder({ ...order, status: "IN_PROGRESS" });
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || t("order.statusChangeError"));
    } finally {
      setChangingStatus(false);
    }
  };

  const handleSendToApproval = async () => {
    if (!order) return;

    try {
      setChangingStatus(true);
      setError(null);
      await ordersService.changeOrderStatus(order.id, "CONSTRUCTOR_PENDING_APPROVAL");
      setOrder({ ...order, status: "CONSTRUCTOR_PENDING_APPROVAL" });
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || t("order.statusChangeError"));
    } finally {
      setChangingStatus(false);
    }
  };

  const handleFileUpload = async (files: FileList | null) => {
    if (!files || files.length === 0) return;

    setUploading(true);
    try {
      const newFiles = Array.from(files);
      const uploadedIds: number[] = [];

      for (const file of newFiles) {
        try {
          const fileMetadata = await filesService.uploadFile(file);
          uploadedIds.push(fileMetadata.id);
        } catch (err) {
          console.error("Failed to upload file:", err);
        }
      }

      setUploadedFiles((prev) => [...prev, ...newFiles]);
      setUploadedFileIds((prev) => [...prev, ...uploadedIds]);
    } catch (err) {
      console.error("Failed to upload files:", err);
    } finally {
      setUploading(false);
    }
  };

  const handleRemoveFile = (index: number) => {
    setUploadedFiles((prev) => prev.filter((_, i) => i !== index));
    setUploadedFileIds((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!conversation || !messageText.trim() || sending) return;

    if (order?.status === "COMPLETED") {
      setError(t("order.cannotSendToCompleted"));
      return;
    }

    // if (uploadedFiles.length > 0 && uploadedFileIds.length === 0) {
    //   await handleFileUpload();
    // }

    try {
      setSending(true);
      setError(null);

      const messageData = {
        content: messageText.trim(),
        attachmentFileIds: uploadedFileIds.length > 0 ? uploadedFileIds : undefined,
      };

      await conversationsService.sendMessage(conversation.id, messageData);
      await loadMessages();

      setMessageText("");
      setUploadedFiles([]);
      setUploadedFileIds([]);
    } catch (err) {
      console.error("Failed to send message:", err);
      const apiError = extractApiError(err);
      setError(apiError.message || "Failed to send message");
    } finally {
      setSending(false);
    }
  };

  const handleClientApprove = async () => {
    if (!orderId) return;

    try {
      setProcessingApproval(true);
      setError(null);
      await ordersService.clientApprove(orderId);
      await loadOrderData();
    } catch (err) {
      console.error("Failed to approve order:", err);
      const apiError = extractApiError(err);
      setError(apiError.message || t("order.approveError") || "Ошибка одобрения заказа");
    } finally {
      setProcessingApproval(false);
    }
  };

  const handleClientDeny = async () => {
    if (!orderId || !denyComment.trim()) {
      setError(t("order.denyCommentRequired") || "Необходимо указать причину отклонения");
      return;
    }

    try {
      setProcessingApproval(true);
      setError(null);

      if (conversation) {
        await conversationsService.sendMessage(conversation.id, {
          content: denyComment.trim(),
        });
      }

      await ordersService.clientDeny(orderId);

      await loadOrderData();
      setShowDenyModal(false);
      setDenyComment("");
    } catch (err) {
      console.error("Failed to deny order:", err);
      const apiError = extractApiError(err);
      setError(apiError.message || t("order.denyError") || "Ошибка отклонения заказа");
    } finally {
      setProcessingApproval(false);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  const getStatusLabel = (status: string): string => {
    return t(getOrderStatusTranslationKey(status as OrderStatus));
  };

  const getStatusStyle = (status: string): string => {
    return getOrderStatusStyle(status as OrderStatus);
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

  const getAuthorName = (authorId: number): string => {
    return authorNames[authorId] || `${t("order.messageAuthor")} #${authorId}`;
  };

  const canSendMessage = order?.status !== "COMPLETED";

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center text-gray-400">{t("order.loading")}</div>
      </div>
    );
  }

  if (error && !order) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center text-red-400">{error || t("order.notFound")}</div>
      </div>
    );
  }

  if (!order) return null;

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex gap-6 h-[calc(100vh-12rem)]">
        <div className="w-1/3 flex-shrink-0">
          <div className="bg-stone-950/70 rounded-xl border border-gray-700 p-6 h-full overflow-y-auto">
            <h1 className="text-2xl font-bold text-white mb-6">
              {formatOrderName(order)}
            </h1>

            {application && application.catalogProductId && (
              <div className="mb-4 p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/40">
                <div className="text-emerald-400 text-sm font-medium mb-1">
                  📦 {t("order.basedOnCatalogProduct") || "Заказ создан на основе товара из каталога"}
                </div>
                <Link
                  to={`/catalog/${application.catalogProductId}`}
                  className="text-emerald-300 hover:text-emerald-200 text-xs underline"
                >
                  {t("order.viewInCatalog") || "Посмотреть в каталоге"}
                </Link>
              </div>
            )}

            <div className="space-y-4">
              <div>
                <label className="text-xs text-gray-500 uppercase mb-1 block">
                  {t("order.statusLabel")}
                </label>
                <div className="flex items-center gap-2">
                  <span
                    className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ring-1 ${getStatusStyle(order.status)}`}
                  >
                    {getStatusLabel(order.status)}
                  </span>
                  {role === "SALES_MANAGER" && order.status === "CREATED" && (
                    <button
                      onClick={handleChangeStatusToInProgress}
                      disabled={changingStatus}
                      className="px-3 py-1 rounded-full bg-emerald-500 text-white text-xs font-medium hover:bg-emerald-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {changingStatus ? t("order.changingStatus") : t("order.startProcessing")}
                    </button>
                  )}
                  {role === "SALES_MANAGER" && order.status === "REWORK" && (
                    <button
                      onClick={handleSendToApproval}
                      disabled={changingStatus}
                      className="px-3 py-1 rounded-full bg-emerald-500 text-white text-xs font-medium hover:bg-emerald-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {changingStatus ? t("order.changingStatus") : t("order.sendToApproval")}
                    </button>
                  )}
                  {isClient && order.status === "CLIENT_PENDING_APPROVAL" && (
                    <div className="flex gap-2 mt-2">
                      <button
                        onClick={handleClientApprove}
                        disabled={processingApproval}
                        className="flex-1 px-3 py-1.5 rounded-full bg-emerald-500 text-white text-xs font-medium hover:bg-emerald-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        {processingApproval ? t("order.processing") || "Обработка..." : t("order.approve") || "Принять"}
                      </button>
                      <button
                        onClick={() => setShowDenyModal(true)}
                        disabled={processingApproval}
                        className="flex-1 px-3 py-1.5 rounded-full bg-red-500 text-white text-xs font-medium hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        {t("order.deny") || "Отклонить"}
                      </button>
                    </div>
                  )}
                </div>
              </div>

              {isClient && order.status === "CLIENT_PENDING_APPROVAL" && (
                <div className="p-4 rounded-lg bg-amber-500/10 border border-amber-500/40">
                  <p className="text-amber-400 text-sm">
                    ⚠️ {t("order.clientApprovalRequired") || "Требуется ваше одобрение заказа"}
                  </p>
                </div>
              )}

              <div>
                <label className="text-xs text-gray-500 uppercase mb-1 block">
                  {t("order.price")}
                </label>
                <p className="text-white font-semibold">
                  {order.price !== null && order.price !== undefined
                    ? `${order.price.toLocaleString("ru-RU")} ₽`
                    : "—"}
                </p>
              </div>

              <div>
                <label className="text-xs text-gray-500 uppercase mb-1 block">
                  {t("order.createdAt")}
                </label>
                <p className="text-gray-300">{formatDate(order.createdAt)}</p>
              </div>

              {isClient && managerInfo && (
                <div>
                  <label className="text-xs text-gray-500 uppercase mb-1 block">
                    {t("order.manager")}
                  </label>
                  <p className="text-gray-300">
                    {managerInfo.person.firstName} {managerInfo.person.lastName}
                  </p>
                </div>
              )}

              {isStaff && clientInfo && (
                <div>
                  <label className="text-xs text-gray-500 uppercase mb-1 block">
                    {t("order.client")}
                  </label>
                  <p className="text-gray-300">
                    {clientInfo.person.firstName} {clientInfo.person.lastName}
                  </p>
                </div>
              )}

              {order.clientApplicationId && (
                <div>
                  <label className="text-xs text-gray-500 uppercase mb-2 block">
                    {t("order.attachments")}
                  </label>
                  {loadingFiles ? (
                    <div className="text-xs text-gray-500">
                      {t("order.loadingFiles")}
                    </div>
                  ) : orderFiles.length > 0 ? (
                    <div className="space-y-2">
                      {orderFiles.map((file) => (
                        <div
                          key={file.id}
                          className="flex items-center justify-between bg-stone-900/50 rounded-lg px-3 py-2 border border-gray-800"
                        >
                          <div className="flex items-center gap-2 flex-1 min-w-0">
                            <AttachFileIcon className="text-gray-400 text-lg flex-shrink-0" />
                            <span className="text-sm text-gray-300 truncate" title={file.filename}>
                              {file.filename}
                            </span>
                            <span className="text-xs text-gray-500 flex-shrink-0">
                              ({(file.sizeBytes / 1024).toFixed(1)} KB)
                            </span>
                          </div>
                          <div className="flex items-center gap-2 ml-2">
                            {file.contentType.startsWith('image/') && (
                              <button
                                onClick={() => handleViewFile(file.id, file.filename, file.contentType)}
                                className="p-1.5 text-emerald-400 hover:text-emerald-300 hover:bg-emerald-500/10 rounded-lg transition-colors"
                                title={t("order.view")}
                              >
                                <VisibilityIcon className="text-lg" />
                              </button>
                            )}
                            <button
                              onClick={() => handleDownloadFile(file.id, file.filename)}
                              className="p-1.5 text-emerald-400 hover:text-emerald-300 hover:bg-emerald-500/10 rounded-lg transition-colors"
                              title={t("order.download")}
                            >
                              <DownloadIcon className="text-lg" />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-xs text-gray-500">
                      {t("order.noFiles") || "Нет прикрепленных файлов"}
                    </div>
                  )}
                </div>
              )}

              {isClient && !(order.status === "CREATED" || order.status === "IN_PROGRESS") && order.productDesignId && (
                <div>
                  <label className="text-xs text-gray-500 uppercase mb-2 block">
                    {t("order.designFiles") || "Файлы дизайна"}
                  </label>
                  {loadingDesignFiles ? (
                    <div className="text-xs text-gray-500">
                      {t("order.loadingFiles")}
                    </div>
                  ) : designFiles.length > 0 ? (
                    <div className="space-y-2">
                      {designFiles.map((file) => (
                        <div
                          key={file.id}
                          className="flex items-center justify-between bg-stone-900/50 rounded-lg px-3 py-2 border border-gray-800"
                        >
                          <div className="flex items-center gap-2 flex-1 min-w-0">
                            <AttachFileIcon className="text-gray-400 text-lg flex-shrink-0" />
                            <span className="text-sm text-gray-300 truncate" title={file.filename}>
                              {file.filename}
                            </span>
                            <span className="text-xs text-gray-500 flex-shrink-0">
                              ({(file.sizeBytes / 1024).toFixed(1)} KB)
                            </span>
                          </div>
                          <div className="flex items-center gap-2 ml-2">
                            {file.contentType.startsWith('image/') && (
                              <button
                                onClick={() => handleViewFile(file.id, file.filename, file.contentType)}
                                className="p-1.5 text-emerald-400 hover:text-emerald-300 hover:bg-emerald-500/10 rounded-lg transition-colors"
                                title={t("order.view")}
                              >
                                <VisibilityIcon className="text-lg" />
                              </button>
                            )}
                            <button
                              onClick={() => handleDownloadFile(file.id, file.filename)}
                              className="p-1.5 text-emerald-400 hover:text-emerald-300 hover:bg-emerald-500/10 rounded-lg transition-colors"
                              title={t("order.download")}
                            >
                              <DownloadIcon className="text-lg" />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-xs text-gray-500">
                      {t("order.noFiles") || "Нет файлов"}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="w-2/3 flex flex-col">
          <div className="px-6 py-4 border-b border-gray-700">
            <h2 className="text-lg font-semibold text-white">
              {t("order.chat")}
            </h2>
          </div>

          <div className="flex-1 overflow-y-auto p-6 space-y-4">
            {!Array.isArray(messages) || messages.length === 0 ? (
              <div className="text-center text-gray-500 py-8">
                {t("order.noMessages")}
              </div>
            ) : (
              messages.map((message) => (
                <div
                  key={message.id}
                  className="bg-stone-900/50 rounded-lg p-4 border border-gray-800"
                >
                  <div className="flex justify-between items-start mb-2">
                    <span className="text-xs text-gray-400">
                      {getAuthorName(message.authorId)}
                    </span>
                    <span className="text-xs text-gray-500">
                      {formatDate(message.sentAt)}
                    </span>
                  </div>
                  <p className="text-white whitespace-pre-wrap">{message.content}</p>
                  {message.attachmentFileIds && message.attachmentFileIds.length > 0 && (
                    <div className="mt-2 space-y-2">
                      {messageAttachments.get(message.id)?.map((file) => (
                        <div
                          key={file.id}
                          className="flex items-center justify-between bg-stone-800/50 rounded-lg px-3 py-2 border border-gray-700"
                        >
                          <div className="flex items-center gap-2 flex-1 min-w-0">
                            <AttachFileIcon className="text-gray-400 text-lg flex-shrink-0" />
                            <span className="text-sm text-gray-300 truncate" title={file.filename}>
                              {file.filename}
                            </span>
                            <span className="text-xs text-gray-500 flex-shrink-0">
                              ({(file.sizeBytes / 1024).toFixed(1)} KB)
                            </span>
                          </div>
                          <button
                            onClick={() => handleDownloadFile(file.id, file.filename)}
                            className="text-gray-400 hover:text-white transition-colors ml-2"
                            title={t("order.download")}
                          >
                            <DownloadIcon fontSize="small" />
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))
            )}
            <div ref={messagesEndRef} />
          </div>

          {conversation && (
            <form
              onSubmit={handleSendMessage}
              className="px-6 py-4 border-t border-gray-700 space-y-3"
            >
              {!canSendMessage && (
                <div className="text-sm text-amber-400 bg-amber-500/10 border border-amber-500/40 rounded-lg p-3">
                  {t("order.cannotSendToCompleted")}
                </div>
              )}

              {uploadedFiles.length > 0 && (
                <div className="flex flex-wrap gap-2">
                  {uploadedFiles.map((file, index) => (
                    <div
                      key={index}
                      className="flex items-center gap-2 bg-stone-900/50 rounded-lg px-3 py-2 text-sm"
                    >
                      <AttachFileIcon className="text-gray-400 text-lg" />
                      <span className="text-gray-300 truncate max-w-[200px]">
                        {file.name}
                      </span>
                      <button
                        type="button"
                        onClick={() => handleRemoveFile(index)}
                        className="text-red-400 hover:text-red-300 transition-colors"
                      >
                        <DeleteIcon className="text-lg" />
                      </button>
                    </div>
                  ))}
                </div>
              )}

              <div className="flex gap-3">
                <input
                  type="text"
                  value={messageText}
                  onChange={(e) => setMessageText(e.target.value)}
                  placeholder={t("order.messagePlaceholder")}
                  className="flex-1 rounded-lg bg-stone-900/50 border border-gray-700 px-4 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                  disabled={sending || !canSendMessage}
                />
                <label className="cursor-pointer">
                  <input
                    type="file"
                    multiple
                    onChange={(e) => handleFileUpload(e.target.files)}
                    className="hidden"
                    disabled={uploading || !canSendMessage}
                  />
                  <div
                    className={`px-4 py-2 rounded-lg border border-gray-700 transition-colors ${
                      uploading || !canSendMessage
                        ? "bg-gray-800 text-gray-500 cursor-not-allowed"
                        : "bg-stone-900/50 text-white hover:bg-stone-800"
                    }`}
                  >
                    <UploadFileIcon className="text-lg" />
                  </div>
                </label>
                <button
                  type="submit"
                  disabled={(!messageText.trim() && uploadedFileIds.length === 0) || sending || !canSendMessage}
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 disabled:bg-gray-700 disabled:cursor-not-allowed text-white rounded-lg transition-colors flex items-center gap-2"
                >
                  <SendIcon className="text-lg" />
                  {sending ? t("order.sending") : t("order.send")}
                </button>
              </div>
            </form>
          )}

          {!conversation && (
            <div className="px-6 py-4 border-t border-gray-700 text-center text-gray-500 text-sm">
              {t("order.conversationNotFound")}
            </div>
          )}
        </div>
      </div>

      {/* Модальное окно отклонения заказа */}
      {showDenyModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-stone-900 rounded-xl border border-gray-700 p-6 max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold text-white mb-4">
              {t("order.denyOrder") || "Отклонить заказ"}
            </h3>
            <p className="text-sm text-gray-400 mb-4">
              {t("order.denyOrderDescription") || "Пожалуйста, укажите причину отклонения заказа"}
            </p>
            <textarea
              value={denyComment}
              onChange={(e) => setDenyComment(e.target.value)}
              placeholder={t("order.denyCommentPlaceholder") || "Введите причину отклонения..."}
              className="w-full h-32 px-3 py-2 bg-stone-950 border border-gray-700 rounded-lg text-white placeholder-gray-500 resize-none focus:outline-none focus:border-emerald-500"
            />
            <div className="flex gap-3 mt-4">
              <button
                onClick={handleClientDeny}
                disabled={processingApproval || !denyComment.trim()}
                className="flex-1 px-4 py-2 rounded-lg bg-red-500 text-white font-medium hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {processingApproval ? t("order.processing") || "Обработка..." : t("order.deny") || "Отклонить"}
              </button>
              <button
                onClick={() => {
                  setShowDenyModal(false);
                  setDenyComment("");
                }}
                disabled={processingApproval}
                className="flex-1 px-4 py-2 rounded-lg bg-stone-700 text-white font-medium hover:bg-stone-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {t("cancel") || "Отмена"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default OrderDetails;
