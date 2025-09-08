% Запрос ищет мать и отца (moskalenko_tatyana).
% Результат вернет имена родителей Татьяны moskalenko_tatyana (Mother и Father) => moskalenko_svetlana; moskalenko_petr.
parents(moskalenko_tatyana, Mother, Father).

% Запрос находит всех братьев и сестер (vasilev_nikita).
% Результат вернет список имен всех братьев и сестер vasilev_nikita, исключая самого vasilev_nikita => vasileva_olesya; vasilev_pavel.
siblings(vasilev_nikita, Sibling).

% Запрос ищет всех полных братьев и сестер (vasileva_olesya).
% Результат вернет список полных братьев и сестер vasileva_olesya => vasilev_pavel.
full_siblings(vasileva_olesya, Sibling).

% Запрос ищет всех братьев и сестер по отцу (vasilev_pavel).
% Результат вернет список всех братьев и сестер, у которых общий отец с vasilev_pavel, но разные матери => vasileva_olesya; vasilev_nikita.
siblings_same_father(vasilev_pavel, Sibling).

% Запрос ищет всех бабушек и дедушек (vasilev_nikita).
% Результат вернет имена бабушек и дедушек Никиты => moskalenko_petr; moskalenko_svetlana; vasilev_nikolay; vasileva_antonina.
grandparent(Grandparent, vasilev_nikita).

% Запрос находит всех потомков (vasilev_nikolay), которые были живы в 2000 году.
% Результат вернет список имен всех потомков vasilev_nikolay, которые родились до или в 2000 году и были живы в этот момент => vasilev_alexey; vasilev_pavel; vasileva_olesya.
descendants(vasilev_nikolay, Descendant, 2000).

% Запрос ищет всех людей, которые были старше 50 лет в 2025 году.
% Результат вернет список людей, которые родились до 1975 года, и которые были живы в 2025 году (исключая тех, кто умер до 2025) => moskalenko_tatyana; vasilev_alexey; bordousova_natalya; vasileva_olga; moskalenko_dmitriy; bordousov_alexey; goreva_lyudmila; goreva_yulia; kovalyov_alexander; kovalyova_tatyana.
older(X, 2025, 50).

% Запрос находит все браки, заключенные в 2000 году.
% Результат вернет список всех браков, заключенных в 2000 году => X= vasilev_pavel; Y= vasileva_viktoria.
was_married_in_year(X, Y, 2006).

% Запрос находит людей, которые не были в браке и которым больше 50 лет.
% Результат вернет список всех людей старше 50 лет, которые никогда не были в браке => goreva_yulia.
never_married_over_50(X).

% Запрос находит людей, которые родились в год свадьбы своих родителей.
% Результат вернет список всех людей, которые родились в тот же год, когда их родители заключили брак => vasileva_olesya.
born_in_same_year_as_parents_marriage(X).

% Запрос находит всех людей, которые были в браке менее 5 лет по состоянию на 2025 год.
% Результат вернет список людей, у которых браки длились менее 5 лет на 2025 год => X= moskalenko_irina; Y= zaytsev_nikita.
short_marriages(X, Y, 2025).
