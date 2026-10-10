package com.smartfreelance.service;

import com.smartfreelance.dto.KnowledgeChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RAGRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(RAGRetrievalService.class);

    private static final int MAX_CHUNKS = 2;
    private static final int MAX_CONTEXT_LENGTH = 1500;
    private static final int MIN_SCORE_THRESHOLD = 2;

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
            "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
            "to", "was", "were", "will", "with", "how", "do", "can", "i", "what",
            "does", "should", "could", "would", "my", "me", "you", "your"
    );

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[^a-z0-9]+");

    private final List<KnowledgeChunk> knowledgeBase;

    public RAGRetrievalService() {
        this.knowledgeBase = initializeKnowledgeBase();
    }

    public List<KnowledgeChunk> retrieveRelevantChunks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> queryTokens = extractTokens(query);
        if (queryTokens.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScoredChunk> scoredChunks = new ArrayList<>();
        for (KnowledgeChunk chunk : knowledgeBase) {
            int score = calculateRelevanceScore(chunk, queryTokens);
            if (score >= MIN_SCORE_THRESHOLD) {
                scoredChunks.add(new ScoredChunk(chunk, score));
            }
        }

        scoredChunks.sort((a, b) -> Integer.compare(b.score, a.score));

        List<KnowledgeChunk> selectedChunks = new ArrayList<>();
        int currentLength = 0;

        for (ScoredChunk scored : scoredChunks) {
            if (selectedChunks.size() >= MAX_CHUNKS) {
                break;
            }

            String chunkText = formatChunk(scored.chunk);
            if (currentLength + chunkText.length() > MAX_CONTEXT_LENGTH) {
                if (selectedChunks.isEmpty()) {
                    // Truncate first chunk if it's longer than limit
                    String truncatedText = scored.chunk.getContent().substring(0, Math.min(scored.chunk.getContent().length(), MAX_CONTEXT_LENGTH - 100));
                    KnowledgeChunk truncatedChunk = new KnowledgeChunk(
                            scored.chunk.getId(),
                            scored.chunk.getTitle(),
                            scored.chunk.getCategory(),
                            truncatedText + "...",
                            scored.chunk.getKeywords()
                    );
                    selectedChunks.add(truncatedChunk);
                }
                break;
            }

            selectedChunks.add(scored.chunk);
            currentLength += chunkText.length();
        }

        logger.debug("Query '{}' matched {} relevant knowledge chunk(s).", query, selectedChunks.size());
        return selectedChunks;
    }

    public String buildFormattedKnowledgeContext(String query) {
        List<KnowledgeChunk> chunks = retrieveRelevantChunks(query);
        if (chunks.isEmpty()) {
            return "No specific SmartFreelance platform knowledge document found for this query.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SmartFreelance Platform Knowledge Base Context:\n");
        for (KnowledgeChunk chunk : chunks) {
            sb.append(formatChunk(chunk)).append("\n---\n");
        }
        return sb.toString();
    }

    private int calculateRelevanceScore(KnowledgeChunk chunk, Set<String> queryTokens) {
        int score = 0;
        Set<String> lowerKeywords = chunk.getKeywords().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> titleTokens = extractTokens(chunk.getTitle());
        Set<String> contentTokens = extractTokens(chunk.getContent());

        for (String token : queryTokens) {
            // Keyword match (highest weight)
            if (lowerKeywords.contains(token)) {
                score += 3;
            }
            // Title match
            if (titleTokens.contains(token)) {
                score += 2;
            }
            // Content match
            if (contentTokens.contains(token)) {
                score += 1;
            }
        }
        return score;
    }

    private Set<String> extractTokens(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptySet();
        }
        String[] rawTokens = TOKEN_PATTERN.split(text.toLowerCase());
        Set<String> tokens = new HashSet<>();
        for (String token : rawTokens) {
            if (token.length() > 1 && !STOP_WORDS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private String formatChunk(KnowledgeChunk chunk) {
        return String.format("[%s: %s] (%s)\n%s", chunk.getId(), chunk.getTitle(), chunk.getCategory(), chunk.getContent());
    }

    private static class ScoredChunk {
        final KnowledgeChunk chunk;
        final int score;

        ScoredChunk(KnowledgeChunk chunk, int score) {
            this.chunk = chunk;
            this.score = score;
        }
    }

    private List<KnowledgeChunk> initializeKnowledgeBase() {
        List<KnowledgeChunk> chunks = new ArrayList<>();

        chunks.add(new KnowledgeChunk(
                "KB-ROLES-01",
                "SmartFreelance Roles and Permitted Workflows",
                "Roles & Permissions",
                "SmartFreelance supports three roles: CLIENT, FREELANCER, and ADMIN. " +
                        "CLIENTs can create, publish, and manage projects, review submitted proposals, accept or reject proposals, define project milestones, review milestone deliverables, approve work or request revisions, release simulated payments, and leave ratings/reviews. " +
                        "FREELANCERs can browse published projects, submit proposals with bid amounts, submit milestone deliverables, and leave ratings/reviews. " +
                        "ADMINs can manage user accounts, oversee project statuses, manage categories and skills, and view platform metrics.",
                Set.of("role", "roles", "client", "freelancer", "admin", "permission", "permissions", "workflow", "create", "publish", "browse", "proposal", "deliverable")
        ));

        chunks.add(new KnowledgeChunk(
                "KB-PROJECTS-01",
                "Project Creation, Publishing, Discovery, and Proposals",
                "Projects & Proposals",
                "Project Creation & Discovery: Clients create projects in DRAFT or OPEN status. Only OPEN projects are discoverable in public project search and category filters. " +
                        "Proposal Workflow: Freelancers can submit proposals for OPEN projects, specifying a bid amount, estimated delivery time, and cover letter. " +
                        "Proposal Decision: Clients evaluate submitted proposals and can accept one proposal or reject proposals. Accepting a proposal assigns the freelancer and updates project status to IN_PROGRESS.",
                Set.of("project", "create", "publish", "discovery", "search", "filter", "proposal", "proposals", "bid", "cover letter", "open", "draft", "submit", "accept", "reject")
        ));

        chunks.add(new KnowledgeChunk(
                "KB-PROPOSALS-01",
                "Proposal Acceptance and Rejection Rules",
                "Proposals",
                "When a Client accepts a proposal on SmartFreelance: 1. The proposal status changes to ACCEPTED. 2. The Freelancer is assigned to the project. 3. The project status automatically transitions from OPEN to IN_PROGRESS. 4. All other pending proposals for that project are automatically marked REJECTED. Freelancers cannot edit or retract proposals once accepted.",
                Set.of("proposal", "proposals", "accept", "accepted", "acceptance", "reject", "rejected", "rejection", "assigned", "in_progress", "status")
        ));

        chunks.add(new KnowledgeChunk(
                "KB-MILESTONES-01",
                "Milestone Deliverables, Approvals, and Revision Requests",
                "Milestones",
                "Milestone Workflow: Projects can be divided into specific milestones with assigned amounts and deadlines. " +
                        "Work Submission: The assigned Freelancer submits milestone work with submission notes and asset links. " +
                        "Client Review: The Client can either APPROVE the milestone work (which triggers simulated payment release) or request REVISION with specific feedback notes. " +
                        "Revisions: When a revision is requested, the milestone status returns to IN_PROGRESS for the freelancer to submit updated work.",
                Set.of("milestone", "milestones", "deliverable", "deliverables", "submission", "submit", "approve", "approval", "revision", "revisions", "request revision", "feedback")
        ));

        chunks.add(new KnowledgeChunk(
                "KB-PAYMENTS-01",
                "Simulated Payments, Escrow, and Project Completion",
                "Payments & Completion",
                "Simulated Payments: All financial transactions, wallet balances, and escrow releases on SmartFreelance are 100% simulated for educational project demonstration. No real money or real credit card processing occurs. " +
                        "Settlement Conditions: Milestone payments are held in simulated escrow when work starts and settled to the freelancer's simulated wallet when the Client approves the milestone. " +
                        "Project Completion: Once all project milestones are approved and settled, the project status is marked COMPLETED.",
                Set.of("payment", "payments", "simulated", "escrow", "wallet", "settlement", "settled", "completed", "completion", "money", "card", "transaction")
        ));

        chunks.add(new KnowledgeChunk(
                "KB-REVIEWS-01",
                "Reviews, Ratings, Notifications, and FAQs",
                "Feedback & Community",
                "Reviews & Ratings: Upon project completion, both Clients and Freelancers can post reviews with star ratings (1-5 stars) and comment feedback. " +
                        "Notifications: SmartFreelance sends automated in-app notifications for proposal acceptances, milestone work submissions, revision requests, payment settlements, and review postings. " +
                        "FAQs: Freelancers and clients can manage their profile settings, view active projects from their dashboard, and track milestone progress in real-time.",
                Set.of("review", "reviews", "rating", "ratings", "star", "feedback", "notification", "notifications", "faq", "faqs", "alert", "dashboard")
        ));

        chunks.add(new KnowledgeChunk(
                "KB-SECURITY-01",
                "Platform Security and Data Privacy Boundaries",
                "Security & Privacy",
                "Platform Security: SmartFreelance enforces strict Spring Security, JWT authentication, and Role-Based Access Control (RBAC). " +
                        "Privacy Boundaries: Authenticated user identity and permissions are derived strictly from backend JWT security context, not request parameters. " +
                        "Users can only view or modify projects, proposals, and milestones that they own or are assigned to. Admin endpoints are strictly restricted to users with the ADMIN role.",
                Set.of("security", "privacy", "jwt", "authentication", "authorization", "rbac", "role", "permission", "boundary", "untrusted", "admin")
        ));

        return Collections.unmodifiableList(chunks);
    }
}
