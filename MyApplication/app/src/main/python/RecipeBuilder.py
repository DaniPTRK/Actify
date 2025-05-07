import pandas as pd, numpy as np, re
import collections

# Get recipe DB from HuggingFace
df = pd.read_csv("recipe.csv")

# Drop irrelevant columns
df = df.drop(
    ["author", "description", "rating", "rating_count", "review_count", "yields", "instructions_list", ],
    axis="columns"
)

# Extract total hour & minutes and return the total number of minutes
def to_minutes(txt):
    if pd.isna(txt) or not str(txt).strip(): return np.nan
    hours = re.search(r"(\d+)\s*hrs", txt)
    minutes = re.search(r"(\d+)\s*mins", txt)
    return (int(hours.group(1))*60 if hours else 0) + (int(minutes.group(1)) if minutes else 0)

for col in ["prep_time", "cook_time", "total_time"]:
    df[col] = df[col].apply(to_minutes)

# Determine difficulty based on directions and time to finish the recipe
def estimate_difficulty(row):
    directions = str(row["directions"])

    # Compute number of steps
    steps = re.split(r'\.\s+', directions.strip())
    num_steps = len([s for s in steps if len(s.strip()) > 10])

    # Get total time
    time = row.get("total_time") or (
        (row.get("prep_time") or 0) + (row.get("cook_time") or 0)
    )
    time = time if not pd.isna(time) else 0

    # Combine into simple tiers
    if num_steps <= 5 and time <= 60:
        return "Easy", time if time != 0 else 50.0
    elif num_steps <= 10 and time <= 120:
        return "Medium", time if time != 0 else 110.0
    else:
        return "Hard", time if time != 0 else 90.0

df[["difficulty", "total_time"]] = df.apply(estimate_difficulty, axis=1, result_type="expand")

# Determine diet type
meat = r"beef|pork|bacon|ham|lamb|lean|drippings|bleu|colman"
poultry = r"chicken|turkey|duck"
fish = r"salmon|tuna|cod|trout|anchovy|sardine|mackerel|langostinos|coho"
seafood = r"shrimp|prawn|crab|lobster|scallop"
dairy = r"milk|cheese|butter|yogurt|cream|whey|creme|nonfat|clarified"
eggs = r"\begg[s]?\b"

def diet_type(ing):
    txt = str(ing).lower()
    if re.search(meat, txt) or re.search(poultry, txt):
        return "omnivore"
    if re.search(fish, txt) or re.search(seafood, txt):
        return "pescatarian"
    if re.search(dairy, txt) or re.search(eggs, txt):
        return "vegetarian"
    return "vegan"

df["diet_type"] = df["ingredients"].apply(diet_type)

# Allergens
ALLERGEN_PATTERNS = {
    "gluten": r"wheat|flour|breadcrumbs|barley|rye|bulgur|spelt|pasta|macaroni",
    "dairy":  dairy,
    "eggs":   eggs,
    "nuts":   r"almond|walnut|pecan|hazelnut|cashew|pistachio",
    "soy":    r"soy|tofu|edamame|soybean",
    "fish":   fish,
    "shellfish": seafood,
    "sesame": r"sesame"
}

def allergen_list(ing):
    txt = str(ing).lower()
    found = [name for name, pat in ALLERGEN_PATTERNS.items() if re.search(pat, txt)]
    return found or ["none"]

df["allergens"] = df["ingredients"].apply(allergen_list)

category_map_en = {
    'main-dish': 'Main Course',
    'meat-and-poultry': 'Main Course',
    'seafood': 'Main Course',
    'soups-stews-and-chili': 'Main Course',
    'pasta-and-noodles': 'Main Course',
    'bbq-grilling': 'Main Course',

    'breakfast-and-brunch': 'Breakfast & Snacks',
    'appetizers-and-snacks': 'Breakfast & Snacks',
    'fruits-and-vegetables': 'Breakfast & Snacks',
    'trusted-brands-recipes-and-tips': 'Breakfast & Snacks',

    'side-dish': 'Sides & Salads',
    'salad': 'Sides & Salads',
    'ingredients': 'Sides & Salads',

    'desserts': 'Desserts & Baked Goods',
    'bread': 'Desserts & Baked Goods',

    'drinks': 'Drinks',

    'world-cuisine': 'Other',
    'holidays-and-events': 'Other',
    'everyday-cooking': 'Other',
    'uncategorized': 'Other',
    '515': 'Other',
    '251': 'Other'
}

df['main_category'] = df['category'].map(category_map_en)

df.to_csv('enriched_recipes.csv')
