(function () {
    const page = document.querySelector(".space-page");
    if (!page) {
        return;
    }

    const branchForm = document.querySelector("[data-branch-form]");
    const branchSelect = document.querySelector("[data-branch-select]");
    if (branchForm && branchSelect) {
        branchSelect.addEventListener("change", () => branchForm.submit());
    }

    const nodes = Array.from(document.querySelectorAll("[data-space-node]"));
    const selectedInput = document.getElementById("selectedSpaceName");
    const maKGInput = document.getElementById("maKG");
    const arrivalInput = document.getElementById("arrivalTime");
    const durationInput = document.getElementById("durationHours");
    const dateInput = document.getElementById("bookingDate");
    const startInput = document.getElementById("startHour");
    const endInput = document.getElementById("endHour");
    const timeFilterForm = document.getElementById("timeFilterForm");
    const bookingForm = document.getElementById("mapBookingForm");
    const canhBaoThoiGianInput = document.getElementById("canhBaoThoiGian");
    const thongBaoDatCho = document.getElementById("thongBaoDatCho");
    const totalLabel = document.getElementById("estimateTotal");
    const continueButton = document.getElementById("continueButton");
    let selectedPrice = 0;

    function minutes(value) {
        if (!value) {
            return 0;
        }
        const parts = value.split(":").map(Number);
        return (parts[0] || 0) * 60 + (parts[1] || 0);
    }

    function formatMoney(value) {
        return new Intl.NumberFormat("vi-VN").format(value) + " VNĐ";
    }

    function syncHiddenTime() {
        if (!dateInput || !startInput || !endInput || !arrivalInput || !durationInput || !totalLabel) {
            return;
        }
        const startMinutes = minutes(startInput.value);
        const endMinutes = minutes(endInput.value);
        let delta = endMinutes - startMinutes;
        if (delta <= 0) {
            delta += 24 * 60;
        }
        const duration = Math.max(1, Math.round(delta / 60));
        durationInput.value = duration;
        arrivalInput.value = dateInput.value && startInput.value ? dateInput.value + "T" + startInput.value : "";
        totalLabel.textContent = formatMoney(selectedPrice * duration);
    }

    function updateSubmitState() {
        if (continueButton && maKGInput) {
            continueButton.disabled = !maKGInput.value;
        }
    }

    function showMessage(message, type) {
        if (!thongBaoDatCho) {
            return;
        }
        thongBaoDatCho.textContent = message || "";
        thongBaoDatCho.className = "form-hint " + (type || "warning");
        thongBaoDatCho.style.display = message ? "block" : "none";
    }

    function clearSelection() {
        nodes.forEach((node) => node.classList.remove("selected"));
        selectedPrice = 0;
        if (maKGInput) {
            maKGInput.value = "";
        }
        if (selectedInput) {
            selectedInput.value = "Chưa chọn";
        }
        syncHiddenTime();
        updateSubmitState();
    }

    function validateTime() {
        syncHiddenTime();
        if (!dateInput || !startInput || !endInput) {
            return "";
        }
        if (!dateInput.value || !startInput.value || !endInput.value) {
            return "Vui lòng chọn đầy đủ ngày giờ đặt chỗ.";
        }

        const openValue = page.dataset.open || "07:00";
        const closeValue = page.dataset.close || "22:00";
        const startMin = minutes(startInput.value);
        const endMin = minutes(endInput.value);
        const openMin = minutes(openValue);
        const closeMin = minutes(closeValue);
        const overnight = openMin > closeMin;
        const allDay = openMin === closeMin;

        const startInHours = allDay
            || (!overnight && startMin >= openMin && startMin < closeMin)
            || (overnight && (startMin >= openMin || startMin < closeMin));
        let normalizedEndMin = endMin;
        if (overnight && endMin <= startMin) {
            normalizedEndMin += 24 * 60;
        }
        const normalizedCloseMin = allDay ? openMin + 24 * 60 : (overnight ? closeMin + 24 * 60 : closeMin);
        const normalizedStartMin = overnight && startMin < closeMin ? startMin + 24 * 60 : startMin;

        if (!startInHours) {
            return "Khung giờ đặt chỗ phải nằm trong giờ hoạt động của chi nhánh: " + openValue + " - " + closeValue + ".";
        }
        if (!allDay && normalizedEndMin > normalizedCloseMin) {
            return "Thời gian đặt chỗ không được vượt quá giờ đóng cửa của chi nhánh.";
        }
        if (normalizedEndMin <= normalizedStartMin) {
            return "Giờ kết thúc phải sau giờ bắt đầu.";
        }

        const startDate = new Date(dateInput.value + "T" + startInput.value + ":00");
        if (Number.isNaN(startDate.getTime()) || startDate <= new Date()) {
            return "Thời gian đặt chỗ không hợp lệ. Vui lòng chọn thời gian lớn hơn thời điểm hiện tại.";
        }
        return "";
    }

    function onTimeChanged() {
        clearSelection();
        const timeError = validateTime();
        if (timeError) {
            showMessage(timeError, "error");
            return;
        }
        showMessage("Bạn đã thay đổi thời gian. Vui lòng chọn lại không gian phù hợp với khung giờ mới.", "warning");
        if (canhBaoThoiGianInput) {
            canhBaoThoiGianInput.value = "chon-lai-khong-gian";
        }
        if (timeFilterForm) {
            window.setTimeout(() => timeFilterForm.submit(), 200);
        }
    }

    nodes.forEach((node) => {
        node.addEventListener("click", (event) => {
            if (event.target && event.target.closest("button[disabled]")) {
                return;
            }
            if (node.dataset.available !== "true") {
                return;
            }
            nodes.forEach((item) => item.classList.remove("selected"));
            node.classList.add("selected");
            selectedPrice = Number(node.dataset.price || 0);
            if (maKGInput) {
                maKGInput.value = node.dataset.id || "";
            }
            if (selectedInput) {
                selectedInput.value = node.dataset.name || "Không gian";
            }
            syncHiddenTime();
            updateSubmitState();
            showMessage("", "warning");
        });
    });

    [dateInput, startInput, endInput].forEach((input) => {
        if (input) {
            input.addEventListener("change", onTimeChanged);
        }
    });

    if (bookingForm) {
        bookingForm.addEventListener("submit", (event) => {
            const timeError = validateTime();
            if (timeError) {
                event.preventDefault();
                showMessage(timeError, "error");
                return;
            }
            if (!maKGInput || !maKGInput.value) {
                event.preventDefault();
                showMessage("Vui lòng chọn một không gian trống trên sơ đồ.", "warning");
            }
        });
    }

    syncHiddenTime();
    updateSubmitState();
})();
