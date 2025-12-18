import os
import pickle

import numpy as np
import rdflib
import torch
from pykeen.models import TransE
from pykeen.pipeline import pipeline
from pykeen.triples import TriplesFactory
import json

try:
    import torch_directml
except ImportError:
    torch_directml = None

try:
    import matplotlib.pyplot as plt
except ImportError:
    plt = None

try:
    from sklearn.manifold import TSNE
    from sklearn.decomposition import PCA
except ImportError:
    TSNE = None
    PCA = None


def train_graph_embeddings(rdf_file_path: str,
                           save_dir: str = "hsr_embedding_results"):

    g = rdflib.Graph()
    g.parse(rdf_file_path, format="xml")

    triples = []
    entity_types = {}
    for s, p, o in g:
        s_str, p_str, o_str = str(s), str(p), str(o)
        triples.append([s_str, p_str, o_str])
        if p_str == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type":
            entity_types.setdefault(s_str, set()).add(o_str)
    triples = np.array(triples, dtype=str)

    tf = TriplesFactory.from_labeled_triples(triples)
    training, testing = tf.split([0.8, 0.2], random_state=42)

    if torch.cuda.is_available():
        device = "cuda"
    elif torch_directml is not None:
        device = torch_directml.device()
    else:
        device = "cpu"

    os.makedirs(save_dir, exist_ok=True)

    result = pipeline(
        training=training,
        testing=testing,
        model="DistMult",
        model_kwargs=dict(embedding_dim=100),     
        training_kwargs=dict(num_epochs=400),     
        random_seed=42,
        device=device,
    )

    result.save_to_directory(save_dir)

    tf_path = os.path.join(save_dir, "triples_factory.pkl")
    with open(tf_path, "wb") as f:
        pickle.dump(tf, f)
    types_path = os.path.join(save_dir, "entity_types.pkl")
    with open(types_path, "wb") as f:
        pickle.dump(entity_types, f)

    return result.model, tf, entity_types


def load_saved_model(directory: str = "hsr_embedding_results"):
    
    model = TransEModel.load(os.path.join(directory, "model.pkl"))
    
    tf_path = os.path.join(directory, "triples_factory.pkl")
    with open(tf_path, "rb") as f:
        tf = pickle.load(f)
    types_path = os.path.join(directory, "entity_types.pkl")
    entity_types = {}
    if os.path.exists(types_path):
        with open(types_path, "rb") as f:
            entity_types = pickle.load(f)
    
    return model, tf, entity_types

def get_entity_embedding(entity_uri: str, model, tf):

    try:
        entity_id = tf.entity_to_id[entity_uri]
        embedding = model.entity_representations[0](indices=torch.tensor([entity_id])).detach().numpy()[0]
        return embedding
    except KeyError:
        print(f"Сущность {entity_uri} не найдена в графе.")
        return None


def get_relation_embedding(relation_uri: str, model, tf):

    try:
        relation_id = tf.relation_to_id[relation_uri]
        embedding = model.relation_representations[0](indices=torch.tensor([relation_id])).detach().numpy()[0]
        return embedding
    except KeyError:
        print(f"Отношение {relation_uri} не найдено в графе.")
        return None


def find_similar_entities(entity_uri: str, model, tf, entity_types: dict | None = None, top_k: int = 5):

    from sklearn.metrics.pairwise import cosine_similarity
    
    entity_embedding = get_entity_embedding(entity_uri, model, tf)
    if entity_embedding is None:
        return []
    
    all_entity_ids = torch.arange(len(tf.entity_to_id))
    all_embeddings = model.entity_representations[0](indices=all_entity_ids).detach().numpy()
    
    similarities = cosine_similarity([entity_embedding], all_embeddings)[0]
    
    top_indices = np.argsort(similarities)[::-1][1:]
    
    results = []
    id_to_entity = {v: k for k, v in tf.entity_to_id.items()}
    
    filtered = []
    if entity_types and entity_uri in entity_types:
        target_types = entity_types.get(entity_uri, set())
        for idx in top_indices:
            uri = id_to_entity[idx]
            types = entity_types.get(uri, set())
            if target_types & types:
                filtered.append(idx)
            if len(filtered) >= top_k:
                break
    else:
        filtered = list(top_indices[:top_k])
    
    i = 0
    while len(filtered) < top_k and i < len(top_indices):
        idx = top_indices[i]
        if idx not in filtered:
            filtered.append(idx)
        i += 1
    
    for idx in filtered[:top_k]:
        similar_entity = id_to_entity[idx]
        similarity = similarities[idx]
        results.append((similar_entity, similarity))
    
    return results


