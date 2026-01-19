import { Link, useNavigate } from "react-router-dom";
import {useTranslation} from "react-i18next";
import { useState, useEffect } from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { ordersService, authService, applicationsService, extractApiError, type CurrentUserDto } from "../../../services/api";
import type { ClientOrderResponseDto, ClientApplicationResponseDto } from "../../../services/api/types";
import { getOrderStatusTranslationKey, getOrderStatusStyle } from "../../../utils/orderStatus";

const PASSWORD_REGEX = /^[A-Za-z0-9!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]+$/;

function ProfilePage() {
  const {t} = useTranslation();
  const navigate = useNavigate();
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [orders, setOrders] = useState<ClientOrderResponseDto[]>([]);
  const [applications, setApplications] = useState<ClientApplicationResponseDto[]>([]);
  const [currentUser, setCurrentUser] = useState<CurrentUserDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [profileSuccess, setProfileSuccess] = useState(false);
  const [passwordSuccess, setPasswordSuccess] = useState(false);

  useEffect(() => {
    let alive = true;

    const loadData = async () => {
      try {
        setLoading(true);
        setError(null);

        const userData = await authService.getCurrentUser();

        if (!alive) return;

        const [ordersData, applicationsData] = await Promise.all([
          ordersService.getOrders(),
          applicationsService.getApplications({ page: 0, size: 1000 })
        ]);

        if (!alive) return;

        setOrders(Array.isArray(ordersData) ? ordersData : []);
        setApplications(applicationsData.content || []);
        setCurrentUser(userData);
      } catch (err: any) {
        if (!alive) return;

        if (err?.status === 401) {
          authService.login();
          return;
        }

        const apiError = extractApiError(err);
        setError(apiError.message || "Ошибка загрузки данных");
      } finally {
        if (alive) setLoading(false);
      }
    };

    loadData();

    return () => {
      alive = false;
    };
  }, [navigate]);

  const formatOrderName = (order: ClientOrderResponseDto): string => {
    const date = new Date(order.createdAt);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `ORD-${year}-${month}-${day}-${order.id}`;
  };

  const getCompletedDate = (order: ClientOrderResponseDto): string | null => {
    return order.completedAt || null;
  };

  const currentOrders = Array.isArray(orders) ? orders.filter(
    (order) => order.status !== "COMPLETED"
  ) : [];

  const applicationsWithoutOrders = applications.filter(
    (app) => !orders.some((order) => order.clientApplicationId === app.id)
  );

  const allCurrentItems = [
    ...currentOrders.map(order => ({ type: 'order' as const, data: order })),
    ...applicationsWithoutOrders.map(app => ({ type: 'application' as const, data: app }))
  ];

  const historyOrders = Array.isArray(orders) ? orders.filter(
    (order) => order.status === "COMPLETED"
  ) : [];

  const profileInitialValues = {
    firstName: currentUser?.client?.person?.firstName || currentUser?.employee?.person?.firstName || "",
    lastName: currentUser?.client?.person?.lastName || currentUser?.employee?.person?.lastName || "",
    email: currentUser?.client?.email || "",
    phoneNumber: currentUser?.client?.phoneNumber || "",
  };

  const profileValidationSchema = Yup.object({
    firstName: Yup.string().required(t("auth.enterName")),
    lastName: Yup.string().required(t("auth.enterLastName")),
    email: Yup.string()
      .required(t("auth.enterEmail"))
      .email(t("auth.invalidEmail")),
    phoneNumber: Yup.string().required(t("auth.enterPhone")),
  });

  const passwordValidationSchema = Yup.object({
    currentPassword: Yup.string().required(t("profile.currentPassword")),
    newPassword: Yup.string()
      .required(t("auth.inputPassword"))
      .min(8, t("auth.minLength"))
      .matches(PASSWORD_REGEX, t("auth.invalidPassword")),
    confirmNewPassword: Yup.string()
      .required(t("auth.confirmPassword"))
      .oneOf([Yup.ref("newPassword")], t("auth.passwordsDoNotMatch")),
  });

  const handleUpdateContacts = async (values: typeof profileInitialValues) => {
    try {
      setProfileError(null);
      setProfileSuccess(false);
      await authService.updateProfile(values);
      setProfileSuccess(true);
      const userData = await authService.getCurrentUser();
      setCurrentUser(userData);
      setTimeout(() => setProfileSuccess(false), 3000);
    } catch (err) {
      const apiError = extractApiError(err);
      setProfileError(apiError.message || t("auth.loginError"));
    }
  };

  const handleChangePassword = async (values: {
    currentPassword: string;
    newPassword: string;
    confirmNewPassword: string;
  }) => {
    try {
      setPasswordError(null);
      setPasswordSuccess(false);
      await authService.changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });
      setPasswordSuccess(true);
      setTimeout(() => setPasswordSuccess(false), 3000);
    } catch (err) {
      const apiError = extractApiError(err);
      setPasswordError(apiError.message || t("auth.loginError"));
    }
  };

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-72px-72px)] bg-stone-950 text-white px-4 py-10 flex items-center justify-center">
        <div className="text-gray-400">{t("catalog.loading")}</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-[calc(100vh-72px-72px)] bg-stone-950 text-white px-4 py-10 flex items-center justify-center">
        <div className="text-red-400">{error}</div>
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-72px-72px)] bg-stone-950 text-white px-4 py-10 flex justify-center">
      <div className="w-full max-w-6xl space-y-8">
        <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-semibold tracking-tight">
              {t("profile.title")}
            </h1>
            <p className="mt-2 text-sm text-gray-400 max-w-xl">
              {t("profile.profileSubtitle")}
            </p>
          </div>
          <div className="rounded-2xl border border-gray-800 bg-stone-900/80 px-4 py-3 text-sm text-gray-300">
            <div className="flex items-center justify-between gap-4">
              <div className="text-right">
                <div className="text-xs uppercase tracking-wide text-gray-500">
                  {t("profile.completedOrders")}
                </div>
                <div className="font-medium">
                  {historyOrders.filter((o) => o.status === "COMPLETED").length}
                </div>
              </div>
              <button
                onClick={() => {
                  authService.logout();
                }}
                className="rounded-full bg-red-500/10 text-red-400 border border-red-500/40 text-sm font-medium px-4 py-2 hover:bg-red-500/20 transition-colors"
              >
                {t("header.logout")}
              </button>
            </div>
          </div>
        </div>

        <div className="flex flex-col lg:flex-row gap-8 transition-all duration-500 ease-in-out">
          <div className="space-y-6 transition-all duration-500 ease-in-out lg:flex-1 lg:min-w-0">
            <div className={`transition-all duration-500 ease-in-out overflow-hidden ${
              isSettingsOpen
                ? 'opacity-0 max-h-0 scale-y-0'
                : 'opacity-100 scale-y-100'
            }`}>
              {/*<button*/}
              {/*  onClick={() => setIsSettingsOpen(true)}*/}
              {/*  className="w-full rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors"*/}
              {/*>*/}
              {/*  {t("profile.openProfileSettings")}*/}
              {/*</button>*/}
            </div>
            {allCurrentItems.length > 0 && (
              <section className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h2 className="text-lg font-semibold">
                      {t("profile.activeOrders")}
                    </h2>
                    <p className="text-xs text-gray-400 mt-1">
                      {t("profile.activeOrdersInfo")}
                    </p>
                  </div>
                </div>

                <div className="overflow-x-auto">
                  <table className="min-w-full text-sm border-separate border-spacing-y-2">
                    <thead>
                    <tr className="text-xs uppercase text-gray-500">
                      <th className="text-left px-3 pb-2">{t("profile.order")}</th>
                      <th className="text-left px-3 pb-2">{t("profile.applicationDate")}</th>
                      <th className="text-left px-3 pb-2">{t("profile.orderStatus")}</th>
                      <th className="text-right px-3 pb-2">{t("profile.orderActions")}</th>
                    </tr>
                    </thead>
                    <tbody>
                    {allCurrentItems.length === 0 ? (
                      <tr>
                        <td colSpan={4} className="px-3 py-8 text-center text-gray-500">
                          {t("manager.noOrders")}
                        </td>
                      </tr>
                    ) : (
                      allCurrentItems.map((item) => {
                        if (item.type === 'order') {
                          const order = item.data;
                          return (
                            <tr key={`order-${order.id}`}>
                              <td className="px-3 py-3">
                                <div className="text-sm font-medium">{formatOrderName(order)}</div>
                                <div className="text-xs text-gray-500">ID: {order.id}</div>
                              </td>
                              <td className="px-3 py-3 text-xs text-gray-300">
                                {new Date(order.createdAt).toLocaleDateString('ru-RU')}
                              </td>
                              <td className="px-3 py-3">
                              <span className={[
                                "inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ring-1",
                                getOrderStatusStyle(order.status),
                              ].join(" ")}>
                                {t(getOrderStatusTranslationKey(order.status))}
                              </span>
                              </td>
                              <td className="px-3 py-3 text-right">
                                <Link
                                  to={`/orders/${order.id}`}
                                  className="text-xs rounded-full border border-gray-700 px-3 py-1 hover:bg-gray-800 transition-colors"
                                >
                                  {t("profile.openOrder")}
                                </Link>
                              </td>
                            </tr>
                          );
                        } else {
                          const app = item.data;
                          return (
                            <tr key={`app-${app.id}`}>
                              <td className="px-3 py-3">
                                <div className="text-sm font-medium">{t("profile.request")} #{app.id}</div>
                                <div className="text-xs text-gray-500">ID: {app.id}</div>
                              </td>
                              <td className="px-3 py-3 text-xs text-gray-300">
                                {new Date(app.createdAt).toLocaleDateString('ru-RU')}
                              </td>
                              <td className="px-3 py-3">
                              <span className="inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ring-1 bg-blue-500/10 text-blue-400 ring-blue-500/20">
                                {t("profile.applicationCreated") || "Заявка создана"}
                              </span>
                              </td>
                              <td className="px-3 py-3 text-right">
                                <Link
                                  to={`/applications/${app.id}`}
                                  className="text-xs rounded-full border border-gray-700 px-3 py-1 hover:bg-gray-800 transition-colors"
                                >
                                  {t("profile.openApplication") || "Открыть заявку"}
                                </Link>
                              </td>
                            </tr>
                          );
                        }
                      })
                    )}
                    </tbody>
                  </table>
                </div>
              </section>
            )}

            <section className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h2 className="text-lg font-semibold">
                    {t("profile.orderHistory")}
                  </h2>
                  <p className="text-xs text-gray-400 mt-1">
                    {t("profile.orderHistoryInfo")}
                  </p>
                </div>
              </div>

              <div className="overflow-x-auto">
                <table className="min-w-full text-sm border-separate border-spacing-y-2">
                  <thead>
                  <tr className="text-xs uppercase text-gray-500">
                    <th className="text-left px-3 pb-2">{t("profile.order")}</th>
                    <th className="text-left px-3 pb-2">
                      {t("profile.orderDate")}
                    </th>
                    <th className="text-left px-3 pb-2">
                      {t("profile.orderEndDate")}
                    </th>
                    <th className="text-left px-3 pb-2">{t("profile.orderStatus")}</th>
                    <th className="text-right px-3 pb-2">{t("profile.orderActions")}</th>
                  </tr>
                  </thead>
                  <tbody>
                  {historyOrders.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-3 py-8 text-center text-gray-500">
                        {t("manager.noOrders")}
                      </td>
                    </tr>
                  ) : (
                    historyOrders.map((order) => {
                      const completedDate = getCompletedDate(order);
                      return (
                        <tr key={order.id}>
                          <td className="px-3 py-1">
                            <div className="text-sm font-medium">
                              {formatOrderName(order)}
                            </div>
                            <div className="text-xs text-gray-500">
                              ID: {order.id}
                            </div>
                          </td>
                          <td className="px-3 py-1 text-xs text-gray-300">
                            {new Date(order.createdAt).toLocaleDateString('ru-RU')}
                          </td>
                          <td className="px-3 py-1 text-xs text-gray-300">
                            {completedDate ? new Date(completedDate).toLocaleDateString('ru-RU') : '—'}
                          </td>
                          <td className="px-3 py-1">
                              <span
                                className={[
                                  "inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ring-1",
                                  getOrderStatusStyle(order.status),
                                ].join(" ")}
                              >
                                {t(getOrderStatusTranslationKey(order.status))}
                              </span>
                          </td>
                          <td className="px-3 py-1 text-right">
                            <Link
                              to={`/orders/${order.id}`}
                              className="text-xs rounded-full border border-gray-700 px-3 py-1 hover:bg-gray-800 transition-colors"
                            >
                              {t("profile.open")}
                            </Link>
                          </td>
                        </tr>
                      );
                    })
                  )}
                  </tbody>
                </table>
              </div>
            </section>
          </div>

          <aside className={`space-y-6 transition-all duration-500 ease-in-out origin-top ${
            isSettingsOpen 
              ? 'opacity-100 max-h-[5000px] scale-y-100 overflow-visible lg:w-[33.333%] lg:min-w-[300px]' 
              : 'opacity-0 max-h-0 scale-y-0 overflow-hidden lg:w-0 lg:min-w-0'
          }`}>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold">
                {t("profile.profileAndContacts")}
              </h2>
              <div className="flex gap-2">
                <button
                  onClick={() => setIsSettingsOpen(false)}
                  className="rounded-full bg-stone-950 text-white border border-gray-700 text-sm font-medium px-4 py-2 hover:bg-gray-900 transition-colors"
                >
                  {t("profile.closeProfileSettings")}
                </button>
              </div>
            </div>
            <section className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
              <p className="text-xs text-gray-400 mb-4">
                {t("profile.profileAndContactsInfo")}
              </p>

              {profileError && (
                <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/40 text-red-400 text-sm">
                  {profileError}
                </div>
              )}
              {profileSuccess && (
                <div className="mb-4 p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/40 text-emerald-400 text-sm">
                  {t("profile.saveChanges")} {t("profile.success")}
                </div>
              )}
              <Formik
                initialValues={profileInitialValues}
                validationSchema={profileValidationSchema}
                enableReinitialize
                onSubmit={handleUpdateContacts}
              >
                {({ isSubmitting }) => (
                  <Form className="space-y-4">
                    <div>
                      <label className="block text-xs text-gray-400 mb-1">
                        {t("auth.name")}
                      </label>
                      <Field
                        name="firstName"
                        type="text"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder={t("auth.ivan")}
                      />
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="firstName"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs text-gray-400 mb-1">
                        {t("auth.lastName")}
                      </label>
                      <Field
                        name="lastName"
                        type="text"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder={t("auth.petrov")}
                      />
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="lastName"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs text-gray-400 mb-1">
                        {t("auth.email")}
                      </label>
                      <Field
                        name="email"
                        type="email"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder="example@domain.com"
                      />
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="email"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs text-gray-400 mb-1">
                        {t("auth.phone")}
                      </label>
                      <div className="relative">
                        <Field
                          name="phoneNumber"
                          type="tel"
                          className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                          placeholder="+7 999 000 00 00"
                        />
                      </div>
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="phoneNumber"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>

                    <button
                      type="submit"
                      disabled={isSubmitting}
                      className="w-full rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors disabled:opacity-70"
                    >
                      {t("profile.saveChanges")}
                    </button>
                  </Form>
                )}
              </Formik>
            </section>

            <section className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
              <h2 className="text-lg font-semibold mb-1">
                {t("profile.changePassword")}
              </h2>
              <p className="text-xs text-gray-400 mb-4">
                {t("profile.changePasswordRecommendation")}
              </p>

              {passwordError && (
                <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/40 text-red-400 text-sm">
                  {passwordError}
                </div>
              )}
              {passwordSuccess && (
                <div className="mb-4 p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/40 text-emerald-400 text-sm">
                  {t("profile.updatePassword")} {t("profile.success")}
                </div>
              )}
              <Formik
                initialValues={{
                  currentPassword: "",
                  newPassword: "",
                  confirmNewPassword: "",
                }}
                validationSchema={passwordValidationSchema}
                onSubmit={handleChangePassword}
              >
                {({ isSubmitting }) => (
                  <Form className="space-y-4">
                    <div>
                      <label className="block text-xs text-gray-400 mb-1">
                        {t("profile.currentPassword")}
                      </label>
                      <Field
                        name="currentPassword"
                        type="password"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder={t("profile.currentPasswordPlaceholder")}
                      />
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="currentPassword"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs text-gray-400 mb-1">
                        {t("profile.newPassword")}
                      </label>
                      <Field
                        name="newPassword"
                        type="password"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder={t("profile.newPasswordPlaceholder")}
                      />
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="newPassword"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs text-gray-400 mb-1">
                        {t("profile.confirmNewPassword")}
                      </label>
                      <Field
                        name="confirmNewPassword"
                        type="password"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder={t("profile.confirmNewPasswordPlaceholder")}
                      />
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="confirmNewPassword"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>

                    <button
                      type="submit"
                      disabled={isSubmitting}
                      className="w-full rounded-full bg-stone-950 text-white border border-gray-700 text-sm font-medium py-2.5 hover:bg-gray-900 transition-colors disabled:opacity-70"
                    >
                      {t("profile.updatePassword")}
                    </button>
                  </Form>
                )}
              </Formik>
            </section>
          </aside>
        </div>
      </div>
    </div>
  );
}

export default ProfilePage;
