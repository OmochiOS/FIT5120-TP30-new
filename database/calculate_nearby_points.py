import mysql.connector
import math

# ========================
# Configuration
# ========================
HOST = "database-fit5120.c9e4kcye2tv8.ap-southeast-2.rds.amazonaws.com"
USER = "admin"
PASSWORD = "0.77deHotCrossBuns6PK"
PORT = 3306
DATABASE = "amity_space"

def haversine(lat1, lon1, lat2, lon2):
    """Calculate the great-circle distance between two points on Earth (in meters)."""
    R = 6_371_000  # Earth radius in meters
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlam = math.radians(lon2 - lon1)

    a = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlam / 2) ** 2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    return R * c


def create_table(cursor):
    """Drop and recreate the location_nearby_points table."""
    cursor.execute("DROP TABLE IF EXISTS `location_nearby_points`")
    cursor.execute("""
        CREATE TABLE `location_nearby_points` (
            `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
            `source_location_id` BIGINT NOT NULL,
            `nearby_location_id` BIGINT NOT NULL,
            `distance_meters` DOUBLE NOT NULL
        )
    """)


def get_locations(cursor):
    """Fetch all locations with id, latitude, longitude."""
    cursor.execute("SELECT `id`, `latitude`, `longitude` FROM `locations`")
    return cursor.fetchall()


def build_nearby_pairs(locations, max_distance=200.0):
    """Compute pairwise distances and return pairs within max_distance."""
    pairs = []
    n = len(locations)
    for i in range(n):
        id_i, lat_i, lon_i = locations[i]
        for j in range(i + 1, n):
            id_j, lat_j, lon_j = locations[j]
            dist = haversine(lat_i, lon_i, lat_j, lon_j)
            if dist <= max_distance:
                # Insert both directions (i→j and j→i)
                pairs.append((id_i, id_j, round(dist, 2)))
                pairs.append((id_j, id_i, round(dist, 2)))
    return pairs


def insert_pairs(cursor, pairs):
    """Batch-insert nearby pairs into the table."""
    cursor.executemany(
        "INSERT INTO `location_nearby_points` (`source_location_id`, `nearby_location_id`, `distance_meters`) VALUES (%s, %s, %s)",
        pairs
    )


def main():
    connection = None
    try:
        connection = mysql.connector.connect(
            host=HOST,
            user=USER,
            password=PASSWORD,
            port=PORT,
            database=DATABASE
        )
        cursor = connection.cursor()

        # Step 1: create (or recreate) the table
        print("Creating table `location_nearby_points`...")
        create_table(cursor)
        connection.commit()

        # Step 2: fetch all locations
        locations = get_locations(cursor)
        print(f"Fetched {len(locations)} location(s).")

        # Step 3: compute nearby pairs (distance <= 200 m)
        pairs = build_nearby_pairs(locations, max_distance=200.0)
        print(f"Found {len(pairs)} nearby pair record(s) (distance ≤ 200 m).")

        # Step 4: insert into table
        if pairs:
            insert_pairs(cursor, pairs)
            connection.commit()

        # Step 5: verify
        cursor.execute("SELECT COUNT(*) FROM `location_nearby_points`")
        count = cursor.fetchone()[0]
        print(f"Table `location_nearby_points` now contains {count} row(s).")

        # Preview first few rows
        cursor.execute("SELECT * FROM `location_nearby_points` LIMIT 5")
        rows = cursor.fetchall()
        columns = [desc[0] for desc in cursor.description]
        print(f"\nPreview (top 5 rows): {' | '.join(columns)}")
        for row in rows:
            print("  " + " | ".join(str(v) for v in row))

    except mysql.connector.Error as e:
        print(f"Database error: {e}")

    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()
            print("\nDatabase connection closed.")


if __name__ == "__main__":
    main()
