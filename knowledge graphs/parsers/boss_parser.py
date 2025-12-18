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


def _parse_boss_table(graph: Graph, table: Tag):
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
        print(f"Added boss/enemy: {name}")

        weaknesses = _collect_weaknesses_from_td(weakness_td)
        for (wtext, whref) in weaknesses:
            elem_norm = normalize(wtext)
            elem_uri = HSR[elem_norm]

            graph.add((elem_uri, RDF.type, HSR.Element))

            existing_label = list(graph.objects(elem_uri, RDFS.label))
            if not existing_label:
                graph.add((elem_uri, RDFS.label, Literal(wtext)))
            if whref:
                graph.add((elem_uri, HSR.sourceURL, Literal(whref)))
        
            graph.add((enemy_uri, HSR.hasWeakness, elem_uri))
            print(f"  Weakness: {wtext}")


def parse_bosses(graph: Graph, url: str, session: Optional[requests.Session] = None, timeout: int = 10):

    s = session or requests.Session()
    s.headers.update({
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                      "AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/120.0.0.0 Safari/537.36"
    })
    resp = s.get(url, timeout=timeout)
    resp.raise_for_status()
    soup = BeautifulSoup(resp.content, "html.parser")


    header_candidates = ("All Bosses", "All Enemies", "Bosses", "")
    parsed_any = False
    for htxt in header_candidates:
        if htxt:
            h = soup.find(lambda t: t.name in ("h2", "h3") and htxt in (t.get_text() or ""))
            if not h:
                continue
            table = h.find_next("table")
            if table:
                _parse_boss_table(graph, table)
                parsed_any = True

    if not parsed_any:

        tables = soup.find_all("table", class_=lambda v: v and "a-table" in v)
        if not tables:
            print("No suitable tables found on page.")
            return

        for t in tables:
            _parse_boss_table(graph, t)