package com.example.expensetracker.repository;

import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @EntityGraph(attributePaths = {"category", "owner"})
    Page<Expense> findAll(Specification<Expense> spec, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    @Modifying
    @Transactional
    @Query("UPDATE Expense e SET e.owner = :owner WHERE e.owner IS NULL")
    int assignOrphanedExpensesToOwner(@Param("owner") User owner);
}
