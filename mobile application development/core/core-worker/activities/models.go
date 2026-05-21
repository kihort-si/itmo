// activities/models.go
package activities

// === Входные параметры workflow ===
type RegistrationInput struct {
	Name              string `json:"name"`
	Username          string `json:"username"`
	Email             string `json:"email"`
	Password          string `json:"password"`
	BaseCurrencyID    int    `json:"base_currency_id"`
	LanguageCode      string `json:"language_code"`
	RegionID          int    `json:"region_id"`
	BasePortfolioName string `json:"base_portfolio_name"`
}

// === Шаг 1: создание клиента ===
type CreateClientRequest struct {
	Username          string `json:"username"`
	RegionRefsIdentifier int  `json:"regionRefsIdentifier"`
	LanguageCode      string `json:"languageCode"`
	FullName          string `json:"fullName"`
	Email             string `json:"email"`
}

type ClientResponse struct {
	ClntId     int    `json:"clntId"`
	Username   string `json:"username"`
	RegionRefsIdentifier int `json:"regionRefsIdentifier"`
	CreatedAt  string `json:"createdAt"`
	LanguageCode string `json:"languageCode"`
	FullName   string `json:"fullName"`
	Email      string `json:"email"`
	PhoneNumber *string `json:"phoneNumber"`
	AdditionalInfo *string `json:"additionalInfo"`
	ProfileExtension *string `json:"profileExtension"`
}

// === Шаг 2: создание аккаунта авторизации ===
type AuthRegisterRequest struct {
	ClntId   int    `json:"clntId"`
	Email    string `json:"email"`
	Username string `json:"username"`
	Password string `json:"password"`
}

type AuthRegisterResponse struct {
	UserId   int      `json:"userId"`
	Email    string   `json:"email"`
	Username string   `json:"username"`
	ClntId   int      `json:"clntId"`
	Status   string   `json:"status"`
	Roles    []string `json:"roles"`
}

// === Шаг 3: создание ЛС (баланс) ===
type CreateBalanceAccountRequest struct {
	ClntId int `json:"clntId"`
	CurrId int `json:"currId"`
}

type BalanceAccountResponse struct {
	AccId    int `json:"accId"`
	ClntId   int `json:"clntId"`
	Currency struct {
		CurrId int    `json:"currId"`
		Code   string `json:"code"`
		RefsId int    `json:"refsId"`
	} `json:"currency"`
	Status  string  `json:"status"`
	Balance float64 `json:"balance"`
}

// === Шаг 4: создание портфеля ===
type CreatePortfolioRequest struct {
	ClntId int    `json:"clntId"`
	Name   string `json:"name"`
}

type PortfolioResponse struct {
	PortId    int    `json:"portId"`
	ClntId    int    `json:"clntId"`
	Name      string `json:"name"`
	CreatedAt string `json:"createdAt"`
	Positions []interface{} `json:"positions"`
	IsClosed  bool   `json:"isClosed"`
}

// === Компенсационные запросы (пустые тела, только ID) ===
type CloseBalanceAccountRequest struct{} // POST /balm/accounts/{accId}/close

type ClosePortfolioRequest struct{} // POST /depository/portfolios/{portId}/close

// === Уведомления RabbitMQ ===
type MailMessage struct {
	Type       string                 `json:"type"`
	ReportType string                 `json:"report_type"`
	Data       map[string]interface{} `json:"data"`
}