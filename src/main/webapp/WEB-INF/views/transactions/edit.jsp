<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>거래 내역 수정</title>
<link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
</head>
<body>
  <jsp:include page="/WEB-INF/views/common/header.jsp" />
  <jsp:include page="/WEB-INF/views/common/aside.jsp" />

  <main id="main"
        data-ledger-id="${ledgerId}"
        data-selected-root-id="${selectedRootId}"
        data-selected-child-id="${selectedChildId}">
    <div class="container">
      <h2>거래 내역 수정</h2>

      <c:if test="${not empty error}">
        <div class="alert error">${error}</div>
      </c:if>

      <form id="transaction-form" method="post"
            action="<c:url value='/ledgers/${ledgerId}/transaction/${transaction.id}'/>"
            class="card">

        <div class="form-group">
          <label for="date">일자</label>
          <input type="date" id="date" name="date" value="${form.date}" required />
        </div>

        <div class="form-group">
          <label for="type">유형</label>
          <select id="type" name="type" required>
            <c:forEach var="t" items="${transactionTypes}">
              <option value="${t.name()}" <c:if test="${t.name()==form.type}">selected</c:if>>
                ${t.name()}
              </option>
            </c:forEach>
          </select>
        </div>

        <div class="form-group">
          <label for="amount">금액(원)</label>
          <input type="number" id="amount" name="amount" min="0" step="100" value="${form.amount}" required />
        </div>

        <div class="form-group">
          <label for="accountId">자산</label>
          <select id="accountId" name="accountId" required>
            <c:forEach var="a" items="${accounts}">
              <option value="${a.id}" <c:if test="${a.id == form.accountId}">selected</c:if>>
                ${a.name}
              </option>
            </c:forEach>
          </select>
        </div>

        <div class="form-group">
          <label for="rootCategoryId">카테고리(상위)</label>
          <select id="rootCategoryId" required>
            <option value="">(선택)</option>
            <c:forEach var="r" items="${rootCategories}">
              <option value="${r.id}" <c:if test="${r.id == selectedRootId}">selected</c:if>>
                ${r.name}
              </option>
            </c:forEach>
          </select>
        </div>

        <div class="form-group">
          <label for="childCategoryId">카테고리(하위)</label>
          <select id="childCategoryId">
            <option value="">(상위를 먼저 선택하세요)</option>
          </select>
        </div>

        <input type="hidden" name="categoryId" id="categoryIdHidden" value="${form.categoryId}" />

        <div class="form-group">
          <label for="memo">메모</label>
          <input type="text" id="memo" name="memo" value="${form.memo}" />
        </div>

        <div class="toolbar">
          <button type="submit" class="btn btn-primary">저장</button>
          <a class="btn"
             href="<c:url value='/ledgers/${ledgerId}/transaction?month=${form.date.substring(0,7)}'/>">취소</a>
        </div>
      </form>
    </div>
  </main>

  <script src="<c:url value='/resources/js/common.js'/>"></script>
  <script>
    // API 베이스
    var childApiBase = '<c:url value="/api/ledgers/${ledgerId}/categories/"/>'; // + {rootId}/children

    // 요소
    var mainEl   = document.getElementById('main');
    var rootSel  = document.getElementById('rootCategoryId');
    var childSel = document.getElementById('childCategoryId');
    var hiddenId = document.getElementById('categoryIdHidden');
    var formEl   = document.getElementById('transaction-form');

    var selectedRootId  = mainEl.dataset.selectedRootId || '';
    var selectedChildId = mainEl.dataset.selectedChildId || '';

    function clearSelectOptions(sel){ while(sel.firstChild) sel.removeChild(sel.firstChild); }
    function setChildOptions(list, preselect){
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
          if (preselect && preselect === it.id) opt.selected = true;
          childSel.appendChild(opt);
        });
      } else {
        var none = document.createElement('option');
        none.value = '';
        none.textContent = '(하위 없음)';
        childSel.appendChild(none);
      }
    }
    async function loadChildren(rootId, preselectChild){
      clearSelectOptions(childSel);
      if (!rootId){
        var msg = document.createElement('option');
        msg.value = '';
        msg.textContent = '(상위를 먼저 선택하세요)';
        childSel.appendChild(msg);
        return;
      }
      try{
        var res  = await fetch(childApiBase + encodeURIComponent(rootId) + '/children', { method:'GET' });
        var list = await res.json(); // [{id,name}]
        setChildOptions(list, preselectChild);
      }catch(e){
        clearSelectOptions(childSel);
        var err = document.createElement('option');
        err.value = '';
        err.textContent = '(하위 불러오기 실패)';
        childSel.appendChild(err);
      }
    }

    rootSel.addEventListener('change', function(){
      loadChildren(rootSel.value, '');
    });

    formEl.addEventListener('submit', function(e){
      var rootId  = rootSel.value || '';
      var childId = childSel.value || '';
      if (!rootId && !childId){
        e.preventDefault();
        alert('상위 또는 하위 카테고리를 선택해 주세요.');
        return;
      }
      hiddenId.value = childId || rootId;
    });

    document.addEventListener('DOMContentLoaded', function(){
      if (rootSel.value){
        loadChildren(rootSel.value, selectedChildId);
      }
    });
  </script>
</body>
</html>
