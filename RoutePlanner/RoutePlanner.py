import os
from dotenv import load_dotenv
from flask import Flask, request, jsonify
import requests

load_dotenv()

app = Flask(__name__)
API_KEY = os.getenv("GOOGLE_API_KEY")

def decode_polyline(polyline_str):
    index, lat, lng = 0, 0, 0
    coordinates = []

    while index < len(polyline_str):
        shift, result = 0, 0
        while True:
            b = ord(polyline_str[index]) - 63
            index += 1
            result |= (b & 0x1f) << shift
            shift += 5
            if b < 0x20:
                break
        dlat = ~(result >> 1) if (result & 1) else (result >> 1)
        lat += dlat

        shift, result = 0, 0
        while True:
            b = ord(polyline_str[index]) - 63
            index += 1
            result |= (b & 0x1f) << shift
            shift += 5
            if b < 0x20:
                break
        dlng = ~(result >> 1) if (result & 1) else (result >> 1)
        lng += dlng

        coordinates.append({"lat": lat / 1e5, "lng": lng / 1e5})

    return coordinates

def decimeaza_coordonate(coords, pas=10):
    return coords[::pas]

def genereaza_imagine_static_map(start_coords, dest_coords, coordonate, api_key):
    lat1, lng1 = start_coords
    lat2, lng2 = dest_coords

    coordonate_decimate = decimeaza_coordonate(coordonate, pas=5)
    path_param = "path=color:blue|" + "|".join(f"{c['lat']},{c['lng']}" for c in coordonate_decimate)

    static_map_url = (
        "https://maps.googleapis.com/maps/api/staticmap"
        f"?size=600x400"
        f"&{path_param}"
        f"&markers=color:green|label:A|{lat1},{lng1}"
        f"&markers=color:red|label:B|{lat2},{lng2}"
        f"&key={api_key}"
    )

    print(f"URL lungime: {len(static_map_url)}")
    return static_map_url

def route_api(payload):
    start = payload.get("start", "")
    destination = payload.get("destination", "")
    mode = payload.get("mode", "walking")

    if not start or not destination:
        return {"error": "Start and destination are required."}

    url = "https://maps.googleapis.com/maps/api/directions/json"
    params = {
        "origin": start,
        "destination": destination,
        "mode": mode,
        "key": API_KEY,
        "language": "en"
    }

    response = requests.get(url, params=params)
    data = response.json()

    if data["status"] != "OK":
        return {"error": data["status"]}

    steps = data["routes"][0]["legs"][0]["steps"]
    instructiuni = []
    coordonate = []

    for pas in steps:
        instructiuni.append({
            "text": pas["html_instructions"],
            "distanta": pas["distance"]["text"],
            "durata": pas["duration"]["text"]
        })
        coordonate.extend(decode_polyline(pas["polyline"]["points"]))

    start_coords = (
        data["routes"][0]["legs"][0]["start_location"]["lat"],
        data["routes"][0]["legs"][0]["start_location"]["lng"]
    )
    dest_coords = (
        data["routes"][0]["legs"][0]["end_location"]["lat"],
        data["routes"][0]["legs"][0]["end_location"]["lng"]
    )

    map_url = genereaza_imagine_static_map(start_coords, dest_coords, coordonate, API_KEY)

    return {
        "instructiuni": instructiuni,
        "coordonate": coordonate,
        "map_image_url": map_url
    }

@app.route("/Route", methods=["POST"])
def Route():
    payload = request.json
    result = route_api(payload)
    return jsonify(result)

if __name__ == "__main__":
    app.run(debug=True)