(function () {
    document.documentElement.classList.add("landing-js");

    var nav = document.querySelector("[data-landing-nav]");
    if (!nav) {
        return;
    }

    var toggle = nav.querySelector(".landing-menu-toggle");
    var menu = document.getElementById("landingMenu");
    var sectionLinks = document.querySelectorAll("[data-section-link]");

    function setMenuOpen(isOpen) {
        if (!toggle || !menu) {
            return;
        }
        toggle.setAttribute("aria-expanded", String(isOpen));
        menu.classList.toggle("is-open", isOpen);
        nav.classList.toggle("is-menu-open", isOpen);
    }

    if (toggle && menu) {
        toggle.addEventListener("click", function () {
            setMenuOpen(toggle.getAttribute("aria-expanded") !== "true");
        });
    }

    sectionLinks.forEach(function (link) {
        link.addEventListener("click", function (event) {
            var targetSelector = link.getAttribute("href");
            if (!targetSelector || targetSelector.charAt(0) !== "#") {
                return;
            }
            var target = document.querySelector(targetSelector);
            if (!target) {
                return;
            }
            event.preventDefault();
            setMenuOpen(false);
            target.scrollIntoView({
                behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth",
                block: "start"
            });
            history.replaceState(null, "", targetSelector);
        });
    });

    if ("IntersectionObserver" in window) {
        var revealObserver = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add("is-visible");
                    revealObserver.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.12
        });

        document.querySelectorAll(".landing-reveal").forEach(function (element) {
            revealObserver.observe(element);
        });
    } else {
        document.querySelectorAll(".landing-reveal").forEach(function (element) {
            element.classList.add("is-visible");
        });
    }

    var observedSections = Array.prototype.slice.call(document.querySelectorAll("main section[id]"));
    if ("IntersectionObserver" in window && observedSections.length > 0) {
        var activeObserver = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (!entry.isIntersecting) {
                    return;
                }
                var activeId = "#" + entry.target.id;
                sectionLinks.forEach(function (link) {
                    link.classList.toggle("is-active", link.getAttribute("href") === activeId);
                });
            });
        }, {
            rootMargin: "-35% 0px -55% 0px"
        });

        observedSections.forEach(function (section) {
            activeObserver.observe(section);
        });
    }
}());
