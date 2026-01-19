import { useTranslation } from "react-i18next";
import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import {catalogService, filesService} from "../../../services/api";
import type { ProductCatalogResponseDto } from "../../../services/api";
import LocalPhoneIcon from "@mui/icons-material/LocalPhone";
import AttachMoneyIcon from "@mui/icons-material/AttachMoney";
import AssignmentIcon from "@mui/icons-material/Assignment";
import CodeIcon from "@mui/icons-material/Code";
import SupervisorAccountIcon from "@mui/icons-material/SupervisorAccount";
import SupportAgentIcon from "@mui/icons-material/SupportAgent";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import EmailIcon from "@mui/icons-material/Email";
import AssessmentIcon from '@mui/icons-material/Assessment';
import {YandexConstructorMap} from "./YandexConstructorMap.tsx";

function HomePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [popularProducts, setPopularProducts] = useState<ProductCatalogResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [photoUrls, setPhotoUrls] = useState<Record<number, string>>({});

  useEffect(() => {
    loadPopularProducts();
  }, []);

  const loadPopularProducts = async () => {
    try {
      setLoading(true);
      const response = await catalogService.getProducts({
        page: 0,
        size: 4,
        sort: ["id,DESC"],
      });
      setPopularProducts(response?.content || []);

      const photoPromises = (response?.content || [])
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
      console.error("Failed to load popular products:", err);
      setPopularProducts([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateApplication = () => {
    navigate("/applications/create");
  };

  return (
    <div className="bg-stone-950 text-white">
      <section className="px-4 py-20">
        <div className="max-w-7xl mx-auto text-center">
          <h1 className="text-5xl font-bold mb-6">
            {t("home.hero.title")}
          </h1>
          <p className="text-xl text-gray-300 mb-8 max-w-3xl mx-auto">
            {t("home.hero.description")}
          </p>
          <div className="flex items-center justify-center gap-2 text-lg">
            <LocalPhoneIcon className="text-emerald-400" />
            <a
              href={`tel:${t("home.contact.phone")}`}
              className="text-emerald-400 hover:text-emerald-300 transition-colors"
            >
              {t("home.contact.phone")}
            </a>
          </div>
        </div>
      </section>

      <section className="px-4 py-16 bg-stone-900/50">
        <div className="max-w-7xl mx-auto">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-3xl font-semibold">
              {t("home.popular.title")}
            </h2>
            <Link
              to="/catalog"
              className="text-emerald-400 hover:text-emerald-300 transition-colors"
            >
              {t("home.popular.viewAll")} →
            </Link>
          </div>

          {loading ? (
            <div className="text-center py-12 text-gray-400">
              {t("catalog.loading")}
            </div>
          ) : !popularProducts || popularProducts.length === 0 ? (
            <div className="text-center py-12 text-gray-400">
              {t("catalog.noProducts")}
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {popularProducts.map((product) => {
                const firstPhoto = product.photos?.[0];
                const photoUrl = firstPhoto ? photoUrls[firstPhoto.fileId] : null;

                return (
                  <Link
                    key={product.id}
                    to={`/catalog/${product.id}`}
                    className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6 hover:bg-stone-800 transition-colors"
                  >
                    <div className="mb-4 aspect-square bg-stone-800 rounded-xl overflow-hidden">
                      {photoUrl ? (
                        <img
                          src={photoUrl}
                          alt={product.name}
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-gray-500">
                          {firstPhoto ? "Загрузка..." : "Нет фотографий"}
                        </div>
                      )}
                    </div>
                    <h3 className="text-lg font-semibold mb-2">{product.name}</h3>
                    {product.description && (
                      <p className="text-sm text-gray-400 mb-4 line-clamp-2">
                        {product.description}
                      </p>
                    )}
                    <div className="text-2xl font-bold">{product.price} ₽</div>
                  </Link>
                );
              })}
            </div>
          )}
        </div>
      </section>

      <section className="px-4 py-16">
        <div className="max-w-4xl mx-auto text-center rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-12">
          <h2 className="text-3xl font-semibold mb-4">
            {t("home.custom.title")}
          </h2>
          <p className="text-gray-300 mb-8">
            {t("home.custom.description")}
          </p>
          <button
            onClick={handleCreateApplication}
            className="rounded-full bg-white text-black text-sm font-medium py-2.5 px-8 hover:bg-gray-200 transition-colors"
          >
            {t("home.custom.createApplication")}
          </button>
        </div>
      </section>

      <section className="px-4 py-16 bg-stone-900/50">
        <div className="max-w-7xl mx-auto">
          <h2 className="text-3xl font-semibold text-center mb-12">
            {t("home.whyUs.title")}
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-emerald-500/20 mb-4">
                <AttachMoneyIcon className="text-emerald-400 text-3xl" />
              </div>
              <h3 className="text-xl font-semibold mb-2">
                {t("home.whyUs.competitivePrices.title")}
              </h3>
              <p className="text-gray-400">
                {t("home.whyUs.competitivePrices.description")}
              </p>
            </div>

            <div className="text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-emerald-500/20 mb-4">
                <AssignmentIcon className="text-emerald-400 text-3xl" />
              </div>
              <h3 className="text-xl font-semibold mb-2">
                {t("home.whyUs.orderManagement.title")}
              </h3>
              <p className="text-gray-400">
                {t("home.whyUs.orderManagement.description")}
              </p>
            </div>

            <div className="text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-emerald-500/20 mb-4">
                <CodeIcon className="text-emerald-400 text-3xl" />
              </div>
              <h3 className="text-xl font-semibold mb-2">
                {t("home.whyUs.programDevelopment.title")}
              </h3>
              <p className="text-gray-400">
                {t("home.whyUs.programDevelopment.description")}
              </p>
            </div>

            <div className="text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-emerald-500/20 mb-4">
                <SupervisorAccountIcon className="text-emerald-400 text-3xl" />
              </div>
              <h3 className="text-xl font-semibold mb-2">
                {t("home.whyUs.productionControl.title")}
              </h3>
              <p className="text-gray-400">
                {t("home.whyUs.productionControl.description")}
              </p>
            </div>

            <div className="text-center md:col-span-2 lg:col-span-1 lg:col-start-2">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-emerald-500/20 mb-4">
                <SupportAgentIcon className="text-emerald-400 text-3xl" />
              </div>
              <h3 className="text-xl font-semibold mb-2">
                {t("home.whyUs.support.title")}
              </h3>
              <p className="text-gray-400">
                {t("home.whyUs.support.description")}
              </p>
            </div>

            <div className="text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-emerald-500/20 mb-4">
                <AssessmentIcon className="text-emerald-400 text-3xl" />
              </div>
              <h3 className="text-xl font-semibold mb-2">
                {t("home.whyUs.analytics.title")}
              </h3>
              <p className="text-gray-400">
                {t("home.whyUs.analytics.description")}
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="px-4 py-16">
        <div className="max-w-7xl mx-auto">
          <h2 className="text-3xl font-semibold text-center mb-12">
            {t("home.contacts.title")}
          </h2>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <div className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] overflow-hidden">
              <div className="aspect-video bg-stone-800 flex items-center justify-center">
                <div className="text-center w-full">
                  <YandexConstructorMap
                    src="https://api-maps.yandex.ru/services/constructor/1.0/js/?um=constructor%3A60077b966ee3702e7fec625426f14a20e77edf0053936b072eef7e2d0c32def3&width=741&height=508&lang=ru_RU&scroll=true"
                    height={508}
                  />
                </div>
              </div>
            </div>

            <div className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-8">
              <div className="space-y-6">
                <div className="flex items-start gap-4">
                  <LocationOnIcon className="text-emerald-400 mt-1"/>
                  <div>
                  <h3 className="font-semibold mb-1">{t("home.contacts.address.title")}</h3>
                    <p className="text-gray-400">{t("home.contacts.address.value")}</p>
                  </div>
                </div>

                <div className="flex items-start gap-4">
                  <LocalPhoneIcon className="text-emerald-400 mt-1" />
                  <div>
                    <h3 className="font-semibold mb-1">{t("home.contacts.phone.title")}</h3>
                    <a
                      href={`tel:${t("home.contact.phone")}`}
                      className="text-emerald-400 hover:text-emerald-300 transition-colors"
                    >
                      {t("home.contact.phone")}
                    </a>
                  </div>
                </div>

                <div className="flex items-start gap-4">
                  <EmailIcon className="text-emerald-400 mt-1" />
                  <div>
                    <h3 className="font-semibold mb-1">{t("home.contacts.email.title")}</h3>
                    <a
                      href={`mailto:${t("home.contacts.email.value")}`}
                      className="text-emerald-400 hover:text-emerald-300 transition-colors"
                    >
                      {t("home.contacts.email.value")}
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

export default HomePage;

