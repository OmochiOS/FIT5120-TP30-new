#!/usr/bin/env python3
"""
import_csv_to_mysql.py
Reads 3 CSV files, processes data following the Java backend logic,
and populates the MySQL database for Amity Space.

Tables:
  - locations              (with description column)
  - sensory_profiles
  - historical_crowd_patterns
  - location_sensor_mappings
"""

import csv
import mysql.connector
from collections import defaultdict

# ========================
# Configuration
# ========================
HOST = "database-fit5120.c9e4kcye2tv8.ap-southeast-2.rds.amazonaws.com"
USER = "admin"
PASSWORD = "0.77deHotCrossBuns6PK"
PORT = 3306
DATABASE = "amity_space"

CSV_STATIC     = "location_profile_static.csv"
CSV_HISTORICAL = "location_historical_crowd_pattern.csv"
CSV_MAPPING    = "location_sensor_mapping.csv"

DEFAULT_DESCRIPTION = (
    "A Melbourne public place where visitors can spend time for "
    "study, relaxation, social activities, or casual exploration."
)

# ========================
# Mapping Functions
# ========================

def map_place_type(raw: str):
    if not raw:
        return None
    n = raw.strip().lower()
    if any(k in n for k in ("outdoor", "park", "garden", "playground", "trail")):
        return "OUTDOOR"
    if any(k in n for k in ("cultural", "library", "museum", "gallery", "theatre")):
        return "CULTURAL"
    if any(k in n for k in ("social", "market", "precinct", "plaza")):
        return "SOCIAL"
    if any(k in n for k in ("active", "sport", "recreation", "gym")):
        return "ACTIVE"
    return None


def map_place_type_from_name(name: str):
    if not name:
        return None
    n = name.strip().lower()
    if any(k in n for k in ("park", "garden", "square", "bridge", "street",
                             "st ", "esplanade", "point")):
        return "OUTDOOR"
    if any(k in n for k in ("library", "museum", "gallery", "theatre")):
        return "CULTURAL"
    if any(k in n for k in ("community", "hub", "market")):
        return "SOCIAL"
    if any(k in n for k in ("bath", "sport", "gym")):
        return "ACTIVE"
    return None


def is_outdoor_by_name(name: str):
    if not name:
        return False
    n = name.strip().lower()
    return any(k in n for k in ("bridge", "park", "garden", "street",
                                 "st ", "footpath", "walk", "square",
                                 "point", "esplanade"))


def map_noise_level_from_db(noise_db):
    if noise_db is None:
        return None
    if noise_db < 45:
        return "LOW"
    if noise_db < 60:
        return "MODERATE"
    if noise_db < 70:
        return "HIGH"
    return "OVERSTIMULATING"


def map_noise_level_from_text(raw: str):
    if not raw:
        return None
    n = raw.strip().lower()
    if n in ("low", "quiet", "calm"):
        return "LOW"
    if n in ("moderate", "medium", "moderate-busy"):
        return "MODERATE"
    if n in ("high", "busy", "loud"):
        return "HIGH"
    if n in ("overstimulating", "very high", "severe"):
        return "OVERSTIMULATING"
    return "MODERATE"


def map_lighting_level_from_text(raw: str):
    if not raw:
        return None
    n = raw.strip().lower()
    if n in ("low", "dim", "dark", "low-natural", "low-natural/indoor"):
        return "LOW"
    if n in ("moderate", "medium", "moderate-natural", "moderate-artificial",
             "moderate-mixed", "mixed-natural"):
        return "MODERATE"
    if n in ("high", "bright", "very bright", "high-natural",
             "high-artificial", "mixed"):
        return "HIGH"
    if n in ("overstimulating", "very high", "extreme"):
        return "OVERSTIMULATING"
    return "MODERATE"


def map_crowd_level_from_count(count):
    if count is None:
        return "MODERATE"
    if count < 50:
        return "LOW"
    if count < 120:
        return "MODERATE"
    return "HIGH"


