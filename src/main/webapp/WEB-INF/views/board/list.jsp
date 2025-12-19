<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ include file="/WEB-INF/views/common/header.jsp"%>
<%@ include file="/WEB-INF/views/common/aside.jsp"%>
<html>
<head>
<title>커뮤니티</title>
<link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>" />
<link rel="stylesheet" href="<c:url value='/resources/css/board.css'/>" />
<script src="<c:url value='/resources/js/common.js'/>" defer></script>
</head>
<body>
	<main> <!-- 제목 -->
	<h1 class="board-title">커뮤니티</h1>

	<!-- 검색기준 설정 드롭다운 -->
	<div class="board-search">
		<form action="<c:url value='/boards/search'/>" method="get"
			class="search-form">
			<select name="searchType">
				<option value="title"
					${param.searchType == 'title' ? 'selected' : ''}>제목</option>
				<option value="content"
					${param.searchType == 'content' ? 'selected' : ''}>내용</option>
				<option value="author"
					${param.searchType == 'author' ? 'selected' : ''}>작성자</option>
			</select> <input type="text" name="keyword" placeholder="검색어 입력"
				value="${param.keyword}">
			<button type="submit">검색</button>
		</form>
	</div>

	<!-- 검색 결과 정보 표시 -->
    <c:if test="${not empty searchKeyword}">
        <c:if test="${!noResults}">
            <p>
                검색어 <strong>'${searchKeyword}'</strong>에 해당하는 <strong>${totalCount}</strong>개의 게시글이 있습니다.
            </p>
        </c:if>
        <c:if test="${noResults}">
            <p>검색어 <strong>'${searchKeyword}'</strong>에 해당하는 게시글이 없습니다.</p>
        </c:if>
    </c:if>
	
	<!-- 게시판 테이블 -->
	<table class="board-table">
		<thead>
			<tr>
				<th>제목</th>
				<th>작성자</th>
				<th>작성일</th>
				<th>조회수</th>
				<th>좋아요</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="board" items="${boardList}">
				<tr>
					<td><a href="<c:url value='/boards/${board.id}'/>">${board.title}</a></td>
					<td>${board.owner}</td>
					<td>${fn:replace(fn:substring(board.createdAt, 0, 10), "/", ".")}</td>
					<td>${board.viewCount}</td>
					<td>0</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>

	<!-- 게시글 작성 버튼 -->
	<div class="board-create">
		<a href="<c:url value='/boards/new'/>" class="btn-create">게시글 작성</a>
	</div>

	<!-- 페이지네이션 -->
<div class="pagination">
    <c:if test="${pageInfo.hasPrev}">
        <!-- 검색어와 검색타입이 있을 때만 해당 파라미터를 포함 -->
        <a href="?page=${pageInfo.startPage - 1}&searchType=${searchType}&keyword=${searchKeyword}">&laquo;</a>
    </c:if>

    <c:forEach var="i" begin="${pageInfo.startPage}" end="${pageInfo.endPage}">
        <a href="?page=${i}&searchType=${searchType}&keyword=${searchKeyword}" 
           class="${i == pageInfo.page ? 'active' : ''}">${i}</a>
    </c:forEach>

    <c:if test="${pageInfo.hasNext}">
        <a href="?page=${pageInfo.endPage + 1}&searchType=${searchType}&keyword=${searchKeyword}">&raquo;</a>
    </c:if>
</div>


	</main>
</body>
</html>
