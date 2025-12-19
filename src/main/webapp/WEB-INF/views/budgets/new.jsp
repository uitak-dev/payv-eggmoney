<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

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

	<main id="main" data-ledger-id="${ledgerId}">
	<div class="container">
		<h2>예산 추가</h2>

		<c:if test="${not empty error}">
			<div class="alert error">${error}</div>
		</c:if>

		<form id="budget-form" method="post"
			action="<c:url value='/ledgers/${ledgerId}/budgets'/>" class="card">
			<div class="form-group">
				<label for="month">월</label> <input type="month" id="month"
					name="month" value="${form.month}" required />
			</div>

			<div class="form-group">
				<label for="rootCategoryId">카테고리(상위)</label> <select
					id="rootCategoryId" required>
					<option value="">(선택)</option>
					<c:forEach var="r" items="${rootCategories}">
						<option value="${r.id}">${r.name}</option>
					</c:forEach>
				</select>
			</div>

			<div class="form-group">
				<label for="childCategoryId">카테고리(하위)</label> <select
					id="childCategoryId">
					<option value="">(상위를 먼저 선택하세요)</option>
				</select>
			</div>
			
			<!-- 최종 전송용 hidden -->
	        <input type="hidden" name="categoryId" id="categoryIdHidden" />

			<div class="form-group">
				<label for="limit">한도(원)</label> <input type="number" id="limit"
					name="limit" min="0" step="100" value="${form.limit}" required />
			</div>

			<div class="toolbar">
				<button type="submit" class="btn btn-primary">저장</button>
				<a class="btn"
					href="<c:url value='/ledgers/${ledgerId}/budgets?month=${month}'/>">취소</a>
			</div>
		</form>

	</div>
	</main>

	<script src="<c:url value='/resources/js/common.js'/>"></script>
	<script>
    // API 베이스 (컨텍스트 경로 포함)
    var childApiBase = '<c:url value="/api/ledgers/${ledgerId}/categories/"/>'; // + {rootId}/children

    // 요소 참조
    var rootSel  = document.getElementById('rootCategoryId');
    var childSel = document.getElementById('childCategoryId');
    var hiddenId = document.getElementById('categoryIdHidden');
    var formEl   = document.getElementById('budget-form');

    function clearSelectOptions(sel) {
      while (sel.firstChild) sel.removeChild(sel.firstChild);
    }
    function setChildOptions(list) {
      clearSelectOptions(childSel);
      if (Array.isArray(list) && list.length > 0) {
        var base = document.createElement('option');
        base.value = '';
        base.textContent = '(하위 선택)';
        childSel.appendChild(base);
        list.forEach(function(it){
          var opt = document.createElement('option');
          opt.value = it.id;
          opt.textContent = it.name;
          childSel.appendChild(opt);
        });
      } else {
        var none = document.createElement('option');
        none.value = '';
        none.textContent = '(하위 없음)';
        childSel.appendChild(none);
      }
    }
    async function loadChildren(rootId) {
      clearSelectOptions(childSel);
      if (!rootId) {
        var msg = document.createElement('option');
        msg.value = '';
        msg.textContent = '(상위를 먼저 선택하세요)';
        childSel.appendChild(msg);
        return;
      }
      try {
        var res  = await fetch(childApiBase + encodeURIComponent(rootId) + '/children', { method: 'GET' });
        var list = await res.json(); // [{id,name}, ...]
        setChildOptions(list);
      } catch (e) {
        clearSelectOptions(childSel);
        var err = document.createElement('option');
        err.value = '';
        err.textContent = '(하위 불러오기 실패)';
        childSel.appendChild(err);
      }
    }
    rootSel.addEventListener('change', function(){
      loadChildren(rootSel.value);
    });

    // 제출: 하위 선택값 우선, 없으면 상위. 둘 다 없으면 막기.
    formEl.addEventListener('submit', function(e) {
      var rootId  = rootSel.value || '';
      var childId = childSel.value || '';
      if (!rootId && !childId) {
        e.preventDefault();
        alert('상위 또는 하위 카테고리를 선택해 주세요.');
        return;
      }
      hiddenId.value = childId || rootId;
    });

    document.addEventListener('DOMContentLoaded', function() {
      if (rootSel.value) {
        loadChildren(rootSel.value);
      }
    });
  </script>

</body>
</html>
