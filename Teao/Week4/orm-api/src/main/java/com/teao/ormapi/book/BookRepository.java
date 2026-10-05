package com.teao.ormapi.book;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    // 카테고리를 함께 조회해 N+1 쿼리를 막는다.
    @EntityGraph(attributePaths = "category")
    List<Book> findAllByOrderByBookIdDesc();
}
