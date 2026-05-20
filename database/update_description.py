import requests
from openai import OpenAI
import mysql.connector
import json
import os
import sys

# ========================
# Configuration
# ========================
BRAVE_API_KEY = "BSAZL4pizyoivTj62rYk01PCkhs7Wxw"
LM_STUDIO_URL = "http://127.0.0.1:1234/v1"
LM_MODEL_NAME = "qwen/qwen3.6-35b-a3b"

DB_HOST = "database-fit5120.c9e4kcye2tv8.ap-southeast-2.rds.amazonaws.com"
DB_USER = "admin"
DB_PASSWORD = "0.77deHotCrossBuns6PK"
DB_PORT = 3306
DB_NAME = "amity_space"

CACHE_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "brave_cache")
os.makedirs(CACHE_DIR, exist_ok=True)

MAX_RETRIES = 3
# ==========================================


def _sanitize_cache_key(name: str) -> str:
    safe = "".join(c if c.isalnum() or c in " -_" else "_" for c in name)
    safe = safe.strip("_- ")[:200]
    if not safe:
        safe = "unknown"
    return safe


# ---------- Brave Search ----------

def fetch_brave_snippets(location_query: str) -> str:
    cache_key = _sanitize_cache_key(location_query)
    cache_file = os.path.join(CACHE_DIR, cache_key + ".json")

    if os.path.exists(cache_file):
        try:
            with open(cache_file, "r", encoding="utf-8") as f:
                cached = json.load(f)
                return cached["raw_text"]
        except Exception:
            pass

    url = "https://api.search.brave.com/res/v1/web/search"
    headers = {
        "Accept": "application/json",
        "Accept-Encoding": "gzip",
        "X-Subscription-Token": BRAVE_API_KEY,
    }
    params = {
        "q": f"{location_query} Melbourne Australia",
        "search_lang": "en",
        "country": "AU",
        "count": 5,
        "extra_snippets": True,
        "freshness": "py",
    }

    snippets = []
    for attempt in range(1, MAX_RETRIES + 1):
        try:
            response = requests.get(url, headers=headers, params=params, timeout=15)
            response.raise_for_status()
            data = response.json()
            web_results = data.get("web", {}).get("results", [])
            for res in web_results:
                title = res.get("title", "")
                desc = res.get("description", "")
                extras = res.get("extra_snippets", [])
                snippet_text = f"[Source] {title}\n[Main] {desc}\n[Details] {' | '.join(extras[:3])}"
                snippets.append(snippet_text)
            break
        except Exception as e:
            print(f"    ⚠ Brave API request (retry {attempt}/{MAX_RETRIES}): {e}")

    raw_text = "\n\n---\n\n".join(snippets) if snippets else ""

    cache_data = {
        "query": location_query,
        "result_count": len(snippets),
        "raw_text": raw_text,
    }
    try:
        with open(cache_file, "w", encoding="utf-8") as f:
            json.dump(cache_data, f, ensure_ascii=False, indent=2)
    except Exception as e:
        print(f"    ⚠ Cache write failed: {e}")

    return raw_text


# ---------- LLM genarate ----------

def generate_description(context: str, location: str) -> str:
    client = OpenAI(base_url=LM_STUDIO_URL, api_key="lm-studio")

    system_prompt = """You are a precise location description assistant.
All locations you are asked about are in Melbourne, Victoria, Australia.
Your task is to output EXACTLY ONE sentence in English describing the given location based on the provided search snippets.
STRICT RULES:
1. Output ONLY the single sentence. No greetings, no explanations, no markdown formatting.
2. Focus on what the place is, its primary function, or its cultural/historical significance within Melbourne.
3. If the context is insufficient or contradictory, output exactly: "A reliable description could not be generated from the provided data."
4. Keep it factual and under 25 words."""

    user_prompt = f"""Location: {location}
Search Context:
{context}

Generate the one-sentence description:"""

    for attempt in range(1, MAX_RETRIES + 1):
        try:
            response = client.chat.completions.create(
                model=LM_MODEL_NAME,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt},
                ],
                temperature=0.1,
                max_tokens=100,
                top_p=0.95,
            )
            raw_output = response.choices[0].message.content.strip()
            return raw_output.strip('"').strip("'").strip()
        except Exception as e:
            print(f"    ⚠ LLM request failed (retry {attempt}/{MAX_RETRIES}): {e}")

    return "[LLM ERROR] Failed to generate description after multiple retries."


# ---------- Database opteration ----------

def get_all_locations(cursor):
    cursor.execute("SELECT `id`, `name` FROM `locations` ORDER BY `id`")
    return cursor.fetchall()


def update_description(connection, location_id: int, name: str, new_desc: str) -> bool:
    try:
        with connection.cursor() as cursor:
            cursor.execute(
                "UPDATE `locations` SET `description` = %s WHERE `id` = %s",
                (new_desc, location_id),
            )
            connection.commit()
            if cursor.rowcount > 0:
                return True
            else:
                return False
    except mysql.connector.Error as e:
        print(f"    ❌ Update failed: {e}")
        connection.rollback()
        return False


# ---------- Main ----------

def main():
    print("=" * 60)
    print("🚀 Start updating locations.description")
    print("=" * 60)

    connection = mysql.connector.connect(
        host=DB_HOST, user=DB_USER, password=DB_PASSWORD,
        port=DB_PORT, database=DB_NAME,
    )
    cursor = connection.cursor()

    all_rows = get_all_locations(cursor)
    total = len(all_rows)
    print(f"\n📊 Find {total} records to process\n")
    if total == 0:
        print("❌ No records, exit")
        cursor.close()
        connection.close()
        return

    processed_names: dict[str, str] = {}

    success_count = 0
    skip_count = 0
    fail_count = 0

    try:
        for idx, (location_id, location_name) in enumerate(all_rows, start=1):

            print("-" * 50)
            print(f"[{idx}/{total}] id={location_id} | name='{location_name}'")

            if location_name in processed_names:
                description = processed_names[location_name]
                reuse_flag = "✓ (复用已有描述)"
                print(f"  [SKIP] {reuse_flag}")
                print(f"  [LLM] > {description}")
            else:
                reuse_flag = ""

                context = fetch_brave_snippets(location_name)
                if not context:
                    print(f"  ⚠ No results")
                    fail_count += 1
                    continue

                description = generate_description(context, location_name)

                print(f"  [LLM] > {description}")

                processed_names[location_name] = description

            success = update_description(connection, location_id, location_name, description)
            if success:
                print(f"  [DB]  ✅ Uodate success {reuse_flag}")
                success_count += 1
            else:
                print(f"  [DB]  ❌ Update fail {reuse_flag}")
                fail_count += 1

    except KeyboardInterrupt:
        print("\n\n⚠️ User interpret, stopping...")
    except Exception as e:
        print(f"\n\n❌ Error: {e}")
    finally:
        # ---------- 最终统计 ----------
        print("\n" + "=" * 60)
        print("📈 Done")
        print("=" * 60)
        print(f"  Total records:      {total}")
        print(f"  ✅ Success:   {success_count}")
        print(f"  ⏩ Skip:  {skip_count}")
        print(f"  ❌ Fail:       {fail_count}")
        print(f"  🗂️  Different locations:  {len(processed_names)}")
        print("=" * 60)

        cursor.close()
        connection.close()
        print("\nDatabase disconnected.")


if __name__ == "__main__":
    main()
