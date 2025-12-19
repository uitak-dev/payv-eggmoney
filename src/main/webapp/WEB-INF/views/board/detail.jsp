<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ include file="/WEB-INF/views/common/header.jsp"%>
<%@ include file="/WEB-INF/views/common/aside.jsp"%>

<html>
<head>
<title>${board.title}</title>
<link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>" />
<link rel="stylesheet" href="<c:url value='/resources/css/board.css'/>" />
<script src="<c:url value='/resources/js/common.js'/>" defer></script>

<style>
/* 버튼 영역 전용 */
.post-actions-right {
	display: flex;
	gap: 8px;
	align-items: center;
}

.post-actions-right form {
	display: inline; /* 댓글 영역 영향 안주게 제한 */
}
</style>
</head>
<body>
	<main class="board-detail">
	<h2 class="board-title">커뮤니티</h2>

	<article class="post-card">
		<!-- (1) 제목 -->
		<h1 class="post-title">${board.title}</h1>

		<!-- (2) 메타: 왼쪽 작성자/날짜 · 오른쪽 수정/삭제/목록 -->
		<div class="post-meta-bar">
			<div class="post-meta-left">
				<span class="author-name">${board.owner}</span> <span
					class="meta-sep"></span> <span class="post-date">${boardCreatedAtText}</span>
			</div>

			<div class="post-actions-right">
				<!-- 작성자 본인만 보이는 영역 -->
				<c:if test="${loginUserId != null && loginUserId == board.userId}">
					<a class="btn btn-primary outline"
						href="<c:url value='/boards/${board.id}/edit'/>">수정</a>
					<form action="<c:url value='/boards/${board.id}/delete'/>"
						method="post">
						<input type="hidden" name="${_csrf.parameterName}"
							value="${_csrf.token}" />
						<button type="submit" class="btn danger"
							onclick="return confirm('정말 삭제하시겠습니까?');">삭제</button>
					</form>

				</c:if>

				<%-- 임시: 로그인 기능 없을 때 표시할 수정/삭제 
				<c:if test="${loginUserId == null}">
					<a
						href="${pageContext.request.contextPath}/boards/${board.id}/edit"
						class="btn btn-primary outline">수정</a>
					<form
						action="${pageContext.request.contextPath}/boards/${board.id}/delete"
						method="post">
						<button type="submit" class="btn danger"
							onclick="return confirm('정말 삭제하시겠습니까?');">삭제</button>
					</form>
				</c:if>
				--%>

				<!-- 목록 버튼 -->
				<a href="<c:url value='/boards'/>" class="btn-accent">목록</a>
			</div>
		</div>

		<hr class="post-divider" />

		<!-- (3) 본문 -->
		<div class="post-content">${fn:escapeXml(board.content)}</div>

		<!-- (4) 좋아요 / 댓글수 -->
		<div class="post-stats" data-board-id="${board.id}">
			<%-- 좋아요 
			<button type="button" class="like-toggle" aria-pressed="false"
				title="좋아요">
				<svg class="icon-heart" viewBox="0 0 24 24" width="20" height="20"
					aria-hidden="true">
            <path
						d="M12 21s-6.716-4.21-9.193-7.32C1.24 12.07 1 10.94 1 9.75 1 7.13 3.14 5 5.75 5c1.54 0 2.97.73 3.89 1.88A5.02 5.02 0 0 1 13.5 5C16.09 5 18.25 7.13 18.25 9.75c0 1.19-.24 2.32-1.807 3.93C18.716 16.79 12 21 12 21z" />
          </svg>
				<span class="like-count">${likeCount}</span>
			</button>
			--%>

			<span class="meta-sep"></span>

			<!-- 댓글 -->
			<div class="stat">
				<svg class="icon-comment" viewBox="0 0 24 24" width="20" height="20"
					aria-hidden="true">
            <path
						d="M21 6a3 3 0 0 0-3-3H6A3 3 0 0 0 3 6v8a3 3 0 0 0 3 3h8l4 4v-4a3 3 0 0 0 3-3V6z" />
          </svg>
				<span class="comment-count">${fn:length(comments)}</span>
			</div>
		</div>

		<!-- (5) 댓글 목록 -->
		<section class="comments">
			<h3>댓글</h3>
			<ul class="comment-list">
				<c:forEach var="comment" items="${comments}">
					<li class="comment-item">
						<div class="comment-head">
							<strong>${comment.writer}</strong> <span class="meta-sep"></span>
							<span>${comment.createdAt}</span>
						</div>
						<p class="comment-content">${fn:escapeXml(comment.content)}</p>
					</li>
				</c:forEach>
			</ul>

			<!-- 댓글 작성 -->
			<form action="<c:url value='/boards/${board.id}/comments'/>"
				method="post" class="comment-form">
				<input type="hidden" name="userId" value="anonymous" />
				<textarea name="content" placeholder="댓글을 입력하세요"></textarea>
				<button type="submit" class="btn btn-primary">등록</button>
			</form>
		</section>
	</article>
	</main>
</body>
</html>
