package com.eggmoney.payv.presentation;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eggmoney.payv.application.service.CommentAppService;
import com.eggmoney.payv.domain.model.vo.BoardId;
import com.eggmoney.payv.domain.model.vo.UserId;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.security.CustomUser;

import lombok.RequiredArgsConstructor;

/**
 * Controller: CommentController
 * 
 * 책임:
 * - 특정 게시글(Board)에 달린 댓글(Comment) 관련 요청 처리
 * - Application Service(CommentAppService) 호출
 * - 인증 사용자 검증 후 댓글 등록 처리
 * 
 * Layer: Presentation (Web MVC Controller)
 * 
 * author 한지원
 */
@Controller
@RequestMapping("/boards/{boardId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentAppService commentAppService;

    @PostMapping
    public String addComment(@PathVariable String boardId,
                             Authentication authentication,
                             @RequestParam String content,
                             RedirectAttributes ra) {

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUser)) {
            ra.addFlashAttribute("error", "로그인 후 댓글을 작성할 수 있습니다.");
            return "redirect:/boards/" + boardId;
        }

        try {
            CustomUser customUser = (CustomUser) authentication.getPrincipal();

            commentAppService.addComment(
                    BoardId.of(boardId),
                    UserId.of(customUser.getUserId().toString()),
                    content
            );

            ra.addFlashAttribute("message", "댓글이 등록되었습니다.");
        } catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/boards/" + boardId;
    }
}