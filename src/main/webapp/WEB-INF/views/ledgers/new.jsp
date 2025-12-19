<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ include file="/WEB-INF/views/common/header.jsp"%>

<html>
<head>
<title>가계부 생성</title>
<link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>" />
<script src="<c:url value='/resources/js/common.js'/>" defer></script>
</head>
<body>
<main>
	<h1>가계부 생성</h1>
	<form method="post" action="<c:url value='/ledgers'/>">
		<label>이름 <input type="text" name="name" required></label>
		<button type="submit">생성</button>
	</form>
	<p>
		<a href="<c:url value='/ledgers'/>">목록으로</a>
	</p>
</main>
</body>
</html>
