# Match each location to its nearest crowd sensor and noise sensor.
# Uses haversine distance. Outputs location_sensor_mapping.csv.

import csv
import os
from math import radians, cos, sin, asin, sqrt

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def haversine(lat1, lon1, lat2, lon2):
    # great-circle distance in metres
    R = 6371000
    lat1, lon1, lat2, lon2 = map(radians, [float(lat1), float(lon1), float(lat2), float(lon2)])
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2
    return 2 * R * asin(sqrt(a))


def crowd_confidence(dist_m):
    if dist_m < 300:  return 'high'
    if dist_m < 600:  return 'medium'
    return 'low'


def noise_confidence(dist_m):
    if dist_m < 500:   return 'high'
    if dist_m < 1000:  return 'medium'
    return 'low'


def load_locations(path):
    with open(path, encoding='utf-8-sig') as f:
        return list(csv.DictReader(f))


def load_crowd_sensors(path):
    # only keep active ('A') sensors
    with open(path, encoding='utf-8-sig') as f:
        rows = list(csv.DictReader(f))
    return [r for r in rows if r['Status'] == 'A']


def load_noise_sensors(path):
    # dedupe by Device_id (csv has multiple readings per device)
    seen = {}
    with open(path, encoding='utf-8-sig') as f:
        for r in csv.DictReader(f):
            k = r['Device_id']
            if k not in seen:
                seen[k] = {
                    'device_id': r['Device_id'],
                    'name': r['SensorLocation'],
                    'lat': float(r['Lat']),
                    'lng': float(r['Lng'])
                }
    return list(seen.values())


def main():
    locations = load_locations(os.path.join(BASE, 'melbourne_locations_tagged.csv'))
    crowd_sensors = load_crowd_sensors(
        os.path.join(BASE, '../Crowd_level/pedestrian-counting-system-sensor-locations.csv'))
    noise_sensors = load_noise_sensors(os.path.join(BASE, 'noise_labeled_clean.csv'))

    results = []
    for loc in locations:
        try:
            lat, lng = float(loc['latitude']), float(loc['longitude'])
        except (ValueError, KeyError):
            continue

        # find nearest crowd sensor
        best_crowd, best_crowd_dist = None, float('inf')
        for s in crowd_sensors:
            try:
                d = haversine(lat, lng, s['Latitude'], s['Longitude'])
                if d < best_crowd_dist:
                    best_crowd_dist, best_crowd = d, s
            except Exception:
                pass

        # find nearest noise sensor
        best_noise, best_noise_dist = None, float('inf')
        for s in noise_sensors:
            d = haversine(lat, lng, s['lat'], s['lng'])
            if d < best_noise_dist:
                best_noise_dist, best_noise = d, s

        c_conf = crowd_confidence(best_crowd_dist)
        n_conf = noise_confidence(best_noise_dist)

        # add a note when a match is far away
        notes = []
        if c_conf == 'low':
            notes.append(f'Crowd sensor {round(best_crowd_dist)}m away - low confidence')
        if n_conf == 'low':
            notes.append(f'Noise sensor {round(best_noise_dist)}m away - low confidence, use with caution')
        elif n_conf == 'medium':
            notes.append(f'Noise sensor {round(best_noise_dist)}m away - medium confidence')

        results.append({
            'location_id':               loc['location_id'],
            'location_name':             loc['location_name'],
            'crowd_sensor_location_id':  best_crowd['Location_ID'] if best_crowd else '',
            'crowd_sensor_name':         best_crowd['Sensor_Description'] if best_crowd else '',
            'crowd_sensor_dist_m':       round(best_crowd_dist),
            'crowd_mapping_confidence':  c_conf,
            'noise_sensor_id':           best_noise['device_id'] if best_noise else '',
            'noise_sensor_name':         best_noise['name'] if best_noise else '',
            'noise_sensor_lat':          best_noise['lat'] if best_noise else '',
            'noise_sensor_lng':          best_noise['lng'] if best_noise else '',
            'noise_sensor_dist_m':       round(best_noise_dist),
            'noise_mapping_confidence':  n_conf,
            'mapping_method':            'nearest_neighbor_haversine',
            'notes':                     '; '.join(notes),
        })

    # write csv
    out = os.path.join(BASE, 'location_sensor_mapping.csv')
    fields = list(results[0].keys())
    with open(out, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        writer.writerows(results)

    print(f'Output: {out}')
    print(f'  Total: {len(results)} mappings')
    from collections import Counter
    cc = Counter(r['crowd_mapping_confidence'] for r in results)
    nc = Counter(r['noise_mapping_confidence'] for r in results)
    print(f'  pedestrian sensor confidence: {dict(cc)}')
    print(f'  noise sensor confidence: {dict(nc)}')


if __name__ == '__main__':
    main()
