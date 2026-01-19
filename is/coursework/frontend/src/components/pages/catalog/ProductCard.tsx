import { useTranslation } from "react-i18next";
import { useState, useEffect } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { catalogService, filesService, designsService, materialsService, extractApiError } from "../../../services/api";
import type { ProductCatalogResponseDto } from "../../../services/api/types";

function ProductCard() {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [product, setProduct] = useState<ProductCatalogResponseDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [photoUrls, setPhotoUrls] = useState<Record<number, string>>({});
  const [material, setMaterial] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      loadProduct();
    }
  }, [id]);

  const loadProduct = async () => {
    if (!id) return;

    try {
      setLoading(true);
      setError(null);
      const productData = await catalogService.getProductById(Number(id));
      setProduct(productData);

      if (productData.photos && productData.photos.length > 0) {
        const urls: Record<number, string> = {};
        for (const photo of productData.photos) {
          try {
            const blob = await filesService.getFileBlob(photo.fileId);
            urls[photo.fileId] = URL.createObjectURL(blob);
          } catch (err) {
            console.error(`Failed to load photo ${photo.fileId}:`, err);
          }
        }
        setPhotoUrls(urls);
      }

      if (productData.productDesignId) {
        try {
          const design = await designsService.getDesignById(productData.productDesignId);
          if (design.requiredMaterials && design.requiredMaterials.length > 0) {
            const firstMaterial = design.requiredMaterials[0];
            try {
              const material = await materialsService.getMaterialById(firstMaterial.materialId);
              setMaterial(`${material.name} (${firstMaterial.amount} ${material.unitOfMeasure})`);
            } catch (err) {
              console.error("Failed to load material:", err);
            }
          }
        } catch (err) {
          console.error("Failed to load design:", err);
        }
      }
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || "Ошибка загрузки товара");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateApplication = () => {
    navigate(`/applications/create?catalogProductId=${id}`);
  };

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-72px-72px)] bg-stone-950 text-white px-4 py-10 flex items-center justify-center">
        <div className="text-gray-400">{t("catalog.loading")}</div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="min-h-[calc(100vh-72px-72px)] bg-stone-950 text-white px-4 py-10 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-400 mb-4">{error || "Товар не найден"}</div>
          <Link
            to="/catalog"
            className="text-emerald-400 hover:text-emerald-300"
          >
            {t("product.backToCatalog")}
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-72px-72px)] bg-stone-950 text-white px-4 py-10">
      <div className="max-w-6xl mx-auto">
        <Link
          to="/catalog"
          className="text-emerald-400 hover:text-emerald-300 mb-6 inline-block"
        >
          ← {t("product.backToCatalog")}
        </Link>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div>
            <h2 className="text-lg font-semibold mb-4">{t("product.photos")}</h2>
            {product.photos && product.photos.length > 0 ? (
              <div className="space-y-4">
                {product.photos.map((photo) => (
                  <div
                    key={photo.id}
                    className="aspect-square bg-stone-800 rounded-xl overflow-hidden"
                  >
                    {photoUrls[photo.fileId] ? (
                      <img
                        src={photoUrls[photo.fileId]}
                        alt={product.name}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-gray-500">
                        Загрузка...
                      </div>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <div className="aspect-square bg-stone-800 rounded-xl flex items-center justify-center text-gray-500">
                Нет фотографий
              </div>
            )}
          </div>

          <div className="space-y-6">
            <div>
              <h1 className="text-3xl font-semibold mb-4">{product.name}</h1>
              {product.category && (
                <span className="inline-block text-xs rounded-full border border-gray-700 px-3 py-1 mb-4">
                  {product.category}
                </span>
              )}
            </div>

            {product.description && (
              <div>
                <h2 className="text-lg font-semibold mb-2">{t("product.description")}</h2>
                <p className="text-gray-300">{product.description}</p>
              </div>
            )}

            <div className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
              <div className="space-y-4">
                {material && (
                  <div>
                    <div className="text-xs text-gray-500 mb-1">{t("product.material")}</div>
                    <div className="text-lg">{material}</div>
                  </div>
                )}

                <div>
                  <div className="text-xs text-gray-500 mb-1">{t("product.price")}</div>
                  <div className="text-3xl font-bold">{product.price} ₽</div>
                </div>

                <div>
                  <div className="text-xs text-gray-500 mb-1">
                    {t("product.minimalAmount")}
                  </div>
                  <div className="text-lg">{product.minimalAmount}</div>
                </div>

                <button
                  onClick={handleCreateApplication}
                  className="w-full rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors"
                >
                  {t("product.createApplication")}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductCard;

