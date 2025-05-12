from flask import Flask, request, jsonify
import requests

app = Flask(__name__)
API_KEY = "AIzaSyCcOZwn4LP3EiyFwpn2b7nT890OOJNG95M"

def get_route_google_maps(start, destination, transport_mode, api_key):
    url = "https://maps.googleapis.com/maps/api/directions/json"
    params = {
        "origin": start,
        "destination": destination,
        "mode": transport_mode,
        "key": api_key,
        "language": "en"
    }

    response = requests.get(url, params=params)
    data = response.json()

    if data["status"] != "OK":
        return {"error": data["status"]}

    ruta = data["routes"][0]["legs"][0]["steps"]
    instructiuni = []
    coordonate = []

    for pas in ruta:
        instructiune_text = pas["html_instructions"]
        distanta = pas["distance"]["text"]
        durata = pas["duration"]["text"]
        instructiuni.append({
            "text": instructiune_text,
            "distanta": distanta,
            "durata": durata
        })

        # Extrage și decodează polyline-ul pasului
        polyline = pas["polyline"]["points"]
        coordonate.extend(decode_polyline(polyline))

    return {
        "instructiuni": instructiuni,
        "coordonate": coordonate
    }


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

def genereaza_imagine_static_map(start_coords, dest_coords, polyline, api_key):
    lat1, lng1 = start_coords
    lat2, lng2 = dest_coords

    static_map_url = (
        "https://maps.googleapis.com/maps/api/staticmap"
        f"?size=600x400"
        f"&path=enc:{polyline}"
        f"&markers=color:green|label:A|{lat1},{lng1}"
        f"&markers=color:red|label:B|{lat2},{lng2}"
        f"&key={api_key}"
    )

    return static_map_url

@app.route("/Route", methods=["POST"])
def Route():
    payload = request.json
    start = payload.get("start")
    destination = payload.get("destination")
    mode = payload.get("mode", "walking")

    if not start or not destination:
        return jsonify({"error": "Start and destination are necessary."}), 400

    # Obține datele despre rută
    rezultat = get_route_google_maps(start, destination, mode, API_KEY)

    if "error" in rezultat:
        return jsonify(rezultat), 400

    # Obține overview_polyline pentru a genera imaginea hărții
    directions_url = "https://maps.googleapis.com/maps/api/directions/json"
    params = {
        "origin": start,
        "destination": destination,
        "mode": mode,
        "key": API_KEY,
        "language": "en"
    }

    response = requests.get(directions_url, params=params)
    data = response.json()

    if data["status"] != "OK":
        return jsonify({"error": "Failed to retrieve polyline"}), 400

    polyline_full = data["routes"][0]["overview_polyline"]["points"]

    # Generează imaginea hărții
    start_coords = (
    data["routes"][0]["legs"][0]["start_location"]["lat"],
    data["routes"][0]["legs"][0]["start_location"]["lng"]
)
    dest_coords = (
    data["routes"][0]["legs"][0]["end_location"]["lat"],
    data["routes"][0]["legs"][0]["end_location"]["lng"]
    )

    map_url = genereaza_imagine_static_map(start_coords, dest_coords, polyline_full, API_KEY)

    rezultat["map_image_url"] = map_url
    return jsonify(rezultat)

if __name__ == "__main__":
    app.run(debug=True)