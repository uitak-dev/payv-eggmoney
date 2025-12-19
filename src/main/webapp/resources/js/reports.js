google.charts.load('current', { packages: ['corechart'] });
google.charts.setOnLoadCallback(drawCharts);

function drawCharts() {
  // === 카테고리별 지출 ===
  if (!pieData || pieData.length <= 1) {
    document.getElementById('no-data').style.display = '';
    document.getElementById('chart-categories').style.display = 'none';
  } else {
    var data = google.visualization.arrayToDataTable(pieData);
    var options = {
      pieHole: 0.5,
      legend: { position: 'right' },
      chartArea: { left: 20, top: 20, width: '90%', height: '80%' },
      height: 480
    };
    new google.visualization.PieChart(
      document.getElementById('chart-categories')
    ).draw(data, options);
  }

  // === 월별 수입/지출 ===
  if (monthlyData) {
    var data2 = google.visualization.arrayToDataTable(monthlyData);
    var options2 = {
      legend: { position: 'top' },
      height: 480,
      vAxis: { format: 'short' },
      bar: { groupWidth: '70%' },
      chartArea: { left: 60, right: 20, width: '90%', height: '80%' },
      colors: ['#1a73e8', '#d93025']
    };
    new google.visualization.ColumnChart(
      document.getElementById('chart-monthly')
    ).draw(data2, options2);
  }
}

// === 탭 버튼 토글 ===
document.addEventListener('DOMContentLoaded', function () {
  const btnCat = document.getElementById('tab-categories');
  const btnMon = document.getElementById('tab-monthly');
  const secCat = document.getElementById('section-categories');
  const secMon = document.getElementById('section-monthly');

  btnCat.addEventListener('click', () => {
    btnCat.classList.add('active');
    btnMon.classList.remove('active');
    secCat.classList.add('active');
    secMon.classList.remove('active');
    drawCharts();
  });

  btnMon.addEventListener('click', () => {
    btnMon.classList.add('active');
    btnCat.classList.remove('active');
    secMon.classList.add('active');
    secCat.classList.remove('active');
    drawCharts();
  });
});

// 반응형 리사이즈
window.addEventListener('resize', () => {
  if (google && google.visualization) {
    drawCharts();
  }
});
