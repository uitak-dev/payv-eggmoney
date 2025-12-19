<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title></title>
<link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
</head>

<body>
	<jsp:include page="/WEB-INF/views/common/header.jsp" />
	<jsp:include page="/WEB-INF/views/common/aside.jsp" />

	<main id="main" data-ledger-id="${ledgerId}" data-month="${month}">
	<div class="container">
		<h2>예산 (월: ${month})</h2>

		<c:if test="${not empty message}">
			<div class="alert success">${message}</div>
		</c:if>
		<c:if test="${not empty error}">
			<div class="alert error">${error}</div>
		</c:if>

		<div class="toolbar" style="margin-bottom: 16px;">
			<a class="btn-accent"
				href="<c:url value='/ledgers/${ledgerId}/budgets/new?month=${month}'/>">예산
				추가</a> <a class="btn" href="<c:url value='/ledgers/${ledgerId}'/>"
				style="margin-left: 8px;">← 가계부 홈</a>
		</div>

		<div class="toolbar">
			<form method="get"
				action="<c:url value='/ledgers/${ledgerId}/budgets'/>"
				class="search-form">
				<label>월 선택: <input type="month" name="month"
					value="${month}" />
				</label>
				<button type="submit" class="btn btn-primary">조회</button>
			</form>
		</div>

		<table class="table" style="width: 100%; border-collapse: collapse;">
			<thead>
				<tr>
					<th
						style="text-align: left; padding: 8px; border-bottom: 1px solid #ccc;">카테고리</th>
					<th
						style="text-align: right; padding: 8px; border-bottom: 1px solid #ccc;">한도</th>
					<th
						style="text-align: right; padding: 8px; border-bottom: 1px solid #ccc;">사용액</th>
					<th
						style="text-align: center; padding: 8px; border-bottom: 1px solid #ccc;">작업</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="b" items="${budgets}">
					<tr>
						<td style="padding: 8px;">${b.categoryName}</td>
						<td style="padding: 8px; text-align: right;"><fmt:formatNumber
								value="${b.limit}" pattern="#,###" /></td>
						<td style="padding: 8px; text-align: right;"><fmt:formatNumber
								value="${b.spent}" pattern="#,###" /></td>
						<td style="padding: 8px; text-align: center;"><a class="btn"
							href="<c:url value='/ledgers/${ledgerId}/budgets/${b.id}/edit?month=${month}'/>">한도
								변경</a> <%-- 
							<button class="btn danger js-del" data-id="${b.id}" style="margin-left: 8px;">삭제</button>
							--%></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
	</main>

	<script src="<c:url value='/resources/js/common.js'/>"></script>

	<%--
	<script>
		// 삭제는 비동기(JSON)
		document.addEventListener('click', async (e)=>{
		  const btn = e.target.closest('.js-del');
		  if(!btn) return;
		
		  if(!confirm('정말 삭제하시겠습니까?')) return;
		
		  const budgetId = btn.getAttribute('data-id');
		  const month = document.getElementById('main').dataset.month;
		  const url = '<c:url value="/ledgers/${ledgerId}/budgets/"/>' + budgetId + '?month=' + encodeURIComponent(month);
		
		  try {
		    const res = await fetch(url, { method: 'DELETE' });
		    const json = await res.json();
		    if(!json.ok){
		      alert(json.message || '삭제 실패');
		      return;
		    }
		    location.reload();
		  } catch (err) {
		    alert('네트워크 오류로 삭제하지 못했습니다.');
		  }
		});
	</script>
	--%>

</body>
</html>
