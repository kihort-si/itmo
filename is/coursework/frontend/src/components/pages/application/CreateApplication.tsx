import { useTranslation } from "react-i18next";
import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { applicationsService, filesService, catalogService, extractApiError, authService } from "../../../services/api";
import type { ClientApplicationRequestDto, ProductCatalogResponseDto } from "../../../services/api/types";
import UploadFileIcon from "@mui/icons-material/UploadFile";
import DeleteIcon from "@mui/icons-material/Delete";

function CreateApplication() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const catalogProductId = searchParams.get("catalogProductId");
  const [uploadedFiles, setUploadedFiles] = useState<File[]>([]);
  const [uploadedFileIds, setUploadedFileIds] = useState<number[]>([]);
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [catalogProduct, setCatalogProduct] = useState<ProductCatalogResponseDto | null>(null);
  const [loadingProduct, setLoadingProduct] = useState(false);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const authenticated = await authService.isAuthenticated();
        if (!authenticated) {
          navigate("/auth?mode=login", { state: { from: "/applications/create" } });
        }
      } catch {
        navigate("/auth?mode=login", { state: { from: "/applications/create" } });
      }
    };
    checkAuth();
  }, [navigate]);

  useEffect(() => {
    if (catalogProductId) {
      loadCatalogProduct();
    }
  }, [catalogProductId]);

  const loadCatalogProduct = async () => {
    if (!catalogProductId) return;

    try {
      setLoadingProduct(true);
      const product = await catalogService.getProductById(Number(catalogProductId));
      setCatalogProduct(product);
    } catch (err) {
      console.error("Failed to load catalog product:", err);
    } finally {
      setLoadingProduct(false);
    }
  };

  const minAmount = catalogProduct?.minimalAmount || 1;

  const validationSchema = Yup.object({
    description: Yup.string()
      .required(t("application.descriptionRequired"))
      .min(10, t("application.descriptionMinLength")),
    amount: Yup.number()
      .required(t("application.amountRequired"))
      .min(minAmount, t("application.amountMin", { min: minAmount })),
  });

  const initialValues: ClientApplicationRequestDto = {
    description: "",
    amount: minAmount,
    catalogProductId: catalogProductId ? Number(catalogProductId) : undefined,
    attachmentFileIds: [],
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
          setError(t("application.fileUploadError"));
        }
      }

      setUploadedFiles((prev) => [...prev, ...newFiles]);
      setUploadedFileIds((prev) => [...prev, ...uploadedIds]);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || t("application.fileUploadError"));
    } finally {
      setUploading(false);
    }
  };

  const handleRemoveFile = (index: number) => {
    setUploadedFiles((prev) => prev.filter((_, i) => i !== index));
    setUploadedFileIds((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (values: ClientApplicationRequestDto) => {
    try {
      setSubmitting(true);
      setError(null);

      const requestData: ClientApplicationRequestDto = {
        description: values.description,
        amount: values.amount,
        attachmentFileIds: values.attachmentFileIds,
      };

      if (values.catalogProductId) {
        requestData.catalogProductId = values.catalogProductId;
      }
      if (values.templateProductDesignId) {
        requestData.templateProductDesignId = values.templateProductDesignId;
      }
      if (uploadedFileIds && uploadedFileIds.length > 0) {
        requestData.attachmentFileIds = uploadedFileIds;
      } else {
        requestData.attachmentFileIds = [];
      }

      const response = await applicationsService.createApplication(requestData);

      navigate(`/profile?applicationId=${response.id}`);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || t("application.createError"));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-72px-72px)] bg-stone-950 text-white px-4 py-10">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-3xl font-semibold tracking-tight mb-8">
          {t("application.title")}
        </h1>

        {error && (
          <div className="mb-6 p-4 rounded-xl bg-red-500/10 border border-red-500/40 text-red-400 text-sm">
            {error}
          </div>
        )}

        <div className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-8">
          <Formik
            initialValues={initialValues}
            validationSchema={validationSchema}
            enableReinitialize={true}
            onSubmit={handleSubmit}
          >
            {({ isSubmitting: formSubmitting }) => (
              <Form className="space-y-6">
                <div>
                  <label className="block text-sm text-gray-300 mb-2">
                    {t("application.description")} *
                  </label>
                  <Field
                    name="description"
                    as="textarea"
                    rows={6}
                    className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400 resize-none"
                    placeholder={t("application.descriptionPlaceholder")}
                  />
                  <div className="min-h-[18px] mt-1">
                    <ErrorMessage
                      name="description"
                      component="div"
                      className="text-xs text-red-400"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm text-gray-300 mb-2">
                    {t("application.amount")} *
                    {catalogProduct && (
                      <span className="text-xs text-gray-500 ml-2">
                        ({t("application.minAmount")}: {catalogProduct.minimalAmount})
                      </span>
                    )}
                  </label>
                  <Field
                    name="amount"
                    type="number"
                    min={minAmount}
                    className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                  />
                  <div className="min-h-[18px] mt-1">
                    <ErrorMessage
                      name="amount"
                      component="div"
                      className="text-xs text-red-400"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm text-gray-300 mb-2">
                    {t("application.attachments")}
                  </label>
                  <div className="space-y-4">
                    <div className="relative">
                      <input
                        type="file"
                        multiple
                        onChange={(e) => handleFileUpload(e.target.files)}
                        className="hidden"
                        id="file-upload"
                        disabled={uploading}
                      />
                      <label
                        htmlFor="file-upload"
                        className={`flex items-center justify-center gap-2 w-full rounded-xl border border-gray-700 px-4 py-3 text-sm cursor-pointer transition-colors ${
                          uploading
                            ? "bg-stone-800 text-gray-500 cursor-not-allowed"
                            : "bg-stone-950/70 text-white hover:bg-stone-800"
                        }`}
                      >
                        <UploadFileIcon className="text-lg" />
                        {uploading ? t("application.uploading") : t("application.selectFiles")}
                      </label>
                    </div>

                    {uploadedFiles.length > 0 && (
                      <div className="space-y-2">
                        {uploadedFiles.map((file, index) => (
                          <div
                            key={index}
                            className="flex items-center justify-between bg-stone-800/50 rounded-lg px-4 py-2"
                          >
                            <span className="text-sm text-gray-300 truncate flex-1">
                              {file.name}
                            </span>
                            <button
                              type="button"
                              onClick={() => handleRemoveFile(index)}
                              className="text-red-400 hover:text-red-300 transition-colors ml-2"
                            >
                              <DeleteIcon className="text-lg" />
                            </button>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                {loadingProduct ? (
                  <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/40">
                    <p className="text-sm text-emerald-300">
                      {t("catalog.loading")}
                    </p>
                  </div>
                ) : catalogProduct ? (
                  <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/40">
                    <p className="text-sm font-semibold text-emerald-300 mb-2">
                      {t("application.catalogProductInfo")}
                    </p>
                    <p className="text-sm text-emerald-200 mb-1">
                      <strong>{t("product.title")}:</strong> {catalogProduct.name}
                    </p>
                    {catalogProduct.description && (
                      <p className="text-xs text-emerald-200/80 mb-2">
                        {catalogProduct.description}
                      </p>
                    )}
                    <p className="text-sm text-emerald-200">
                      <strong>{t("application.minAmount")}:</strong> {catalogProduct.minimalAmount}
                    </p>
                  </div>
                ) : catalogProductId ? (
                  <div className="p-4 rounded-xl bg-yellow-500/10 border border-yellow-500/40">
                    <p className="text-sm text-yellow-300">
                      {t("application.productNotFound")}
                    </p>
                  </div>
                ) : null}

                <div className="flex gap-4 pt-4">
                  <button
                    type="button"
                    onClick={() => navigate(-1)}
                    className="flex-1 rounded-full border border-gray-700 text-white text-sm font-medium py-2.5 hover:bg-gray-800 transition-colors"
                  >
                    {t("application.cancel")}
                  </button>
                  <button
                    type="submit"
                    disabled={formSubmitting || submitting}
                    className="flex-1 rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {submitting ? t("application.submitting") : t("application.submit")}
                  </button>
                </div>
              </Form>
            )}
          </Formik>
        </div>
      </div>
    </div>
  );
}

export default CreateApplication;

