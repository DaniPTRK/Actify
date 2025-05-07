import os
import pickle

from flask import Flask, jsonify, request
import json
import ast
import pandas as pd
import numpy as np
import faiss
import torch
from sentence_transformers import SentenceTransformer
import re

from RecipeRecommender import extract_keywords

# Definirea aplicației Flask
app = Flask(__name__)

# Încarcă datele din CSV (sau folosește-le dintr-o bază de date cache)
CSV_PATH = "enriched_recipes.csv"
INDEX_PKL = "faiss_index.pkl"
EMB_MODEL = "intfloat/e5-small-v2"
EMB_DIM = 384
TOP_K = 25
USE_CUDA = torch.cuda.is_available() and faiss.get_num_gpus() > 0

# Încarcă CSV-ul cu rețetele
df = pd.read_csv(CSV_PATH)


# Transformă ingredientele și alergenele în liste
def parse_ingredients(x):
    if isinstance(x, str):
        return [item.strip() for item in x.split(";") if item.strip()]
    return x if isinstance(x, list) else []


df["ingredients"] = df["ingredients"].apply(parse_ingredients)


def parse_allergens(x):
    if isinstance(x, str):
        try:
            return ast.literal_eval(x)
        except:
            return []
    return x if isinstance(x, list) else []


df["allergens"] = df["allergens"].apply(parse_allergens)

# Încarcă FAISS indexul
if os.path.exists(INDEX_PKL):
    with open(INDEX_PKL, "rb") as f:
        index = pickle.load(f)
else:
    # Creează indexul FAISS
    model = SentenceTransformer(EMB_MODEL, device="cuda" if USE_CUDA else "cpu")
    texts = (df["title"] + ". " + df["directions"].fillna("")).tolist()
    embs = model.encode(texts, batch_size=64, show_progress_bar=True, normalize_embeddings=True)
    df["embedding"] = embs.tolist()
    emb_matrix = np.vstack(df["embedding"].tolist()).astype("float32")
    index = faiss.IndexFlatIP(EMB_DIM)
    index.add(emb_matrix)
    with open(INDEX_PKL, "wb") as f:
        pickle.dump(index, f)


# Funcția de recomandare
def recommend(user_text, allergens, diet, dish_category, time_min=10, time_max=500):
    model = SentenceTransformer(EMB_MODEL, device="cuda" if USE_CUDA else "cpu")
    query_emb = model.encode([user_text], normalize_embeddings=True)

    similarity_scores, top_indexes = index.search(query_emb.astype("float32"), TOP_K)
    candidates = df.iloc[top_indexes[0]].copy()
    candidates["semantic_score"] = similarity_scores[0]

    # Filtrare ingrediente, alergene și tip dietetic
    mask = (
            candidates["diet_type"].str.lower().eq(diet.lower())
            & candidates["main_category"].str.contains(dish_category, case=False, na=False)
            & candidates["total_time"].between(time_min, time_max)
    )
    for a in allergens:
        mask &= ~candidates["allergens"].apply(lambda lst: a.lower() in [x.lower() for x in lst])

    candidates = candidates[mask]

    if candidates.empty:
        return {"error": "No recipe has been found."}

    # Re-rank cu scoruri suplimentare pe baza cuvintelor cheie
    def extra_score(row):
        names = " ".join(row["ingredients"]).lower()
        return sum(kw in names for kw in extract_keywords(user_text))

    candidates["extra"] = candidates.apply(extra_score, axis=1)
    candidates["final"] = candidates["semantic_score"] + 0.3 * candidates["extra"]
    best = candidates.sort_values("final", ascending=False).iloc[0]

    return {
        "image": best["image"],
        "name": best["title"],
        "diet_type": best["diet_type"],
        "allergens": best["allergens"],
        "total_time": int(best["total_time"]),
        "ingredients": best["ingredients"],
        "directions": best["directions"],
        "site": best["url"],
        "calories": best["calories"]
    }


# API route pentru a obține recomandări
@app.route('/recommend', methods=["POST"])
def get_recommendation():
    try:
        payload = request.json
        app.logger.info(
            f"Received JSON: {json.dumps(payload, indent=2)}")  # Folosim logger pentru a afisa JSON-ul formatat
        user_text = payload.get("user_text", "")
        allergens = payload.get("allergens", [])
        diet = payload.get("diet", "")
        dish_category = payload.get("dish_category", "")
        time_min = int(payload.get("time_min", 10))
        time_max = int(payload.get("time_max", 500))

        # Apelăm funcția de recomandare
        recommendation = recommend(user_text, allergens, diet, dish_category, time_min, time_max)
        app.logger.info(f"Sending response: {json.dumps(recommendation, indent=2)}")
        return jsonify(recommendation), 200

    except Exception as e:
        app.logger.error(f"Error: {e}")  # Log errorul
        return jsonify({'error': str(e)}), 500


# Rulare server
if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)

