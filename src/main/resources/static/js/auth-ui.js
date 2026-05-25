(function () {
    var reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    function setLoading(button) {
        if (!button || button.classList.contains("is-loading")) {
            return;
        }
        var label = button.querySelector(".btn-label");
        if (label && button.dataset.loadingText) {
            label.textContent = button.dataset.loadingText;
        }
        button.classList.add("is-loading");
        button.disabled = true;
    }

    document.querySelectorAll("form[data-loading-form]").forEach(function (form) {
        form.addEventListener("submit", function () {
            setLoading(form.querySelector(".js-submit-loading"));
        });
    });

    document.querySelectorAll("[data-toggle-password]").forEach(function (button) {
        button.addEventListener("click", function () {
            var selector = button.getAttribute("data-toggle-password");
            var input = selector ? document.querySelector(selector) : null;
            if (!input) {
                return;
            }
            var showing = input.type === "text";
            input.type = showing ? "password" : "text";
            button.setAttribute("aria-label", showing ? "Hiện mật khẩu" : "Ẩn mật khẩu");
            button.classList.toggle("is-visible", !showing);
            input.focus();
        });
    });

    if (!reduceMotion) {
        document.querySelectorAll(".pink-button, .landing-button, .landing-nav-btn, .auth-brand-link").forEach(function (button) {
            button.addEventListener("click", function (event) {
                if (button.classList.contains("is-loading")) {
                    return;
                }
                var ripple = document.createElement("span");
                var rect = button.getBoundingClientRect();
                var size = Math.max(rect.width, rect.height);
                ripple.className = "auth-click-ripple";
                ripple.style.width = size + "px";
                ripple.style.height = size + "px";
                ripple.style.left = (event.clientX - rect.left - size / 2) + "px";
                ripple.style.top = (event.clientY - rect.top - size / 2) + "px";
                button.appendChild(ripple);
                window.setTimeout(function () {
                    ripple.remove();
                }, 520);
            });
        });
    }
}());
