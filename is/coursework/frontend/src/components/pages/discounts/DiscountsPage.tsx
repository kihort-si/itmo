import { useTranslation } from "react-i18next";

function DiscountsPage() {
  const { t } = useTranslation();

  return (
    <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10">
      <div className="max-w-4xl mx-auto space-y-8">
        <div>
          <h1 className="text-4xl font-bold mb-4">{t("discounts.title")}</h1>
          <p className="text-xl text-gray-300">{t("discounts.subtitle")}</p>
        </div>

        <section className="space-y-6">
          <div className="rounded-2xl border border-emerald-500/30 bg-emerald-500/10 p-6">
            <h2 className="text-2xl font-semibold mb-3 text-emerald-400">
              {t("discounts.currentPromotion")}
            </h2>
            <p className="text-gray-300 leading-relaxed mb-4">
              {t("discounts.currentPromotionText")}
            </p>
            <div className="text-3xl font-bold text-emerald-400">
              {t("discounts.discountAmount")}
            </div>
          </div>
        </section>

        <section className="space-y-6">
          <h2 className="text-2xl font-semibold">{t("discounts.regularDiscounts")}</h2>
          <div className="space-y-4">
            <div className="rounded-xl border border-gray-800 bg-stone-900/50 p-5">
              <h3 className="text-xl font-semibold mb-2">{t("discounts.bulkOrder")}</h3>
              <p className="text-gray-300">{t("discounts.bulkOrderText")}</p>
            </div>
            <div className="rounded-xl border border-gray-800 bg-stone-900/50 p-5">
              <h3 className="text-xl font-semibold mb-2">{t("discounts.regularCustomer")}</h3>
              <p className="text-gray-300">{t("discounts.regularCustomerText")}</p>
            </div>
            <div className="rounded-xl border border-gray-800 bg-stone-900/50 p-5">
              <h3 className="text-xl font-semibold mb-2">{t("discounts.firstOrder")}</h3>
              <p className="text-gray-300">{t("discounts.firstOrderText")}</p>
            </div>
          </div>
        </section>

        <section className="space-y-6">
          <h2 className="text-2xl font-semibold">{t("discounts.howToGet")}</h2>
          <ol className="space-y-3 text-gray-300 list-decimal list-inside">
            <li>{t("discounts.step1")}</li>
            <li>{t("discounts.step2")}</li>
            <li>{t("discounts.step3")}</li>
          </ol>
        </section>

        <section className="rounded-2xl border border-gray-800 bg-stone-900/50 p-6">
          <h2 className="text-2xl font-semibold mb-4">{t("discounts.questions")}</h2>
          <p className="text-gray-300 mb-4">{t("discounts.questionsText")}</p>
          <a
            href="tel:+79310000123"
            className="inline-block rounded-full bg-emerald-600 px-6 py-3 font-medium hover:bg-emerald-700 transition-colors"
          >
            {t("discounts.contactUs")}
          </a>
        </section>
      </div>
    </div>
  );
}

export default DiscountsPage;

