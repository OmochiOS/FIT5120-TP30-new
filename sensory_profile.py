# Build per-location sensory profile by combining noise data, crowd
# predictability and place-type proxies for lighting and quiet space.
# Outputs location_sensory_profile.csv.

import csv
import os
import statistics
from collections import Counter, defaultdict

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


# indoor venue types get a -1 noise adjustment (walls dampen sound)
INDOOR_TYPES = {
    'library', 'museum', 'gallery', 'theatre', 'cinema',
    'community_centre', 'arts_centre'
}


# noise score from median dB
def noise_score_from_db(db):
    if db < 45:  return 1
    if db < 60:  return 2
    if db < 70:  return 3
    return 4

def noise_label_from_score(score):
    return {1: 'Calm', 2: 'Moderate', 3: 'Busy', 4: 'Overstimulating'}[score]


# we don't have lighting sensors, so we use place type as a rough proxy.
# (score, label, type, notes)
LIGHTING_MAP = {
    'park':             (1, 'Natural',       'Natural',    'Proxy: outdoor park lighting'),
    'museum':           (2, 'Mixed-Natural', 'Mixed',      'Proxy: museum/gallery curated lighting'),
    'gallery':          (2, 'Mixed-Natural', 'Mixed',      'Proxy: museum/gallery curated lighting'),
    'library':          (3, 'Artificial',    'Artificial', 'Proxy: indoor library lighting'),
    'theatre':          (4, 'Harsh',         'Artificial', 'Proxy: stage/screen lighting'),
    'cinema':           (4, 'Harsh',         'Artificial', 'Proxy: screen lighting'),
    'community_centre': (3, 'Artificial',    'Artificial', 'Proxy: indoor facility lighting'),
    'arts_centre':      (3, 'Artificial',    'Artificial', 'Proxy: indoor arts facility lighting'),
}

def get_lighting(place_type):
    if place_type in LIGHTING_MAP:
        score, label, ltype, notes = LIGHTING_MAP[place_type]
        return score, label, ltype, 'place_type_proxy', notes
    # fall back to a safe mid value
    return 2, 'Mixed-Natural', 'Mixed', 'place_type_proxy', 'Proxy: default mixed lighting'

LIGHTING_LABELS = {1: 'Natural', 2: 'Mixed-Natural', 3: 'Artificial', 4: 'Harsh'}


# quiet-space availability is also estimated from place type
QUIET_MAP = {
    'library':          ('Available',     'Libraries have dedicated quiet zones by policy'),
    'museum':           ('Available',     'Most museums have quiet galleries or rest areas'),
    'gallery':          ('Available',     'Galleries typically have quiet viewing spaces'),
    'park':             ('Limited',       'Parks have natural open space but no designated quiet rooms'),
    'community_centre': ('Limited',       'Community centres may have quieter rooms on request'),
    'arts_centre':      ('Not Available', 'Arts centres are typically performance-oriented environments'),
    'theatre':          ('Not Available', 'Theatres are performance environments without quiet rooms'),
    'cinema':           ('Not Available', 'Cinemas are active screening environments'),
}

def get_quiet_space(place_type):
    if place_type in QUIET_MAP:
        return QUIET_MAP[place_type]
    return 'Limited', 'Unknown venue type - quiet space availability uncertain'

QUIET_SCORE = {'Available': 1, 'Limited': 2, 'Not Available': 4}


# convert predictability -> unpredictability score (used in the formula)
PRED_TO_UNPREDICT = {'High': 1, 'Medium': 2, 'Low': 3}
UNPREDICT_LABELS  = {1: 'Low', 2: 'Medium-Low', 3: 'Medium-High'}

def unpredict_score_from_pred(pred_level):
    return PRED_TO_UNPREDICT.get(pred_level, 2)


# overall label from weighted score
def overall_label(score):
    if score < 1.8:  return 'Calm'
    if score < 2.8:  return 'Moderate'
    return 'Busy'

SCORE_FORMULA = 'Noise×0.35 + Unpredictability×0.30 + Lighting×0.20 + QuietModifier×0.15'


