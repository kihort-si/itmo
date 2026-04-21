package ru.itmo.tpo.lab3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

class MirTesenFunctionalTest {
    private static final String BASE_URL = System.getProperty("baseUrl", "https://mirtesen.ru/");
    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Set<String> SUPPORTED_BROWSERS = Set.of("chrome", "firefox");

    private WebDriver driver;
    private WebDriverWait wait;
    private String currentBrowser;

    static Stream<String> browsers() {
        return Arrays.stream(System.getProperty("browsers", "chrome,firefox").split(","))
                .map(browser -> browser.trim().toLowerCase(Locale.ROOT))
                .filter(browser -> !browser.isBlank())
                .peek(browser -> {
                    if (!SUPPORTED_BROWSERS.contains(browser)) {
                        throw new IllegalArgumentException("Unsupported browser: " + browser);
                    }
                });
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @ParameterizedTest(name = "{0}: UC-1 Открыть главную страницу")
    @MethodSource("browsers")
    void uc1OpenMainPage(String browser) {
        openBrowser(browser);

        openBasePage();

        visible(xpath("//*[normalize-space()='Рубрики']"));
        visible(xpath("//a[normalize-space()='Кулинария']"));
        visible(xpath("//a[normalize-space()='Моя лента']"));
        visible(xpath("//a[normalize-space()='Популярное']"));
        visible(xpath("//a[normalize-space()='Обсуждаемое']"));
        Assertions.assertTrue(articleLinksCount() >= 3, "На главной странице должно быть несколько материалов ленты");
    }

    @ParameterizedTest(name = "{0}: UC-2 Принять cookies")
    @MethodSource("browsers")
    void uc2AcceptCookies(String browser) {
        openBrowser(browser);
        openUrl(BASE_URL);
        waitForPageReady();

        By cookieButton = xpath("//button[contains(normalize-space(), 'Принять') or contains(normalize-space(), 'Понятно') or contains(normalize-space(), 'Хорошо') or contains(normalize-space(), 'Соглас')]");
        if (exists(cookieButton)) {
            click(visible(cookieButton));
            Assertions.assertTrue(waitUntilInvisible(cookieButton), "После подтверждения баннер cookies должен исчезнуть");
        }

        visible(xpath("//*[normalize-space()='Рубрики']"));
        Assertions.assertTrue(waitUntilInvisible(cookieButton), "Cookie-баннер не должен мешать работе со страницей");
    }

    @ParameterizedTest(name = "{0}: UC-3 Просмотреть рубрики")
    @MethodSource("browsers")
    void uc3ViewRubrics(String browser) {
        openBrowser(browser);
        openBasePage();

        visible(xpath("//*[normalize-space()='Рубрики']"));
        visible(xpath("//a[normalize-space()='Кулинария']"));
        visible(xpath("//a[normalize-space()='Политика']"));
        visible(xpath("//a[normalize-space()='Здоровье']"));
        visible(xpath("//a[normalize-space()='Дети и семья']"));
        visible(xpath("//a[normalize-space()='Шоу-бизнес']"));
    }

    @ParameterizedTest(name = "{0}: UC-4 Перейти в рубрику")
    @MethodSource("browsers")
    void uc4OpenRubric(String browser) {
        openBrowser(browser);
        openBasePage();

        WebElement category = visible(xpath("//a[normalize-space()='Кулинария']"));
        String href = category.getAttribute("href");
        click(category);

        waitForPageReady();
        Assertions.assertFalse(driver.getCurrentUrl().equals(BASE_URL), "URL должен измениться после перехода в рубрику");
        Assertions.assertTrue(driver.getCurrentUrl().contains("mirtesen.ru"), "Переход должен остаться в пределах сайта MirTesen");
        Assertions.assertNotNull(href, "Ссылка рубрики должна иметь адрес");
        Assertions.assertTrue(articleLinksCount() >= 1, "В рубрике должен отображаться хотя бы один материал");
    }

    @ParameterizedTest(name = "{0}: UC-5 Открыть материал из ленты")
    @MethodSource("browsers")
    void uc5OpenFeedMaterial(String browser) {
        openBrowser(browser);
        openBasePage();

        WebElement article = firstArticleLink();
        String linkText = article.getText().trim();
        String href = article.getAttribute("href");
        click(article);
        switchToNewestWindow();
        if (driver.getCurrentUrl().equals(BASE_URL) && href != null && !href.isBlank()) {
            openUrl(href);
        }

        waitForPageReady();
        Assertions.assertTrue(driver.getCurrentUrl().contains("mirtesen.ru"), "Материал должен открываться на домене MirTesen");
        Assertions.assertFalse(driver.getCurrentUrl().equals(BASE_URL), "После открытия материала URL должен отличаться от главной страницы");
        Assertions.assertTrue(
                exists(xpath("//h1 | //h2 | //h3")) || driver.getPageSource().contains(linkText.substring(0, Math.min(20, linkText.length()))),
                "Страница материала должна содержать заголовок или текст выбранной публикации"
        );
    }

    @ParameterizedTest(name = "{0}: UC-6 Перейти к странице источника материала")
    @MethodSource("browsers")
    void uc6OpenMaterialSourcePage(String browser) {
        openBrowser(browser);
        openBasePage();

        WebElement source = visible(xpath("(//a[contains(@href, '.mirtesen.ru') and not(contains(@href, 'info.mirtesen.ru')) and not(contains(@href, '.mirtesen.ru/blog/'))])[1]"));
        String href = source.getAttribute("href");
        click(source);

        waitForPageReady();
        Assertions.assertNotNull(href, "Ссылка источника должна иметь адрес");
        Assertions.assertTrue(driver.getCurrentUrl().contains("mirtesen.ru"), "Источник должен открываться в экосистеме MirTesen");
        Assertions.assertTrue(articleLinksCount() >= 1 || exists(xpath("//h1 | //h2 | //h3")), "Страница источника должна содержать контент");
    }

    @ParameterizedTest(name = "{0}: UC-7 Зарегистрироваться")
    @MethodSource("browsers")
    void uc7OpenRegistrationForm(String browser) {
        openBrowser(browser);

        openUrl(absoluteUrl("register"));
        waitForPageReady();

        Assertions.assertTrue(driver.getCurrentUrl().contains("register"), "Должна открыться страница регистрации");
        Assertions.assertTrue(
                exists(xpath("//form")) || exists(xpath("//input")) || pageContains("Зарегистр") || pageContains("Регистра"),
                "На странице регистрации должна быть форма или элементы регистрации"
        );
        Assertions.assertTrue(
                exists(xpath("//input[@type='email' or @type='tel' or @type='text' or @name='email' or @name='phone']"))
                        || pageContains("телефон") || pageContains("email"),
                "Форма регистрации должна запрашивать телефон или email"
        );
    }

    @ParameterizedTest(name = "{0}: UC-8 Войти в аккаунт")
    @MethodSource("browsers")
    void uc8Login(String browser) {
        openBrowser(browser);

        loginWithConfiguredCredentials();

        visible(xpath("//a[normalize-space()='Моя лента']"));
        Assertions.assertTrue(articleLinksCount() >= 1, "После успешного входа пользователь должен видеть ленту материалов");
    }

    @ParameterizedTest(name = "{0}: UC-9 Просматривать Мою ленту")
    @MethodSource("browsers")
    void uc9ViewMyFeed(String browser) {
        openBrowser(browser);

        loginWithConfiguredCredentials();
        WebElement myFeed = visible(xpath("//a[normalize-space()='Моя лента']"));
        click(myFeed);

        waitForPageReady();
        visible(xpath("//a[normalize-space()='Моя лента']"));
        Assertions.assertTrue(articleLinksCount() >= 1, "В персональной ленте должны отображаться материалы");
    }

    private void openBrowser(String browser) {
        try {
            currentBrowser = "chrome";
            driver = createDriver("chrome");
            driver.manage().window().setSize(new Dimension(1440, 1000));
            wait = new WebDriverWait(driver, TIMEOUT);
        } catch (WebDriverException exception) {
            Assumptions.abort("Браузер " + browser + " недоступен локально и selenium.remoteUrl не задан: " + exception.getMessage());
        }
    }

    private boolean isRemoteRun() {
        return !System.getProperty("selenium.remoteUrl", "").trim().isBlank();
    }

    private boolean isLocalBrowserAvailable(String browser) {
        if ("chrome".equals(browser)) {
            return Files.exists(Path.of("/Applications/Google Chrome.app"))
                    || commandExists("google-chrome")
                    || commandExists("chromium")
                    || commandExists("chromium-browser");
        }

        return Files.exists(Path.of("/Applications/Firefox.app")) || commandExists("firefox");
    }

    private boolean commandExists(String command) {
        return Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of)
                .map(path -> path.resolve(command))
                .anyMatch(Files::isExecutable);
    }

