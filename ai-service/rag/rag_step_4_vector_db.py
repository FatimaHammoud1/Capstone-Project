"""
RAG Step 4: Vector Database (ChromaDB)
Manages vector storage and retrieval
"""
import chromadb
import json
import os

_vector_db_client = None
_my_db_collection = None


def get_vector_db_client(persist_directory="./chroma_persist"):
    """
    Get or create ChromaDB client (singleton pattern).
    
    Args:
        persist_directory (str): Path to persist database
        
    Returns:
        chromadb.Client: ChromaDB client
    """
    global _vector_db_client

    if _vector_db_client is None:
        _vector_db_client = chromadb.PersistentClient(path=persist_directory)

    return _vector_db_client


def get_db_collection(my_db_collection_name="my_demo_rag_collection"):
    """
    Get or create ChromaDB collection (singleton pattern).
    
    Args:
        my_db_collection_name (str): Name of the collection
        
    Returns:
        chromadb.Collection: ChromaDB collection
    """
    global _my_db_collection
    
    if _my_db_collection is None:
        client = get_vector_db_client()
        existing_collections = [c.name for c in client.list_collections()]

        if my_db_collection_name in existing_collections:
            _my_db_collection = client.get_collection(name=my_db_collection_name)
        else:
            _my_db_collection = client.create_collection(name=my_db_collection_name)

    return _my_db_collection


def should_reindex_documents(collection, folder_path):
    """
    Check if documents need reindexing based on file changes.
    
    Args:
        collection: ChromaDB collection
        folder_path (str): Path to documents folder
        
    Returns:
        tuple: (needs_reindex: bool, current_files: dict)
    """
    metadata_file = "./chroma_persist/index_metadata.json"
    
    # Get current files info
    current_files = {}
    if os.path.isdir(folder_path):
        for filename in os.listdir(folder_path):
            filepath = os.path.join(folder_path, filename)
            if os.path.isfile(filepath):
                current_files[filename] = os.path.getmtime(filepath)
    
    # Check if metadata exists
    if not os.path.exists(metadata_file):
        print("📝 No index metadata found - will index documents")
        return True, current_files
    
    # Load previous metadata
    try:
        with open(metadata_file, 'r') as f:
            previous_files = json.load(f)
    except:
        print("📝 Error reading metadata - will reindex")
        return True, current_files
    
    # Compare
    if current_files != previous_files:
        print("📝 Documents changed - will reindex")
        print(f"   Previous: {len(previous_files)} files")
        print(f"   Current: {len(current_files)} files")
        return True, current_files
    
    # Check if collection is empty
    if collection.count() == 0:
        print("📝 Collection is empty - will index")
        return True, current_files
    
    print("✅ Documents unchanged - using existing index")
    print(f"   Indexed chunks: {collection.count()}")
    return False, current_files


def save_index_metadata(current_files):
    """
    Save current files metadata to track changes.
    
    Args:
        current_files (dict): Dictionary of filename -> modification time
    """
    metadata_file = "./chroma_persist/index_metadata.json"
    os.makedirs(os.path.dirname(metadata_file), exist_ok=True)
    
    with open(metadata_file, 'w') as f:
        json.dump(current_files, f, indent=2)
    
    print("💾 Index metadata saved")
