% члены семьи
birth(2005, vasilev_nikita).
birth(1969, moskalenko_tatyana).
birth(1959, vasilev_alexey).
birth(1941, moskalenko_petr).
birth(1938, moskalenko_svetlana).
birth(1930, vasilev_nikolay).
birth(1931, vasileva_antonina).
birth(1983, vasilev_pavel).
birth(1979, vasileva_olesya).
birth(1959, vasileva_olga).
birth(1962, moskalenko_dmitriy).
birth(1994, moskalenko_irina).
birth(2008, vasileva_elizaveta).
birth(2008, vasileva_evgeniya).
birth(1982, vasileva_viktoria).
birth(1976, kuznetsov_kirill).
birth(1968, bordousova_natalya).
birth(1965, bordousov_alexey).
birth(1988, bordousov_alexander).
birth(1918, gorev_leonid).
birth(1914, goreva_antonina).
birth(1946, gorev_nikolay).
birth(1946, goreva_lyudmila).
birth(1972, goreva_yulia).
birth(2005, goreva_valeria).
birth(1951, goreva_lyubov).
birth(1972, kovalyov_alexander).
birth(1973, kovalyova_tatyana).
birth(1993, kovalyova_ksenia).
birth(1998, zaytsev_nikita).

% заключенные браки
marriage(moskalenko_tatyana, vasilev_alexey, 2004).
marriage(vasilev_alexey, vasileva_olga, 1979).
marriage(moskalenko_petr, moskalenko_svetlana, 1961).
marriage(vasilev_nikolay, vasileva_antonina, 1950).
marriage(vasilev_pavel, vasileva_viktoria, 2006).
marriage(moskalenko_dmitriy, bordousova_natalya, 2009).
marriage(moskalenko_irina, zaytsev_nikita, 2025).
marriage(bordousova_natalya, bordousov_alexey, 1987).
marriage(gorev_leonid, goreva_antonina, 1936).
marriage(gorev_nikolay, goreva_lyudmila, 1968).
marriage(gorev_nikolay, goreva_lyubov, 1976).
marriage(kovalyov_alexander, kovalyova_tatyana, 1990).

% смерти
death(2015, moskalenko_petr).
death(2012, vasilev_nikolay).
death(2023, vasileva_antonina).
death(1987, gorev_leonid).
death(1995, goreva_antonina).
death(2023, gorev_nikolay).

% расторгнутые браки
divorce(vasilev_alexey, vasileva_olga, 1997).
divorce(bordousova_natalya, bordousov_alexey, 1992).
divorce(gorev_nikolay, goreva_lyudmila, 1972).

% Пол
male(vasilev_nikita).
male(vasilev_alexey).
male(moskalenko_petr).
male(vasilev_nikolay).
male(vasilev_pavel).
male(moskalenko_dmitriy).
male(kuznetsov_kirill).
male(bordousov_alexey).
male(bordousov_alexander).
male(gorev_leonid).
male(gorev_nikolay).
male(kovalyov_alexander).
male(zaytsev_nikita).

female(moskalenko_tatyana).
female(moskalenko_svetlana).
female(vasileva_antonina).
female(vasileva_olesya).
female(vasileva_olga).
female(moskalenko_irina).
female(vasileva_elizaveta).
female(vasileva_evgeniya).
female(vasileva_viktoria).
female(bordousova_natalya).
female(goreva_antonina).
female(goreva_lyudmila).
female(goreva_yulia).
female(goreva_valeria).
female(goreva_lyubov).
female(kovalyova_tatyana).
female(kovalyova_ksenia).

