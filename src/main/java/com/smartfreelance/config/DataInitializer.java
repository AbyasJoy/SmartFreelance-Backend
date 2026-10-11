package com.smartfreelance.config;

import com.smartfreelance.entity.Category;
import com.smartfreelance.entity.Skill;
import com.smartfreelance.entity.User;
import com.smartfreelance.repository.CategoryRepository;
import com.smartfreelance.repository.SkillRepository;
import com.smartfreelance.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           SkillRepository skillRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.skillRepository = skillRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed Admin User
        if (userRepository.findByEmail("admin@smartfreelance.com").isEmpty()) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@smartfreelance.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setActive(true);
            userRepository.save(admin);
        }

        // Seed Default Categories
        if (categoryRepository.count() == 0) {
            List<Category> defaultCategories = List.of(
                new Category(null, "Web Development", "Frontend, backend, and full-stack web applications", true),
                new Category(null, "Mobile Development", "Native iOS, Android, and cross-platform mobile apps", true),
                new Category(null, "AI & Machine Learning", "LLMs, data science, RAG systems, and AI models", true),
                new Category(null, "UI/UX & Product Design", "User interface design, wireframing, and user research", true),
                new Category(null, "Backend & APIs", "Microservices, REST APIs, databases, and system architecture", true),
                new Category(null, "DevOps & Cloud Infrastructure", "CI/CD pipelines, Docker, Kubernetes, AWS, and GCP", true)
            );
            categoryRepository.saveAll(defaultCategories);
        }

        // Seed Default Skills
        if (skillRepository.count() == 0) {
            List<Skill> defaultSkills = List.of(
                new Skill(null, "React", true),
                new Skill(null, "Java", true),
                new Skill(null, "Spring Boot", true),
                new Skill(null, "Python", true),
                new Skill(null, "Node.js", true),
                new Skill(null, "TypeScript", true),
                new Skill(null, "SQL & MySQL", true),
                new Skill(null, "Docker & DevOps", true),
                new Skill(null, "Figma & UI Design", true)
            );
            skillRepository.saveAll(defaultSkills);
        }
    }
}
