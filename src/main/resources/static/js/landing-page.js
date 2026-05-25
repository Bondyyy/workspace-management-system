(function () {
    document.documentElement.classList.add("landing-js");

    var nav = document.querySelector("[data-landing-nav]");
    if (!nav) {
        return;
    }

    var toggle = nav.querySelector(".landing-menu-toggle");
    var menu = document.getElementById("landingMenu");
    var sectionLinks = document.querySelectorAll("[data-section-link]");
    var reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

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
                behavior: reduceMotion ? "auto" : "smooth",
                block: "start"
            });
            history.replaceState(null, "", targetSelector);
        });
    });

    document.querySelectorAll(".landing-process-card").forEach(function (card) {
        card.addEventListener("mouseenter", function () {
            if (!reduceMotion) {
                card.classList.add("is-hovered");
            }
        });
        card.addEventListener("mouseleave", function () {
            card.classList.remove("is-hovered");
        });
        card.addEventListener("focusin", function () {
            if (!reduceMotion) {
                card.classList.add("is-hovered");
            }
        });
        card.addEventListener("focusout", function () {
            card.classList.remove("is-hovered");
        });
    });

    if (reduceMotion) {
        document.querySelectorAll(".landing-reveal").forEach(function (element) {
            element.classList.add("is-visible");
        });
    } else if ("IntersectionObserver" in window) {
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

    function setActiveLink(targetSelector) {
        var activeSelector = targetSelector || "#top";
        sectionLinks.forEach(function (link) {
            link.classList.toggle("is-active", link.getAttribute("href") === activeSelector);
        });
    }

    function updateActiveSection() {
        var hero = document.querySelector(".landing-hero");
        var topBoundary = hero ? hero.offsetHeight * 0.5 : 320;
        if ((!window.location.hash || window.location.hash === "#top") && window.scrollY < topBoundary) {
            setActiveLink("#top");
            return;
        }

        var bestSelector = "#top";
        var bestVisible = 0;
        Array.prototype.slice.call(document.querySelectorAll("main section[id]")).forEach(function (section) {
            var rect = section.getBoundingClientRect();
            var visible = Math.min(rect.bottom, window.innerHeight) - Math.max(rect.top, 0);
            if (visible > bestVisible && rect.bottom > 0 && rect.top < window.innerHeight) {
                bestVisible = visible;
                bestSelector = "#" + section.id;
            }
        });
        setActiveLink(bestSelector);
    }

    sectionLinks.forEach(function (link) {
        link.addEventListener("click", function () {
            window.setTimeout(function () {
                setActiveLink(link.getAttribute("href"));
            }, 0);
        });
    });

    window.addEventListener("scroll", updateActiveSection, { passive: true });
    window.addEventListener("resize", updateActiveSection);
    if (window.location.hash) {
        setActiveLink(window.location.hash);
    } else {
        setActiveLink("#top");
    }
    updateActiveSection();
}());
