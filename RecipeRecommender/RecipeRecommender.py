# Recipe Recommender for Actify App
import os, json, re, ast, pickle
from typing import List, Dict, Any
import regex as rgx
import numpy as np
import pandas as pd
import faiss, torch
from sentence_transformers import SentenceTransformer
import sys, importlib, platform


if platform.system() == "Darwin":
    importlib.import_module("faiss.swigfaiss")
    sys.modules.setdefault(
        "faiss.swigfaiss_avx2",
        sys.modules["faiss.swigfaiss"]
    )

# Configuration
BASE_DIR = os.path.dirname(__file__)
CSV_PATH = os.path.join(BASE_DIR, "enriched_recipes.csv")     # path to csv file
INDEX_PKL  = os.path.join(BASE_DIR, "faiss_index.pkl")          # pkl index
EMB_MODEL  = "intfloat/e5-small-v2"      # embedding model
EMB_DIM    = 384                         # embedding dimension
TOP_K      = 25                          # re-rank candidates
USE_CUDA = torch.cuda.is_available() and faiss.get_num_gpus() > 0 # check for GPU

# Load data
# df = pd.read_csv("hf://datasets/DaniPTRK/ActifyRecipeDataset/enriched_recipes.csv")
df = pd.read_csv(CSV_PATH)

# Transform ingredients string into a list
def parse_ingredients(x):
    if isinstance(x, str):
        return [item.strip() for item in x.split(";") if item.strip()]
    return x if isinstance(x, list) else []

df["ingredients"] = df["ingredients"].apply(parse_ingredients)

# Transform allergens into a list
def parse_allergens(x):
    if isinstance(x, str):
        try:
            return ast.literal_eval(x)
        except:
            return []
    return x if isinstance(x, list) else []

df["allergens"] = df["allergens"].apply(parse_allergens)

# Compute embedding of recipes
if "embedding" not in df.columns:
    print("[INFO] Computing embeddings")

    # Create sentence transformer and set device to CUDA/CPU
    model = SentenceTransformer(EMB_MODEL,
                            device="cuda" if USE_CUDA else "cpu")
    cpu_idx = faiss.IndexFlatIP(EMB_DIM)
    if USE_CUDA:
        res   = faiss.StandardGpuResources()
        index = faiss.index_cpu_to_gpu(res, 0, cpu_idx)
    else:
        index = cpu_idx
    
    # Use the name of the recipe and the directions of it as embedding
    texts = (df["title"] + ". " + df["directions"].fillna("")).tolist()
    embs  = model.encode(texts, batch_size=64, show_progress_bar=True, normalize_embeddings=True)

    # Save the embeddings
    df["embedding"] = embs.tolist()
    df.to_csv(CSV_PATH, index=False) 

# Set FAISS index for fast data access
if os.path.exists(INDEX_PKL):
    print("[INFO] Loading FAISS from cache..")
    with open(INDEX_PKL, "rb") as f:
        index = pickle.load(f)
else:
    # Set embeddings as list of values and get the embedding matrix
    df["embedding"] = df["embedding"].apply(lambda x: ast.literal_eval(x) if isinstance(x,str) else x)
    emb_matrix = np.vstack(df["embedding"].tolist()).astype("float32")

    # Load FAISS in cache
    print("[INFO] Loading FAISS to cache..")
    index = faiss.IndexFlatIP(EMB_DIM)
    index.add(emb_matrix)
    with open(INDEX_PKL, "wb") as f:
        pickle.dump(index, f)

# Extract keywords from the input given by the user
def extract_keywords(req_text):
    req_text = req_text.lower()

    # Keep only words
    words = re.findall(r"[a-zA-Z]{4,}", req_text)
    stop = {"want","with","that","have","like","need","some","make","recipe","dish","meal","containing"}
    return [w for w in words if w not in stop][:5]   # max 5 keywords

# Recipe Recommender
def recommend(user_text, allergens, diet, dish_category, time_min=10, time_max=500):
    # Initiate semantic search using the model
    model = SentenceTransformer(EMB_MODEL, device="cuda" if USE_CUDA else "cpu")
    query_emb = model.encode([user_text], normalize_embeddings=True)

    similarity_scores, top_indexes = index.search(query_emb.astype("float32"), TOP_K)

    candidates = df.iloc[top_indexes[0]].copy()
    candidates["semantic_score"] = similarity_scores[0]

    # Hard filter - get rid of all the other recipes which don't fit the user's request
    mask = (
        candidates["diet_type"].str.lower().eq(diet.lower())
        & candidates["main_category"].str.contains(dish_category, case=False, na=False)
        & candidates["total_time"].between(time_min, time_max)
    )
    for a in allergens:
        mask &= ~candidates["allergens"].apply(lambda lst: a.lower() in [x.lower() for x in lst])

    candidates = candidates[mask]

    # Check if there's any recipe left
    if candidates.empty:
        return {"error": "No recipe has been found."}

    # Heuristic re-rank
    want_kw = extract_keywords(user_text)

    # Define extra score as num of keywords which cand be found inside ingredients
    def extra_score(row):
        names = " ".join(row["ingredients"]).lower()
        return sum(kw in names for kw in want_kw)
    
    candidates["extra"] = candidates.apply(extra_score, axis=1)

    # Get the best candidate after applying the extra score
    candidates["final"] = candidates["semantic_score"] + 0.3*candidates["extra"]
    best = candidates.sort_values("final", ascending=False).iloc[0]

    # Send JSON payload to backend
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

# Extract info from JSON payload
def recommend_api(payload):
    user_text = payload.get("user_text", "")
    allergens = payload.get("allergens", [])
    diet = payload.get("diet", "")
    dish_category = payload.get("dish_category", "")
    time_min = int(payload.get("time_min", 10))
    time_max = int(payload.get("time_max", 500))
    return recommend(user_text, allergens, diet, dish_category, time_min, time_max)

# Test
if __name__ == "__main__":
    demo = recommend(
        user_text = "I'd like to eat some cookies with chocolate",
        allergens = ["eggs"],
        diet       = "vegetarian",
        dish_category = "Dessert"
    )
    print(json.dumps(demo, indent=2, ensure_ascii=False))
