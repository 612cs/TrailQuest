(function () {
  const stage = document.getElementById("deckStage");
  const slides = Array.from(document.querySelectorAll(".slide"));
  const progressBar = document.getElementById("progressBar");
  const pageIndicator = document.getElementById("pageIndicator");
  const prevBtn = document.getElementById("prevBtn");
  const nextBtn = document.getElementById("nextBtn");
  const overview = document.getElementById("overview");
  const overviewGrid = document.getElementById("overviewGrid");
  const presenter = document.getElementById("presenter");
  const presenterCurrent = document.getElementById("presenterCurrent");
  const presenterNext = document.getElementById("presenterNext");
  const presenterScript = document.getElementById("presenterScript");
  const timerText = document.getElementById("timerText");
  const timerSlide = document.getElementById("timerSlide");
  const resetTimer = document.getElementById("resetTimer");

  let current = readInitialIndex();
  let startedAt = Date.now();

  function readInitialIndex() {
    const raw = window.location.hash.replace("#/", "").replace("#", "");
    const n = Number.parseInt(raw, 10);
    if (Number.isFinite(n) && n >= 1 && n <= slides.length) {
      return n - 1;
    }
    return 0;
  }

  function fitStage() {
    const scale = Math.min(window.innerWidth / 1600, window.innerHeight / 900);
    const x = Math.max(0, (window.innerWidth - 1600 * scale) / 2);
    const y = Math.max(0, (window.innerHeight - 900 * scale) / 2);
    stage.style.transform = `translate(${x}px, ${y}px) scale(${scale})`;
  }

  function go(index, pushHash = true) {
    current = Math.max(0, Math.min(index, slides.length - 1));
    slides.forEach((slide, i) => {
      slide.classList.toggle("is-active", i === current);
    });
    progressBar.style.width = `${((current + 1) / slides.length) * 100}%`;
    pageIndicator.textContent = `${current + 1} / ${slides.length}`;
    if (pushHash) {
      history.replaceState(null, "", `#/${current + 1}`);
    }
    updateOverview();
    updatePresenter();
  }

  function next() {
    go(current + 1);
  }

  function prev() {
    go(current - 1);
  }

  function buildOverview() {
    overviewGrid.innerHTML = "";
    slides.forEach((slide, i) => {
      const card = document.createElement("button");
      card.className = "overview-card";
      card.type = "button";
      card.innerHTML = `<b>${String(i + 1).padStart(2, "0")}</b><span>${slide.dataset.title || "未命名"}</span>`;
      card.addEventListener("click", () => {
        closeOverview();
        go(i);
      });
      overviewGrid.appendChild(card);
    });
  }

  function updateOverview() {
    const cards = overviewGrid.querySelectorAll(".overview-card");
    cards.forEach((card, i) => card.classList.toggle("is-current", i === current));
  }

  function openOverview() {
    overview.classList.add("is-open");
    overview.setAttribute("aria-hidden", "false");
    updateOverview();
  }

  function closeOverview() {
    overview.classList.remove("is-open");
    overview.setAttribute("aria-hidden", "true");
  }

  function makeMiniSlide(index) {
    const wrapper = document.createElement("div");
    const clone = slides[index].cloneNode(true);
    clone.classList.add("is-active");
    wrapper.appendChild(clone);
    return wrapper.innerHTML;
  }

  function updatePresenter() {
    if (!presenter.classList.contains("is-open")) {
      return;
    }
    const nextIndex = Math.min(current + 1, slides.length - 1);
    presenterCurrent.innerHTML = makeMiniSlide(current);
    presenterNext.innerHTML = makeMiniSlide(nextIndex);
    const notes = slides[current].querySelector(".notes");
    presenterScript.innerHTML = notes ? notes.innerHTML : "本页暂无讲稿。";
    timerSlide.textContent = `${current + 1} / ${slides.length}`;
  }

  function openPresenter() {
    presenter.classList.add("is-open");
    presenter.setAttribute("aria-hidden", "false");
    updatePresenter();
  }

  function closePresenter() {
    presenter.classList.remove("is-open");
    presenter.setAttribute("aria-hidden", "true");
  }

  function updateTimer() {
    const elapsed = Math.floor((Date.now() - startedAt) / 1000);
    const mm = String(Math.floor(elapsed / 60)).padStart(2, "0");
    const ss = String(elapsed % 60).padStart(2, "0");
    timerText.textContent = `${mm}:${ss}`;
  }

  function toggleFullscreen() {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen?.();
    } else {
      document.exitFullscreen?.();
    }
  }

  prevBtn.addEventListener("click", prev);
  nextBtn.addEventListener("click", next);
  resetTimer.addEventListener("click", () => {
    startedAt = Date.now();
    updateTimer();
  });

  document.addEventListener("keydown", (event) => {
    const key = event.key.toLowerCase();
    if (key === "arrowright" || key === "pagedown" || key === " ") {
      event.preventDefault();
      next();
    } else if (key === "arrowleft" || key === "pageup") {
      event.preventDefault();
      prev();
    } else if (key === "home") {
      go(0);
    } else if (key === "end") {
      go(slides.length - 1);
    } else if (key === "s") {
      presenter.classList.contains("is-open") ? closePresenter() : openPresenter();
    } else if (key === "o") {
      overview.classList.contains("is-open") ? closeOverview() : openOverview();
    } else if (key === "f") {
      toggleFullscreen();
    } else if (key === "escape") {
      closeOverview();
      closePresenter();
    }
  });

  overview.addEventListener("click", (event) => {
    if (event.target === overview) closeOverview();
  });

  presenter.addEventListener("click", (event) => {
    if (event.target === presenter) closePresenter();
  });

  window.addEventListener("resize", fitStage);
  window.addEventListener("hashchange", () => go(readInitialIndex(), false));

  buildOverview();
  fitStage();
  go(current, false);
  setInterval(updateTimer, 1000);
  updateTimer();
})();
