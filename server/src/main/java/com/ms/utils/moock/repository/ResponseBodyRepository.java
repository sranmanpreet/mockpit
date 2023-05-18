

package com.ms.utils.moock.repository;

import com.ms.utils.moock.domain.ResponseBody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseBodyRepository extends JpaRepository<ResponseBody, Long> {

    @Query("DELETE FROM ResponseBody rb WHERE rb.mock = :mockId")
    void deleteByMockId(@Param("mockId") Long mockId);
}

