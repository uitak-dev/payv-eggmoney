package com.eggmoney.payv.presentation;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eggmoney.payv.application.mapper.BoardDtoMapper;
import com.eggmoney.payv.application.service.BoardAppService;
import com.eggmoney.payv.application.service.CommentAppService;
import com.eggmoney.payv.domain.model.entity.Board;
import com.eggmoney.payv.domain.model.entity.User;
import com.eggmoney.payv.domain.model.repository.UserRepository;
import com.eggmoney.payv.domain.model.vo.BoardId;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.presentation.dto.BoardItemDto;
import com.eggmoney.payv.presentation.dto.CommentItemDto;
import com.eggmoney.payv.presentation.dto.PageInfo;
import com.eggmoney.payv.security.CustomUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller: BoardController
 * 
 * 책임:
 * - Board 관련 웹 요청을 처리하는 Presentation 계층의 진입점
 * - Application Service(BoardAppService, CommentAppService) 호출
 * - Model에 DTO/데이터를 담아 View(JSP)에 전달
 * - 게시글 목록, 생성, 수정, 삭제, 상세 조회, 검색 기능 제공
 * 
 * Layer: Presentation (Web MVC Controller)
 * 
 * author 한지원
 */
@Slf4j
@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardAppService boardAppService;
    private final CommentAppService commentAppService;
    private final UserRepository userRepository;

    // 게시글 목록 화면
    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 10; // 한 페이지에 보여줄 개수
        int blockSize = 5;	// 한 블럭에 들어갈 페이지 수
        
        int totalCount = boardAppService.getBoardCount(); // 전체 게시글 수
        int totalPage = (int) Math.ceil((double) totalCount / pageSize);
        
        // 현재 페이지에 보여줄 시작 offset
        int offset = (page - 1) * pageSize;

        List<BoardItemDto> boardDtoList = boardAppService.getBoardsByPage(offset, pageSize)
                .stream().map(b -> {
                    User user = userRepository.findById(b.getUserId())
                            .orElseThrow(() -> new DomainException("작성자를 찾을 수 없습니다."));
                    return BoardDtoMapper.toDto(b, user);
                }).collect(Collectors.toList());
        
        int currentBlock = (int) Math.ceil((double) page / blockSize);
        int startPage = (currentBlock - 1) * blockSize + 1;
        int endPage = Math.min(startPage + blockSize - 1, totalPage);

        PageInfo pageInfo = new PageInfo(
                page, totalPage, startPage, endPage,
                startPage > 1, endPage < totalPage
            );
        
        model.addAttribute("boardList", boardDtoList);
        model.addAttribute("currentPage", "boards"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)
        
        // 페이지네이션 정보 전달
        model.addAttribute("pageInfo", pageInfo);
        
        return "board/list"; // WEB-INF/views/board/list.jsp
    }

    // 글쓰기 폼 화면
    @GetMapping("/new")
    public String createForm(Model model) {
    	model.addAttribute("currentPage", "boards"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)
        return "board/create"; // WEB-INF/views/board/create.jsp
    }

    // 게시글 작성 처리
    @PostMapping
    public String create(Authentication authentication,
                         @RequestParam String title,
                         @RequestParam String content,
                         RedirectAttributes ra) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUser)) {
                ra.addFlashAttribute("error", "로그인이 필요합니다.");
                return "redirect:/boards/new";
            }
            CustomUser cu = (CustomUser) authentication.getPrincipal();
            boardAppService.createBoardByUserId(cu.getUserId().toString(), title, content);
            ra.addFlashAttribute("message", "게시글이 등록되었습니다.");
            return "redirect:/boards";
        } catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/boards/new";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "저장 중 오류: " + e.getMessage());
            return "redirect:/boards/new";
        }
    }



    // 게시글 상세 조회
    @GetMapping("/{boardId}")
    public String detail(Authentication authentication,
                         @PathVariable String boardId,
                         Model model,
                         RedirectAttributes ra) {
        try {
            log.info("[detail] boardId={}", boardId);

            // 1) 인증 사용자 처리 (비로그인 대비)
            String loginUserId = null;
            if (authentication != null && authentication.getPrincipal() instanceof CustomUser) {
                CustomUser cu = (CustomUser) authentication.getPrincipal();
                if (cu.getUserId() != null) {
                    loginUserId = cu.getUserId().toString();
                }
            } else {
                log.warn("[detail] authentication is null or not CustomUser: {}", authentication);
            }
            model.addAttribute("loginUserId", loginUserId);

            // 2) 게시글 로드
            Board board = boardAppService.getBoard(BoardId.of(boardId));
            log.info("[detail] board loaded id={}, userId={}", board.getId(), board.getUserId());

            // 3) 작성자 로드
            User owner = userRepository.findById(board.getUserId())
                    .orElseThrow(() -> new DomainException("작성자를 찾을 수 없습니다."));
            log.info("[detail] owner email={}", owner.getEmail());

            // 4) DTO 변환
            BoardItemDto boardDto = BoardDtoMapper.toDto(board, owner);

            // 5) 댓글 변환 (작성자 이메일 조회 포함)
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
            List<CommentItemDto> comments = commentAppService.getComments(BoardId.of(boardId))
                    .stream()
                    .map(c -> {
                        String writerEmail = userRepository.findById(c.getUserId())
                                .map(User::getEmail)
                                .orElse("탈퇴회원");
                        return new CommentItemDto(
                                c.getId().toString(),
                                c.getBoardId().toString(),
                                c.getUserId().toString(),
                                writerEmail,
                                c.getContent(),
                                c.getCreatedAt() != null ? c.getCreatedAt().format(fmt) : "",
                                c.getUpdatedAt()
                        );
                    }).collect(Collectors.toList());

            model.addAttribute("boardCreatedAtText",
                    board.getCreatedAt() != null ? board.getCreatedAt().format(fmt) : "");
            model.addAttribute("currentPage", "boards");
            model.addAttribute("board", boardDto);
            model.addAttribute("comments", comments);

            return "board/detail";
        } catch (DomainException e) {
            log.warn("[detail] domain error: {}", e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/boards";
        } catch (Exception e) {
            log.error("[detail] unexpected error", e);
            ra.addFlashAttribute("error", "상세 조회 중 오류가 발생했습니다.");
            return "redirect:/boards";
        }
    }
    
    /** ===== 수정 폼 ===== */
    @GetMapping("/{boardId}/edit")
    public String editForm(@PathVariable String boardId, Model model,
                           RedirectAttributes ra) {
        try {
            Board board = boardAppService.getBoard(BoardId.of(boardId));
            User owner = userRepository.findById(board.getUserId())
                    .orElseThrow(() -> new DomainException("작성자를 찾을 수 없습니다."));

            // ✅ DTO 변환
            BoardItemDto boardDto = BoardDtoMapper.toDto(board, owner);

            model.addAttribute("board", boardDto);
            model.addAttribute("currentPage", "boards");
            return "board/edit";
        } catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/boards";
        }
    }

    /** ===== 수정 처리 ===== */
    @PostMapping("/{boardId}")
    public String update(@PathVariable String boardId,
                         @RequestParam String title,
                         @RequestParam String content,
                         RedirectAttributes ra) {
        try {
            if (title == null || title.trim().isEmpty()) {
                ra.addFlashAttribute("error", "제목은 필수입니다.");
                return "redirect:/boards/" + boardId + "/edit";
            }
            boardAppService.updateBoard(BoardId.of(boardId), title.trim(), content.trim());
            ra.addFlashAttribute("message", "게시글을 수정했습니다.");
            return "redirect:/boards/" + boardId;
        } catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/boards/" + boardId + "/edit";
        }
    }

    /** ===== 삭제 ===== */
    @PostMapping("/{boardId}/delete")
    public String delete(@PathVariable String boardId,
                         RedirectAttributes ra) {
        try {
            boardAppService.deleteBoard(BoardId.of(boardId));
            ra.addFlashAttribute("message", "게시글을 삭제했습니다.");
            return "redirect:/boards";
        } catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/boards/" + boardId;
        }
    }	
    
 // 🔍 게시글 검색
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "title") String searchType,
                         @RequestParam(defaultValue = "1") int page,
                         Model model) {
        if (keyword == null) keyword = "";

        int pageSize = 10;
        int blockSize = 5;

        int totalCount = boardAppService.getBoardsCountBySearch(keyword, searchType);
        int totalPage = (int) Math.ceil((double) totalCount / pageSize);
        int offset = (page - 1) * pageSize;

        List<BoardItemDto> boardDtoList = boardAppService.getBoardsBySearch(keyword, searchType, offset, pageSize);

        int currentBlock = (int) Math.ceil((double) page / blockSize);
        int startPage = (currentBlock - 1) * blockSize + 1;
        int endPage = Math.min(startPage + blockSize - 1, totalPage);

        PageInfo pageInfo = new PageInfo(
                page, totalPage, startPage, endPage,
                startPage > 1, endPage < totalPage
        );

        model.addAttribute("boardList", boardDtoList);
        model.addAttribute("currentPage", "boards");
        model.addAttribute("pageInfo", pageInfo);

        // 검색 관련 추가 데이터
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("noResults", boardDtoList.isEmpty());

        // 👉 결과를 list.jsp에 그대로 보여주기
        return "board/list";
    }
}
