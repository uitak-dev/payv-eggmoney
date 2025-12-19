package com.eggmoney.payv.domain.model.repository;

import java.util.List;
import java.util.Optional;

import com.eggmoney.payv.domain.model.entity.Board;
import com.eggmoney.payv.domain.model.vo.BoardId;
import com.eggmoney.payv.domain.model.vo.UserId;
import com.eggmoney.payv.presentation.dto.BoardItemDto;

/**
 * Repository Interface: BoardRepository
 * 
 * 책임:
 * - Board Entity의 영속성 추상화
 * - Application/Domain 계층에서는 이 인터페이스를 통해 데이터 접근
 * - 구현체(MyBatis, JPA 등)에 의존하지 않도록 함 (의존 역전)
 * 
 * Layer: Domain (Repository 인터페이스는 Domain 영역에 가까움)
 * 
 * author 한지원
 */
public interface BoardRepository {
	// 식별자로 게시글 조회
    Optional<Board> findById(BoardId id);

    // 저장/갱신
    void save(Board board);

    // 전체 게시글 조회 
    List<Board> findAll();

    // 특정 사용자의 게시글 조회
    List<Board> findByUser(UserId userId);

    // 전체 게시글 수 조회 
    int count();

    // 페이징 조회
    List<Board> findByPage(int offset, int limit);
    
    // 🔹 검색은 DTO 직접 반환 (email join 지원)
    List<BoardItemDto> findBySearch(String keyword, String searchType, int offset, int limit);

    // 제목, 내용, 작성자별 검색된 게시글 수
    int countBySearch(String keyword, String searchType);
    
    // 삭제
    void delete(BoardId id);
}
