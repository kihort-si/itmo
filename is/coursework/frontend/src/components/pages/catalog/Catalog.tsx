import { useTranslation } from "react-i18next";
import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { catalogService, filesService, extractApiError } from "../../../services/api";
import type { ProductCatalogResponseDto, CatalogQueryParams } from "../../../services/api/types";

function Catalog() {
  const { t } = useTranslation();
  const [products, setProducts] = useState<ProductCatalogResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [categoryFilter, setCategoryFilter] = useState<string>("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [categories, setCategories] = useState<string[]>([]);
  const [photoUrls, setPhotoUrls] = useState<Record<number, string>>({});

  useEffect(() => {
    loadProducts();
  }, [page, categoryFilter]);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const response = await catalogService.getProducts({ page: 0, size: 1000 });
      const uniqueCategories = Array.from(
        new Set(
          response.content
            .map(p => p.category)
            .filter((c): c is string => c !== undefined && c !== null)
        )
      );
      setCategories(uniqueCategories);
    } catch (err) {
      console.error("Failed to load categories:", err);
    }
  };

  const loadProducts = async () => {
    try {
      setLoading(true);
      setError(null);
      const params: CatalogQueryParams = {
        page,
        size: 20,
        sort: ["id,ASC"],
      };

      if (categoryFilter) {
        params.category = categoryFilter;
      }

      const response = await catalogService.getProducts(params);
      setProducts(response.content);
      setTotalPages(response.page.totalPages);

      const photoPromises = response.content
        .filter(product => product.photos && product.photos.length > 0)
        .map(async (product) => {
          const firstPhoto = product.photos![0];
          try {
            const blob = await filesService.getFileBlob(firstPhoto.fileId);
            const url = URL.createObjectURL(blob);
            return { fileId: firstPhoto.fileId, url };
          } catch (error) {
            console.error(`Failed to load photo for product ${product.id}:`, error);
            return null;
          }
        });

      const photoResults = await Promise.all(photoPromises);
      const newPhotoUrls: Record<number, string> = {};
      photoResults.forEach(result => {
        if (result) {
          newPhotoUrls[result.fileId] = result.url;
        }
      });
      setPhotoUrls(newPhotoUrls);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || "Ошибка загрузки каталога");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-72px-72px)] bg-stone-950 text-white px-4 py-10">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-3xl font-semibold tracking-tight mb-8">
          {t("catalog.title")}
        </h1>

        <div className="mb-8 rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => {
                setCategoryFilter("");
                setPage(0);
              }}
              className={`rounded-full px-4 py-2 text-sm font-medium transition-colors ${
                categoryFilter === ""
                  ? "bg-white text-black"
                  : "bg-stone-800 text-white hover:bg-stone-700"
              }`}
            >
              {t("catalog.allCategories")}
            </button>
            {categories.map((category) => (
              <button
                key={category}
                onClick={() => {
                  setCategoryFilter(category);
                  setPage(0);
                }}
                className={`rounded-full px-4 py-2 text-sm font-medium transition-colors ${
                  categoryFilter === category
                    ? "bg-white text-black"
                    : "bg-stone-800 text-white hover:bg-stone-700"
                }`}
              >
                {category}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="text-center py-12 text-gray-400">
            {t("catalog.loading")}
          </div>
        ) : error ? (
          <div className="text-center py-12 text-red-400">
            {error}
          </div>
        ) : products.length === 0 ? (
          <div className="text-center py-12 text-gray-400">
            {t("catalog.noProducts")}
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {products.map((product) => {
                const firstPhoto = product.photos?.[0];
                const photoUrl = firstPhoto ? photoUrls[firstPhoto.fileId] : null;

                return (
                  <Link
                    key={product.id}
                    to={`/catalog/${product.id}`}
                    className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6 hover:bg-stone-800 transition-colors"
                  >
                    <div className="aspect-square bg-stone-800 rounded-xl overflow-hidden mb-4">
                      {photoUrl ? (
                        <img
                          src={photoUrl}
                          alt={product.name}
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-gray-500">
                          {firstPhoto ? t("catalog.loading") : "Нет фотографий"}
                        </div>
                      )}
                    </div>
                    <h3 className="text-lg font-semibold mb-2">{product.name}</h3>
                    {product.description && (
                      <p className="text-sm text-gray-400 mb-4 line-clamp-2">
                        {product.description}
                      </p>
                    )}
                    <div className="flex items-center justify-between">
                      <div>
                        <div className="text-2xl font-bold">{product.price} ₽</div>
                        {product.minimalAmount > 1 && (
                          <div className="text-xs text-gray-500">
                            {t("catalog.minimalAmount")}: {product.minimalAmount}
                          </div>
                        )}
                      </div>
                      {product.category && (
                        <span className="text-xs rounded-full border border-gray-700 px-3 py-1">
                          {product.category}
                        </span>
                      )}
                    </div>
                  </Link>
                );
              })}
            </div>

            {totalPages > 1 && (
              <div className="mt-8 flex justify-center gap-2">
                <button
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="px-4 py-2 rounded-full border border-gray-700 hover:bg-gray-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Назад
                </button>
                <span className="px-4 py-2 text-sm text-gray-400">
                  {page + 1} / {totalPages}
                </span>
                <button
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={page >= totalPages - 1}
                  className="px-4 py-2 rounded-full border border-gray-700 hover:bg-gray-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Вперед
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default Catalog;