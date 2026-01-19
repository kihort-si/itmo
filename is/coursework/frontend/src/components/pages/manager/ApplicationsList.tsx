import { useTranslation } from "react-i18next";
import { Link, useSearchParams } from "react-router-dom";
import { useState, useMemo, useEffect } from "react";
import {
  applicationsService,
  ordersService,
  type ClientApplicationResponseDto,
  type ClientOrderResponseDto,
  type ClientResponseDto, clientsService,
  extractApiError
} from "../../../services/api";

function ApplicationsList() {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const filter = searchParams.get("filter");
  const [applications, setApplications] = useState<ClientApplicationResponseDto[]>([]);
  const [clients, setClients] = useState<Map<number, ClientResponseDto>>(new Map());
  const [orders, setOrders] = useState<ClientOrderResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadApplications = async () => {
      try {
        setLoading(true);
        setError(null);

        const [applicationsResponse, ordersData] = await Promise.all([
          applicationsService.getApplications(),
          ordersService.getOrders()
        ]);
        
        const apps = applicationsResponse.content || [];
        setApplications(apps);

        const ordersArray = Array.isArray(ordersData) ? ordersData : [];
        setOrders(ordersArray);

        const clientsMap = new Map<number, ClientResponseDto>();
        for (const app of apps) {
          if (!clientsMap.has(app.clientId)) {
            try {
              const client = await clientsService.getClientById(app.clientId);
              clientsMap.set(app.clientId, client);
            } catch (err) {
              console.error(`Failed to load client ${app.clientId}:`, err);
            }
          }
        }
        setClients(clientsMap);
      } catch (err) {
        const apiError = extractApiError(err);
        setError(apiError.message || 'Ошибка загрузки заявок');
      } finally {
        setLoading(false);
      }
    };
    
    loadApplications();
  }, []);

  const filteredApplications = useMemo(() => {
    if (filter === "new") {
      const applicationIdsWithOrders = new Set(
        orders
          .filter(order => order.clientApplicationId !== null && order.clientApplicationId !== undefined)
          .map(order => order.clientApplicationId)
      );
      
      return applications.filter((app) => !applicationIdsWithOrders.has(app.id));
    }
    return applications;
  }, [filter, applications, orders]);

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
            {t("manager.applicationsList")}
          </h1>
        </div>

        <section className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-lg font-semibold">
                {t("manager.applicationsList")}
              </h2>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full text-sm border-separate border-spacing-y-2">
              <thead>
                <tr className="text-xs uppercase text-gray-500">
                  <th className="text-left px-3 pb-2">
                    {t("manager.application")}
                  </th>
                  <th className="text-left px-3 pb-2">{t("manager.client")}</th>
                  <th className="text-left px-3 pb-2">
                    {t("manager.applicationDate")}
                  </th>
                  <th className="text-right px-3 pb-2">
                    {t("profile.orderActions")}
                  </th>
                </tr>
              </thead>
              <tbody>
                {filteredApplications.length === 0 ? (
                  <tr>
                    <td
                      colSpan={4}
                      className="px-3 py-8 text-center text-gray-500"
                    >
                      {t("manager.noApplications")}
                    </td>
                  </tr>
                ) : (
                  filteredApplications.map((application) => (
                    <tr key={application.id}>
                      <td className="px-3 py-3">
                        <div className="text-sm font-medium">
                          № {application.id}
                        </div>
                      </td>
                      <td className="px-3 py-3 text-sm text-gray-300">
                        {clients.get(application.clientId) ? (
                          <>
                            {clients.get(application.clientId)!.person.firstName}{' '}
                            {clients.get(application.clientId)!.person.lastName}
                          </>
                        ) : (
                          <span className="text-gray-500">{t("catalog.loading")}</span>
                        )}
                      </td>
                      <td className="px-3 py-3 text-xs text-gray-300">
                        {new Date(application.createdAt).toLocaleDateString('ru-RU')}
                      </td>
                      <td className="px-3 py-3 text-right">
                        <Link
                          to={`/applications/${application.id}`}
                          className="text-xs rounded-full border border-gray-700 px-3 py-1 hover:bg-gray-800 transition-colors"
                        >
                          {t("manager.openApplication")}
                        </Link>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </div>
  );
}

export default ApplicationsList;

