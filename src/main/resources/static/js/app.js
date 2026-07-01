document.addEventListener("DOMContentLoaded", () => {
  const canUseTheme = Boolean(document.querySelector("[data-theme-scope]"));

  if (!canUseTheme) {
    document.documentElement.classList.remove("theme-dark");
  }

  const applyTheme = (theme) => {
    if (!canUseTheme) return;

    const isDark = theme === "dark";
    document.documentElement.classList.toggle("theme-dark", isDark);
    localStorage.setItem("clinica-theme", theme);

    document.querySelectorAll("[data-theme-icon-light]").forEach((icon) => {
      icon.classList.toggle("hidden", !isDark);
    });

    document.querySelectorAll("[data-theme-icon-dark]").forEach((icon) => {
      icon.classList.toggle("hidden", isDark);
    });
  };

  const storedTheme = localStorage.getItem("clinica-theme");
  const currentTheme = document.documentElement.classList.contains("theme-dark")
    ? "dark"
    : storedTheme || "light";

  if (canUseTheme) {
    applyTheme(currentTheme);
  }

  document.querySelectorAll("[data-theme-toggle]").forEach((button) => {
    button.addEventListener("click", () => {
      const nextTheme = document.documentElement.classList.contains("theme-dark") ? "light" : "dark";
      applyTheme(nextTheme);
    });
  });

  if (window.lucide) {
    window.lucide.createIcons();
  }

  const toast = document.querySelector("[data-toast]");
  if (toast) {
    setTimeout(() => toast.remove(), 4500);
  }

  const modal = document.querySelector("[data-confirm-modal]");
  const confirmButton = document.querySelector("[data-confirm-submit]");
  const cancelButton = document.querySelector("[data-confirm-cancel]");
  let activeForm = null;

  document.querySelectorAll("[data-confirm-target]").forEach((button) => {
    button.addEventListener("click", () => {
      activeForm = document.getElementById(button.dataset.confirmTarget);
      if (!activeForm || !modal) return;
      modal.classList.remove("hidden");
      const message = button.dataset.confirmMessage;
      const messageNode = modal.querySelector("[data-confirm-message]");
      if (message && messageNode) {
        messageNode.textContent = message;
      }
    });
  });

  if (cancelButton) {
    cancelButton.addEventListener("click", () => {
      modal.classList.add("hidden");
      activeForm = null;
    });
  }

  if (confirmButton) {
    confirmButton.addEventListener("click", () => {
      if (activeForm) {
        activeForm.submit();
      }
    });
  }
});
