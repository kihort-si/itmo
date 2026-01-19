import { useTranslation } from "react-i18next";
import { useState, useEffect } from "react";
import {
  ordersService,
  designsService,
  filesService,
  materialsService,
  extractApiError,
  type ProductionTaskResponseDto, productionService
} from "../../../services/api";
import type { ClientOrderResponseDto, FileMetadataResponseDto, RequiredMaterialDto, MaterialResponseDto } from "../../../services/api/types";
import DownloadIcon from "@mui/icons-material/Download";
import AttachFileIcon from "@mui/icons-material/AttachFile";

function OperatorDashboard() {
  const { t } = useTranslation();
  const [currentOrder, setCurrentOrder] = useState<ClientOrderResponseDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [designFiles, setDesignFiles] = useState<FileMetadataResponseDto[]>([]);
  const [loadingFiles, setLoadingFiles] = useState(false);
  const [fileTypes, setFileTypes] = useState<Map<number, '3d' | 'up'>>(new Map());
  const [changingStatus, setChangingStatus] = useState(false);
  const [requiredMaterials, setRequiredMaterials] = useState<RequiredMaterialDto[]>([]);
  const [materialsMap, setMaterialsMap] = useState<Map<number, MaterialResponseDto>>(new Map());
  const [currentTask, setCurrentTask] = useState<ProductionTaskResponseDto | null>(null);

  const loadCurrentTask = async () => {
    try {
      setLoading(true);
      setError(null);

      const task = await productionService.getCurrentProductionTask();
      setCurrentTask(task);

      if (!task) {
        setCurrentOrder(null);
        setDesignFiles([]);
        setFileTypes(new Map());
        setRequiredMaterials([]);
        return;
      }

      const order = await ordersService.getOrderById(task.clientOrderId);
      setCurrentOrder(order);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || apiError.detail || "Ошибка загрузки задачи");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCurrentTask();
  }, []);

  useEffect(() => {
    if (currentOrder?.productDesignId) {
      loadDesignFiles(currentOrder.productDesignId);
    } else {
      setDesignFiles([]);
      setFileTypes(new Map());
      setRequiredMaterials([]);
    }
  }, [currentOrder?.productDesignId]);

  const loadMaterial = async (materialId: number) => {
    if (materialsMap.has(materialId)) return;
    try {
      const material = await materialsService.getMaterialById(materialId);
      setMaterialsMap(prev => new Map(prev).set(materialId, material));
    } catch (err) {
      console.error(`Failed to load material ${materialId}:`, err);
    }
  };

  const loadDesignFiles = async (designId: number) => {
    try {
      setLoadingFiles(true);
      const design = await designsService.getDesignById(designId);
      setDesignFiles(design.files || []);
      setRequiredMaterials(design.requiredMaterials || []);

      if (design.requiredMaterials) {
        for (const mat of design.requiredMaterials) {
          await loadMaterial(mat.materialId);
        }
      }

      const filesMap = new Map<number, '3d' | 'up'>();
      design.files?.forEach(file => {
        const ext = file.filename.toLowerCase().split('.').pop();
        if (['stl', 'obj', '3ds', 'step', 'iges', 'stp', 'igs'].includes(ext || '')) {
          filesMap.set(file.id, '3d');
        } else if (['nc', 'cnc', 'tap', 'gcode'].includes(ext || '')) {
          filesMap.set(file.id, 'up');
        }
      });
      setFileTypes(filesMap);
    } catch (err) {
      console.error(`Failed to load design files for design ${designId}:`, err);
      setDesignFiles([]);
      setFileTypes(new Map());
      setRequiredMaterials([]);
    } finally {
      setLoadingFiles(false);
    }
  };

  const handleDownloadFile = async (fileId: number, filename: string) => {
    try {
      await filesService.downloadFile(fileId, filename);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка загрузки файла');
      console.error('Failed to download file:', err);
    }
  };

  const handleStartProduction = async () => {
    if (currentTask && currentTask.status === "PENDING") {
      try {
        setChangingStatus(true);
        setError(null);
        await productionService.startTask(currentTask.id);
        await loadCurrentTask();
      } catch (err) {
        const apiError = extractApiError(err);
        setError(apiError.message || apiError.detail || "Ошибка начала выполнения");
      } finally {
        setChangingStatus(false);
      }
    }
  };

  const handleCompleteOrder = async () => {
    if (currentTask && currentTask.status === "IN_PROGRESS") {
      try {
        setChangingStatus(true);
        setError(null);
        await productionService.completeTask(currentTask.id);

        setCurrentTask(null);
        setCurrentOrder(null);
        setDesignFiles([]);
        setFileTypes(new Map());
        setRequiredMaterials([]);

        await loadCurrentTask();
      } catch (err) {
        const apiError = extractApiError(err);
        setError(apiError.message || apiError.detail || "Ошибка завершения задачи");
      } finally {
        setChangingStatus(false);
      }
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
            {t("operator.dashboard")}
          </h1>
        </div>

        <section className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
          <h2 className="text-lg font-semibold mb-4">
            {t("operator.currentTask")}
          </h2>

          {(currentTask && currentOrder) ? (
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <div className="text-xs text-gray-500 mb-1">
                    {t("manager.order")}
                  </div>
                  <div className="text-sm font-medium text-white">
                    {(() => {
                      const date = new Date(currentOrder.createdAt);
                      const year = date.getFullYear();
                      const month = String(date.getMonth() + 1).padStart(2, '0');
                      const day = String(date.getDate()).padStart(2, '0');
                      return `ORD-${year}-${month}-${day}-${currentOrder.id}`;
                    })()}
                  </div>
                  <div className="text-xs text-gray-500">
                    {new Date(currentOrder.createdAt).toLocaleDateString('ru-RU')}
                  </div>
                </div>

                <div>
                  <div className="text-xs text-gray-500 mb-1">
                    {t("order.price")}
                  </div>
                  <div className="text-sm text-white">
                    {currentOrder.price ? `${currentOrder.price.toLocaleString("ru-RU")} ₽` : "—"}
                  </div>
                </div>
              </div>

              {currentOrder.productDesignId && (
                <div className="mt-4">
                  <div className="text-xs text-gray-500 mb-2">
                    {t("operator.designFiles") || "Файлы дизайна"}
                  </div>
                  {loadingFiles ? (
                    <div className="text-sm text-gray-400">{t("catalog.loading")}...</div>
                  ) : designFiles.length > 0 ? (
                    <div className="space-y-2">
                      {designFiles.map((file) => {
                        const fileType = fileTypes.get(file.id);
                        const is3D = fileType === '3d';
                        const isUP = fileType === 'up';
                        
                        return (
                          <div
                            key={file.id}
                            className="flex items-center justify-between bg-stone-800/50 rounded-lg px-3 py-2 border border-gray-700"
                          >
                            <div className="flex items-center gap-2 flex-1 min-w-0">
                              <AttachFileIcon className="text-gray-400 text-lg flex-shrink-0" />
                              <span className="text-sm text-gray-300 truncate" title={file.filename}>
                                {file.filename}
                              </span>
                              {(is3D || isUP) && (
                                <span className="text-xs text-gray-500 flex-shrink-0">
                                  ({is3D ? t("operator.3dModel") : t("operator.upFile")})
                                </span>
                              )}
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
                        );
                      })}
                    </div>
                  ) : (
                    <div className="text-sm text-gray-500">
                      {t("operator.noDesignFiles") || "Нет файлов дизайна"}
                    </div>
                  )}
                </div>
              )}

              {requiredMaterials.length > 0 && (
                <div className="mt-4">
                  <div className="text-xs text-gray-500 mb-2">
                    {t("operator.materials") || "Материалы"}
                  </div>
                  <div className="space-y-2">
                    {requiredMaterials.map((mat, index) => {
                      const material = materialsMap.get(mat.materialId);
                      return (
                        <div
                          key={index}
                          className="flex items-center justify-between bg-stone-800/50 rounded-lg px-3 py-2 border border-gray-700"
                        >
                          <div className="flex items-center gap-2 flex-1 min-w-0">
                            <span className="text-sm text-gray-300">
                              {material ? material.name : `Материал #${mat.materialId}`}
                            </span>
                            {material && (
                              <span className="text-xs text-gray-500">
                                ({material.unitOfMeasure})
                              </span>
                            )}
                          </div>
                          <div className="text-sm text-white">
                            {mat.amount} {material ? material.unitOfMeasure : ''}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              <div className="flex gap-4 pt-2">
                {currentTask?.status === "PENDING" && (
                  <button
                    onClick={handleStartProduction}
                    disabled={changingStatus}
                    className="flex-1 rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors disabled:opacity-50"
                  >
                    {changingStatus ? t("operator.starting") : t("operator.startWork")}
                  </button>
                )}
                {currentTask?.status === "IN_PROGRESS" && (
                  <button
                    onClick={handleCompleteOrder}
                    disabled={changingStatus}
                    className="flex-1 rounded-full bg-emerald-600 text-white text-sm font-medium py-2.5 hover:bg-emerald-700 transition-colors disabled:opacity-50"
                  >
                    {changingStatus ? t("operator.completing") : t("operator.completeTask")}
                  </button>
                )}
              </div>
            </div>
          ) : (
            <div className="text-center py-8 text-gray-500">
              {t("operator.noCurrentTask")}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}

export default OperatorDashboard;
