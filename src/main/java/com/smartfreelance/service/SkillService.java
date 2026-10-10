package com.smartfreelance.service;

import com.smartfreelance.entity.Skill;
import com.smartfreelance.exception.ResourceNotFoundException;
import com.smartfreelance.repository.SkillRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @CacheEvict(value = "skills", allEntries = true)
    @Transactional
    public Skill createSkill(Skill skill) {
        if (skill.getName() == null || skill.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        return skillRepository.save(skill);
    }

    @Cacheable(value = "skills")
    @Transactional(readOnly = true)
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Cacheable(value = "skills", key = "#id")
    @Transactional(readOnly = true)
    public Skill getSkillById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
    }

    @CacheEvict(value = "skills", allEntries = true)
    @Transactional
    public Skill updateSkill(Long id, Skill skillRequest) {
        Skill skill = getSkillById(id);
        if (skillRequest.getName() != null && !skillRequest.getName().trim().isEmpty()) {
            skill.setName(skillRequest.getName());
        }
        return skillRepository.save(skill);
    }

    @CacheEvict(value = "skills", allEntries = true)
    @Transactional
    public Skill updateSkillStatus(Long id, boolean active) {
        Skill skill = getSkillById(id);
        skill.setActive(active);
        return skillRepository.save(skill);
    }
}
