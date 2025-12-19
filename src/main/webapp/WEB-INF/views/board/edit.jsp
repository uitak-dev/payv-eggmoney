<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ include file="/WEB-INF/views/common/header.jsp"%>
<%@ include file="/WEB-INF/views/common/aside.jsp"%>
<html>
<head>
    <title>게시글 수정</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>" />
    <link rel="stylesheet" href="<c:url value='/resources/css/board.css'/>" />
</head>
<body>
<main>
    <h1 class="board-title">게시글 수정</h1>

    <form action="${pageContext.request.contextPath}/boards/${board.id}" method="post" class="board-form">
        <!-- Spring MVC에서 PUT 지원하도록 hidden 추가 -->
        <input type="hidden" name="_method" value="put" />

        <div class="form-group">
            <label>작성자 이메일</label>
            <input type="text" name="userId" value="${board.owner}" readonly />
        </div>

        <div class="form-group">
            <label>제목</label>
            <input type="text" name="title" value="${board.title}" required />
        </div>

        <div class="form-group">
            <label>내용</label>
            <textarea name="content" rows="10" required>${board.content}</textarea>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn-create">수정</button>
            <a href="${pageContext.request.contextPath}/boards/${board.id}" class="btn-cancel">취소</a>
        </div>
    </form>
</main>
</body>
</html>