% родители и дети
parent_of(moskalenko_tatyana, vasilev_nikita).
parent_of(vasilev_alexey, vasilev_nikita).
parent_of(moskalenko_petr, moskalenko_tatyana).
parent_of(moskalenko_svetlana, moskalenko_tatyana).
parent_of(vasilev_nikolay, vasilev_alexey).
parent_of(vasileva_antonina, vasilev_alexey).
parent_of(gorev_leonid, moskalenko_svetlana).
parent_of(goreva_antonina, moskalenko_svetlana).
parent_of(vasilev_alexey, vasilev_pavel).
parent_of(vasileva_olga, vasilev_pavel).
parent_of(vasilev_alexey, vasileva_olesya).
parent_of(vasileva_olga, vasileva_olesya).
parent_of(moskalenko_petr, moskalenko_dmitriy).
parent_of(moskalenko_svetlana, moskalenko_dmitriy).
parent_of(moskalenko_dmitriy, moskalenko_irina).
parent_of(bordousova_natalya, moskalenko_irina).
parent_of(vasilev_pavel, vasileva_elizaveta).
parent_of(vasileva_viktoria, vasileva_elizaveta).
parent_of(vasileva_olesya, vasileva_evgeniya).
parent_of(kuznetsov_kirill, vasileva_evgeniya).
parent_of(bordousov_alexey, bordousov_alexander).
parent_of(bordousova_natalya, bordousov_alexander).
parent_of(gorev_leonid, gorev_leonid).
parent_of(goreva_antonina, gorev_nikolay).
parent_of(gorev_nikolay, goreva_yulia).
parent_of(goreva_lyudmila, goreva_yulia).
parent_of(goreva_yulia, goreva_valeria).
parent_of(goreva_lyubov, kovalyov_alexander).
parent_of(kovalyov_alexander, kovalyova_ksenia).
parent_of(kovalyova_tatyana, kovalyova_ksenia).

% текущий год
current_year(2025).

% ================================ ПРАВИЛА ================================

% правило для вывода родителей
parents(X, Mother, Father) :-
    parent_of(Mother, X), female(Mother),
    parent_of(Father, X), male(Father).

% правило для поиска братьев и сестер
siblings(X, Y) :-
    parent_of(P, X), parent_of(P, Y), X \= Y.

% правило для поиска братьев и сестер с одинаковыми родителями
full_siblings(X, Y) :-
    parent_of(Mother, X), parent_of(Mother, Y), female(Mother),
    parent_of(Father, X), parent_of(Father, Y), male(Father),
    X \= Y.

% правило для нахождения братьев и сестер с общей матерью
siblings_same_mother(X, Y) :-
    parent_of(Mother, X), parent_of(Mother, Y), female(Mother),
    parent_of(Father1, X), parent_of(Father2, Y), male(Father1), male(Father2),
    Father1 \= Father2, 
    X \= Y.

% правило для нахождения братьев и сестер с общим отцом
siblings_same_father(X, Y) :-
    parent_of(Father, X), parent_of(Father, Y), male(Father),
    parent_of(Mother1, X), parent_of(Mother2, Y), female(Mother1), female(Mother2),
    Mother1 \= Mother2,
    X \= Y.

% правило для нахождения бабушек и дедушек
grandparent(X, Y) :-
    parent_of(X, Z), parent_of(Z, Y).

% правило для нахождения дядей и тетей
aunt_or_uncle(X, Y) :-
    setof(Z, (parent_of(Parent, Y), siblings(Parent, Z), Z \= Y), Siblings),
    member(X, Siblings).

% правило для нахождения племянников и племянниц
niece_or_nephew(X, Y) :-
    setof(Sibling, (siblings(Sibling, Y), parent_of(Sibling, X)), Siblings),
    member(Sibling, Siblings),
    X \= Y.

% правило для нахождения всех текущих браков
current_married(X, Y) :-
    marriage(X, Y, _),
    \+ divorce(X, Y, _),
    \+ death(_, X),
    \+ death(_, Y),
    X \= Y.

% правило для нахождения продолжительности брака между супругами
marriage_duration(X, Y, Duration) :-
    (marriage(X, Y, Year); marriage(Y, X, Year)),
    current_year(CurrentYear),
    \+ divorce(X, Y, _),
    \+ divorce(Y, X, _),
    Duration is CurrentYear - Year.

% правило для нахождения возраста человека
age(X, Age) :-
    birth(YearOfBirth, X),
    death(YearOfDeath, X),
    Age is YearOfDeath - YearOfBirth.

age(X, Age) :-
    birth(YearOfBirth, X),
    current_year(CurrentYear),
    \+ death(_, X),
    Age is CurrentYear - YearOfBirth.