def reduce_embeddings(embeddings,
                      method: str = "tsne",
                      random_state: int = 42,
                      perplexity: int = 30):
    if method == "pca":
        if PCA is None:
            raise ImportError("Скрипт запущен без scikit-learn. Установи scikit-learn для PCA.")
        reducer = PCA(n_components=2, random_state=random_state)
    else:
        if TSNE is None:
            raise ImportError("Скрипт запущен без scikit-learn. Установи scikit-learn для t-SNE.")
        n_samples = len(embeddings)
        if n_samples < 2:
            raise ValueError("Слишком мало точек для t-SNE: нужно хотя бы 2 сущности.")
        safe_perplexity = min(perplexity, n_samples - 1)
        reducer = TSNE(
            n_components=2,
            random_state=random_state,
            perplexity=safe_perplexity,
            init="pca",
            learning_rate="auto",
        )

    return reducer.fit_transform(embeddings)


def collect_all_embeddings(model, tf):

    all_entity_ids = torch.arange(len(tf.entity_to_id))
    embeddings = model.entity_representations[0](indices=all_entity_ids).detach().numpy()
    
    id_to_entity = {v: k for k, v in tf.entity_to_id.items()}
    labels = [id_to_entity[i] for i in range(len(embeddings))]
    
    return embeddings, labels

def plot_embeddings_2d(points_2d,
                       labels,
                       title: str = "Embedding map",
                       max_labels: int = 75,
                       save_path: str | None = None,
                       figsize=(12, 10)):

    if plt is None:
        raise ImportError("matplotlib не установлен. Установи matplotlib для визуализации.")

    plt.figure(figsize=figsize)
    plt.scatter(points_2d[:, 0], points_2d[:, 1], s=12, alpha=0.6)

    def _shorten(uri: str) -> str:
        if "#" in uri:
            return uri.split("#")[-1]
        return uri

    for i, label in enumerate(labels[:max_labels]):
        short = _shorten(label)
        plt.annotate(short, (points_2d[i, 0], points_2d[i, 1]), fontsize=8, alpha=0.7)

    plt.title(title)
    plt.tight_layout()

    if save_path:
        plt.savefig(save_path, dpi=200)
    else:
        plt.show()


def visualize_embeddings(model: TransE,
                         triples_factory: TriplesFactory,
                         method: str = "tsne",
                         max_labels: int = 75,
                         save_path: str = "hsr_embedding_results/embeddings.png",
                         perplexity: int = 30,
                         max_points: int = 1200,
                         random_state: int = 42):

    embeddings, labels = collect_all_embeddings(model, triples_factory)

    if max_points and len(embeddings) > max_points:
        rng = np.random.default_rng(random_state)
        indices = rng.choice(len(embeddings), size=max_points, replace=False)
        embeddings = embeddings[indices]
        labels = [labels[i] for i in indices]

    points_2d = reduce_embeddings(embeddings, method=method, perplexity=perplexity)

    plot_embeddings_2d(
        points_2d,
        labels,
        title=f"HSR Ontology Embeddings ({method.upper()})",
        max_labels=max_labels,
        save_path=save_path,
    )


def find_rdf_file():

    rdf_path = "data/hsr_ontology.rdf"
    if os.path.exists(rdf_path):
        return rdf_path
    
    for file in os.listdir("."):
        if file.endswith(".rdf"):
            return file
    
    raise FileNotFoundError("RDF файл не найден. Ожидается data/hsr_ontology.rdf")


