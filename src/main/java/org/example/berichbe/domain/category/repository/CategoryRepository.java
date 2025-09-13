package org.example.berichbe.domain.category.repository;

import org.example.berichbe.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
