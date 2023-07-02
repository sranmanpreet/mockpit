package com.ms.utils.mockpit.repository;

import com.ms.utils.mockpit.domain.ResponseHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseHeaderRepository extends JpaRepository<ResponseHeader, Long> {

    @Query("DELETE FROM ResponseHeader rh WHERE rh.mockId = :mockId")
    void deleteByMockId(@Param("mockId") Long mockId);
}

