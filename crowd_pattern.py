# Build historical crowd pattern per location, day-of-week and hour.
# Reads sensor mapping + monthly pedestrian counts. Outputs
# location_historical_crowd_pattern.csv.

import csv
import math
import os
from collections import defaultdict
from datetime import datetime, date

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


# crowd thresholds (avg pedestrians per hour)
def crowd_label(avg):
    if avg <= 68:    return 'Low'
    if avg <= 316:   return 'Medium'
    return 'High'

# coefficient of variation -> how predictable the pattern is
def predictability_level(cv):
    if cv < 0.3:   return 'High'
    if cv < 0.5:   return 'Medium'
    return 'Low'

def time_block(hour):
    h = int(hour)
    if h <= 6:    return 'Early Morning'
    if h <= 9:    return 'Morning'
    if h <= 12:   return 'Late Morning'
    if h <= 15:   return 'Afternoon'
    if h <= 18:   return 'Late Afternoon'
    if h <= 21:   return 'Evening'
    return 'Night'

DAY_ORDER = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
WEEKDAYS  = {'Mon', 'Tue', 'Wed', 'Thu', 'Fri'}


def main():
    # 1. load mapping (location -> sensor)
    print('Loading sensor mapping...')
    with open(os.path.join(BASE, 'location_sensor_mapping.csv'), encoding='utf-8-sig') as f:
        mapping_rows = list(csv.DictReader(f))

    # only keep locations whose nearest sensor is high/medium confidence
    loc_to_sensor = {}
    for m in mapping_rows:
        if m['crowd_mapping_confidence'] in ('high', 'medium'):
            loc_to_sensor[m['location_id']] = m['crowd_sensor_location_id']
    needed_sensors = set(loc_to_sensor.values())
    print(f'  high/medium confidence locations: {len(loc_to_sensor)}, sensors involved: {len(needed_sensors)}')

    # 2. load location names for display
    with open(os.path.join(BASE, 'melbourne_locations_tagged.csv'), encoding='utf-8-sig') as f:
        loc_names = {r['location_id']: r['location_name'] for r in csv.DictReader(f)}

    # 3. load historical hourly counts, bucket by sensor/dow/hour
    print('Loading monthly counts (large file, please wait)...')
    buckets     = defaultdict(lambda: defaultdict(lambda: defaultdict(list)))  # sensor -> dow -> hour -> [counts]
    dates_seen  = defaultdict(list)

    monthly_path = os.path.join(BASE, '../Crowd_level/pedestrian-counting-system-monthly-counts-per-hour.csv')
    with open(monthly_path, encoding='utf-8-sig') as f:
        for row in csv.DictReader(f):
            sid = row['Location_ID']
            if sid not in needed_sensors:
                continue
            try:
                dt    = datetime.strptime(row['Sensing_Date'], '%Y-%m-%d')
                dow   = dt.strftime('%a')
                hour  = int(row['HourDay'])
                count = int(row['Total_of_Directions'])
                buckets[sid][dow][hour].append(count)
                dates_seen[sid].append(row['Sensing_Date'])
            except Exception:
                pass

    print(f'  sensors with history: {len(buckets)}')

    # 4. aggregate stats per (location, dow, hour)
    print('Generating pattern rows...')
    today = date.today().isoformat()
    rows_out = []

    for loc_id, sensor_id in loc_to_sensor.items():
        if sensor_id not in buckets:
            continue

        date_start = min(dates_seen[sensor_id])
        date_end   = max(dates_seen[sensor_id])
        loc_name   = loc_names.get(loc_id, '')

        for dow in DAY_ORDER:
            if dow not in buckets[sensor_id]:
                continue
            day_type = 'Weekday' if dow in WEEKDAYS else 'Weekend'

            for hour in range(24):
                counts = buckets[sensor_id][dow].get(hour, [])
                if not counts:
                    continue

                n   = len(counts)
                avg = sum(counts) / n
                mn  = min(counts)
                mx  = max(counts)
                std = math.sqrt(sum((c - avg) ** 2 for c in counts) / n) if n > 1 else 0
                cv  = std / avg if avg > 0 else 0

                label = crowd_label(avg)
                pred  = predictability_level(cv)
                tb    = time_block(hour)
                # recommend a timeslot if it's reliably calm
                recommended = (label == 'Low' and pred in ('High', 'Medium'))

                rows_out.append({
                    'location_id':           loc_id,
                    'location_name':         loc_name,
                    'crowd_sensor_id':       sensor_id,
                    'day_type':              day_type,
                    'day_of_week':           dow,
                    'hour':                  hour,
                    'time_block':            tb,
                    'avg_crowd_count':       round(avg, 1),
                    'min_crowd_count':       mn,
                    'max_crowd_count':       mx,
                    'std_crowd_count':       round(std, 1),
                    'sample_size':           n,
                    'crowd_level_label':     label,
                    'predictability_level':  pred,
                    'recommended_visit_flag': recommended,
                    'date_range_start':      date_start,
                    'date_range_end':        date_end,
                    'updated_at':            today,
                    'notes':                 '',
                })

    # 5. write csv
    out = os.path.join(BASE, 'location_historical_crowd_pattern.csv')
    fields = list(rows_out[0].keys()) if rows_out else []
    with open(out, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows_out)

    print(f'Output: {out}')
    print(f'  Total rows: {len(rows_out):,}')
    from collections import Counter
    lc = Counter(r['crowd_level_label'] for r in rows_out)
    pc = Counter(r['predictability_level'] for r in rows_out)
    rc = sum(1 for r in rows_out if r['recommended_visit_flag'])
    ul = len(set(r['location_id'] for r in rows_out))
    print(f'  locations with data: {ul}')
    print(f'  crowd levels: {dict(lc)}')
    print(f'  predictability: {dict(pc)}')
    print(f'  recommended timeslots: {rc} ({round(rc / len(rows_out) * 100, 1)}%)')


if __name__ == '__main__':
    main()
