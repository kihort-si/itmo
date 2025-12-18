from rdflib import Graph
from ontology.build import build_ontology
from parsers.character_parser import parse_characters
from parsers.lightcone_parser import parse_light_cones
from parsers.relics_parser import parse_relics
from parsers.enemy_parser import parse_enemies
from parsers.boss_parser import parse_bosses
from parsers.team_parser import parse_teams

ONTOLOGY_PATH = "data/hsr_ontology_clean.rdf"

if __name__ == "__main__":
    build_ontology(ONTOLOGY_PATH)

    g = Graph()
    g.parse(ONTOLOGY_PATH, format="xml")

    parse_characters(g, "https://game8.co/games/Honkai-Star-Rail/archives/404256")
    parse_light_cones(g, "https://game8.co/games/Honkai-Star-Rail/archives/406599")
    parse_relics(g, "https://game8.co/games/Honkai-Star-Rail/archives/406885")
    parse_enemies(g, "https://game8.co/games/Honkai-Star-Rail/archives/408174")
    parse_bosses(g, "https://game8.co/games/Honkai-Star-Rail/archives/409817")
    parse_teams(g, "https://game8.co/games/Honkai-Star-Rail/archives/409824")

    g.serialize(destination=ONTOLOGY_PATH, format="xml")
    print("Онтология обновлена.")
