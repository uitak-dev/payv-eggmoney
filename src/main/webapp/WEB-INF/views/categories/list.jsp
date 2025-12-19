<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>카테고리 목록</title>
<link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
<style>
.badge {
    display: inline-block;
    padding: 2px 6px;
    font-size: 0.5rem;
    font-weight: bold;
    color: #fff;
    background-color: #888;
    border-radius: 4px;
    vertical-align: middle;
    margin-left: 4px;
}
</style>
</head>

<body>
	<jsp:include page="/WEB-INF/views/common/header.jsp" />
	<jsp:include page="/WEB-INF/views/common/aside.jsp" />

	<main id="main" data-ledger-id="${ledgerId}">
	<div class="container">
		<h2>카테고리</h2>

		<c:if test="${not empty message}">
			<div class="alert success">${message}</div>
		</c:if>
		<c:if test="${not empty error}">
			<div class="alert error">${error}</div>
		</c:if>

		<p style="margin-bottom: 12px;">
			<a class="btn-accent" href="<c:url value='/ledgers/${ledgerId}/categories/new'/>">새 카테고리</a> 
			<a class="btn" href="<c:url value='/ledgers/${ledgerId}'/>">← 가계부 홈</a>
		</p>

		<c:forEach var="r" items="${roots}">
			<div class="card" style="margin-bottom: 12px;">
				<div style="display: flex; justify-content: space-between; align-items: center;">
					<div>
						<strong>${r.name}</strong>
						<c:if test="${r.system}">
							<span class="badge">SYSTEM</span>
						</c:if>
					</div>
					<div>
						<c:if test="${not r.system}">
							<a class="btn"
								href="<c:url value='/ledgers/${ledgerId}/categories/${r.id}/edit'/>">수정</a>
							<button class="btn danger js-del" data-id="${r.id}"
								style="margin-left: 8px;">삭제</button>
						</c:if>
					</div>
				</div>

				<c:if test="${not empty r.children}">
					<ul style="margin-top: 8px; padding-left: 18px;">
						<c:forEach var="cNode" items="${r.children}">
							<li
								style="margin: 6px 0; display: flex; justify-content: space-between; align-items: center;">
								<span> ${cNode.name} <c:if test="${cNode.system}">
										<span class="badge">SYSTEM</span>
									</c:if>
							</span> <span> <c:if test="${not cNode.system}">
										<a class="btn"
											href="<c:url value='/ledgers/${ledgerId}/categories/${cNode.id}/edit'/>">수정</a>
										<button class="btn danger js-del" data-id="${cNode.id}"
											style="margin-left: 8px;">삭제</button>
									</c:if>
							</span>
							</li>
						</c:forEach>
					</ul>
				</c:if>
			</div>
		</c:forEach>
	</div>
	</main>

	<script src="<c:url value='/resources/js/common.js'/>"></script>
	<script>
		// 컨텍스트 경로를 포함한 베이스 URL을 JSP에서 한번에 생성
		var deleteBaseUrl = '<c:url value="/ledgers/${ledgerId}/categories/"/>'; // 끝에 슬래시 포함
		
		document.addEventListener('click', async (e) => {
			const btn = e.target.closest('.js-del');
			if(!btn) return;
			const id = btn.getAttribute('data-id');
			if(!id) return;
			if(!confirm('정말 삭제하시겠습니까?')) return;
			
			try {
				const res = await fetch(deleteBaseUrl + id, { method: 'DELETE' });
				const json = await res.json();
				if(!json.ok) { 
					alert(json.message || '삭제 실패'); 
					return;
				}
				location.reload(); // 성공 시 새로고침
			} catch(err){
				alert('네트워크 오류로 삭제하지 못했습니다.');
			}
		});
	</script>


</body>
</html>
