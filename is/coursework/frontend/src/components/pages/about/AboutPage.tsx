import { useTranslation } from "react-i18next";

function AboutPage() {
  const { t } = useTranslation();

  return (
    <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10">
      <div className="max-w-4xl mx-auto space-y-8">
        <div>
          <h1 className="text-4xl font-bold mb-4">{t("about.title")}</h1>
          <p className="text-xl text-gray-300">{t("about.subtitle")}</p>
        </div>

        <section className="space-y-6">
          <h2 className="text-2xl font-semibold">{t("about.whoWeAre")}</h2>
          <p className="text-gray-300 leading-relaxed">{t("about.whoWeAreText")}</p>
        </section>

        <section className="space-y-6">
          <h2 className="text-2xl font-semibold">{t("about.ourMission")}</h2>
          <p className="text-gray-300 leading-relaxed">{t("about.ourMissionText")}</p>
        </section>

        <section className="space-y-6">
          <h2 className="text-2xl font-semibold">{t("about.whyChooseUs")}</h2>
          <ul className="space-y-4 text-gray-300">
            <li className="flex items-start gap-3">
              <span className="text-emerald-400 mt-1">✓</span>
              <span>{t("about.advantage1")}</span>
            </li>
            <li className="flex items-start gap-3">
              <span className="text-emerald-400 mt-1">✓</span>
              <span>{t("about.advantage2")}</span>
            </li>
            <li className="flex items-start gap-3">
              <span className="text-emerald-400 mt-1">✓</span>
              <span>{t("about.advantage3")}</span>
            </li>
            <li className="flex items-start gap-3">
              <span className="text-emerald-400 mt-1">✓</span>
              <span>{t("about.advantage4")}</span>
            </li>
            <li className="flex items-start gap-3">
              <span className="text-emerald-400 mt-1">✓</span>
              <span>{t("about.advantage5")}</span>
            </li>
          </ul>
        </section>

        <section className="space-y-6">
          <h2 className="text-2xl font-semibold">{t("about.contactUs")}</h2>
          <div className="space-y-2 text-gray-300">
            <p>
              <strong>{t("about.phone")}:</strong>{" "}
              <a href="tel:+79310000123" className="text-emerald-400 hover:text-emerald-300">
                +7(931)000-01-23
              </a>
            </p>
            <p>
              <strong>{t("about.email")}:</strong>{" "}
              <a href="mailto:info@laserworks.ru" className="text-emerald-400 hover:text-emerald-300">
                info@laserworks.ru
              </a>
            </p>
          </div>
        </section>
      </div>
    </div>
  );
}

export default AboutPage;

