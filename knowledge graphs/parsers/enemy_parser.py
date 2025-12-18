from typing import Optional, List
import re
import requests
from bs4 import BeautifulSoup, Tag
from rdflib import Graph, Namespace, RDF, RDFS, Literal

HSR = Namespace("http://example.org/hsr-ontology#")


def normalize(term: str) -> str:
    if not term:
        return ""
    s = term.replace("The ", "").strip()
    s = s.replace("%", "_percent")
    s = re.sub(r'[^0-9A-Za-z_]', '_', s)
    s = re.sub(r'_+', '_', s)
    return s.strip('_')


def _text_from_enemy_td(td: Tag) -> Optional[str]:

    if td is None:
        return None
    td_copy = BeautifulSoup(str(td), "html.parser")
    for img in td_copy.find_all("img"):
        img.decompose()
    text = td_copy.get_text(separator=" ", strip=True)
    return text or None


def _collect_weaknesses_from_td(td: Tag) -> List[tuple]:

    if td is None:
        return []
    res = []

    for a in td.find_all("a", href=True):
        text = (a.text or "").strip()
        href = a["href"].strip()
        if not text:
            continue

        text = re.sub(r'\s+', ' ', text)
        res.append((text, href))
    return res


def _parse_table(graph: Graph, table: Tag, enemy_type_label: str):
    tbody = table.find("tbody") or table
    rows = tbody.find_all("tr")
    for tr in rows:
        if tr.find_all("th"):
            continue

        tds = tr.find_all(["td", "th"])
        if not tds:
            continue

        enemy_td = None
        weakness_td = None
        for td in tds:
            classes = td.get("class") or []
            cls_join = " ".join(classes)
            if "Enemy_cell" in cls_join or "Enemy" in cls_join:
                enemy_td = td
            if "Weakness_cell" in cls_join or "Weakness" in cls_join:
                weakness_td = td
    
        if enemy_td is None and len(tds) >= 1:
            enemy_td = tds[0]
        if weakness_td is None and len(tds) >= 2:
            weakness_td = tds[1]

        name = _text_from_enemy_td(enemy_td)
        if not name:
            continue

        enemy_href = None
        a_enemy = enemy_td.find("a", href=True)
        if a_enemy:
            enemy_href = a_enemy["href"].strip()

        enemy_norm = normalize(name)
        enemy_uri = HSR[enemy_norm]

    
        graph.add((enemy_uri, RDF.type, HSR.Enemies))
        graph.add((enemy_uri, RDFS.label, Literal(name)))
        if enemy_href:
            graph.add((enemy_uri, HSR.sourceURL, Literal(enemy_href)))


        weaknesses = _collect_weaknesses_from_td(weakness_td)
        for (wtext, whref) in weaknesses:
            elem_norm = normalize(wtext)
            elem_uri = HSR[elem_norm]
            
            if whref:
                graph.add((elem_uri, HSR.sourceURL, Literal(whref)))
                graph.add((elem_uri, RDFS.label, Literal(wtext)))

            graph.add((enemy_uri, HSR.hasWeakness, elem_uri))


def parse_enemies(graph: Graph, url: str):

    resp = requests.get(url)
    resp.raise_for_status()
    soup = BeautifulSoup(resp.content, "html.parser")


    for header_text in ("All Normal Enemies", "All Elite Enemies", "All Enemies in Honkai: Star Rail"):
        h = soup.find(lambda t: t.name in ("h2", "h3") and t.get_text(strip=True) and header_text in t.get_text())
        if not h:
            continue
        table = h.find_next("table")
        if table:
            _parse_table(graph, table, header_text)