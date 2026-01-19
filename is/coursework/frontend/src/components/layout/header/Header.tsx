import MainLogo from "../../../assets/main-logo.svg";
import {useTranslation} from "react-i18next";
import {useState, useEffect} from "react";
import {Link, useLocation} from "react-router-dom";
import IconButton from "@mui/material/IconButton";
import Menu from "@mui/material/Menu";
import MenuItem from "@mui/material/MenuItem";
import ListItemIcon from "@mui/material/ListItemIcon";
import ListItemText from "@mui/material/ListItemText";
import HomeIcon from "@mui/icons-material/Home";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import { authService, type CurrentUserDto, LOGIN_URL } from "../../../services/api";
import { useUserRole } from "../../../hooks/useUserRole.ts";

type LangCode = "ru" | "en";

interface LanguageOption {
  code: LangCode;
  label: string;
  short: "RU" | "EN";
  flag: string;
}

const LANGUAGES: LanguageOption[] = [
  { code: "ru", label: "Русский", short: "RU", flag: "🇷🇺" },
  { code: "en", label: "English", short: "EN", flag: "🇬🇧" },
];

function Header() {
  const { t, i18n } = useTranslation()

  const handleChangeLanguage = (code: LangCode) => {
    i18n.changeLanguage(code);
    handleClose();
  };

  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [staffMenuAnchor, setStaffMenuAnchor] = useState<null | HTMLElement>(null);

  const open = Boolean(anchorEl);
  const staffMenuOpen = Boolean(staffMenuAnchor);

  const currentLang =
    LANGUAGES.find((l) => l.code === i18n.language) ?? LANGUAGES[0];

  const handleButtonClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleStaffMenuClick = (event: React.MouseEvent<HTMLElement>) => {
    setStaffMenuAnchor(event.currentTarget);
  };

  const handleStaffMenuClose = () => {
    setStaffMenuAnchor(null);
  };

  const handleLogout = () => {
    handleStaffMenuClose();
    authService.logout();
  };

  const otherLanguages = LANGUAGES.filter((l) => l.code !== currentLang.code);

  const location = useLocation();
  const [currentUser, setCurrentUser] = useState<CurrentUserDto | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const { isStaff } = useUserRole();

  const isAuthPage = location.pathname === "/auth";

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const authenticated = await authService.isAuthenticated();
        setIsAuthenticated(authenticated);
        if (authenticated && isStaff) {
          const user = await authService.getCurrentUser();
          setCurrentUser(user);
        }
      } catch {
        setIsAuthenticated(false);
      }
    };
    checkAuth();
  }, [isStaff, location.pathname]);

  if (isStaff) {
    return (
      <header className="header bg-gray-700 px-24 py-4 sticky top-0 z-50 flex row justify-between items-center">
        <div className="header-logo">
          {isAuthenticated ? (
              <Link to="/">
                <img src={MainLogo} alt={"logo"} />
              </Link>
            ) : (
              <Link to="/auth">
                <img src={MainLogo} alt={"logo"} />
              </Link>
            )
          }
        </div>
        <div className="header-menu flex items-center gap-4">
          {isAuthenticated ? (
            <div className="relative inline-flex">
              <button
                onClick={handleStaffMenuClick}
                className="flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium
                  border border-gray-500 text-white
                  hover:bg-gray-600 transition-colors"
              >
                <span className="text-white">
                  {currentUser?.role === 'ADMIN'
                    ? "admin"
                    : `${currentUser?.client?.person?.firstName || currentUser?.employee?.person?.firstName || ""} ${(currentUser?.employee?.person?.lastName?.charAt(0) || currentUser?.client?.person?.lastName?.charAt(0) || "") + "."}`
                  }
                </span>
                <ArrowDropDownIcon className="text-white" />
              </button>

            <Menu
              anchorEl={staffMenuAnchor}
              open={staffMenuOpen}
              onClose={handleStaffMenuClose}
              transformOrigin={{ horizontal: "right", vertical: "top" }}
              anchorOrigin={{ horizontal: "right", vertical: "bottom" }}
              MenuListProps={{
                className:
                  "py-1 bg-white dark:bg-gray-950 border border-gray-200 dark:border-gray-700 shadow-lg",
              }}
            >
              <MenuItem
                onClick={handleLogout}
                className="
                  flex items-center
                  hover:bg-gray-100 dark:hover:bg-gray-800
                  px-3
                "
              >
                <ListItemText
                  primary={
                    <span className="text-sm font-medium text-red-700 dark:text-red-500">{t("header.logout")}</span>
                  }
                />
              </MenuItem>
            </Menu>
          </div>
          ) : (
            <a
              href={`${LOGIN_URL}?returnTo=${encodeURIComponent(window.location.href)}`}
              className="px-4 py-2 rounded-full text-sm font-medium border border-gray-500 text-white hover:bg-gray-600 transition-colors"
            >
              {t("auth.signIn")}
            </a>
          )}
        </div>
      </header>
    );
  }

  return (
    <header className="header bg-stone-950 px-24 py-4 sticky top-0 z-50 flex row justify-between items-center">
      {isAuthenticated ? (
        <Link to="/">
          <img src={MainLogo} alt={"logo"} />
        </Link>
      ) : (
        <Link to="/auth">
          <img src={MainLogo} alt={"logo"} />
        </Link>
      )
      }
      <div className="header-menu flex items-center gap-4">
        {!isAuthPage && !isAuthenticated && (
          <>
            <div>
              <a
                href={`${LOGIN_URL}?returnTo=${encodeURIComponent(window.location.href)}`}
                className="px-4 py-2 rounded-full text-sm font-medium
              border border-gray-500 text-white
              hover:bg-gray-800 transition-colors"
              >
                {t("login")}
              </a>
            </div>

            <div>
              <Link
                  to="/auth?mode=register"
                  className="px-4 py-2 rounded-full text-sm font-medium
                  bg-white text-black
                  hover:bg-gray-200 transition-colors"
                  >
                {t("register")}
              </Link>
            </div>
          </>
        )}

        <div className="relative flex items-center justify-center group">
          <Link to="/" className="flex items-center justify-center">
            <div
              className="transform transition-transform duration-200
              group-hover:-translate-y-1 group-hover:scale-90"
            >
              <IconButton size="small" className="!p-1">
                <HomeIcon className="text-white" />
              </IconButton>
            </div>
          </Link>

          <span
            className="pointer-events-none
              absolute left-1/2 top-full -translate-x-1/2
              text-xs text-gray-200
              opacity-0 translate-y-1 transform
              transition-all duration-200
              group-hover:opacity-100 group-hover:translate-y-0"
          >
            {t("header.home")}
          </span>
        </div>

        {isAuthenticated && (
          <div className="relative flex items-center justify-center group">
            <Link to="/profile" className="flex items-center justify-center">
              <div
                className="transform transition-transform duration-200
                group-hover:-translate-y-1 group-hover:scale-90"
              >
                <IconButton size="small" className="!p-1">
                  <AccountCircleIcon className="text-white" />
                </IconButton>
              </div>
            </Link>

            <span
              className="pointer-events-none
                absolute left-1/2 top-full -translate-x-1/2
                text-xs text-gray-200
                opacity-0 translate-y-1 transform
                transition-all duration-200
                group-hover:opacity-100 group-hover:translate-y-0"
            >
              {t("header.profile")}
            </span>
          </div>
        )}

        <div className="relative inline-flex">
          <IconButton
            onClick={handleButtonClick}
            size="small"
            className="
          border border-gray-300 dark:border-gray-600
          rounded-full px-3 py-1
          hover:bg-gray-100 dark:hover:bg-gray-800
          transition-colors
        "
          >
            <span className="mr-2 text-lg">{currentLang.flag}</span>
            <span className="text-sm font-medium text-white">{currentLang.short}</span>
          </IconButton>

          <Menu
            anchorEl={anchorEl}
            open={open}
            onClose={handleClose}
            transformOrigin={{ horizontal: "right", vertical: "top" }}
            anchorOrigin={{ horizontal: "right", vertical: "bottom" }}
            MenuListProps={{
              className:
                "py-1 bg-white dark:bg-gray-950 border border-gray-200 dark:border-gray-700 shadow-lg",
            }}
          >
            {otherLanguages.map((lang) => (
              <MenuItem
                key={lang.code}
                onClick={() => handleChangeLanguage(lang.code)}
                className="
              flex items-center
              hover:bg-gray-100 dark:hover:bg-gray-800
              px-3
            "
              >
                <ListItemIcon className="min-w-0 mr-3">
                  <span className="text-xl">{lang.flag}</span>
                </ListItemIcon>
                <ListItemText
                  primary={
                    <span className="text-sm font-medium text-white">{lang.short}</span>
                  }
                  secondary={
                    <span className="text-xs text-gray-500 dark:text-gray-400">
                  {lang.label}
                </span>
                  }
                />
              </MenuItem>
            ))}
          </Menu>
        </div>
      </div>
    </header>
  )
}

export default Header;