# RAG Package
# Retrieval-Augmented Generation system for career guidance

from .rag_step_1_loading import load_documents_from_folder
from .rag_step_2_chunking import chunk_documents
from .rag_step_3_embeddings import embed_texts
from .rag_step_4_vector_db import (
    get_db_collection,
    should_reindex_documents,
    save_index_metadata
)
from .rag_step_6_similarity import retrieve_relevant_chunks
from .rag_step_7_prompt import prepare_prompt
from .rag_step_8_call_llm import generate_answer

__all__ = [
    'load_documents_from_folder',
    'chunk_documents',
    'embed_texts',
    'get_db_collection',
    'should_reindex_documents',
    'save_index_metadata',
    'retrieve_relevant_chunks',
    'prepare_prompt',
    'generate_answer',
]
