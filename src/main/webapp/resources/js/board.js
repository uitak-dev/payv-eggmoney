(function () {
  const TRY_SERVER_CALLS = false; // 서버 준비되면 true로 변경
  const stats = document.querySelector('.post-stats');
  if (!stats) return;

  const boardId = stats.dataset.boardId;
  const likeBtn = stats.querySelector('.like-toggle');
  const likeCountEl = stats.querySelector('.like-count');

  // 게시글별 로컬 저장 키
  const LS_KEY = `liked_board_${boardId}`;

  // 초기 상태
  const initiallyLiked = localStorage.getItem(LS_KEY) === '1';
  likeBtn.setAttribute('aria-pressed', initiallyLiked ? 'true' : 'false');

  // 숫자 파싱/표시 도우미
  const toInt = (el) => parseInt(el.textContent.trim().replace(/[^0-9\-]/g, ''), 10) || 0;
  const setInt = (el, v) => { el.textContent = String(v); };

  /*
  async function likeOnServer(id) {
    // 준비된 서버라면 여기서 실제 호출
    const res = await fetch(`/boards/${id}/like`, { method: 'POST' });
    if (!res.ok) throw new Error('like failed');
  }
  async function unlikeOnServer(id) {
    const res = await fetch(`/boards/${id}/like`, { method: 'DELETE' });
    if (!res.ok) throw new Error('unlike failed');
  }
  

  likeBtn.addEventListener('click', async () => {
    const isPressed = likeBtn.getAttribute('aria-pressed') === 'true';
    const next = !isPressed;

    // 즉시 UI 반영
    likeBtn.setAttribute('aria-pressed', next ? 'true' : 'false');
    setInt(likeCountEl, toInt(likeCountEl) + (next ? 1 : -1));
    localStorage.setItem(LS_KEY, next ? '1' : '0');

    // 서버 연동 시도 (옵션)
    if (TRY_SERVER_CALLS) {
      try {
        if (next) await likeOnServer(boardId);
        else await unlikeOnServer(boardId);
      } catch (e) {
        // 실패하면 롤백
        likeBtn.setAttribute('aria-pressed', isPressed ? 'true' : 'false');
        setInt(likeCountEl, toInt(likeCountEl) + (next ? -1 : 1));
        localStorage.setItem(LS_KEY, isPressed ? '1' : '0');
        console.warn('서버 좋아요 연동 실패:', e);
      }
    }
  });
  */
})();
