

export type Lang = 'ru' | 'en';

export type StringSet = {
  tabPortfolio: string;
  tabExchange: string;
  tabProfile: string;
  exchange: string;
  searchPlaceholder: string;
  sortPrice: string;
  sortName: string;
  sortChange: string;
  filterAll: string;
  filterMoex: string;
  filterNasdaq: string;
  filterFavs: string;
  filters: string;
  priceRange: string;
  dayChange: string;
  apply: string;
  reset: string;
  losers: string;
  gainers: string;
  bid: string; ask: string; spread: string;
  open: string; high: string; low: string; prevClose: string; volume: string; marketCap: string;
  buy: string; sell: string;
  overview: string; book: string; deals: string; news: string;
  chartLine: string; chartCandles: string; indicators: string;
  ma: string; ema: string; rsi: string; vol: string;
  period1d: string; period1w: string; period1m: string; period6m: string; period1y: string; periodAll: string;
  tf5m: string; tf30m: string; tf1h: string; tf1d: string; tf1w: string;
  trade: string; market: string; limit: string;
  qty: string; price: string; total: string; available: string; fee: string;
  placeBuy: string; placeSell: string;
  confirmTitle: string;
  confirmBuy: string; confirmSell: string; cancel: string;
  sentTitle: string;
  sentSubBuy: string;
  sentSubSell: string;
  sentDone: string;
  insufficient: string;
  bookSide: string; bookPrice: string; bookSize: string; bookMid: string;
  quickQty: string;
  upperPrice: string; lowerPrice: string;
  estFill: string; estReserve: string;
  portfolio: string;
  totalValue: string;
  cashWallet: string;
  holdings: string; history: string; activeOrders: string;
  today: string; allTime: string; avg: string; pnl: string; pnlOpen: string;
  changeCurrency: string;
  rub: string; usd: string; eur: string;
  historyTitle: string;
  statusFilled: string; statusPartial: string; statusCancelled: string;
  statusRejected: string; statusPending: string;
  typeBuy: string; typeSell: string; typeCancel: string; typeReject: string;
  of: string;
  profile: string;
  signIn: string; signUp: string;
  email: string; password: string; username: string; fullName: string; emailOrUsername: string;
  initialCurrency: string;
  accountCreated: string; welcomeBack: string;
  forgot: string; hasAccount: string; noAccount: string;
  settings: string; language: string; theme: string;
  notifications: string; biometry: string; signOut: string;
  authPromptTitle: string;
  authPromptSub: string;
  back: string;
  favorite: string; unfavorite: string;
  riskWarning: string;
  confirmHold: string;
  holdToConfirm: string;
  add: string;
  sortBy: string;
  none: string;
  blocked: string;
  showMore: string;
  operationsToday: string;
  bestBid: string;
  bestAsk: string;
  hi: string;
  instruments: string;
  sector: string;
  exchangeLabel: string;
  currency: string;
  lot: string;
  action: string;
  instrument: string;
  signInTitle: string;
  signInSub: string;
  signedOutTagline: string;
  signedInTagline: string;
  tradeSounds: string;
  termsFees: string;
  privacy: string;
  support: string;
  memberSince: string;
  orders: string;
  streak: string;
  nothingFound: string;
};

