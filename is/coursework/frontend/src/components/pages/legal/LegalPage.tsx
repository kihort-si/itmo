import { useTranslation } from "react-i18next";

function LegalPage() {
  const { t } = useTranslation();

  return (
    <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10">
      <div className="max-w-4xl mx-auto space-y-8">
        <div>
          <h1 className="text-4xl font-bold mb-4">{t("legal.title")}</h1>
          <p className="text-xl text-gray-300">{t("legal.subtitle")}</p>
        </div>

        <section className="space-y-6">
          <h2 className="text-2xl font-semibold">{t("legal.cooperation")}</h2>
          <p className="text-gray-300 leading-relaxed">{t("legal.cooperationText")}</p>
        </section>

        <section className="space-y-6">
          <h2 className="text-2xl font-semibold">{t("legal.advantages")}</h2>
          <ul className="space-y-4 text-gray-300">
            <li className="flex items-start gap-3">
              <span className="text-emerald-400 mt-1">✓</span>
              <span>{t("legal.advantage1")}</span>
            </li>
            <li className="flex items-start gap-3">
              <span className="text-emerald-400 mt-1">✓</span>
              <span>{t("legal.advantage2")}</span>
            </li>
            <li className="flex items-start gap-3">
              <span className="text-emerald-400 mt-1">✓</span>
              <span>{t("legal.advantage3")}</span>
            </li>
            <li className="flex items-start gap-3">
              <span className="text-emerald-400 mt-1">✓</span>
              <span>{t("legal.advantage4")}</span>
            </li>
            <li className="flex items-start gap-3">
              <span className="text-emerald-400 mt-1">✓</span>
              <span>{t("legal.advantage5")}</span>
            </li>
          </ul>
        </section>

        <section className="space-y-6">
          <h2 className="text-2xl font-semibold">{t("legal.payment")}</h2>
          <div className="space-y-3 text-gray-300">
            <p>{t("legal.paymentText")}</p>
            <ul className="list-disc list-inside space-y-2 ml-4">
              <li>{t("legal.paymentMethod1")}</li>
              <li>{t("legal.paymentMethod2")}</li>
              <li>{t("legal.paymentMethod3")}</li>
            </ul>
          </div>
        </section>

        <section className="space-y-6">
          <h2 className="text-2xl font-semibold">{t("legal.delivery")}</h2>
          <p className="text-gray-300 leading-relaxed">{t("legal.deliveryText")}</p>
        </section>

        <section className="space-y-6">
          <h2 className="text-2xl font-semibold">{t("legal.documents")}</h2>
          <p className="text-gray-300 leading-relaxed">{t("legal.documentsText")}</p>
        </section>

        <section className="rounded-2xl border border-gray-800 bg-stone-900/50 p-6">
          <h2 className="text-2xl font-semibold mb-4">{t("legal.contact")}</h2>
          <div className="space-y-3 text-gray-300">
            <p>
              <strong>{t("legal.phone")}:</strong>{" "}
              <a href="tel:+79310000123" className="text-emerald-400 hover:text-emerald-300">
                +7(931)000-01-23
              </a>
            </p>
            <p>
              <strong>{t("legal.email")}:</strong>{" "}
              <a href="mailto:corporate@laserworks.ru" className="text-emerald-400 hover:text-emerald-300">
                corporate@laserworks.ru
              </a>
            </p>
            <p className="mt-4">{t("legal.contactText")}</p>
          </div>
        </section>
      </div>
    </div>
  );
}

export default LegalPage;

