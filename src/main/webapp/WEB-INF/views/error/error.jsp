<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isErrorPage="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>오류가 발생했습니다 | PayV</title>
<link rel="stylesheet"
	href="<c:url value='/resources/css/error-common.css' />">
</head>
<body class="bg-gradient-general">
	<div class="error-container large">
		<div class="error-icon spin icon-general">⚙️</div>
		<div class="error-code small code-general">ERROR</div>
		<h1 class="error-title">예상치 못한 오류가 발생했습니다</h1>
		<p class="error-description">
			죄송합니다. 시스템에서 예상치 못한 문제가 발생했습니다.<br> 잠시 후 다시 시도해주시거나 관리자에게
			문의해주세요.
		</p>

		<!-- 개발 단계에서만 에러 상세 정보 표시 -->
		<c:if
			test="${not empty pageContext.errorData.throwable || not empty exception}">
			<div class="error-details">
				<button class="error-details-toggle" onclick="toggleErrorDetails()">
					기술적 세부사항 보기 (개발자용)</button>
				<div class="error-details-content" id="errorDetails">
					<strong>요청 URI:</strong> ${pageContext.errorData.requestURI}<br>
					<strong>상태 코드:</strong> ${pageContext.errorData.statusCode}<br>
					<strong>예외 타입:</strong>
					${pageContext.errorData.throwable.class.name}<br> <strong>오류
						메시지:</strong> ${pageContext.errorData.throwable.message}<br>
					<br> <strong>Stack Trace:</strong><br>
					<c:forEach var="trace"
						items="${pageContext.errorData.throwable.stackTrace}" begin="0"
						end="9" varStatus="status">
                        ${trace}<br>
					</c:forEach>

					<c:if
						test="${fn:length(pageContext.errorData.throwable.stackTrace) > 10}">
                        ... (총 ${fn:length(pageContext.errorData.throwable.stackTrace)}개 스택 트레이스)
                    </c:if>
				</div>
			</div>
		</c:if>

		<div class="action-buttons">
			<button onclick="location.reload()" class="btn btn-secondary">새로고침</button>
			<a href="<c:url value='/' />" class="btn btn-primary">홈으로 가기</a>
			<button onclick="reportError()" class="btn btn-report">오류 신고</button>
		</div>

		<div class="footer-text">
			문제가 지속되면 고객센터로 연락해주세요.<br> <small> 오류 ID: <span
				id="errorId"></span><br> 발생 시간: <span id="errorTime"></span>
			</small>
		</div>
	</div>

	<script src="<c:url value='/resources/js/error-common.js' />"></script>
	<script>
		// 일반 에러 로깅
		ErrorPageUtils.logErrorInfo('GENERAL', 'Unexpected Error');

		// JSP 에러 데이터가 있는 경우 상세 로깅
		<c:if test="${not empty pageContext.errorData.throwable}">
		ErrorGeneralUtils
				.logDetailedError({
					exceptionType : '${pageContext.errorData.throwable.class.name}',
					exceptionMessage : '${fn:escapeXml(pageContext.errorData.throwable.message)}',
					requestURI : '${pageContext.errorData.requestURI}',
					statusCode : '${pageContext.errorData.statusCode}'
				});
		</c:if>
	</script>
</body>
</html>