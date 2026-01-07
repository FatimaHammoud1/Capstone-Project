"""
RAG Step 2: Document Chunking
Splits documents into smaller chunks with overlap
"""

def chunk_text(text, chunk_size=500, overlap=50):
    """
    Split text into overlapping chunks.
    
    Args:
        text (str): Text to chunk
        chunk_size (int): Size of each chunk in characters
        overlap (int): Overlap between chunks
        
    Returns:
        list: List of text chunks
    """
    chunks = []
    start = 0
    
    while start < len(text):
        end = start + chunk_size
        chunk = text[start:end]
        chunks.append(chunk.strip())
        start = end - overlap
    
    return chunks


def chunk_documents(documents, chunk_size=500, overlap=50):
    """
    Chunk all documents with metadata.
    
    Args:
        documents (list): List of document dictionaries
        chunk_size (int): Size of each chunk
        overlap (int): Overlap between chunks
        
    Returns:
        list: List of chunks with metadata
    """
    print("\n" + "=" * 60)
    print("STEP 2: Chunking Documents")
    print("=" * 60)
    print(f"Chunk size: {chunk_size} characters")
    print(f"Overlap: {overlap} characters")
    print()
    
    all_chunks = []
    
    for doc_idx, doc in enumerate(documents):
        chunks = chunk_text(doc['content'], chunk_size, overlap)
        
        for chunk_idx, chunk in enumerate(chunks):
            all_chunks.append({
                'text': chunk,
                'source': doc['source'],
                'doc_id': doc_idx,
                'chunk_id': chunk_idx,
                'chunk_length': len(chunk)
            })
        
        print(f"Document {doc_idx + 1}: {doc['source']}")
        print(f"  - Created {len(chunks)} chunks")
    
    print(f"\nTotal chunks created: {len(all_chunks)}")
    
    return all_chunks
