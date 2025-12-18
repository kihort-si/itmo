from typing import Optional
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


def _text_from_first_link(td: Optional[Tag]) -> Optional[str]:
    if td is None:
        return None
    a = td.find("a")
    if a and a.text and a.text.strip():
        return a.text.strip()
    txt = td.get_text(separator=" ", strip=True)
    return txt or None


def _href_from_first_link(td: Optional[Tag]) -> Optional[str]:
    if td is None:
        return None
    a = td.find("a", href=True)
    if a:
        return a["href"].strip()
    return None


def _create_team_instance(graph: Graph, page_label: str, subgroup: str, idx: int, source_url: Optional[str]):
    team_label = f"{page_label} — {subgroup}" if subgroup else page_label
    if idx > 0:
        team_label = f"{team_label} ({idx})"
    team_norm = normalize(team_label)
    team_uri = HSR[team_norm]
    graph.add((team_uri, RDF.type, HSR.Team))
    graph.add((team_uri, RDFS.label, Literal(team_label)))
    if source_url:
        graph.add((team_uri, HSR.sourceURL, Literal(source_url)))
    return team_uri


def parse_teams(graph: Graph, url: str):
    s = requests.Session()
    s.headers.update({
        "User-Agent": "Mozilla/5.0 (compatible; team-parser/1.0)"
    })
    resp = s.get(url)
    resp.raise_for_status()
    soup = BeautifulSoup(resp.content, "html.parser")

    headers = soup.find_all(lambda t: t.name == "h4" and (t.get_text(strip=True)))
    if not headers:
        tables = soup.find_all("table", class_=lambda v: v and "a-table" in v)
        page_label = "Teams"
        for table in tables:
            _parse_team_table(graph, table, page_label, url)
        return

    for h in headers:
        page_label = h.get_text(strip=True)
        node = h
        while True:
            node = node.find_next_sibling()
            if node is None:
                break
            if isinstance(node, Tag) and node.name == "table":
                _parse_team_table(graph, node, page_label, url)
            if isinstance(node, Tag) and node.name in ("h2", "h3", "h4"):
                break


def _parse_team_table(graph: Graph, table: Tag, page_label: str, source_url: Optional[str]):

    tbody = table.find("tbody") or table
    rows = tbody.find_all("tr", recursive=False)

    current_subgroup = ""
    team_counter = {} 
    i = 0
    while i < len(rows):
        tr = rows[i]
        ths = tr.find_all("th")
        if ths and len(ths) == 1 and (ths[0].get("colspan") or "").strip():
            subgroup_text = ths[0].get_text(separator=" ", strip=True)
            current_subgroup = subgroup_text
            i += 1
            if i < len(rows):
                next_tr = rows[i]
                if next_tr.find_all("th") and len(next_tr.find_all("th")) >= 2:
                    i += 1
            member_row_idx = 0
            while i < len(rows):
                look = rows[i]
                look_ths = look.find_all("th")
                if look_ths and len(look_ths) == 1 and (look_ths[0].get("colspan") or "").strip():
                    break
                tds = look.find_all("td")
                if not tds:
                    i += 1
                    continue
                cols = [td for td in tds]
                count = team_counter.get(current_subgroup, 0)
                team_uri = _create_team_instance(graph, page_label, current_subgroup, count, source_url)
                team_counter[current_subgroup] = count + 1

                role_names = ["DPS", "Support", "Support", "Sustain"]
                for col_idx, td in enumerate(cols):
                    actor_name = _text_from_first_link(td)
                    actor_href = _href_from_first_link(td)
                    if not actor_name:
                        continue
                    actor_norm = normalize(actor_name)
                    actor_uri = HSR[actor_norm]
                    existing_label = list(graph.objects(actor_uri, RDFS.label))
                    if not existing_label:
                        graph.add((actor_uri, RDFS.label, Literal(actor_name)))
                    if actor_href:
                        graph.add((actor_uri, HSR.sourceURL, Literal(actor_href)))
                    role = role_names[col_idx] if col_idx < len(role_names) else f"Role_{col_idx+1}"
                    prop = None
                    if role.lower().startswith("dps"):
                        prop = HSR.hasDPS
                    elif role.lower().startswith("support"):
                        prop = HSR.hasSupport
                    elif role.lower().startswith("sustain"):
                        prop = HSR.hasSustain
                    else:
                        prop = HSR.hasMember
                    graph.add((team_uri, prop, actor_uri))
                member_row_idx += 1
                i += 1
            continue
        else:
            if tr.find_all("th") and len(tr.find_all("th")) >= 2:
                i += 1
                if i < len(rows):
                    member_tr = rows[i]
                    tds = member_tr.find_all("td")
                    if tds:
                        count = team_counter.get(page_label, 0)
                        team_uri = _create_team_instance(graph, page_label, "", count, source_url)
                        team_counter[page_label] = count + 1
                        role_names = [th.get_text(strip=True) for th in tr.find_all("th")]
                        for col_idx, td in enumerate(tds):
                            actor_name = _text_from_first_link(td)
                            actor_href = _href_from_first_link(td)
                            if not actor_name:
                                continue
                            actor_norm = normalize(actor_name)
                            actor_uri = HSR[actor_norm]
                            existing_label = list(graph.objects(actor_uri, RDFS.label))
                            if not existing_label:
                                graph.add((actor_uri, RDFS.label, Literal(actor_name)))
                            if actor_href:
                                graph.add((actor_uri, HSR.sourceURL, Literal(actor_href)))
                            r = role_names[col_idx] if col_idx < len(role_names) else "Member"
                            prop = HSR.hasMember
                            if "DPS" in r.upper():
                                prop = HSR.hasDPS
                            elif "SUPPORT" in r.upper():
                                prop = HSR.hasSupport
                            elif "SUSTAIN" in r.upper():
                                prop = HSR.hasSustain
                            graph.add((team_uri, prop, actor_uri))
                i += 1
                continue
            i += 1
            continue