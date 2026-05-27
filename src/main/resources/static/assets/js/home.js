/**
 * home.js – DriveEasy
 * Chỉ chạy trên trang home:
 *   - Set min date cho date picker
 *   - Validate search form trước khi submit
 *   - Scroll reveal nhẹ cho các section
 */
(function () {
    'use strict';

    /* ── Date picker: ngày tối thiểu = hôm nay ── */
    const today = new Date().toISOString().split('T')[0];
    const dateFrom = document.getElementById('dateFrom');
    const dateTo   = document.getElementById('dateTo');

    if (dateFrom) {
        dateFrom.min = today;
        dateFrom.addEventListener('change', function () {
            if (dateTo) {
                dateTo.min = this.value || today;
                if (dateTo.value && dateTo.value < this.value) {
                    dateTo.value = this.value;
                }
            }
        });
    }
    if (dateTo) {
        dateTo.min = today;
    }

    /* ── Search form validation ── */
    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', function (e) {
            const from = dateFrom ? dateFrom.value : '';
            const to   = dateTo   ? dateTo.value   : '';

            if (from && to && to < from) {
                e.preventDefault();
                alert('Ngày trả xe phải sau ngày nhận xe!');
                dateTo.focus();
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
                    entry.target.style.opacity  = '1';
                    entry.target.style.transform = 'translateY(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.12 });

        targets.forEach((el, i) => {
            el.style.opacity   = '0';
            el.style.transform = 'translateY(20px)';
            el.style.transition = `opacity .45s ease ${i * 0.07}s, transform .45s ease ${i * 0.07}s`;
            observer.observe(el);
        });
    }

})();