"""
RAG Step 8: LLM Call
Generates answers using DeepSeek API
"""
from openai import OpenAI


def generate_answer(prompt, api_key):
    """
    Generate answer using DeepSeek API.
    
    Args:
        prompt (str): Formatted prompt with context
        api_key (str): DeepSeek API key
        
    Returns:
        str: Generated answer from LLM
    """
    print("\n" + "=" * 60)
    print("STEP 8: Generate Answer with LLM")
    print("=" * 60)
    
    # Initialize OpenAI client with DeepSeek endpoint
    client = OpenAI(
        api_key=api_key,
        base_url="https://api.deepseek.com",
        timeout=60,
    )
    
    print("\nSending request to DeepSeek...")
    print(f"  - Prompt length: {len(prompt)} characters")
    
    try:
        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=[
                {"role": "user", "content": prompt}
            ],
            temperature=0.3,
            max_tokens=300,
        )
        
        answer = response.choices[0].message.content
        
        print("✓ Answer generated successfully")
        print(f"  - Response length: {len(answer)} characters")
        
        return answer
        
    except Exception as e:
        error_msg = f"Error calling DeepSeek API: {e}"
        print(f"✗ {error_msg}")
        return error_msg
