"""
RAG Step 6: Similarity Search
Retrieves relevant chunks from vector database
"""

def retrieve_relevant_chunks(query_embedding, collection, top_k=3):
    """
    Search vector database for most relevant chunks.
    
    Args:
        query_embedding: Query vector
        collection: ChromaDB collection
        top_k (int): Number of results to return
        
    Returns:
        dict: Retrieved documents, distances, and metadata
    """
    print("\n" + "=" * 60)
    print("STEP 6: Retrieve Relevant Chunks")
    print("=" * 60)
    print(f"Searching for top {top_k} most relevant chunks...")
    
    # Query the collection
    results = collection.query(
        query_embeddings=query_embedding,
        n_results=top_k
    )
    
    print(f"✓ Retrieved {len(results['documents'][0])} chunks")
    print("\nRetrieved chunks (ranked by relevance):")
    print("-" * 60)
    
    for i, (doc, distance, metadata) in enumerate(zip(
        results['documents'][0],
        results['distances'][0],
        results['metadatas'][0]
    )):
        similarity = 1 - distance
        
        print(f"\nChunk {i + 1} (Similarity: {similarity:.3f})")
        print(f"Source: {metadata.get('source', 'Unknown')}")
        print(f"Preview: {doc[:150]}...")
        print("-" * 60)
    
    return results
