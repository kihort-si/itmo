import {useMemo, useState} from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import {useTranslation} from "react-i18next";
import type {TFunction} from "i18next";
import {useSearchParams} from "react-router-dom";
import { authService, extractApiError } from "../../../services/api";

type AuthMode = "login" | "register" | "reset";

const PASSWORD_REGEX = /^[A-Za-z0-9!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]+$/;

const getRegisterSchema = (t: TFunction) => Yup.object({
  firstName: Yup.string().required(t("auth.enterName")),
  lastName: Yup.string().required(t("auth.enterLastName")),
  email: Yup.string()
    .required(t("auth.enterEmail"))
    .email(t("auth.invalidEmail")),
  phone: Yup.string().required(t("auth.enterPhone")),
  password: Yup.string()
    .required(t("auth.inputPassword"))
    .min(8, t("auth.minLength"))
    .matches(PASSWORD_REGEX, t("auth.invalidPassword")),
  confirmPassword: Yup.string()
    .required(t("auth.confirmPassword"))
    .oneOf([Yup.ref("password")], t("auth.passwordsDoNotMatch")),
});

const getResetSchema = (t: TFunction) => Yup.object({
  identifier: Yup.string()
    .required(t("auth.requiredField"))
    .email(t("auth.invalidEmail")),
});

