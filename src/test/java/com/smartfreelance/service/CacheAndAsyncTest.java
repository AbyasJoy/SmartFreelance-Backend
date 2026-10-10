package com.smartfreelance.service;

import com.smartfreelance.entity.Category;
import com.smartfreelance.entity.Skill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CacheAndAsyncTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkillService skillService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AsyncNotificationService asyncNotificationService;

    @BeforeEach
    void setUp() {
        if (cacheManager.getCache("categories") != null) {
            cacheManager.getCache("categories").clear();
        }
        if (cacheManager.getCache("skills") != null) {
            cacheManager.getCache("skills").clear();
        }
    }

    @Test
    void categoryCachingAndEviction_WorksCorrectly() {
        String uniqueCatName = "Cat-" + UUID.randomUUID().toString().substring(0, 8);
        Category cat = new Category();
        cat.setName(uniqueCatName);
        cat.setDescription("Category Caching Test");
        categoryService.createCategory(cat);

        // First read populates cache
        List<Category> firstList = categoryService.getAllCategories();
        assertNotNull(firstList);
        assertNotNull(cacheManager.getCache("categories").get(SimpleKey.EMPTY));

        // Creating a new category evicts cache
        Category cat2 = new Category();
        cat2.setName("Cat2-" + UUID.randomUUID().toString().substring(0, 8));
        categoryService.createCategory(cat2);

        // Cache must be cleared after creation
        assertNull(cacheManager.getCache("categories").get(SimpleKey.EMPTY));
    }

    @Test
    void skillCachingAndEviction_WorksCorrectly() {
        String uniqueSkillName = "Skill-" + UUID.randomUUID().toString().substring(0, 8);
        Skill skill = new Skill();
        skill.setName(uniqueSkillName);
        skillService.createSkill(skill);

        List<Skill> firstList = skillService.getAllSkills();
        assertNotNull(firstList);
        assertNotNull(cacheManager.getCache("skills").get(SimpleKey.EMPTY));

        Skill skill2 = new Skill();
        skill2.setName("Skill2-" + UUID.randomUUID().toString().substring(0, 8));
        skillService.createSkill(skill2);

        assertNull(cacheManager.getCache("skills").get(SimpleKey.EMPTY));
    }

    @Test
    void asyncNotificationService_ExecutesOnAsyncPoolThread() throws Exception {
        CompletableFuture<String> future = asyncNotificationService.logNotificationSideEffectAsync(100L, "Proposal Accepted", "PROPOSAL");
        String threadName = future.get();
        assertNotNull(threadName);
        assertTrue(threadName.startsWith("SmartFreelance-Async-"), "Async task must execute on bounded ThreadPoolTaskExecutor thread pool");
    }
}