def main():
    save_dir = "hsr_embedding_results"
    
    try:
        rdf_file = find_rdf_file()
    except FileNotFoundError as e:
        return
    
    if os.path.exists(save_dir) and os.path.exists(os.path.join(save_dir, "model.pkl")):
        model, tf, entity_types = load_saved_model(save_dir)
    else:
        model, tf, entity_types = train_graph_embeddings(rdf_file, save_dir)
        print("Обучение завершено.")
    
    results_path = os.path.join(save_dir, "results.json")
    if os.path.exists(results_path):
        with open(results_path, "r") as f:
            metrics = json.load(f)
        
        print("\n" + "=" * 60)
        print("МЕТРИКИ КАЧЕСТВА МОДЕЛИ")
        print("=" * 60)
        
        metrics_data = metrics.get("metrics", {}).get("both", {}).get("realistic", {})
        mr = metrics_data.get("arithmetic_mean_rank")
        mrr = metrics_data.get("inverse_harmonic_mean_rank")
        hits_at_1 = metrics_data.get("hits_at_1")
        hits_at_3 = metrics_data.get("hits_at_3")
        hits_at_10 = metrics_data.get("hits_at_10")
        
        if mr is not None:
            print(f"MR (Mean Rank): {mr:.4f}")
        if mrr is not None:
            print(f"MRR (Mean Reciprocal Rank): {mrr:.4f}")
        if hits_at_1 is not None:
            print(f"Hits@1: {hits_at_1:.4f}")
        if hits_at_3 is not None:
            print(f"Hits@3: {hits_at_3:.4f}")
        if hits_at_10 is not None:
            print(f"Hits@10: {hits_at_10:.4f}")
        
        print("\nВЫВОД:")
        if mrr is not None and mrr > 0.5:
            print("Модель показывает хорошие результаты (MRR > 0.5).")
        elif mrr is not None and mrr > 0.3:
            print("Модель показывает удовлетворительные результаты (MRR > 0.3).")
        else:
            print("Модель требует улучшения (низкий MRR).")
        
        if hits_at_10 is not None and hits_at_10 > 0.7:
            print("Высокая точность в топ-10 (Hits@10 > 0.7).")
        elif hits_at_10 is not None and hits_at_10 > 0.5:
            print("Приемлемая точность в топ-10 (Hits@10 > 0.5).")
        else:
            print("Низкая точность в топ-10.")
    else:
        print("Метрики не найдены. Возможно, модель не была обучена с сохранением результатов.")
    
    print("\n")
    print("Примеры")

    
    ontology_ns = "http://example.org/hsr-ontology#"
    class_to_entities: dict[str, list[str]] = {}

    for ent_uri, types in entity_types.items():
        if not ent_uri.startswith(ontology_ns):
            continue
        for t in types:
            if not isinstance(t, str):
                continue
            if not t.startswith(ontology_ns):
                continue
            class_to_entities.setdefault(t, []).append(ent_uri)

    if not class_to_entities:
        print("Не удалось найти классы в онтологии для демонстрации.")
    else:
        for class_uri in sorted(class_to_entities.keys(), key=lambda u: u.split("#")[-1]):
            class_name = class_uri.split("#")[-1]
            representative = class_to_entities[class_uri][0]
            rep_name = representative.split("#")[-1]
            print(f"\n→ Класс: {class_name}")
            print(f"  Представитель: {rep_name}")

            similar = find_similar_entities(representative, model, tf, entity_types=entity_types, top_k=3)
            if similar:
                print("  Похожие сущности (топ-3):")
                for sim_entity, similarity in similar:
                    sim_name = sim_entity.split("#")[-1]
                    print(f"    • {sim_name}: {similarity:.4f}")
            else:
                print("  Похожие сущности не найдены.")
    
    example_entity = list(tf.entity_to_id.keys())[0] 
    example_embedding = get_entity_embedding(example_entity, model, tf)
    if example_embedding is not None:
        print(f"\nПример вектора встраивания для сущности '{example_entity.split('#')[-1]}':")
        print(f"  Размерность: {len(example_embedding)}")
        print(f"  Вектор: {example_embedding[:10]}...") 
    
    visualize_embeddings(model, tf, method="tsne", max_labels=75, save_path="hsr_embedding_results/embeddings.png")


if __name__ == "__main__":
    main()
