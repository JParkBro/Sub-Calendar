// 앱 웹뷰가 ?theme=light|dark 를 붙이면 그 테마로, 없으면 시스템 설정을 따른다
(function () {
  var theme = new URLSearchParams(location.search).get("theme");
  if (theme === "light" || theme === "dark") document.documentElement.setAttribute("data-theme", theme);
})();
