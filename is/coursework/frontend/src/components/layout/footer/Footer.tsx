import { FaTelegramPlane, FaWhatsapp, FaVk } from "react-icons/fa";
import { Link } from "react-router-dom";
import {useTranslation} from "react-i18next";

function Footer() {
  const { t } = useTranslation()

  return (
    <footer className="bg-stone-950 px-24 py-10 text-white">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-10">
        <div className="space-y-4 text-lg">
          <Link to="/about" className="hover:text-gray-300 transition-colors">
            {t("footer.about")}
          </Link>
          <Link to="/discounts" className="hover:text-gray-300 transition-colors block">
            {t("footer.discounts")}
          </Link>
          <Link to="/legal" className="hover:text-gray-300 transition-colors block">
            {t("footer.legal")}
          </Link>
        </div>

        <div className="flex flex-col items-end gap-6">
          <div className="flex gap-4">
            <a
              href="#"
              className="w-10 h-10 rounded-full bg-neutral-900 flex items-center justify-center hover:bg-neutral-800 transition-colors"
            >
              <FaTelegramPlane size={20} />
            </a>
            <a
              href="#"
              className="w-10 h-10 rounded-full bg-neutral-900 flex items-center justify-center hover:bg-neutral-800 transition-colors"
            >
              <FaWhatsapp size={20} />
            </a>
            <a
              href="#"
              className="w-10 h-10 rounded-full bg-neutral-900 flex items-center justify-center hover:bg-neutral-800 transition-colors"
            >
              <FaVk size={20} />
            </a>
          </div>

          <a
            href="tel:+79310000123"
            className="text-2xl font-medium tracking-wide hover:text-gray-300 transition-colors"
          >
            +7(931)000-01-23
          </a>
        </div>
      </div>

      <div className="mt-10 border-t border-gray-500 pt-4">
        <p className="text-right text-sm text-gray-300">
          © 2025 LaserWorks. {t("footer.allRights")}
        </p>
      </div>
    </footer>
  );
}

export default Footer;