function AuthPage() {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const urlMode = searchParams.get("mode");
  const [mode, setMode] = useState<AuthMode>(
    urlMode === "register" ? "register" : "login"
  );
  // eslint-disable-next-line @typescript-eslint/no-unused-vars,@typescript-eslint/ban-ts-comment
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const isLogin: boolean = mode === "login";
  const isRegister: boolean = mode === "register";
  const isReset: boolean = mode === "reset";

  const registerSchema = useMemo(() => getRegisterSchema(t), [t]);
  const resetSchema = useMemo(() => getResetSchema(t), [t]);

  return (
    <div className="min-h-[calc(100vh-72px-72px)] flex items-center justify-center bg-stone-950 px-4">
      <div className="w-full max-w-md">

        <div className="rounded-3xl border border-gray-800 bg-stone-900/80 shadow-[0_0_40px_rgba(0,0,0,0.5)] backdrop-blur-md p-8">
          <h1 className="text-2xl font-semibold text-white mb-4">
            {isLogin && t("auth.signInAccount")}
            {isRegister && t("auth.createAccount")}
            {isReset && t("auth.resetPassword")}
          </h1>

          <div className="mt-2">
            {isLogin && (
                <div className="space-y-4">
                  <p className="text-sm text-gray-300">
                    {t("auth.signInAccount")}
                  </p>

                  {error && (
                      <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/40 text-red-400 text-sm">
                        {error}
                      </div>
                  )}

                  <button
                      type="button"
                      disabled={loading}
                      onClick={() => {
                        setLoading(true);
                        setError(null);
                        authService.login();
                      }}
                      className="mt-4 w-full rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors disabled:opacity-70"
                  >
                    {loading ? t("auth.loggingIn") : t("auth.signIn")}
                  </button>
                </div>
            )}
            {isRegister && (
              <Formik
                key="register"
                initialValues={{
                  firstName: "",
                  lastName: "",
                  email: "",
                  phone: "",
                  password: "",
                  confirmPassword: "",
                }}
                validationSchema={registerSchema}
                onSubmit={async (values) => {
                  try {
                    setLoading(true);
                    setError(null);
                    await authService.register({
                      username: values.email,
                      email: values.email,
                      password: values.password,
                      firstName: values.firstName,
                      lastName: values.lastName,
                      phoneNumber: values.phone,
                    });
                    setMode("login");
                    setError(null);
                  } catch (err) {
                    const apiError = extractApiError(err);
                    let errorMessage = apiError.message || t("auth.loginError");
                    if (apiError.status === 500) {
                      errorMessage = "Ошибка сервера при регистрации. Пожалуйста, попробуйте позже или обратитесь в поддержку.";
                    } else if (apiError.status === 400) {
                      errorMessage = "Проверьте правильность введенных данных.";
                    }
                    setError(errorMessage);
                  } finally {
                    setLoading(false);
                  }
                }}
              >
                {({ isSubmitting }) => (
                  <Form className="space-y-4">
                    <div className="flex gap-3">
                      <div className="flex-1">
                        <label className="block text-sm text-gray-300 mb-1">
                          {t("auth.name")}
                        </label>
                        <Field
                          name="firstName"
                          type="text"
                          autoComplete="given-name"
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
                      <div className="flex-1">
                        <label className="block text-sm text-gray-300 mb-1">
                          {t("auth.lastName")}
                        </label>
                        <Field
                          name="lastName"
                          type="text"
                          autoComplete="family-name"
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
                    </div>

                    <div>
                      <label className="block text-sm text-gray-300 mb-1">
                        {t("auth.email")}
                      </label>
                      <Field
                        name="email"
                        type="email"
                        autoComplete="email"
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
                      <label className="block text-sm text-gray-300 mb-1">
                        {t("auth.phone")}
                      </label>
                      <Field
                        name="phone"
                        type="tel"
                        autoComplete="tel"
                        className="
                            w-full rounded-xl bg-stone-950/70 border border-gray-700
                            px-4 py-2.5 text-sm text-white placeholder-gray-500
                            focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400
                            transition-colors
                          "
                        placeholder="+7 999 000 00 00"
                      />
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="phone"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-sm text-gray-300 mb-1">
                        {t("auth.password")}
                      </label>
                      <Field
                        name="password"
                        type="password"
                        autoComplete="new-password"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder={t("auth.createPassword")}
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
                      <label className="block text-sm text-gray-300 mb-1">
                        {t("auth.confirmPassword")}
                      </label>
                      <Field
                        name="confirmPassword"
                        type="password"
                        autoComplete="new-password"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder={t("auth.confirmPassword")}
                      />
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="confirmPassword"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>

                    <button
                      type="submit"
                      disabled={isSubmitting || loading}
                      className="mt-4 w-full rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors disabled:opacity-70"
                    >
                      {loading ? t("auth.loggingIn") : t("register")}
                    </button>
                  </Form>
                )}
              </Formik>
            )}

            {isReset && (
              <Formik
                key="reset"
                initialValues={{ identifier: "" }}
                validationSchema={resetSchema}
                onSubmit={(values): void => {
                  console.log("RESET PASSWORD", values);
                }}
              >
                {({ isSubmitting }) => (
                  <Form className="space-y-4">
                    <div>
                      <p className="text-sm text-gray-300 mb-3">
                        {t("auth.resetPasswordInstructions")}
                      </p>

                      <label className="block text-sm text-gray-300 mb-1">
                        E-mail
                      </label>
                      <Field
                        name="identifier"
                        type="email"
                        autoComplete="email"
                        className="w-full rounded-xl bg-stone-950/70 border border-gray-700 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-400 focus:border-emerald-400"
                        placeholder="example@domain.com"
                      />
                      <div className="min-h-[18px]">
                        <ErrorMessage
                          name="identifier"
                          component="div"
                          className="mt-1 text-xs text-red-400"
                        />
                      </div>
                    </div>

                    <button
                      type="submit"
                      disabled={isSubmitting}
                      className="mt-2 w-full rounded-full bg-white text-black text-sm font-medium py-2.5 hover:bg-gray-200 transition-colors disabled:opacity-70"
                    >
                      {t("auth.resetPasswordButton")}
                    </button>

                    <div className="mt-4 flex justify-center">
                      <button
                        type="button"
                        onClick={() => setMode("login")}
                        className="text-xs text-gray-400 hover:text-gray-200 transition-colors"
                      >
                        {t("auth.backToLogin")}
                      </button>
                    </div>
                  </Form>
                )}
              </Formik>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default AuthPage;
