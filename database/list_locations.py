import csv
import mysql.connector

# ========================
# Configuration
# ========================
HOST = "database-fit5120.c9e4kcye2tv8.ap-southeast-2.rds.amazonaws.com"
USER = "admin"
PASSWORD = "0.77deHotCrossBuns6PK"
PORT = 3306
DATABASE = "amity_space"

def print_entire_table(cursor, table):
    """Print all rows of a table."""
    print(f"\n{'=' * 60}")
    print(f"Table: {table}")
    print(f"{'=' * 60}")

    try:
        cursor.execute(f"SELECT * FROM `{table}`")
        rows = cursor.fetchall()

        columns = [desc[0] for desc in cursor.description]

        if not rows:
            print("  (no data)")
            return len(rows)

        # Compute column widths
        col_widths = []
        for i, col in enumerate(columns):
            max_len = len(str(col))
            for row in rows:
                max_len = max(max_len, len(str(row[i])))
            col_widths.append(min(max_len, 60))

        # Print header
        header = " | ".join(str(col).ljust(col_widths[i]) for i, col in enumerate(columns))
        print(header)
        print("-" * len(header))

        # Print all rows
        for row in rows:
            line = " | ".join(str(val).ljust(col_widths[i]) for i, val in enumerate(row))
            print(line)

        return len(rows)

    except mysql.connector.Error as e:
        print(f"  ERROR: {e}")
        return 0

def export_to_csv(cursor, table, filename):
    """Export table to CSV file."""
    try:
        cursor.execute(f"SELECT * FROM `{table}`")
        rows = cursor.fetchall()
        columns = [desc[0] for desc in cursor.description]

        with open(filename, "w", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            writer.writerow(columns)
            writer.writerows(rows)
        print(f"\nCSV exported to: {filename}")
    except mysql.connector.Error as e:
        print(f"CSV export ERROR: {e}")

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

        total = print_entire_table(cursor, "locations")
        print(f"\nTotal rows: {total}")

        # Optional: export to CSV
        export_to_csv(cursor, "locations", "locations.csv")

    except mysql.connector.Error as e:
        print(f"Database connection error: {e}")

    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()
            print("Database connection closed.")

if __name__ == "__main__":
    main()
