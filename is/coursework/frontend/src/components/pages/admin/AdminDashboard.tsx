import { useTranslation } from "react-i18next";
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { employeesService, authService, extractApiError, type AccountRole } from "../../../services/api";
import type { EmployeeResponseDto, EmployeeRequestDto } from "../../../services/api";
import DeleteIcon from "@mui/icons-material/Delete";
import AddIcon from "@mui/icons-material/Add";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import CancelIcon from "@mui/icons-material/Cancel";

const PASSWORD_REGEX = /^[A-Za-z0-9!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]+$/;

const ROLE_OPTIONS: { value: AccountRole; label: string }[] = [
  { value: 'SALES_MANAGER', label: 'Менеджер по продажам' },
  { value: 'CONSTRUCTOR', label: 'Конструктор' },
  { value: 'CNC_OPERATOR', label: 'Оператор ЧПУ' },
  { value: 'WAREHOUSE_WORKER', label: 'Складской работник' },
  { value: 'SUPPLY_MANAGER', label: 'Менеджер по снабжению' },
];

function AdminDashboard() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [employees, setEmployees] = useState<EmployeeResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [selectedRole, setSelectedRole] = useState<AccountRole | undefined>(undefined);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    let alive = true;

    (async () => {
      try {
        setLoading(true);
        setError(null);

        const me = await authService.getCurrentUser();

        if (!alive) return;

        if (me.role !== 'ADMIN') {
          navigate('/', { replace: true });
          return;
        }

        await loadEmployees();
      } catch (err: any) {
        if (!alive) return;

        if (err?.status === 401) {
          authService.login();
          return;
        }

        const apiError = extractApiError(err);
        setError(apiError.message || 'Ошибка проверки авторизации');
      } finally {
        if (alive) setLoading(false);
      }
    })();

    return () => {
      alive = false;
    };
  }, [selectedRole, navigate]);

  const loadEmployees = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await employeesService.getEmployees({
        page: 0,
        size: 100,
      });
      const filteredEmployees = selectedRole
        ? (response.content || []).filter(emp => emp.role === selectedRole)
        : (response.content || []);
      setEmployees(filteredEmployees);
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка загрузки сотрудников');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateEmployee = async (values: EmployeeRequestDto) => {
    try {
      setCreating(true);
      setError(null);
      await employeesService.createEmployee(values);
      setShowCreateForm(false);
      await loadEmployees();
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка создания сотрудника');
    } finally {
      setCreating(false);
    }
  };

  const handleDeleteEmployee = async (id: number) => {
    if (!window.confirm(t("admin.confirmDelete"))) {
      return;
    }

    try {
      await employeesService.deleteEmployee(id);
      await loadEmployees();
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка удаления сотрудника. Возможно, эндпоинт еще не реализован на бэкенде.');
    }
  };

  const handleEnableAccount = async (accountId: number) => {
    try {
      setError(null);
      await employeesService.enableAccount(accountId);
      await loadEmployees();
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка активации аккаунта');
    }
  };

  const handleDisableAccount = async (accountId: number) => {
    try {
      setError(null);
      await employeesService.disableAccount(accountId);
      await loadEmployees();
    } catch (err) {
      const apiError = extractApiError(err);
      setError(apiError.message || 'Ошибка деактивации аккаунта');
    }
  };

  const validationSchema = Yup.object({
    username: Yup.string()
      .required(t("auth.requiredField"))
      .min(3, t("auth.minLength")),
    password: Yup.string()
      .required(t("auth.inputPassword"))
      .min(8, t("auth.minLength"))
      .matches(PASSWORD_REGEX, t("auth.invalidPassword")),
    firstName: Yup.string().required(t("auth.enterName")),
    lastName: Yup.string().required(t("auth.enterLastName")),
    role: Yup.string().required(t("auth.requiredField")),
  });

  const getRoleLabel = (role: string): string => {
    const option = ROLE_OPTIONS.find(opt => opt.value === role);
    return option?.label || role;
  };

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10 flex items-center justify-center">
        <div className="text-gray-400">{t("catalog.loading")}</div>
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-72px)] bg-stone-950 text-white px-4 py-10 flex justify-center">
      <div className="w-full max-w-7xl space-y-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-semibold tracking-tight">
              {t("admin.dashboard")}
            </h1>
            <p className="mt-2 text-sm text-gray-400">
              {t("admin.dashboardDescription")}
            </p>
          </div>
          <button
            onClick={() => setShowCreateForm(!showCreateForm)}
            className="flex items-center gap-2 px-4 py-2 rounded-full bg-white text-black text-sm font-medium hover:bg-gray-200 transition-colors"
          >
            <AddIcon fontSize="small" />
            {t("admin.createEmployee")}
          </button>
        </div>

        {error && (
          <div className="p-4 rounded-lg bg-red-500/10 border border-red-500/40 text-red-400 text-sm">
            {error}
          </div>
        )}

        {showCreateForm && (
          <section className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
            <h2 className="text-lg font-semibold mb-4">
              {t("admin.createEmployee")}
            </h2>
            <Formik
              initialValues={{
                username: "",
                password: "",
                firstName: "",
                lastName: "",
                role: "SALES_MANAGER" as AccountRole,
              }}
              validationSchema={validationSchema}
              onSubmit={handleCreateEmployee}
            >
              {({ isSubmitting }) => (
                <Form className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs text-gray-400 mb-1">
                        {t("auth.username")}
                      </label>
                      <Field
                        name="username"
                        type="text"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder={t("auth.usernamePlaceholder")}
                      />
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="username"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs text-gray-400 mb-1">
                        {t("auth.password")}
                      </label>
                      <Field
                        name="password"
                        type="password"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder={t("auth.inputPassword")}
                      />
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="password"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>

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
                        {t("admin.role")}
                      </label>
                      <Field
                        name="role"
                        as="select"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                      >
                        {ROLE_OPTIONS.map((option) => (
                          <option key={option.value} value={option.value}>
                            {option.label}
                          </option>
                        ))}
                      </Field>
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="role"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>
                  </div>

                  <div className="flex gap-4 pt-2">
                    <button
                      type="submit"
                      disabled={isSubmitting || creating}
                      className="flex-1 rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors disabled:opacity-70"
                    >
                      {creating ? t("admin.creating") : t("admin.create")}
                    </button>
                    <button
                      type="button"
                      onClick={() => setShowCreateForm(false)}
                      className="flex-1 rounded-full bg-stone-950 text-white border border-gray-700 text-sm font-medium py-2.5 hover:bg-gray-900 transition-colors"
                    >
                      {t("admin.cancel")}
                    </button>
                  </div>
                </Form>
              )}
            </Formik>
          </section>
        )}

        <section className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold">
              {t("admin.employeesList")}
            </h2>
            <div className="flex gap-2">
              <button
                onClick={() => setSelectedRole(undefined)}
                className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${
                  !selectedRole
                    ? "bg-white text-black"
                    : "bg-stone-800 text-gray-300 hover:bg-stone-700"
                }`}
              >
                {t("admin.allRoles")}
              </button>
              {ROLE_OPTIONS.map((option) => (
                <button
                  key={option.value}
                  onClick={() => setSelectedRole(option.value)}
                  className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${
                    selectedRole === option.value
                      ? "bg-white text-black"
                      : "bg-stone-800 text-gray-300 hover:bg-stone-700"
                  }`}
                >
                  {option.label}
                </button>
              ))}
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full text-sm border-separate border-spacing-y-2">
              <thead>
                <tr className="text-xs uppercase text-gray-500">
                  <th className="text-left px-3 pb-2">{t("admin.employee")}</th>
                  <th className="text-left px-3 pb-2">{t("admin.username")}</th>
                  <th className="text-left px-3 pb-2">{t("admin.role")}</th>
                  <th className="text-right px-3 pb-2">{t("profile.orderActions")}</th>
                </tr>
              </thead>
              <tbody>
                {employees.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="px-3 py-8 text-center text-gray-500">
                      {t("admin.noEmployees")}
                    </td>
                  </tr>
                ) : (
                  employees.map((employee) => (
                    <tr key={employee.id}>
                      <td className="px-3 py-3">
                        <div className="text-sm font-medium">
                          {employee.person.firstName} {employee.person.lastName}
                        </div>
                        <div className="text-xs text-gray-500">ID: {employee.id}</div>
                      </td>
                      <td className="px-3 py-3 text-sm text-gray-300">
                        {employee.username || `Account #${employee.accountId}`}
                      </td>
                      <td className="px-3 py-3">
                        <span className="inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ring-1 bg-emerald-500/10 text-emerald-300 ring-emerald-500/40">
                          {getRoleLabel(employee.role)}
                        </span>
                      </td>
                      <td className="px-3 py-3 text-right">
                        <div className="flex items-center justify-end gap-2">
                          {employee.enabled ? (
                            <button
                              onClick={() => handleDisableAccount(employee.accountId)}
                              className="text-amber-400 hover:text-amber-300 transition-colors"
                              title={t("admin.disable")}
                            >
                              <CancelIcon fontSize="small" />
                            </button>
                          ) : (
                            <button
                              onClick={() => handleEnableAccount(employee.accountId)}
                              className="text-emerald-400 hover:text-emerald-300 transition-colors"
                              title={t("admin.enable")}
                            >
                              <CheckCircleIcon fontSize="small" />
                            </button>
                          )}
                          <button
                            onClick={() => handleDeleteEmployee(employee.id)}
                            className="text-red-400 hover:text-red-300 transition-colors"
                            title={t("admin.delete")}
                          >
                            <DeleteIcon fontSize="small" />
                          </button>
                        </div>
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

export default AdminDashboard;

