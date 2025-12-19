<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ include file="/WEB-INF/views/common/header.jsp"%>
<%@ include file="/WEB-INF/views/common/aside.jsp"%>
<html>
<head>
    <title>게시글 작성</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>" />
    <link rel="stylesheet" href="<c:url value='/resources/css/board.css'/>" />
    <script src="<c:url value='/resources/js/common.js'/>" defer></script>
</head>
<body>
<main>
    <h1 class="board-title">게시글 작성</h1>

    <form action="${pageContext.request.contextPath}/boards" method="post" class="board-form">
   		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <div class="form-group">
            <label>제목</label>
            <input type="text" name="title" required />
        </div>

        <div class="form-group">
            <label>내용</label>
            <textarea name="content" rows="10" required></textarea>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn-create">작성</button>
            <a href="${pageContext.request.contextPath}/boards" class="btn-cancel">취소</a>
        </div>
    </form>
</main>
</body>
</html>

