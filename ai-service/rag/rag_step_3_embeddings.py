"""
RAG Step 3: Text Embeddings
Converts text to vector embeddings using sentence-transformers
"""
from sentence_transformers import SentenceTransformer
import numpy as np

_model = None

def get_embedder(model_name="all-MiniLM-L6-v2"):
    """
    Get or create embedding model (singleton pattern).
    
    Args:
        model_name (str): Name of the sentence transformer model
        
    Returns:
        SentenceTransformer: Embedding model
    """
    global _model
    if _model is None:
        _model = SentenceTransformer(model_name)
    return _model


def embed_texts(texts):
    """
    Convert texts to vector embeddings.
    
    Args:
        texts (list): List of text strings
        
    Returns:
        numpy.ndarray: Array of embeddings
    """
    print("\n" + "=" * 60)
    print("STEP 3: Creating Embeddings")
    print("=" * 60)

    model = get_embedder()

    embeddings = model.encode(
        texts,
        show_progress_bar=True,
        batch_size=32
    )
    
    print(f"✓ Embeddings created")
    print(f"  - Shape: {embeddings.shape}")
    print(f"  - Dimension: {embeddings.shape[1]}")
    
    return embeddings
