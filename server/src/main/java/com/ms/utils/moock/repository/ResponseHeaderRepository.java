package com.ms.utils.moock.repository;

import com.ms.utils.moock.domain.ResponseHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseHeaderRepository extends JpaRepository<ResponseHeader, Long> {

    @Query("DELETE FROM ResponseHeader rh WHERE rh.mock = :mockId")
    void deleteByMockId(@Param("mockId") Long mockId);
}

