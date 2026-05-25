(function () {
    "use strict";

    function normalizeNumberText(value, allowNegative) {
        if (!value) return "";
        var text = String(value).trim();
        var negative = allowNegative && text.charAt(0) === "-";
        var digits = text.replace(/[^0-9]/g, "");
        if (!digits) return negative ? "-" : "";
        digits = digits.replace(/^0+(?!$)/, "");
        return negative ? "-" + digits : digits;
    }

    function formatThousands(value, allowNegative) {
        var normalized = normalizeNumberText(value, allowNegative);
        if (!normalized || normalized === "-") return normalized;
        var negative = normalized.charAt(0) === "-";
        var digits = negative ? normalized.slice(1) : normalized;
        var grouped = digits.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
        return negative ? "-" + grouped : grouped;
    }

    function formatInput(input) {
        var allowNegative = input.dataset.allowNegative === "true";
        var before = input.value;
        var caretDigits = before.slice(0, input.selectionStart || 0).replace(/[^0-9]/g, "").length;
        input.value = formatThousands(before, allowNegative);

        var seen = 0;
        var caret = input.value.length;
        if (caretDigits === 0) {
            caret = input.value.charAt(0) === "-" ? 1 : 0;
        } else {
            for (var i = 0; i < input.value.length; i++) {
                if (/[0-9]/.test(input.value.charAt(i))) {
                    seen++;
                    if (seen === caretDigits) {
                        caret = i + 1;
                        break;
                    }
                }
            }
        }
        input.setSelectionRange(caret, caret);
    }

    function attach(input) {
        if (!input || input.dataset.formatAttached === "true") return;
        input.dataset.formatAttached = "true";
        input.addEventListener("input", function () {
            formatInput(input);
        });
        if (input.value) {
            formatInput(input);
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll(".js-money-input, .js-large-number-input").forEach(attach);

        document.querySelectorAll("form").forEach(function (form) {
            form.addEventListener("submit", function () {
                form.querySelectorAll(".js-money-input, .js-large-number-input").forEach(function (input) {
                    if (input.dataset.submitFormatted === "true") return;
                    var allowNegative = input.dataset.allowNegative === "true";
                    input.value = normalizeNumberText(input.value, allowNegative).replace(/^-$/, "");
                });
            });
        });
    });
})();
