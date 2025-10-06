import re
from prolog_handler import PrologHandler

requests = [
    "Кто родители vasilev_nikita?",
    "Кто братья и сестры moskalenko_tatyana?",
    "Кто бабушки и дедушки vasilev_pavel?",
    "Кто дяди и тети moskalenko_irina?",
    "Какой возраст vasilev_alexey?",
    "Кто в настоящее время женат на moskalenko_tatyana?",
    "Какие люди родились в 2005 году?",
    "Кто вступил в брак в 2004 году?",
    "Кто никогда не был в браке после 50 лет?",
    "Какие браки длятся более 20 лет?",
    "Кто родился до свадьбы родителей?",
    r"Я хочу выйти замуж",
    r"Я хочу жениться",
    r"Кто может усыновить ребенка?",
    r"Кто потенциальные наследники?",
    r"У кого самая большая семья?",
    r"Кто овдовел?",
    r"Кто может быть крестным для ребенка?",
    r"Какие пары могут отметить золотую свадьбу в ближайшие n лет?",
    r"Кто одинокий родитель?",
    r"Кто из пенсионеров еще жив?",
]

patterns = {
    r"Кто родители (.+)\?": "parents",
    r"Кто братья и сестры (.+)\?": "siblings",
    r"Кто полные братья и сестры (.+)\?": "full_siblings",
    r"Кто бабушки и дедушки (.+)\?": "grandparent",
    r"Кто дяди и тети (.+)\?": "aunt_or_uncle",
    r"Кто племянники и племянницы (.+)\?": "niece_or_nephew",
    r"Какой возраст (.+)\?": "age",
    r"Кто в настоящее время женат на (.+)\?": "current_married",
    r"Какие люди родились в (\d+) году\?": "birth_year",
    r"Кто вступил в брак в (\d+) году\?": "married_in_year",
    r"Кто никогда не был в браке после 50 лет\?": "never_married_over_50",
    r"Какие браки длятся более 20 лет\?": "long_marriages",
    r"Кто родился до свадьбы родителей\?": "born_before_parents_married",
    r"Какие разводы произошли менее чем через 5 лет\?": "divorce_less_than_five_years",
    r"У кого большая семья\?": "large_family",
    r"Я хочу выйти замуж": "marriage_candidates_female",
    r"Я хочу жениться": "marriage_candidates_male",
    r"Кто может усыновить ребенка\?": "adoption_candidates",
    r"Кто потенциальные наследники (.+)\?": "potential_heirs",
    r"У кого самая большая семья\?": "largest_family",
    r"Кто овдовел\?": "widowed",
    r"Кто может быть крестным для ребенка\?": "godparent_candidates",
    r"Какие пары могут отметить золотую свадьбу в ближайшие (\d+) лет\?": "golden_wedding_soon",
    r"Кто одинокий родитель\?": "single_parents",
    r"Кто из пенсионеров еще жив\?": "living_pensioners",
}


def parse_input(input_str):
    normalized_input = re.sub(r'\s+', ' ', input_str.strip().lower())

    if not normalized_input.endswith('?'):
        normalized_input += '?'

    for pattern, query_type in patterns.items():
        match = re.match(pattern.lower(), normalized_input, re.IGNORECASE)
        if match:
            return query_type, match.groups()
    return None, None


def main():
    print("\nПримеры запросов, которые вам доступны:\n")
    for req in requests:
        print(f" * {req}")
    print("\nДля выхода из программы введите 'exit'")

    prolog_handler = PrologHandler()

    while True:
        user_input = input("\n>> ")
        if user_input.lower() == 'exit':
            print("Выход из программы.")
            break
        try:
            query_type, params = parse_input(user_input)
            if query_type:
                result = prolog_handler.execute_query(query_type, *params)
                print(f"Результат: {result}")
            else:
                print("Неправильный запрос. Попробуйте снова.")
        except Exception as e:
            print(f"Ошибка при выполнении запроса: {e}")


if __name__ == "__main__":
    main()
