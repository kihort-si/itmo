from typing import Optional
import requests
from bs4 import BeautifulSoup
from rdflib import Graph, Namespace, RDF, RDFS, Literal
import re

HSR = Namespace("http://example.org/hsr-ontology#")


def normalize(term: str) -> str:
    if not term:
        return ""
    s = term.replace("The ", "").strip()
    s = s.replace("%", "_percent")
    s = re.sub(r'[^0-9A-Za-z_]', '_', s)
    s = re.sub(r'_+', '_', s)
    return s.strip('_')


def _extract_set_name_from_td(td) -> Optional[str]:
    if td is None:
        return None
    a = td.find("a")
    if a and a.text and a.text.strip():
        return a.text.strip()
    return None


def _extract_link_from_td(td) -> Optional[str]:
    if td is None:
        return None
    a = td.find("a", href=True)
    if a:
        return a["href"].strip()
    return None


def parse_relics(graph: Graph, url: str) -> Graph:
    resp = requests.get(url)
    resp.raise_for_status()
    soup = BeautifulSoup(resp.content, "html.parser")

    for h3 in soup.find_all("h3"):
        heading = h3.get_text(separator=" ", strip=True)
        if "Cavern" in heading or "Cavern Relic" in heading:
            relic_class = HSR.CavernRelics
            section_name = "Cavern"
        elif "Planar" in heading or "Planar Ornament" in heading or "Ornament" in heading:
            relic_class = HSR.PlanarRelics
            section_name = "Planar"
        else:
            continue

        table = h3.find_next("table")
        if table is None:
            continue

        tbody = table.find("tbody") or table
        rows = tbody.find_all("tr")
        data_rows = [r for r in rows if not r.find_all("th")]

        for tr in data_rows:
            tds = tr.find_all("td")
            if not tds:
                continue

            name = _extract_set_name_from_td(tds[0])
            if not name:
                continue
            norm_name = normalize(name)
            set_uri = HSR[norm_name]

            graph.add((set_uri, RDF.type, HSR.Set))
            graph.add((set_uri, RDF.type, relic_class))

            effect_text = ""
            if len(tds) >= 2:
                effect_text = tds[1].get_text(separator=" ", strip=True)
            if effect_text:
                graph.add((set_uri, RDFS.comment, Literal(effect_text)))

            specific_link = _extract_link_from_td(tds[0])
            if specific_link:
                graph.add((set_uri, HSR.sourceURL, Literal(specific_link)))
            else:
                graph.add((set_uri, HSR.sourceURL, Literal(url)))

            print(f"{section_name} набор: '{name}' -> HSR:{norm_name}")

    return graph