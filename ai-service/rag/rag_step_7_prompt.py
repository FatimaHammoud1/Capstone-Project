"""
RAG Step 7: Prompt Preparation
Prepares prompts for LLM with retrieved context
"""

def prepare_prompt(query, retrieved_chunks):
    """
    Prepare a prompt in Arabic with retrieved context.
    
    Args:
        query (str): User's question
        retrieved_chunks (list): List of relevant document chunks
        
    Returns:
        str: Formatted prompt in Arabic
    """
    print("\n" + "=" * 60)
    print("STEP 7: Prepare Prompt")
    print("=" * 60)
    
    # Build context from retrieved chunks
    context = ""
    for i, chunk in enumerate(retrieved_chunks):
        context += f"\n\n[السياق {i+1}]:\n{chunk}"
    
    # Create prompt in Arabic
    prompt = f"""
أنت مساعد ذكاء اصطناعي متخصص في تحليل سمات الشخصية واقتراح المهن المناسبة.

استخدم المعلومات الموجودة في السياق فقط للإجابة.

السياق:
{context}

السؤال: {query}

المطلوب:
1. قدّم شرحًا مختصرًا جدًا لكل سمة شخصية مذكورة (سطر واحد لكل سمة).
2. اقترح ٥ إلى ٦ مهن مناسبة بناءً على السمات فقط.
3. لا تتجاوز الإجابة ٢٠٠ كلمة.
4. الإجابة بالعربية الفصحى المختصرة.

الإجابة:
"""
    
    print(f"Prompt prepared ({len(prompt)} characters)")
    return prompt
