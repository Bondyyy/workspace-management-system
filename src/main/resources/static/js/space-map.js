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
    const durationPicker = document.getElementById("durationHoursPicker");
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
        if (!dateInput || !startInput || !durationPicker || !arrivalInput || !durationInput || !totalLabel) {
            return;
        }
        const duration = parseInt(durationPicker.value, 10);
        durationInput.value = Number.isInteger(duration) && duration >= 1 ? duration : "";
        arrivalInput.value = dateInput.value && startInput.value ? dateInput.value + "T" + startInput.value : "";
        totalLabel.textContent = formatMoney(selectedPrice * (Number.isInteger(duration) && duration >= 1 ? duration : 0));
    }

    function updateSubmitState() {
        if (continueButton && maKGInput) {
            continueButton.disabled = !maKGInput.value || Boolean(validateTime());
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
        if (!dateInput || !startInput || !durationPicker) {
            return "";
        }
        if (!dateInput.value || !startInput.value) {
            return "Vui lòng chọn đầy đủ ngày giờ đặt chỗ.";
        }
        const duration = parseInt(durationPicker.value, 10);
        if (!Number.isInteger(duration) || duration < 1) {
            return "Vui lòng chọn thời gian sử dụng hợp lệ.";
        }

        const openValue = page.dataset.open || "07:00";
        const closeValue = page.dataset.close || "22:00";
        const startMin = minutes(startInput.value);
        const openMin = minutes(openValue);
        const closeMin = minutes(closeValue);
        const allDay = openMin === closeMin || (openMin === 0 && closeMin === 24 * 60);
        const overnight = !allDay && openMin > closeMin;

        const startInHours = allDay
            || (!overnight && startMin >= openMin && startMin < closeMin)
            || (overnight && (startMin >= openMin || startMin < closeMin));
        const normalizedCloseMin = allDay ? openMin + 24 * 60 : (overnight ? closeMin + 24 * 60 : closeMin);
        const normalizedStartMin = overnight && startMin < closeMin ? startMin + 24 * 60 : startMin;
        const computedEndMin = normalizedStartMin + duration * 60;

        if (!startInHours) {
            return "Khung giờ đặt chỗ phải nằm trong giờ hoạt động của chi nhánh: " + openValue + " - " + closeValue + ".";
        }
        if (!allDay && computedEndMin > normalizedCloseMin) {
            return "Thời gian đặt chỗ không được vượt quá giờ đóng cửa của chi nhánh.";
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

    [dateInput, startInput, durationPicker].forEach((input) => {
        if (input) {
            input.addEventListener("change", onTimeChanged);
            input.addEventListener("input", () => {
                syncHiddenTime();
                updateSubmitState();
            });
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
