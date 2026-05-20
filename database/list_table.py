import mysql.connector

# ========================
# Configuration
# ========================
HOST = "database-fit5120.c9e4kcye2tv8.ap-southeast-2.rds.amazonaws.com"
USER = "admin"
PASSWORD = "0.77deHotCrossBuns6PK"
PORT = 3306
DATABASE = "amity_space"

def get_tables(cursor):
    """Get all table names in the database."""
    cursor.execute("SHOW TABLES")
    tables = [row[0] for row in cursor.fetchall()]
    return tables

def print_table_preview(cursor, table, limit=3):
    """Print the first `limit` rows of a table."""
    print(f"\n{'=' * 60}")
    print(f"Table: {table}  (showing top {limit} rows)")
    print(f"{'=' * 60}")

    try:
        cursor.execute(f"SELECT * FROM `{table}` LIMIT {limit}")
        rows = cursor.fetchall()

        # Get column names from description
        columns = [desc[0] for desc in cursor.description]

        if not rows:
            print(f"  (no data in table)")
            return

        # Print column headers
        col_widths = []
        for i, col in enumerate(columns):
            max_len = len(str(col))
            for row in rows:
                max_len = max(max_len, len(str(row[i])))
            col_widths.append(min(max_len, 40))

        header = " | ".join(str(col).ljust(col_widths[i]) for i, col in enumerate(columns))
        print(header)
        print("-" * len(header))

        # Print rows
        for row in rows:
            line = " | ".join(str(val).ljust(col_widths[i]) for i, val in enumerate(row))
            print(line)

    except mysql.connector.Error as e:
        print(f"  ERROR: {e}")

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

        tables = get_tables(cursor)
        print(f"Found {len(tables)} table(s) in database '{DATABASE}':")

        for table in tables:
            print_table_preview(cursor, table, limit=3)

    except mysql.connector.Error as e:
        print(f"Database connection error: {e}")

    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()
            print("\nDatabase connection closed.")

if __name__ == "__main__":
    main()