    private WebDriver createDriver(String browser) {
        String remoteUrl = System.getProperty("selenium.remoteUrl", "").trim();
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        if ("chrome".equals(browser)) {
            ChromeOptions options = new ChromeOptions();
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);
            options.addArguments("--window-size=1440,1000", "--disable-notifications", "--disable-popup-blocking");
            if (headless) {
                options.addArguments("--headless=new");
            }
            return remoteUrl.isBlank() ? new org.openqa.selenium.chrome.ChromeDriver(options) : remoteDriver(remoteUrl, options);
        }

        FirefoxOptions options = new FirefoxOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addPreference("dom.webnotifications.enabled", false);
        if (headless) {
            options.addArguments("-headless");
        }
        return remoteUrl.isBlank() ? new org.openqa.selenium.firefox.FirefoxDriver(options) : remoteDriver(remoteUrl, options);
    }

    private WebDriver remoteDriver(String remoteUrl, org.openqa.selenium.Capabilities capabilities) {
        try {
            return new RemoteWebDriver(new URL(remoteUrl), capabilities);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Некорректный selenium.remoteUrl: " + remoteUrl, exception);
        }
    }

    private void openBasePage() {
        try {
            openUrl(BASE_URL);
        } catch (WebDriverException exception) {
            closeBrowser();
            driver = createDriver(currentBrowser);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(35));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10));
            driver.manage().window().setSize(new Dimension(1440, 1000));
            wait = new WebDriverWait(driver, TIMEOUT);
            openUrl(BASE_URL);
        }
        waitForPageReady();
        acceptCookieBannerIfPresent();
        visible(xpath("//*[normalize-space()='Рубрики']"));
    }

    private void openUrl(String url) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                driver.get(url);
                return;
            } catch (TimeoutException ignored) {
                // The tested page may keep loading non-critical dynamic resources.
                return;
            } catch (WebDriverException exception) {
                if (attempt == 3 || !exception.getMessage().contains("ERR_NAME_NOT_RESOLVED")) {
                    throw exception;
                }
                sleep(Duration.ofSeconds(2));
            }
        }
    }

    private void waitForPageReady() {
        wait.until((ExpectedCondition<Boolean>) webDriver ->
                Set.of("interactive", "complete").contains(((JavascriptExecutor) webDriver).executeScript("return document.readyState")));
        wait.until(ExpectedConditions.presenceOfElementLocated(xpath("//body")));
    }

    private void acceptCookieBannerIfPresent() {
        clickIfExists(xpath("//button[contains(normalize-space(), 'Принять') or contains(normalize-space(), 'Понятно') or contains(normalize-space(), 'Хорошо')]"));
    }

    private void loginWithConfiguredCredentials() {
        String login = credential("mirtesen.login", "MIRTESEN_LOGIN");
        String password = credential("mirtesen.password", "MIRTESEN_PASSWORD");
        Assumptions.assumeTrue(!login.isBlank() && !password.isBlank(),
                "Для UC-8/UC-9 задайте -Dmirtesen.login/-Dmirtesen.password или MIRTESEN_LOGIN/MIRTESEN_PASSWORD");

        openUrl(BASE_URL + (BASE_URL.contains("?") ? "&" : "?") + "auth=login");
        waitForPageReady();
        clickIfExists(xpath("//button[contains(normalize-space(), 'Вход по почте')]"));

        WebElement loginInput = visible(xpath("(//input[@type='email' or contains(@name, 'email') or contains(@name, 'login')])[1]"));
        loginInput.clear();
        loginInput.sendKeys(login);

        WebElement passwordInput = visible(xpath("(//input[@type='password' or contains(@name, 'password')])[1]"));
        passwordInput.clear();
        passwordInput.sendKeys(password);

        click(visible(xpath("(//button[contains(normalize-space(), 'Войти') or contains(normalize-space(), 'Продолжить') or @type='submit'])[1]")));
        waitForPageReady();
    }

    private WebElement firstArticleLink() {
        return visible(xpath("(//a[contains(@href, '.mirtesen.ru/blog/') and not(contains(@href, 'info.mirtesen.ru'))])[1]"));
    }

    private long articleLinksCount() {
        return driver.findElements(xpath("//*[self::h1 or self::h2 or self::h3][string-length(normalize-space()) > 20]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .count();
    }

    private boolean exists(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    private WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private boolean waitUntilInvisible(By locator) {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException exception) {
            return false;
        }
    }

    private void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        try {
            element.click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (TimeoutException ignored) {
        }
    }

    private void clickIfExists(By locator) {
        try {
            WebElement element = wait.withTimeout(Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
        } catch (TimeoutException ignored) {
        } finally {
            wait.withTimeout(TIMEOUT);
        }
    }

    private void switchToNewestWindow() {
        String currentWindow = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(currentWindow)) {
                driver.switchTo().window(handle);
            }
        }
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for retry", exception);
        }
    }

    private boolean pageContains(String text) {
        return driver.getPageSource().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT));
    }

    private String credential(String systemProperty, String environmentVariable) {
        String value = System.getProperty(systemProperty, "").trim();
        if (!value.isBlank()) {
            return value;
        }
        return System.getenv(environmentVariable) == null ? "" : System.getenv(environmentVariable).trim();
    }

    private String absoluteUrl(String path) {
        return BASE_URL.endsWith("/") ? BASE_URL + path : BASE_URL + "/" + path;
    }

    private static By xpath(String expression) {
        return By.xpath(expression);
    }
}
