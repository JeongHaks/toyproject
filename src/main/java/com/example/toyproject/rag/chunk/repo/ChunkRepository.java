package com.example.toyproject.rag.chunk.repo;

import com.example.toyproject.rag.chunk.domain.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {
//
//    @Query(value = """
//          SELECT *
//          FROM chunks
//          ORDER BY vector_l2_distance(embedding_768, embedding_768)
//          LIMIT :topK
//        """, nativeQuery = true)
//    List<Chunk> findTopKByEmbedding(@Param("topK") int topK);

    @Query(value = "select current_database()", nativeQuery = true)
    String currentDatabase();

    @Query(value = "select count(*) from pg_extension where extname='vector'", nativeQuery = true)
    int hasVectorExtension();
}
