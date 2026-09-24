package com.aiservice.infrastructure.persistence.entity;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="ai_knowledge_chunks") public class AiKnowledgeChunkEntity { @Id public UUID id; @Column(name="document_id",nullable=false) public UUID documentId; @Column(name="chunk_index",nullable=false) public int chunkIndex; @Column(nullable=false,columnDefinition="TEXT") public String text; @Column(nullable=false,columnDefinition="TEXT") public String embedding; @Column(name="metadata_json",nullable=false,columnDefinition="TEXT") public String metadataJson; }
