import requests
import re
import json

# --- 配置 ---
# 使用 jsDelivr CDN 加速访问 Github 仓库的静态资源
BASE_URL = "https://cdn.jsdelivr.net/gh/lyc8503/baicizhan-word-meaning-API/data/words"

def get_baicizhan_meaning(word: str):
    """
    根据文档规则获取百词斩单词释义
    """
    print(f"正在查询单词: {word} ...")

    # 1. 【核心逻辑】处理特殊字符 (参考文档提供的 Python 逻辑)
    # 将 / \ : * ? " < > | 替换为下划线
    rstr = r"[\/\\\:\*\?\"\<\>\|]" 
    safe_word = re.sub(rstr, "_", word)
    
    # 2. 处理空格 (将短语中的空格替换为下划线)
    safe_word = safe_word.replace(" ", "_")
    
    # 3. 拼接 URL
    target_url = f"{BASE_URL}/{safe_word}.json"
    print(f"请求地址: {target_url}")

    try:
        # 4. 发起请求
        response = requests.get(target_url, timeout=5)
        
        # 检查状态码
        if response.status_code == 200:
            data = response.json()
            return data
        elif response.status_code == 404:
            return {"error": "未找到该单词，可能未收录。"}
        else:
            return {"error": f"请求失败，状态码: {response.status_code}"}
            
    except Exception as e:
        return {"error": f"发生异常: {str(e)}"}

# --- 测试运行 ---
if __name__ == "__main__":
    # 测试 1: 普通单词
    word_to_test = "average"
    result = get_baicizhan_meaning(word_to_test)
    
    # 打印结果
    print("-" * 30)
    if "error" not in result:
        print(f"【单词】: {result.get('word')}")
        print(f"【音标】: {result.get('accent')}")
        print(f"【中文】: {result.get('mean_cn')}")
        print(f"【英文】: {result.get('mean_en')}")
        print(f"【例句】: {result.get('sentence')}")
        print(f"【翻译】: {result.get('sentence_trans')}")
    else:
        print(result["error"])
    print("-" * 30)

    # 测试 2: 带空格的短语 (如果有收录的话，例如 a lot of -> a_lot_of)
    # 你可以尝试修改这里的单词进行测试