% правило для нахождения людей старше определенного возраста на определенный год
older(X, Year, Age) :-
    birth(YearOfBirth, X),
    \+ death(_, X),
    AgeAtMoment is Year - YearOfBirth,
    AgeAtMoment > Age.

% правило для нахождения всех браков, заключенных в определенном году
was_married_in_year(X, Y, Year) :-
    marriage(X, Y, MarriageYear),
    Year =:= MarriageYear.

% правило для нахождения браков короче 5 лет на определенный год
short_marriages(X, Y, Year) :-
    marriage(X, Y, MarriageYear),
    \+ divorce(X, Y, _),
    Duration is Year - MarriageYear,
    Duration < 5.

% правило для нахождения детей, рожденных после развода родителей
born_after_divorce(X) :-
    parent_of(Mother, X),
    parent_of(Father, X),
    divorce(Mother, Father, DivorceYear),
    birth(BirthYear, X),
    BirthYear > DivorceYear.

% правило для нахождения людей старше 50 лет, которые никогда не были в браке
never_married_over_50(X) :-
    age(X, Age),
    Age > 50,
    \+ marriage(X, _, _),
    \+ marriage(_, X, _).

% правило для нахождения людей с одинаковым годом рождения
same_birth_year(X, Y) :-
    birth(Year, X),
    birth(Year, Y),
    X \= Y.

% правило для нахождения нахождения больших семей (более 3-х детей)
large_family(X) :-
    setof(Child, parent_of(X, Child), Children),
    length(Children, N),
    N > 3.

% правило для нахождения долгих браков (длительностью более 20 лет) на определенный год
long_marriages(X, Y, Year) :-
    marriage(X, Y, MarriageYear),
    \+ divorce(X, Y, _),
    \+ death(_, X),
    \+ death(_, Y),
    Duration is Year - MarriageYear,
    Duration > 20.

% правило для нахождения пар, где супруги родились в один и тот же год
same_birth_year_as_spouse(X, Y) :-
    marriage(X, Y, _),
    birth(YearX, X),
    birth(YearY, Y),
    YearX = YearY.

% правило для нахождения людей, рожденных в год свадьбы родителей
born_in_same_year_as_parents_marriage(X) :-
    birth(YearOfBirth, X),
    parent_of(Mother, X),
    parent_of(Father, X),
    marriage(Mother, Father, MarriageYear),
    YearOfBirth =:= MarriageYear.

% правило для нахождения разводов, состоявшихся менее чем через 5 лет после брака
divorce_less_than_five_years(X, Y) :-
    marriage(X, Y, MarriageYear),
    divorce(X, Y, DivorceYear),
    Duration is DivorceYear - MarriageYear,
    Duration < 5.

% правило для нахождения людей, рожденных до свадьбы родителей
born_before_parents_married(X) :-
    birth(YearOfBirth, X),
    parent_of(Mother, X),
    parent_of(Father, X),
    marriage(Mother, Father, MarriageYear),
    YearOfBirth < MarriageYear.

% правило для нахождения людей, вступивших в брак после 40 лет
married_after_40(X) :-
    marriage(X, Y, MarriageYear),
    birth(YearOfBirth, X),
    current_year(CurrentYear),
    AgeAtMarriage is MarriageYear - YearOfBirth,
    AgeAtMarriage > 40.

% правило для группировки людей по возрасту
age_by_group(MinAge, MaxAge, Year, Person) :-
    birth(YearOfBirth, Person),
    Age is Year - YearOfBirth,
    Age >= MinAge, Age =< MaxAge.

% правило для нахождения всех живых потомков человека, родившихся до определённого года
descendants(X, Descendant, Year) :-
    parent_of(X, Descendant),
    birth(BirthYear, Descendant),
    BirthYear =< Year,
    is_alive(Descendant, Year).

descendants(X, Descendant, Year) :-
    parent_of(X, Child),
    descendants(Child, Descendant, Year).

% правило для проверки, жив ли человек в определённый год
is_alive(Person, Year) :-
    \+ death(Person, DeathYear),
    !.

is_alive(Person, Year) :-
    death(Person, DeathYear),
    DeathYear >= Year.
