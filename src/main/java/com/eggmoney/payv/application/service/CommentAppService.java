package com.eggmoney.payv.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eggmoney.payv.domain.model.entity.Comment;
import com.eggmoney.payv.domain.model.repository.BoardRepository;
import com.eggmoney.payv.domain.model.repository.CommentRepository;
import com.eggmoney.payv.domain.model.vo.BoardId;
import com.eggmoney.payv.domain.model.vo.CommentId;
import com.eggmoney.payv.domain.model.vo.UserId;
import com.eggmoney.payv.domain.shared.error.DomainException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentAppService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    public Comment addComment(BoardId boardId, UserId userId, String content) {
        boardRepository.findById(boardId).orElseThrow(() -> new DomainException("Board not found"));
        Comment comment = Comment.create(boardId, userId, content);
        commentRepository.save(comment);
        return comment;
    }

    public List<Comment> getComments(BoardId boardId) {
        return commentRepository.findByBoard(boardId);
    }

    public void updateComment(CommentId commentId, String newContent, UserId editorId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new DomainException("Comment not found"));
        if (!comment.getUserId().equals(editorId)) throw new DomainException("작성자만 수정 가능");
        comment.update(newContent);
        commentRepository.save(comment);
    }

    public void deleteComment(CommentId commentId, UserId editorId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new DomainException("Comment not found"));
        if (!comment.getUserId().equals(editorId)) throw new DomainException("작성자만 삭제 가능");
        commentRepository.delete(commentId);
    }
}
