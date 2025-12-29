import json
import os

# Configuration
SQL_DIR = r"c:\Users\35742\Desktop\workfile\TrainingWeek\sql"
SCHEMA_FILE = os.path.join(SQL_DIR, "schema.sql")

FILES_TO_TABLES = {
    "CET4.json": "cet4_word",
    "CET6.json": "cet6_word",
    "GEE.json": "graduate_word"
}

def escape_sql(val):
    if val is None:
        return "NULL"
    # Escape single quotes and backslashes
    return "'" + str(val).replace("\\", "\\\\").replace("'", "''") + "'"

def generate_inserts(json_file, table_name):
    path = os.path.join(SQL_DIR, json_file)
    if not os.path.exists(path):
        print(f"File not found: {path}")
        return ""

    try:
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except Exception as e:
        print(f"Error reading {json_file}: {e}")
        return ""

    sql_parts = []
    sql_parts.append(f"-- Data for {table_name}")
    
    # Batch inserts to avoid huge lines
    batch_size = 100
    for i in range(0, len(data), batch_size):
        batch = data[i:i+batch_size]
        values_list = []
        for item in batch:
            word = escape_sql(item.get("word", ""))
            phonetic = escape_sql(item.get("accent", ""))
            translate = escape_sql(item.get("mean_cn", ""))
            definition_en = escape_sql(item.get("mean_en", ""))
            example_en = escape_sql(item.get("sentence", ""))
            example_cn = escape_sql(item.get("sentence_trans", ""))
            mnemonic = escape_sql(item.get("word_etyma", ""))
            cloze = escape_sql(item.get("cloze", ""))
            
            # (word, phonetic, translate, definition_en, example_en, example_cn, mnemonic, cloze)
            val_str = f"({word}, {phonetic}, {translate}, {definition_en}, {example_en}, {example_cn}, {mnemonic}, {cloze})"
            values_list.append(val_str)
        
        if values_list:
            sql = f"INSERT INTO `{table_name}` (`word`, `phonetic`, `translate`, `definition_en`, `example_en`, `example_cn`, `mnemonic`, `cloze`) VALUES\n" + ",\n".join(values_list) + ";"
            sql_parts.append(sql)
            
    return "\n\n".join(sql_parts)

def main():
    all_sql = []
    
    # First, let's verify if schema.sql exists and maybe clear previous inserts?
    # The user asked to "rewrite them into the database".
    # We should append to the existing schema definition.
    # Assuming schema.sql currently has CREATE TABLE statements and maybe some sample data.
    # We will append the new data at the end.
    
    for json_file, table_name in FILES_TO_TABLES.items():
        print(f"Processing {json_file} -> {table_name}...")
        inserts = generate_inserts(json_file, table_name)
        if inserts:
            all_sql.append(inserts)
    
    final_sql = "\n\n".join(all_sql)
    
    if final_sql:
        with open(SCHEMA_FILE, 'a', encoding='utf-8') as f:
            f.write("\n\n" + final_sql)
        print("Successfully appended data to schema.sql")
    else:
        print("No data generated.")

if __name__ == "__main__":
    main()