def build_status_label(crowd_level: str):
    return {
        "LOW": "Calm now",
        "MODERATE": "Moderate now",
        "HIGH": "Busy now",
    }.get(crowd_level, "Status unavailable")

# ========================
# CSV Helpers
# ========================

def safe_float(val):
    if val is None:
        return None
    val = str(val).strip()
    if not val:
        return None
    try:
        return float(val)
    except (ValueError, TypeError):
        return None


def safe_int(val):
    if val is None:
        return None
    val = str(val).strip()
    if not val:
        return None
    try:
        return int(float(val))
    except (ValueError, TypeError):
        return None


def safe_long(val):
    if val is None:
        return None
    val = str(val).strip()
    if not val:
        return None
    try:
        return int(val)
    except (ValueError, TypeError):
        return None


def safe_bool(val):
    if val is None:
        return False
    return str(val).strip().lower() in ("true", "yes", "1")


def safe_text(val):
    if val is None:
        return None
    v = str(val).strip()
    return v if v else None

# ========================
# Database
# ========================

def create_database_and_tables(cursor):
    """
    Drops the old database completely, then re-creates everything fresh.
    This handles any interrupted or partial previous runs.
    """
    cursor.execute("DROP DATABASE IF EXISTS amity_space")
    cursor.execute("CREATE DATABASE amity_space")
    cursor.execute("USE amity_space")

    cursor.execute("""
        CREATE TABLE `locations` (
            `id`            BIGINT  PRIMARY KEY,
            `name`          VARCHAR(255)   NOT NULL,
            `latitude`      DOUBLE         NOT NULL,
            `longitude`     DOUBLE         NOT NULL,
            `crowd_level`   ENUM('LOW','MODERATE','HIGH')
                            NOT NULL DEFAULT 'MODERATE',
            `status_label`  VARCHAR(50)    DEFAULT NULL,
            `place_type`    ENUM('OUTDOOR','CULTURAL','SOCIAL','ACTIVE')
                            DEFAULT NULL,
            `description`   VARCHAR(500)   DEFAULT 'A Melbourne public place where visitors can spend time for study, relaxation, social activities, or casual exploration.'
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    cursor.execute("""
        CREATE TABLE `sensory_profiles` (
            `location_id`             BIGINT  PRIMARY KEY,
            `noise_db`                DOUBLE  DEFAULT NULL,
            `noise_level`             ENUM('LOW','MODERATE','HIGH','OVERSTIMULATING')
                                      DEFAULT NULL,
            `lighting_level`          ENUM('LOW','MODERATE','HIGH','OVERSTIMULATING')
                                      DEFAULT NULL,
            `predictability_level`    ENUM('LOW','MODERATE','HIGH')
                                      NOT NULL DEFAULT 'MODERATE',
            `quiet_space_available`   BOOLEAN DEFAULT FALSE,
            `overall_sensory_score`   VARCHAR(50) DEFAULT 'Data unavailable',
            `best_times_to_visit`     VARCHAR(255) DEFAULT 'Data unavailable',
            `good_to_know_tips`       TEXT,
            `last_updated`            VARCHAR(50) DEFAULT 'Data update time unavailable',
            `summary`                 TEXT,
            CONSTRAINT `fk_profile_location`
                FOREIGN KEY (`location_id`) REFERENCES `locations`(`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    cursor.execute("""
        CREATE TABLE `historical_crowd_patterns` (
            `id`                         BIGINT AUTO_INCREMENT PRIMARY KEY,
            `location_id`                BIGINT  NOT NULL,
            `location_name`              VARCHAR(255) DEFAULT NULL,
            `crowd_sensor_id`            BIGINT  DEFAULT NULL,
            `day_type`                   VARCHAR(20) DEFAULT NULL,
            `day_of_week`                VARCHAR(20) DEFAULT NULL,
            `hour`                       INT       DEFAULT NULL,
            `time_block`                 VARCHAR(50) DEFAULT NULL,
            `avg_crowd_count`            DOUBLE    DEFAULT NULL,
            `min_crowd_count`            INT       DEFAULT NULL,
            `max_crowd_count`            INT       DEFAULT NULL,
            `std_crowd_count`            DOUBLE    DEFAULT NULL,
            `sample_size`                INT       DEFAULT NULL,
            `crowd_level_label`          VARCHAR(20) DEFAULT NULL,
            `predicted_predictability_level` VARCHAR(20) DEFAULT NULL,
            `recommended_visit_flag`     BOOLEAN   DEFAULT FALSE,
            `date_range_start`           VARCHAR(20) DEFAULT NULL,
            `date_range_end`             VARCHAR(20) DEFAULT NULL,
            `updated_at`                 VARCHAR(20) DEFAULT NULL,
            `notes`                      TEXT,
            CONSTRAINT `fk_pattern_location`
                FOREIGN KEY (`location_id`) REFERENCES `locations`(`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    cursor.execute("""
        CREATE TABLE `location_sensor_mappings` (
            `location_id`                BIGINT  PRIMARY KEY,
            `location_name`              VARCHAR(255) DEFAULT NULL,
            `crowd_sensor_location_id`   BIGINT    DEFAULT NULL,
            `crowd_sensor_name`          VARCHAR(255) DEFAULT NULL,
            `crowd_sensor_dist_m`        DOUBLE    DEFAULT NULL,
            `crowd_mapping_confidence`   VARCHAR(20) DEFAULT NULL,
            `noise_sensor_id`            VARCHAR(100) DEFAULT NULL,
            `noise_sensor_name`          VARCHAR(255) DEFAULT NULL,
            `noise_sensor_lat`           DOUBLE    DEFAULT NULL,
            `noise_sensor_lng`           DOUBLE    DEFAULT NULL,
            `noise_sensor_dist_m`        DOUBLE    DEFAULT NULL,
            `noise_mapping_confidence`   VARCHAR(20) DEFAULT NULL,
            `mapping_method`             VARCHAR(50) DEFAULT NULL,
            `notes`                      TEXT,
            CONSTRAINT `fk_mapping_location`
                FOREIGN KEY (`location_id`) REFERENCES `locations`(`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    """)

    print("[DB] Database and tables created (clean state)")

# ========================
# CSV Loading
# ========================

def load_static_profiles():
    profiles = {}
    with open(CSV_STATIC, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            loc_id = safe_long(row.get("id"))
            if loc_id is None:
                continue
            if loc_id not in profiles:
                profiles[loc_id] = {
                    "id":              loc_id,
                    "name":            safe_text(row.get("name")),
                    "latitude":        safe_float(row.get("latitude")),
                    "longitude":       safe_float(row.get("longitude")),
                    "place_type_raw":  safe_text(row.get("place_type")),
                    "quiet_space":     safe_bool(row.get("quiet_space_available")),
                    "overall_sensory": safe_text(row.get("overall_sensory_score")),
                    "good_to_know":    safe_text(row.get("good_to_know_tips")),
                    "last_reviewed":   safe_text(row.get("last_reviewed")),
                    "noise_db":        safe_float(row.get("noise_db")),
                    "noise_level_raw": safe_text(row.get("noise_level")),
                    "lighting_raw":    safe_text(row.get("lighting_level")),
                    "data_source":     safe_text(row.get("data_source")),
                }
    print(f"[Static Profile] Loaded {len(profiles)} unique locations")
    return profiles


def load_historical_patterns():
    patterns = defaultdict(list)
    with open(CSV_HISTORICAL, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            loc_id = safe_long(row.get("location_id"))
            if loc_id is None:
                continue
            patterns[loc_id].append({
                "location_name":          safe_text(row.get("location_name")),
                "crowd_sensor_id":        safe_long(row.get("crowd_sensor_id")),
                "day_type":               safe_text(row.get("day_type")),
                "day_of_week":            safe_text(row.get("day_of_week")),
                "hour":                   safe_int(row.get("hour")),
                "time_block":             safe_text(row.get("time_block")),
                "avg_crowd_count":        safe_float(row.get("avg_crowd_count")),
                "min_crowd_count":        safe_int(row.get("min_crowd_count")),
                "max_crowd_count":        safe_int(row.get("max_crowd_count")),
                "std_crowd_count":        safe_float(row.get("std_crowd_count")),
                "sample_size":            safe_int(row.get("sample_size")),
                "crowd_level_label":      safe_text(row.get("crowd_level_label")),
                "predictability_level":   safe_text(row.get("predictability_level")),
                "recommended_visit_flag": safe_bool(row.get("recommended_visit_flag")),
                "date_range_start":       safe_text(row.get("date_range_start")),
                "date_range_end":         safe_text(row.get("date_range_end")),
                "updated_at":             safe_text(row.get("updated_at")),
                "notes":                  safe_text(row.get("notes")),
            })
    print(f"[Historical Pattern] Loaded patterns for {len(patterns)} locations")
    return patterns


def load_sensor_mappings():
    mappings = {}
    with open(CSV_MAPPING, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            loc_id = safe_long(row.get("location_id"))
            if loc_id is None:
                continue
            if loc_id not in mappings:
                mappings[loc_id] = {
                    "location_id":                loc_id,
                    "location_name":              safe_text(row.get("location_name")),
                    "crowd_sensor_location_id":   safe_long(row.get("crowd_sensor_location_id")),
                    "crowd_sensor_name":          safe_text(row.get("crowd_sensor_name")),
                    "crowd_sensor_dist_m":        safe_float(row.get("crowd_sensor_dist_m")),
                    "crowd_mapping_confidence":   safe_text(row.get("crowd_mapping_confidence")),
                    "noise_sensor_id":            safe_text(row.get("noise_sensor_id")),
                    "noise_sensor_name":          safe_text(row.get("noise_sensor_name")),
                    "noise_sensor_lat":           safe_float(row.get("noise_sensor_lat")),
                    "noise_sensor_lng":           safe_float(row.get("noise_sensor_lng")),
                    "noise_sensor_dist_m":        safe_float(row.get("noise_sensor_dist_m")),
                    "noise_mapping_confidence":   safe_text(row.get("noise_mapping_confidence")),
                    "mapping_method":             safe_text(row.get("mapping_method")),
                    "notes":                      safe_text(row.get("notes")),
                }
    print(f"[Sensor Mapping] Loaded {len(mappings)} unique mappings")
    return mappings

# ========================
# Value Computation
# ========================

def compute_values(profile, loc_id, patterns_by_id):
    pt = map_place_type(profile.get("place_type_raw"))
    if pt is None:
        pt = map_place_type_from_name(profile.get("name"))

    crowd_pats = patterns_by_id.get(loc_id, [])
    max_avg = None
    max_range = 0
    for p in crowd_pats:
        avg = p["avg_crowd_count"]
        if avg is not None:
            if max_avg is None or avg > max_avg:
                max_avg = avg
        mn = p["min_crowd_count"] or 0
        mx = p["max_crowd_count"] or 0
        r = mx - mn
        if r > max_range:
            max_range = r

    crowd_level = map_crowd_level_from_count(
        int(max_avg) if max_avg else None
    )

    best_time = "Data unavailable"
    for p in crowd_pats:
        if p["recommended_visit_flag"]:
            tb = p["time_block"]
            if tb:
                best_time = f"Weekday mornings ({tb})"
                break

    last_updated = profile.get("last_reviewed")
    if not last_updated and crowd_pats:
        for p in crowd_pats:
            if p["updated_at"]:
                last_updated = p["updated_at"]
                break
    if not last_updated:
        last_updated = "Data update time unavailable"

    noise_db = profile.get("noise_db")

    if profile.get("noise_level_raw"):
        noise_level = map_noise_level_from_text(profile["noise_level_raw"])
    elif noise_db is not None:
        noise_level = map_noise_level_from_db(noise_db)
    else:
        noise_level = "MODERATE"

    noise_score = {
        "LOW": 0, "MODERATE": 0.5,
        "HIGH": 0.75, "OVERSTIMULATING": 1,
    }.get(noise_level, 0.5)

    is_outdoor = (pt == "OUTDOOR") or is_outdoor_by_name(profile.get("name"))
    lighting_raw = profile.get("lighting_raw")

    if is_outdoor and lighting_raw:
        lighting_level = map_lighting_level_from_text(lighting_raw)
    else:
        lighting_level = None

    if lighting_level:
        lighting_score = {
            "LOW": 0, "MODERATE": 0.5,
            "HIGH": 0.75, "OVERSTIMULATING": 1,
        }.get(lighting_level, 0.5)
    else:
        lighting_score = 0.5

    quiet_space = profile.get("quiet_space", False)
    quiet_space_bad = 0.0 if quiet_space else 1.0

    if max_range < 20:
        unpredictability_score = 0.0
    elif max_range < 60:
        unpredictability_score = 0.5
    else:
        unpredictability_score = 1.0

    # YANYU FORMULA
    overall = (
        noise_score * 0.35
        + unpredictability_score * 0.30
        + lighting_score * 0.20
        + quiet_space_bad * 0.15
    )

    if overall < 0.75:
        predicted_predictability = "HIGH"
    elif overall < 1.75:
        predicted_predictability = "MODERATE"
    else:
        predicted_predictability = "LOW"

    overall_sensory_raw = profile.get("overall_sensory")
    if not overall_sensory_raw:
        overall_sensory_raw = "Moderate"

    parts = []
    parts.append(f"Overall sensory score: {overall_sensory_raw}")
    if noise_level:
        parts.append(f"Noise level: {noise_level}")
    if lighting_level:
        parts.append(f"Lighting: {lighting_level}")
    else:
        parts.append("Indoor location (lighting data N/A)")
    parts.append(f"Predictability: {predicted_predictability}")
    if quiet_space:
        parts.append("Quiet space available")
    summary = ". ".join(parts) + "."

    good_to_know = profile.get("good_to_know")
    if not good_to_know:
        good_to_know = "Data unavailable"

    return {
        "location_row": {
            "id":            loc_id,
            "name":          profile["name"],
            "latitude":      profile["latitude"],
            "longitude":     profile["longitude"],
            "crowd_level":   crowd_level,
            "status_label":  build_status_label(crowd_level),
            "place_type":    pt,
        },
        "sensory_row": {
            "location_id":            loc_id,
            "noise_db":               noise_db,
            "noise_level":            noise_level,
            "lighting_level":         lighting_level,
            "predictability_level":   predicted_predictability,
            "quiet_space_available":  quiet_space,
            "overall_sensory_score":  overall_sensory_raw,
            "best_times_to_visit":    best_time,
            "good_to_know_tips":      good_to_know,
            "last_updated":           last_updated,
            "summary":                summary,
        },
    }

# ========================
# DB Inserts
# ========================

def insert_locations(cursor, computed_data_list):
    """
    Now inserts 8 columns including description.
    description uses the default value for all rows (can be customised later
    per-location if needed).
    """
    stmt = (
        "INSERT INTO locations "
        "(id, name, latitude, longitude, "
        "crowd_level, status_label, place_type, description) "
        "VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
    )
    rows = []
    for data in computed_data_list:
        loc = data["location_row"]
        rows.append((
            loc["id"],
            loc["name"],
            loc["latitude"],
            loc["longitude"],
            loc["crowd_level"],
            loc["status_label"],
            loc["place_type"],
            DEFAULT_DESCRIPTION,
        ))
    cursor.executemany(stmt, rows)
    print(f"[DB] Inserted {len(rows)} locations")


def insert_sensory_profiles(cursor, computed_data_list):
    stmt = (
        "INSERT INTO sensory_profiles "
        "(location_id, noise_db, noise_level, lighting_level, "
        "predictability_level, quiet_space_available, "
        "overall_sensory_score, best_times_to_visit, "
        "good_to_know_tips, last_updated, summary) "
        "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)"
    )
    rows = []
    for data in computed_data_list:
        sp = data["sensory_row"]
        rows.append((
            sp["location_id"],
            sp["noise_db"],
            sp["noise_level"],
            sp["lighting_level"],
            sp["predictability_level"],
            sp["quiet_space_available"],
            sp["overall_sensory_score"],
            sp["best_times_to_visit"],
            sp["good_to_know_tips"],
            sp["last_updated"],
            sp["summary"],
        ))
    cursor.executemany(stmt, rows)
    print(f"[DB] Inserted {len(rows)} sensory profiles")


def insert_historical_patterns(cursor, patterns_by_id, valid_location_ids):
    stmt = (
        "INSERT INTO historical_crowd_patterns "
        "(location_id, location_name, crowd_sensor_id, "
        "day_type, day_of_week, hour, time_block, "
        "avg_crowd_count, min_crowd_count, max_crowd_count, "
        "std_crowd_count, sample_size, crowd_level_label, "
        "predicted_predictability_level, recommended_visit_flag, "
        "date_range_start, date_range_end, updated_at, notes) "
        "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)"
    )
    rows = []
    for loc_id, pats in patterns_by_id.items():
        if loc_id not in valid_location_ids:
            continue
        for p in pats:
            rows.append((
                loc_id,
                p["location_name"],
                p["crowd_sensor_id"],
                p["day_type"],
                p["day_of_week"],
                p["hour"],
                p["time_block"],
                p["avg_crowd_count"],
                p["min_crowd_count"],
                p["max_crowd_count"],
                p["std_crowd_count"],
                p["sample_size"],
                p["crowd_level_label"],
                p["predictability_level"],
                p["recommended_visit_flag"],
                p["date_range_start"],
                p["date_range_end"],
                p["updated_at"],
                p["notes"],
            ))
    if not rows:
        print("[DB] No historical patterns matching inserted locations")
        return
    cursor.executemany(stmt, rows)
    print(f"[DB] Inserted {len(rows)} historical crowd pattern records")


def insert_sensor_mappings(cursor, mappings_by_id, valid_location_ids):
    stmt = (
        "INSERT INTO location_sensor_mappings "
        "(location_id, location_name, crowd_sensor_location_id, "
        "crowd_sensor_name, crowd_sensor_dist_m, crowd_mapping_confidence, "
        "noise_sensor_id, noise_sensor_name, "
        "noise_sensor_lat, noise_sensor_lng, "
        "noise_sensor_dist_m, noise_mapping_confidence, "
        "mapping_method, notes) "
        "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)"
    )
    rows = []
    for loc_id, m in mappings_by_id.items():
        if loc_id not in valid_location_ids:
            continue
        rows.append((
            m["location_id"],
            m["location_name"],
            m["crowd_sensor_location_id"],
            m["crowd_sensor_name"],
            m["crowd_sensor_dist_m"],
            m["crowd_mapping_confidence"],
            m["noise_sensor_id"],
            m["noise_sensor_name"],
            m["noise_sensor_lat"],
            m["noise_sensor_lng"],
            m["noise_sensor_dist_m"],
            m["noise_mapping_confidence"],
            m["mapping_method"],
            m["notes"],
        ))
    if not rows:
        print("[DB] No sensor mappings matching inserted locations")
        return
    cursor.executemany(stmt, rows)
    print(f"[DB] Inserted {len(rows)} sensor mappings")

# ========================
# Summary
# ========================

def print_summary(cursor):
    print(f"\n{'=' * 50}")
    print("IMPORT SUMMARY")
    print(f"{'=' * 50}")

    tables = [
        ("Locations",         "SELECT COUNT(*) FROM locations"),
        ("Sensory Profiles",  "SELECT COUNT(*) FROM sensory_profiles"),
        ("Crowd Patterns",    "SELECT COUNT(*) FROM historical_crowd_patterns"),
        ("Sensor Mappings",   "SELECT COUNT(*) FROM location_sensor_mappings"),
    ]
    for label, sql in tables:
        cursor.execute(sql)
        print(f"  {label:18s} {cursor.fetchone()[0]}")

    cursor.execute(
        "SELECT crowd_level, COUNT(*) FROM locations "
        "GROUP BY crowd_level ORDER BY crowd_level"
    )
    print("\n  Crowd Level Distribution:")
    for row in cursor.fetchall():
        print(f"    {row[0]}: {row[1]}")

    cursor.execute(
        "SELECT noise_level, COUNT(*) FROM sensory_profiles "
        "GROUP BY noise_level ORDER BY noise_level"
    )
    print("\n  Noise Level Distribution:")
    for row in cursor.fetchall():
        print(f"    {row[0] or '(null)'}: {row[1]}")

    cursor.execute(
        "SELECT predictability_level, COUNT(*) FROM sensory_profiles "
        "GROUP BY predictability_level ORDER BY predictability_level"
    )
    print("\n  Predictability Level Distribution:")
    for row in cursor.fetchall():
        print(f"    {row[0]}: {row[1]}")

    cursor.execute(
        "SELECT place_type, COUNT(*) FROM locations "
        "GROUP BY place_type ORDER BY place_type"
    )
    print("\n  Place Type Distribution:")
    for row in cursor.fetchall():
        print(f"    {row[0] or '(null)'}: {row[1]}")

    print(f"{'=' * 50}")

# ========================
# Main
# ========================

def main():
    print("=" * 60)
    print("Amity Space CSV -> MySQL Import")
    print("=" * 60)

    print("\n--- Loading CSV files ---")
    static_profiles = load_static_profiles()
    patterns_by_id = load_historical_patterns()
    mappings_by_id = load_sensor_mappings()

    print(f"\n--- Connecting to MySQL ---")
    print(f"  Host: {HOST}")
    print(f"  Database: {DATABASE}")

    conn = mysql.connector.connect(
        host=HOST,
        user=USER,
        password=PASSWORD,
        port=PORT,
    )
    cursor = conn.cursor()

    try:
        print("\n--- Dropping old database & creating fresh tables ---")
        create_database_and_tables(cursor)
        conn.commit()

        print("\n--- Computing derived values ---")
        computed_data_list = []
        valid_location_ids = set()

        for loc_id in sorted(static_profiles.keys()):
            profile = static_profiles[loc_id]
            result = compute_values(profile, loc_id, patterns_by_id)
            computed_data_list.append(result)
            valid_location_ids.add(loc_id)

        print(f"  Computed data for {len(computed_data_list)} locations")

        print("\n--- Inserting data ---")
        insert_locations(cursor, computed_data_list)
        insert_sensory_profiles(cursor, computed_data_list)
        insert_historical_patterns(cursor, patterns_by_id, valid_location_ids)
        insert_sensor_mappings(cursor, mappings_by_id, valid_location_ids)
        conn.commit()

        print_summary(cursor)
        print("\nImport completed successfully.")

    except Exception as e:
        print(f"\nERROR: {e}")
        raise
    finally:
        cursor.close()
        conn.close()
        print("\nConnection closed.")


if __name__ == "__main__":
    main()
