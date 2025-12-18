from typing import Optional
import re
import requests
from bs4 import BeautifulSoup
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


def _text_from_first_link(td) -> Optional[str]:
    if td is None:
        return None
    a = td.find("a")
    if a and a.text and a.text.strip():
        return a.text.strip()
    return None


def _href_from_first_link(td) -> Optional[str]:
    if td is None:
        return None
    a = td.find("a", href=True)
    if a:
        return a["href"].strip()
    return None


def _parse_builds_page(graph: Graph, char_uri, page_url: str):
    sess = requests.Session()
    resp = sess.get(page_url)
    resp.raise_for_status()
    soup = BeautifulSoup(resp.content, "html.parser")

    th_best = soup.find(lambda tag: tag.name == "th" and tag.get_text(strip=True) and "Best Light Cone" in tag.get_text())
    if not th_best:
        return

    table = th_best.find_parent("table")
    if not table:
        return

    cone_td = table.find("td", class_="center")
    cone_a = None
    if cone_td:
        cone_a = cone_td.find("a", href=True)
        cone_name = _text_from_first_link(cone_td)
        cone_href = _href_from_first_link(cone_td)
        if cone_name:
            cone_norm = normalize(cone_name)
            cone_uri = HSR[cone_norm]
            graph.add((cone_uri, RDF.type, HSR.LightCone))
            graph.add((cone_uri, RDFS.label, Literal(cone_name)))
            graph.add((char_uri, HSR.recommendedLightCone, cone_uri))
            if cone_href:
                graph.add((cone_uri, HSR.sourceURL, Literal(cone_href)))

    anchors = []
    seen = set()
    for a in table.find_all("a", href=True):
        if cone_a is not None and a is cone_a:
            continue
        text = (a.text or "").strip()
        if not text:
            continue
        key = re.sub(r'\s+', ' ', text)
        if key in seen:
            continue
        seen.add(key)
        anchors.append(a)

    cavern_a = anchors[0] if len(anchors) >= 1 else None
    planar_a = anchors[1] if len(anchors) >= 2 else None

    if cavern_a:
        name = cavern_a.text.strip()
        href = cavern_a.get("href")
        uri = HSR[normalize(name)]
        graph.add((uri, RDF.type, HSR.Set))
        graph.add((uri, RDF.type, HSR.CavernRelics))
        graph.add((uri, RDFS.label, Literal(name)))
        if href:
            graph.add((uri, HSR.sourceURL, Literal(href)))
        graph.add((char_uri, HSR.hasCavernRelic, uri))

    if planar_a:
        name = planar_a.text.strip()
        href = planar_a.get("href")
        uri = HSR[normalize(name)]
        graph.add((uri, RDF.type, HSR.Set))
        graph.add((uri, RDF.type, HSR.PlanarRelics))
        graph.add((uri, RDFS.label, Literal(name)))
        if href:
            graph.add((uri, HSR.sourceURL, Literal(href)))
        graph.add((char_uri, HSR.hasPlanarRelic, uri))

    th_main = table.find(lambda tag: tag.name == "th" and "Main Stats" in tag.get_text())
    if th_main:
        tr_vals = th_main.find_parent("tr").find_next_sibling("tr")
        if tr_vals:
            tds_vals = tr_vals.find_all("td")
            if tds_vals:
                left = tds_vals[0].get_text(separator="\n", strip=True)
                for m in re.finditer(r"(?P<slot>Body|Feet|Sphere|Rope)\s*:\s*(?P<stat>[^\n<]+)", left, flags=re.I):
                    slot = m.group("slot").strip().lower()
                    stat = m.group("stat").strip()
                    stat_uri = HSR[normalize(stat)]
                    if slot == "body":
                        graph.add((char_uri, HSR.recommendedMainStatBody, stat_uri))
                    elif slot == "feet":
                        graph.add((char_uri, HSR.recommendedMainStatFeet, stat_uri))
                    elif slot == "sphere":
                        graph.add((char_uri, HSR.recommendedMainStatSphere, stat_uri))
                    elif slot == "rope":
                        graph.add((char_uri, HSR.recommendedMainStatRope, stat_uri))
                    graph.add((stat_uri, RDFS.label, Literal(stat)))


   
    if th_main:
        tr_vals = th_main.find_parent("tr").find_next_sibling("tr")
        if tr_vals:
            tds_vals = tr_vals.find_all("td")
            if len(tds_vals) >= 2:
                right = tds_vals[1].get_text(separator="\n", strip=True)
                for line in right.splitlines():
                    stat_name = line.split("★")[0].strip()
                    if stat_name:
                        stat_uri = HSR[normalize(stat_name)]
                        graph.add((char_uri, HSR.recommendedSubStats, stat_uri))
                        graph.add((stat_uri, RDFS.label, Literal(stat_name)))



    th_alt = soup.find(lambda tag: tag.name == "th" and tag.get_text(strip=True) and "Alternative Light Cones" in tag.get_text())
    if th_alt:
        table_alt = th_alt.find_parent("table")
        if table_alt:
            alt_seen = set()
            for a in table_alt.find_all("a", href=True):
                alt_name = (a.text or "").strip()
                if not alt_name:
                    continue
                key = re.sub(r'\s+', ' ', alt_name)
                if key in alt_seen:
                    continue
                alt_seen.add(key)

                alt_norm = normalize(alt_name)
                alt_uri = HSR[alt_norm]
                graph.add((alt_uri, RDF.type, HSR.LightCone))
                graph.add((alt_uri, RDFS.label, Literal(alt_name)))
                graph.add((char_uri, HSR.hasAlternativeLightCones, alt_uri))
                alt_href = a.get("href")
                if alt_href:
                    graph.add((alt_uri, HSR.sourceURL, Literal(alt_href)))



def parse_characters(graph: Graph, url: str):
    resp = requests.get(url)
    resp.raise_for_status()
    soup = BeautifulSoup(resp.content, "html.parser")

    table = soup.find("h3", string=re.compile(r"List of All Playable Characters", flags=re.I))
    if not table:
        print("Не удалось найти таблицу списка персонажей на странице.")
        return

    table = table.find_next("table")
    if not table:
        print("Не удалось найти таблицу после заголовка.")
        return

    tbody = table.find("tbody")
    if not tbody:
        print("Таблица не содержит tbody.")
        return

    rows = tbody.find_all("tr")
    for row in rows:
        cells = row.find_all("td")
        if len(cells) < 4:
            continue

        name_tag = cells[0].find("a")
        char_name = name_tag.text.strip() if name_tag else cells[0].text.strip()
        element = normalize(cells[2].text.strip())
        path = normalize(cells[3].text.strip())

        char_uri = HSR[normalize(char_name)]
        graph.add((char_uri, RDF.type, HSR.Character))
        graph.add((char_uri, HSR.hasElement, HSR[element]))
        graph.add((char_uri, HSR.hasPath, HSR[path]))

        print(f"{char_name} → {element}, {path}")

        if name_tag and name_tag.get("href"):
            char_page = name_tag["href"]
            try:
                _parse_builds_page(graph, char_uri, char_page)
            except Exception as e:
                print(f"Ошибка при парсинге билдов для {char_name} ({char_page}): {e}")