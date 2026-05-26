/**
 * home.js – DriveEasy
 * Chỉ chạy trên trang home:
 *   - Set min date cho date picker
 *   - Validate search form trước khi submit
 *   - Scroll reveal nhẹ cho các section
 */
(function () {
    'use strict';

    /* ── Date Picker Logic (Flatpickr) ── */
    const pickUpDisplay = document.getElementById('pickUpDisplay');
    const returnDisplay = document.getElementById('returnDisplay');
    const pickUpInput = document.getElementById('pickUpDate');   // hidden → gửi lên server
    const returnInput = document.getElementById('returnDate');    // hidden → gửi lên server
    const pickupWrap = document.getElementById('pickupWrap');
    const returnWrap = document.getElementById('returnWrap');

    // Biến lưu instance Flatpickr (khai báo ra ngoài khối if để form validation dùng được)
    let fp = null;

    if (pickUpDisplay && returnDisplay && typeof flatpickr !== 'undefined') {

        // 1. Hàm tính toán giờ mặc định: làm tròn 30 phút, cộng 2 tiếng
        function getDefaultDates() {
            const now = new Date();
            const m = now.getMinutes();
            // Math.ceil(m / 30) * 30 sẽ cho 0 -> 0, 1-30 -> 30, 31-60 -> 60
            const roundedMin = Math.ceil(m / 30) * 30;
            if (roundedMin === 60) {
                now.setHours(now.getHours() + 1);
                now.setMinutes(0);
            } else {
                now.setMinutes(roundedMin);
            }
            now.setHours(now.getHours() + 2); // Thêm 2 giờ cho việc chuẩn bị giao xe
            now.setSeconds(0);
            now.setMilliseconds(0);

            const start = new Date(now);
            const end = new Date(now);
            end.setDate(end.getDate() + 1); // Ngày trả = ngày mai cùng giờ
            return { start, end };
        }

        const defaults = getDefaultDates();

        // 2. Format hiển thị thân thiện cho người dùng: "22/05/2026 13:30"
        function formatDisplay(date) {
            const pad = n => String(n).padStart(2, '0');
            return `${pad(date.getDate())}/${pad(date.getMonth() + 1)}/${date.getFullYear()} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
        }

        // 3. Format ISO local ("yyyy-MM-ddTHH:mm") để gửi lên server
        function toLocalISO(date) {
            const tz = date.getTimezoneOffset() * 60000;
            return new Date(date.getTime() - tz).toISOString().slice(0, 16);
        }

        // 4. Khởi tạo Flatpickr, gắn vào ô văn bản hiển thị (pickUpDisplay)
        //    chứ không phải hidden input — tránh hiển thị giá trị thô ra màn hình
        fp = flatpickr(pickUpDisplay, {
            mode: "range",
            enableTime: true,
            minuteIncrement: 30,
            minDate: "today",
            showMonths: 2,         // Hiển 2 tháng cùng lúc giống Mioto
            locale: "vn",
            allowInput: false,     // Chỉ chọn bằng popup, không gõ tay
            disableMobile: true,   // Bắt buộc dùng Flatpickr trên cả mobile
            plugins: [
                new confirmDatePlugin({
                    confirmText: "Xác nhận",
                    showAlways: true,
                    theme: "light"
                })
            ],

            // 4a. Khi Flatpickr vừa khởi tạo xong → điền giá trị mặc định
            onReady: function (selectedDates, dateStr, instance) {
                // Gắn giá trị mặc định (không trigger onChange, false = không dispatch event)
                instance.setDate([defaults.start, defaults.end], false);
                // Cập nhật hiển thị và hidden inputs
                pickUpDisplay.value = formatDisplay(defaults.start);
                returnDisplay.value = formatDisplay(defaults.end);
                pickUpInput.value = toLocalISO(defaults.start);
                returnInput.value = toLocalISO(defaults.end);
            },

            // 4b. Mỗi khi người dùng click chọn ngày trên lịch
            onChange: function (selectedDates, dateStr, instance) {
                // Tìm nút Xác nhận trong popup
                const confirmBtn = instance.calendarContainer.querySelector('.flatpickr-confirm');

                if (selectedDates.length === 1) {
                    // Mới chọn ngày đi, chưa có ngày về → disable nút Xác nhận
                    if (confirmBtn) {
                        confirmBtn.style.pointerEvents = 'none';
                        confirmBtn.style.opacity = '0.4';
                        confirmBtn.style.cursor = 'not-allowed';
                    }
                    pickUpDisplay.value = formatDisplay(selectedDates[0]);
                    returnDisplay.value = '--/--/---- --:--';
                    pickUpInput.value = toLocalISO(selectedDates[0]);
                    returnInput.value = '';

                } else if (selectedDates.length === 2) {
                    // Đã chọn đủ cả 2 mốc thời gian → enable nút Xác nhận
                    if (confirmBtn) {
                        confirmBtn.style.pointerEvents = 'auto';
                        confirmBtn.style.opacity = '1';
                        confirmBtn.style.cursor = 'pointer';
                    }
                    pickUpDisplay.value = formatDisplay(selectedDates[0]);
                    returnDisplay.value = formatDisplay(selectedDates[1]);
                    pickUpInput.value = toLocalISO(selectedDates[0]);
                    returnInput.value = toLocalISO(selectedDates[1]);
                }
            }
        });

        // Click vào bất kỳ chỗ nào trong block Nhận xe hoặc Trả xe → mở popup lịch
        if (pickupWrap) pickupWrap.addEventListener('click', e => { e.preventDefault(); fp.open(); });
        if (returnWrap) returnWrap.addEventListener('click', e => { e.preventDefault(); fp.open(); });
    }

    /* ── Custom Dropdown Logic cho Region ── */
    // Đã sửa lại selector để chỉ chọn các dropdown-item trong form tìm kiếm (tránh ảnh hưởng navbar như nút Đăng xuất)
    const locationItems = document.querySelectorAll('#searchForm .dropdown-item');
    const locationDisplay = document.querySelector('#locationDisplay .text-placeholder');
    const locationValue = document.getElementById('locationValue');

    if (locationItems.length > 0 && locationDisplay && locationValue) {
        locationItems.forEach(item => {
            item.addEventListener('click', function (e) {
                e.preventDefault();
                // Thay chữ mờ xám bằng tên địa điểm được chọn, in đậm lên
                locationDisplay.textContent = this.textContent;
                locationDisplay.classList.add('fw-bold');
                locationDisplay.style.color = '#111827';
                // Lưu regionId vào hidden input để form gửi lên server
                locationValue.value = this.getAttribute('data-value');
                locationItems.forEach(i => i.classList.remove('active'));
                this.classList.add('active');
            });
        });
    }

    /* ── Search Form Validation ── */
    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', function (e) {
            // Sử dụng pickUpInput / returnInput đã khai báo phía trên
            const from = pickUpInput ? pickUpInput.value : '';
            const to = returnInput ? returnInput.value : '';
            // Nếu có nhập ngày mà ngày trả ≤ ngày nhận → cảnh báo
            if (from && to && to <= from) {
                e.preventDefault();
                alert('Thời gian trả xe phải sau thời gian nhận xe!');
            }
        });
    }

    /* ── Scroll reveal (chỉ khi browser hỗ trợ IntersectionObserver) ── */
    if ('IntersectionObserver' in window) {
        const targets = document.querySelectorAll(
            '.dv-feature-card, .dv-car-card, .dv-review-card, .dv-step'
        );

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.12 });

        targets.forEach((el, i) => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(20px)';
            el.style.transition = `opacity .45s ease ${i * 0.07}s, transform .45s ease ${i * 0.07}s`;
            observer.observe(el);
        });
    }

})();