export const STRINGS: Record<Lang, StringSet> = {
  ru: {
    tabPortfolio: 'Портфель',
    tabExchange: 'Биржа',
    tabProfile: 'Профиль',
    exchange: 'Биржа',
    searchPlaceholder: 'Тикер или название',
    sortPrice: 'Цена',
    sortName: 'Алфавит',
    sortChange: 'Изм. за день',
    filterAll: 'Все',
    filterMoex: 'MOEX',
    filterNasdaq: 'NASDAQ',
    filterFavs: 'Избранное',
    filters: 'Фильтры',
    priceRange: 'Диапазон цены',
    dayChange: 'Изменение за день',
    apply: 'Применить',
    reset: 'Сбросить',
    losers: 'Падают',
    gainers: 'Растут',
    bid: 'Bid', ask: 'Ask', spread: 'Спред',
    open: 'Откр.', high: 'Макс.', low: 'Мин.', prevClose: 'Закр.', volume: 'Объём', marketCap: 'Капитал.',
    buy: 'Купить', sell: 'Продать',
    overview: 'Обзор', book: 'Стакан', deals: 'Сделки', news: 'Новости',
    chartLine: 'Линия', chartCandles: 'Свечи', indicators: 'Индикаторы',
    ma: 'MA', ema: 'EMA', rsi: 'RSI', vol: 'Объём',
    period1d: '1Д', period1w: '1Н', period1m: '1М', period6m: '6М', period1y: '1Г', periodAll: 'Всё',
    tf5m: '5м', tf30m: '30м', tf1h: '1ч', tf1d: '1д', tf1w: '1н',
    trade: 'Заявка', market: 'По рынку', limit: 'Лимит',
    qty: 'Кол-во', price: 'Цена', total: 'Сумма', available: 'Доступно', fee: 'Комиссия',
    placeBuy: 'Купить', placeSell: 'Продать',
    confirmTitle: 'Подтвердите заявку',
    confirmBuy: 'Подтвердить покупку', confirmSell: 'Подтвердить продажу', cancel: 'Отмена',
    sentTitle: 'Заявка выставлена',
    sentSubBuy: 'Заявка на покупку отправлена на биржу',
    sentSubSell: 'Заявка на продажу отправлена на биржу',
    sentDone: 'Готово',
    insufficient: 'Недостаточно средств',
    bookSide: 'Стакан', bookPrice: 'Цена', bookSize: 'Объём', bookMid: 'Средняя',
    quickQty: 'Быстрый ввод',
    upperPrice: 'Верх. граница',
    lowerPrice: 'Ниж. граница',
    estFill: 'Будет исполнено',
    estReserve: 'Резерв на счёте',
    portfolio: 'Портфель',
    totalValue: 'Стоимость портфеля',
    cashWallet: 'Электронный кошелёк',
    holdings: 'Активы', history: 'История', activeOrders: 'Активные заявки',
    today: 'сегодня', allTime: 'за всё время', avg: 'Средняя', pnl: 'Доход', pnlOpen: 'Откр. позиция',
    changeCurrency: 'Сменить валюту',
    rub: '₽', usd: '$', eur: '€',
    historyTitle: 'История операций',
    statusFilled: 'Исполнено', statusPartial: 'Частично', statusCancelled: 'Отменено',
    statusRejected: 'Отклонено', statusPending: 'В работе',
    typeBuy: 'Покупка', typeSell: 'Продажа', typeCancel: 'Отзыв заявки', typeReject: 'Отклонено',
    of: 'из',
    profile: 'Профиль',
    signIn: 'Войти', signUp: 'Регистрация',
    email: 'E-mail', password: 'Пароль', username: 'Имя пользователя', fullName: 'Полное ФИО', emailOrUsername: 'Email или username',
    initialCurrency: 'Валюта счёта',
    accountCreated: 'Аккаунт создан', welcomeBack: 'С возвращением',
    forgot: 'Забыли пароль?', hasAccount: 'Уже есть аккаунт?', noAccount: 'Нет аккаунта?',
    settings: 'Настройки', language: 'Язык', theme: 'Тема',
    notifications: 'Уведомления', biometry: 'Вход по биометрии', signOut: 'Выйти',
    authPromptTitle: 'Войдите, чтобы торговать',
    authPromptSub: 'Покупка и продажа доступны только авторизованным пользователям',
    back: '',
    favorite: 'В избранное', unfavorite: 'Удалить',
    riskWarning: 'Симуляция. Не является инвестиционной рекомендацией.',
    confirmHold: 'Удерживайте, чтобы подтвердить',
    holdToConfirm: 'Удерживайте',
    add: 'Пополнить',
    sortBy: 'Сортировка',
    none: 'Нет',
    blocked: 'Заблокировано',
    showMore: 'Показать ещё',
    operationsToday: 'операций сегодня',
    bestBid: 'Лучшая на покупку',
    bestAsk: 'Лучшая на продажу',
    hi: 'Здравствуйте',
    instruments: 'инструментов',
    sector: 'Сектор',
    exchangeLabel: 'Биржа',
    currency: 'Валюта',
    lot: 'Лот',
    action: 'Действие',
    instrument: 'Инструмент',
    signInTitle: 'Войдите в аккаунт',
    signInSub: 'Доступ к портфелю, сделкам и истории',
    signedOutTagline: 'Создайте симулированный счёт за минуту.',
    signedInTagline: 'С возвращением. Рынки уже открыты.',
    tradeSounds: 'Звуки сделок',
    termsFees: 'Условия и тарифы',
    privacy: 'Конфиденциальность',
    support: 'Поддержка',
    memberSince: 'Открыт',
    orders: 'Заявок',
    streak: 'Серия',
    nothingFound: 'Ничего не найдено',
  },
  en: {
    tabPortfolio: 'Portfolio',
    tabExchange: 'Markets',
    tabProfile: 'Profile',
    exchange: 'Markets',
    searchPlaceholder: 'Symbol or name',
    sortPrice: 'Price',
    sortName: 'Alphabet',
    sortChange: 'Day change',
    filterAll: 'All',
    filterMoex: 'MOEX',
    filterNasdaq: 'NASDAQ',
    filterFavs: 'Watchlist',
    filters: 'Filters',
    priceRange: 'Price range',
    dayChange: 'Day change',
    apply: 'Apply',
    reset: 'Reset',
    losers: 'Losers',
    gainers: 'Gainers',
    bid: 'Bid', ask: 'Ask', spread: 'Spread',
    open: 'Open', high: 'High', low: 'Low', prevClose: 'Prev', volume: 'Volume', marketCap: 'Mkt cap',
    buy: 'Buy', sell: 'Sell',
    overview: 'Overview', book: 'Book', deals: 'Deals', news: 'News',
    chartLine: 'Line', chartCandles: 'Candles', indicators: 'Studies',
    ma: 'MA', ema: 'EMA', rsi: 'RSI', vol: 'Vol',
    period1d: '1D', period1w: '1W', period1m: '1M', period6m: '6M', period1y: '1Y', periodAll: 'All',
    tf5m: '5m', tf30m: '30m', tf1h: '1h', tf1d: '1d', tf1w: '1w',
    trade: 'Order', market: 'Market', limit: 'Limit',
    qty: 'Qty', price: 'Price', total: 'Total', available: 'Available', fee: 'Fee',
    placeBuy: 'Buy', placeSell: 'Sell',
    confirmTitle: 'Confirm order',
    confirmBuy: 'Confirm buy', confirmSell: 'Confirm sell', cancel: 'Cancel',
    sentTitle: 'Order placed',
    sentSubBuy: 'Buy order routed to exchange',
    sentSubSell: 'Sell order routed to exchange',
    sentDone: 'Done',
    insufficient: 'Insufficient funds',
    bookSide: 'Order book', bookPrice: 'Price', bookSize: 'Size', bookMid: 'Mid',
    quickQty: 'Quick',
    upperPrice: 'Max price', lowerPrice: 'Min price',
    estFill: 'Estimated fill', estReserve: 'Reserve hold',
    portfolio: 'Portfolio',
    totalValue: 'Portfolio value',
    cashWallet: 'Cash wallet',
    holdings: 'Holdings', history: 'History', activeOrders: 'Active orders',
    today: 'today', allTime: 'all time', avg: 'Avg', pnl: 'P&L', pnlOpen: 'Open P&L',
    changeCurrency: 'Currency',
    rub: '₽', usd: '$', eur: '€',
    historyTitle: 'Order history',
    statusFilled: 'Filled', statusPartial: 'Partial', statusCancelled: 'Cancelled',
    statusRejected: 'Rejected', statusPending: 'Pending',
    typeBuy: 'Buy', typeSell: 'Sell', typeCancel: 'Cancelled', typeReject: 'Rejected',
    of: 'of',
    profile: 'Profile',
    signIn: 'Sign in', signUp: 'Sign up',
    email: 'Email', password: 'Password', username: 'Username', fullName: 'Full name', emailOrUsername: 'Email or username',
    initialCurrency: 'Account currency',
    accountCreated: 'Account created', welcomeBack: 'Welcome back',
    forgot: 'Forgot password?', hasAccount: 'Have an account?', noAccount: 'No account?',
    settings: 'Settings', language: 'Language', theme: 'Theme',
    notifications: 'Notifications', biometry: 'Biometric sign-in', signOut: 'Sign out',
    authPromptTitle: 'Sign in to trade',
    authPromptSub: 'Trading is available to authenticated users only',
    back: '',
    favorite: 'Watch', unfavorite: 'Unwatch',
    riskWarning: 'Simulator. Not investment advice.',
    confirmHold: 'Hold to confirm',
    holdToConfirm: 'Hold',
    add: 'Top up',
    sortBy: 'Sort',
    none: 'None',
    blocked: 'Blocked',
    showMore: 'Show more',
    operationsToday: 'orders today',
    bestBid: 'Best bid',
    bestAsk: 'Best ask',
    hi: 'Hello',
    instruments: 'instruments',
    sector: 'Sector',
    exchangeLabel: 'Exchange',
    currency: 'Currency',
    lot: 'Lot',
    action: 'Action',
    instrument: 'Instrument',
    signInTitle: 'Sign in to your account',
    signInSub: 'Access portfolio, trades and history',
    signedOutTagline: 'Create a simulated account in a minute.',
    signedInTagline: 'Welcome back. Markets are open.',
    tradeSounds: 'Trade sounds',
    termsFees: 'Terms & fees',
    privacy: 'Privacy',
    support: 'Support',
    memberSince: 'Member since',
    orders: 'Orders',
    streak: 'Streak',
    nothingFound: 'Nothing found',
  },
};
