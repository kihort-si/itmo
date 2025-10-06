import logging
import sys
import io
from swiplserver import PrologMQI, create_posix_path


class PrologHandler:
    def __init__(self, knowledge_base_path="./knowledge/family_tree.pl"):
        self.knowledge_base_path = knowledge_base_path
        logging.getLogger('swiplserver').setLevel(logging.CRITICAL)

    def execute_query(self, query_type, *args):
        original_stderr = sys.stderr
        sys.stderr = io.StringIO()

        try:
            with PrologMQI() as mqi:
                with mqi.create_thread() as prolog:
                    prolog.query("set_prolog_flag(verbose, silent)")
                    prolog.query("set_prolog_flag(debug, off)")
                    prolog.query("set_prolog_flag(unknown, warning)")
                    prolog.query("set_prolog_flag(singleton_warning, off)")
                    prolog.query("set_prolog_flag(discontiguous_warnings, off)")

                    path = create_posix_path(self.knowledge_base_path)
                    prolog.query(f'consult("{path}")')

                    return self._process_query(prolog, query_type, *args)
        finally:
            sys.stderr = original_stderr

    def _process_query(self, prolog, query_type, *args):
        handlers = {
            "parents": self._get_parents,
            "siblings": self._get_siblings,
            "grandparent": self._get_grandparents,
            "age": self._get_age,
            "current_married": self._get_current_married,
            "birth_year": self._get_birth_year,
            "married_in_year": self._get_married_in_year,
            "never_married_over_50": self._get_never_married_over_50,
            "long_marriages": self._get_long_marriages,
            "born_before_parents_married": self._get_born_before_parents_married,
            "divorce_less_than_five_years": self._get_divorce_less_than_five_years,
            "large_family": self._get_large_family,
            "marriage_candidates_female": self._get_marriage_candidates_female,
            "marriage_candidates_male": self._get_marriage_candidates_male,
            "adoption_candidates": self._get_adoption_candidates,
            "potential_heirs": self._get_potential_heirs,
            "largest_family": self._get_largest_family,
            "widowed": self._get_widowed,
            "godparent_candidates": self._get_godparent_candidates,
            "golden_wedding_soon": self._get_golden_wedding_soon,
            "single_parents": self._get_single_parents,
            "living_pensioners": self._get_living_pensioners,
        }

        handler = handlers.get(query_type)
        if handler:
            return handler(prolog, *args)
        return "Неизвестный тип запроса"

    def _get_parents(self, prolog, person):
        result = prolog.query(f"parents({person}, Mother, Father)")
        if result:
            r = list(result[0])
            return f"Родители {person}: мать - {r['Mother']}, отец - {r['Father']}"
        return f"Родители для {person} не найдены"

    def _get_siblings(self, prolog, person):
        result = prolog.query(f"siblings({person}, Sibling)")
        if result:
            siblings = list([r['Sibling'] for r in result])
            return f"Братья и сестры {person}: {', '.join(siblings)}"
        return f"Братья и сестры для {person} не найдены"

    def _get_grandparents(self, prolog, person):
        result = prolog.query(f"grandparent(Grandparent, {person})")
        if result:
            grandparents = list([r['Grandparent'] for r in result])
            return f"Бабушки и дедушки {person}: {', '.join(grandparents)}"
        return f"Бабушки и дедушки для {person} не найдены"

    def _get_age(self, prolog, person):
        result = prolog.query(f"age({person}, Age)")
        if result:
            age = list(result[0]['Age'])
            return f"Возраст {person}: {age} лет"
        return f"Возраст для {person} не найден"

    def _get_current_married(self, prolog, person):
        result = prolog.query(f"current_married({person}, Spouse)")
        if result:
            spouses = list([r['Spouse'] for r in result])
            return f"В настоящее время {person} женат/замужем за: {', '.join(spouses)}"
        return f"{person} в настоящее время не состоит в браке"

    def _get_birth_year(self, prolog, year):
        result = prolog.query(f"birth({year}, Person)")
        if result:
            people = list([r['Person'] for r in result])
            return f"Люди, родившиеся в {year} году: {', '.join(people)}"
        return f"Никто не родился в {year} году"

    def _get_married_in_year(self, prolog, year):
        result = prolog.query(f"was_married_in_year(X, Y, {year})")
        if result:
            couples = list([f"{r['X']} и {r['Y']}" for r in result])
            return f"В {year} году поженились: {', '.join(couples)}"
        return f"В {year} году никто не женился"

    def _get_never_married_over_50(self, prolog):
        result = prolog.query("never_married_over_50(Person)")
        if result:
            people = list([r['Person'] for r in result])
            return f"Люди старше 50 лет, никогда не состоявшие в браке: {', '.join(people)}"
        return "Таких людей не найдено"

    def _get_long_marriages(self, prolog):
        result = prolog.query("long_marriages(X, Y, 2025)")
        if result:
            couples = list([f"{r['X']} и {r['Y']}" for r in result])
            return f"Браки более 20 лет: {', '.join(couples)}"
        return "Браков длительностью более 20 лет не найдено"

    def _get_born_before_parents_married(self, prolog):
        result = prolog.query("born_before_parents_married(Person)")
        if result:
            people = list([r['Person'] for r in result])
            return f"Люди, родившиеся до свадьбы родителей: {', '.join(people)}"
        return "Таких людей не найдено"

    def _get_divorce_less_than_five_years(self, prolog):
        result = prolog.query("divorce_less_than_five_years(X, Y)")
        if result:
            couples = list([f"{r['X']} и {r['Y']}" for r in result])
            return f"Разводы менее чем через 5 лет: {', '.join(couples)}"
        return "Таких разводов не найдено"

    def _get_large_family(self, prolog):
        result = prolog.query("large_family(Person)")
        if result:
            people = list([r['Person'] for r in result])
            return f"Люди с большими семьями (более 3 детей): {', '.join(people)}"
        return "Людей с большими семьями не найдено"

    def _get_marriage_candidates_female(self, prolog):
        result = prolog.query("marriage_candidate_male(Person)")
        if result:
            candidates = list([r['Person'] for r in result])
            return f"Подходящие кандидаты для брака: {', '.join(candidates)}"
        return "Подходящих кандидатов не найдено"

    def _get_marriage_candidates_male(self, prolog):
        result = prolog.query("marriage_candidate_female(Person)")
        if result:
            candidates = list([r['Person'] for r in result])
            return f"Подходящие кандидаты для брака: {', '.join(candidates)}"
        return "Подходящих кандидатов не найдено"

    def _get_adoption_candidates(self, prolog):
        result = prolog.query("adoption_candidate(X, Y)")
        if result:
            couples = list([f"{r['X']} и {r['Y']}" for r in result])
            return f"Пары, подходящие для усыновления: {', '.join(couples)}"
        return "Подходящих пар не найдено"

    def _get_potential_heirs(self, prolog, person):
        result = prolog.query(f"potential_heir({person}, Heir)")
        if result:
            heirs = list([r['Heir'] for r in result])
            return f"Потенциальные наследники {person}: {', '.join(heirs)}"
        return f"Потенциальных наследников для {person} не найдено"

    def _get_largest_family(self, prolog):
        result = prolog.query("largest_family(Person, Count)")
        if result:
            r = list(result[0])
            return f"Самая большая семья у {r['Person']} ({r['Count']} детей)"
        return "Данные о семьях не найдены"

    def _get_widowed(self, prolog):
        result = prolog.query("widowed(Person)")
        if result:
            people = list([r['Person'] for r in result])
            return f"Овдовевшие люди: {', '.join(people)}"
        return "Овдовевших людей не найдено"

    def _get_godparent_candidates(self, prolog):
        result = list(prolog.query("godparent_candidate(Person)"))
        if result:
            candidates = [r['Person'] for r in result]
            return f"Подходящие кандидаты в крестные: {', '.join(candidates)}"
        return "Подходящих кандидатов не найдено"

    def _get_golden_wedding_soon(self, prolog, years):
        result = prolog.query(f"golden_wedding_soon(X, Y, {years})")
        if result:
            couples = list([f"{r['X']} и {r['Y']}" for r in result])
            return f"Пары, которые отметят золотую свадьбу в ближайшие {years} лет: {', '.join(couples)}"
        return f"Таких пар не найдено"

    def _get_single_parents(self, prolog):
        result = prolog.query("single_parent(Person)")
        if result:
            parents = list([r['Person'] for r in result])
            return f"Одинокие родители: {', '.join(parents)}"
        return "Одиноких родителей не найдено"

    def _get_living_pensioners(self, prolog):
        result = prolog.query("living_pensioner(Person, Age)")
        if result:
            pensioners = list([f"{r['Person']} ({r['Age']} лет)" for r in result])
            return f"Живые пенсионеры: {', '.join(pensioners)}"
        return "Живых пенсионеров не найдено"
