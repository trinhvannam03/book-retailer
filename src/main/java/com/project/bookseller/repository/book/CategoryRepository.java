package com.project.bookseller.repository.book;

import com.project.bookseller.entity.book.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findCategoriesByCategoryIdIn(List<Long> categoryIdList);
    List<Category> findCategoriesByParentIdIn(List<Long> categoryIdList);
}
