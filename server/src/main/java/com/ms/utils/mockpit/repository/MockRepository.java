package com.ms.utils.mockpit.repository;

import com.ms.utils.mockpit.domain.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockRepository extends JpaRepository<Mock, Long> {

    Page<Mock> findByRouteContaining(String route, Pageable pageable);

    Optional<Mock> findByRoute(String route);

    @Query("SELECT m FROM Mock m JOIN m.route r WHERE r.path = :route AND r.method = :method")
    List<Mock> findByRouteAndMethod(@Param("route") String route, @Param("method") String method);

    @Query("SELECT m FROM Mock m " +
            "LEFT JOIN FETCH m.route r " +
            "LEFT JOIN FETCH m.responseBody rb " +
            "WHERE m.name LIKE %:query% OR m.description LIKE %:query% " +
            "OR r.path LIKE %:query% OR rb.content LIKE %:query%")
    List<Mock> searchMocks(@Param("query") String query);
}