def main():
    # load locations
    print('Loading locations...')
    with open(os.path.join(BASE, 'melbourne_locations_tagged.csv'), encoding='utf-8-sig') as f:
        locations = list(csv.DictReader(f))
    print(f'  total locations: {len(locations)}')

    # load location -> sensor mapping
    print('Loading sensor mapping...')
    with open(os.path.join(BASE, 'location_sensor_mapping.csv'), encoding='utf-8-sig') as f:
        mapping_rows = list(csv.DictReader(f))
    loc_mapping = {r['location_id']: r for r in mapping_rows}

    # compute median noise (dB) per sensor
    print('Loading noise data...')
    sensor_readings = defaultdict(list)
    with open(os.path.join(BASE, 'noise_labeled_clean.csv'), encoding='utf-8-sig') as f:
        for r in csv.DictReader(f):
            try:
                sensor_readings[r['Device_id']].append(float(r['Noise']))
            except (ValueError, KeyError):
                pass

    sensor_median_db = {}
    for sid, readings in sensor_readings.items():
        sensor_median_db[sid] = round(statistics.median(readings), 1)
    print(f'  sensors with noise data: {len(sensor_median_db)}')

    # load historical crowd patterns to derive predictability
    print('Loading crowd patterns...')
    loc_predictability = defaultdict(list)
    crowd_pattern_path = os.path.join(BASE, 'location_historical_crowd_pattern.csv')
    if os.path.exists(crowd_pattern_path):
        with open(crowd_pattern_path, encoding='utf-8-sig') as f:
            for r in csv.DictReader(f):
                pred = r.get('predictability_level', '').strip()
                if pred:
                    loc_predictability[r['location_id']].append(pred)
    print(f'  locations with historical crowd data: {len(loc_predictability)}')

    # build profile rows
    print('Generating sensory profiles...')
    rows_out = []
    label_counter = Counter()

    for loc in locations:
        loc_id    = loc['location_id']
        loc_name  = loc['location_name']
        place_type = loc.get('place_type', '').strip().lower()
        tag_primary = loc.get('tag_primary', '').strip()

        # noise
        mapping   = loc_mapping.get(loc_id, {})
        noise_sid = mapping.get('noise_sensor_id', '')
        noise_conf = mapping.get('noise_mapping_confidence', 'low')

        if noise_sid and noise_sid in sensor_median_db:
            median_db = sensor_median_db[noise_sid]
        else:
            median_db = 62.0  # fallback when no sensor reading

        noise_raw   = noise_score_from_db(median_db)
        is_indoor   = place_type in INDOOR_TYPES
        noise_final = max(1, noise_raw - 1) if is_indoor else noise_raw
        noise_lbl   = noise_label_from_score(noise_final)

        # predictability -> unpredictability
        preds = loc_predictability.get(loc_id, [])
        if preds:
            dominant_pred = Counter(preds).most_common(1)[0][0]
        else:
            dominant_pred = 'Unknown'

        if dominant_pred == 'Unknown':
            unpredict_s = 2
        else:
            unpredict_s = unpredict_score_from_pred(dominant_pred)

        unpredict_lbl = {1: 'Low', 2: 'Medium-Low', 3: 'Medium-High'}.get(unpredict_s, 'Medium-Low')

        # lighting (proxy)
        light_s, light_lbl, light_type, light_src, light_notes = get_lighting(place_type)

        # quiet space (proxy)
        quiet_status, quiet_notes = get_quiet_space(place_type)
        quiet_s = QUIET_SCORE[quiet_status]

        # weighted overall sensory score
        overall = (noise_final * 0.35
                   + unpredict_s  * 0.30
                   + light_s      * 0.20
                   + quiet_s      * 0.15)
        overall = round(overall, 2)
        o_label  = overall_label(overall)
        label_counter[o_label] += 1

        rows_out.append({
            'location_id':             loc_id,
            'location_name':           loc_name,
            'tag_primary':             tag_primary,
            'place_type':              place_type,
            'noise_sensor_id':         noise_sid,
            'noise_median_db':         median_db,
            'noise_score':             noise_final,
            'noise_label':             noise_lbl,
            'noise_sensor_confidence': noise_conf,
            'dominant_predictability': dominant_pred,
            'unpredictability_score':  unpredict_s,
            'unpredictability_label':  unpredict_lbl,
            'lighting_score':          light_s,
            'lighting_label':          LIGHTING_LABELS.get(light_s, 'Mixed-Natural'),
            'lighting_type':           light_type,
            'lighting_source':         light_src,
            'lighting_notes':          light_notes,
            'quiet_space_status':      quiet_status,
            'quiet_space_notes':       quiet_notes,
            'overall_sensory_score':   overall,
            'overall_sensory_label':   o_label,
            'score_formula':           SCORE_FORMULA,
            'lighting_is_proxy':       True,
            'noise_score_raw':         noise_raw,
            'noise_adjusted_for_indoor': is_indoor,
        })

    # write csv
    out = os.path.join(BASE, 'location_sensory_profile.csv')
    fields = list(rows_out[0].keys())
    with open(out, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows_out)

    print(f'Output: {out}')
    print(f'  Total rows: {len(rows_out)}')
    print(f'  overall sensory label distribution: {dict(label_counter)}')
    indoor_count = sum(1 for r in rows_out if r['noise_adjusted_for_indoor'])
    print(f'  indoor soundproofing adjustments applied: {indoor_count}')
    no_crowd = sum(1 for r in rows_out if r['dominant_predictability'] == 'Unknown')
    print(f'  without historical crowd data (defaulted to medium unpredictability): {no_crowd}')


if __name__ == '__main__':
    main()
