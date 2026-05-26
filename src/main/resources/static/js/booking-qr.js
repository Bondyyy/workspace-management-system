document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".qr-copy-button").forEach((button) => {
        button.addEventListener("click", async () => {
            const value = button.dataset.copy || "";
            if (!value.trim()) {
                return;
            }

            const originalText = button.textContent;
            try {
                if (navigator.clipboard && window.isSecureContext) {
                    await navigator.clipboard.writeText(value);
                } else {
                    const textarea = document.createElement("textarea");
                    textarea.value = value;
                    textarea.style.position = "fixed";
                    textarea.style.opacity = "0";
                    document.body.appendChild(textarea);
                    textarea.select();
                    document.execCommand("copy");
                    document.body.removeChild(textarea);
                }

                button.textContent = "Đã sao chép";
                button.classList.add("is-copied");
                window.setTimeout(() => {
                    button.textContent = originalText;
                    button.classList.remove("is-copied");
                }, 1500);
            } catch (error) {
                button.textContent = "Không thể sao chép";
                window.setTimeout(() => {
                    button.textContent = originalText;
                }, 1500);
            }
        });
    });
});
