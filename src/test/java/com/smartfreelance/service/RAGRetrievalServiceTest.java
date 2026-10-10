package com.smartfreelance.service;

import com.smartfreelance.dto.KnowledgeChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RAGRetrievalServiceTest {

    private RAGRetrievalService ragRetrievalService;

    @BeforeEach
    void setUp() {
        ragRetrievalService = new RAGRetrievalService();
    }

    @Test
    void retrieveRelevantChunks_ProjectQuestion_ReturnsProjectChunk() {
        List<KnowledgeChunk> chunks = ragRetrievalService.retrieveRelevantChunks("How do clients create and publish projects for discovery?");
        assertFalse(chunks.isEmpty(), "Should retrieve project knowledge chunk");
        assertTrue(chunks.stream().anyMatch(c -> c.getId().equals("KB-PROJECTS-01")), "Should contain KB-PROJECTS-01");
    }

    @Test
    void retrieveRelevantChunks_ProposalQuestion_ReturnsProposalChunk() {
        List<KnowledgeChunk> chunks = ragRetrievalService.retrieveRelevantChunks("What happens when a client accepts a freelancer proposal?");
        assertFalse(chunks.isEmpty(), "Should retrieve proposal knowledge chunk");
        assertTrue(chunks.stream().anyMatch(c -> c.getId().equals("KB-PROPOSALS-01") || c.getId().equals("KB-PROJECTS-01")));
    }

    @Test
    void retrieveRelevantChunks_MilestoneAndRevisionQuestion_ReturnsMilestoneChunk() {
        List<KnowledgeChunk> chunks = ragRetrievalService.retrieveRelevantChunks("How to submit milestone deliverables or request revisions?");
        assertFalse(chunks.isEmpty(), "Should retrieve milestone knowledge chunk");
        assertTrue(chunks.stream().anyMatch(c -> c.getId().equals("KB-MILESTONES-01")));
    }

    @Test
    void retrieveRelevantChunks_SimulatedPaymentQuestion_ReturnsPaymentChunk() {
        List<KnowledgeChunk> chunks = ragRetrievalService.retrieveRelevantChunks("Are payments held in escrow and settled with real money?");
        assertFalse(chunks.isEmpty(), "Should retrieve simulated payment knowledge chunk");
        assertTrue(chunks.stream().anyMatch(c -> c.getId().equals("KB-PAYMENTS-01")));
    }

    @Test
    void retrieveRelevantChunks_IrrelevantQuery_ReturnsNoPlatformChunks() {
        List<KnowledgeChunk> chunks = ragRetrievalService.retrieveRelevantChunks("What is the distance between the Earth and the Moon in kilometers?");
        assertTrue(chunks.isEmpty(), "Irrelevant query must return empty list of chunks");
    }

    @Test
    void retrieveRelevantChunks_LimitsMaxChunksAndContextLength() {
        // Broad query that could potentially match multiple chunks
        List<KnowledgeChunk> chunks = ragRetrievalService.retrieveRelevantChunks("client freelancer role project proposal milestone payment security");
        assertTrue(chunks.size() <= 2, "Must enforce maximum chunk count limit of 2");

        String formattedContext = ragRetrievalService.buildFormattedKnowledgeContext("client freelancer role project proposal milestone payment security");
        assertTrue(formattedContext.length() <= 1600, "Context string length must be bounded to control token limits");
    }

    @Test
    void buildFormattedKnowledgeContext_IrrelevantQuery_ReturnsDefaultMessage() {
        String context = ragRetrievalService.buildFormattedKnowledgeContext("How to make a chocolate cake?");
        assertEquals("No specific SmartFreelance platform knowledge document found for this query.", context);
    }
}
