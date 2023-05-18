package com.ms.utils.moock.repository;

import com.ms.utils.moock.domain.ResponseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResponseStatusRepository extends JpaRepository<ResponseStatus, Long> {
    @Query("DELETE FROM ResponseStatus rs WHERE rs.mock = :mockId")
    void deleteByMockId(@Param("mockId") Long mockId);
}

