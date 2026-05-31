/**
 * home.js – DriveEasy
 * Chỉ chạy trên trang home:
 *   - Set min date cho date picker
 *   - Validate search form trước khi submit
 *   - Scroll reveal nhẹ cho các section
 */
(function () {
    'use strict';

    /* ── Region dropdown (custom select in search form) ── */
    document.querySelectorAll('#searchForm .ct-dropdown-menu .dropdown-item')
        .forEach(function (item) {
            item.addEventListener('click', function (e) {
                e.preventDefault();
                const name = this.textContent.trim();
                const val = this.dataset.value ||
                    this.getAttribute('data-value') || name;

                const display = document.getElementById('locationDisplay');
                if (display) {
                    display.innerHTML =
                        `<span style="color:#18181B; font-weight:600;">${name}</span>`;
                }

                const hidden = document.getElementById('locationValue');
                if (hidden) hidden.value = val;
            });
        });

    /* ── Date modal (home search bar) ── */
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const minDateStr = tomorrow.toISOString().split('T')[0];

    const homeModalPickupDate = document.getElementById('homeModalPickupDate');
    const homeModalReturnDate = document.getElementById('homeModalReturnDate');
    const homeModalPickupTime = document.getElementById('homeModalPickupTime');
    const homeModalReturnTime = document.getElementById('homeModalReturnTime');

    if (homeModalPickupDate) {
        homeModalPickupDate.min = minDateStr;
        homeModalReturnDate.min = minDateStr;
        homeModalPickupDate.value = minDateStr;

        const dayAfter = new Date(tomorrow);
        dayAfter.setDate(dayAfter.getDate() + 1);
        homeModalReturnDate.value = dayAfter.toISOString().split('T')[0];

        function formatDateVN(dateStr) {
            const parts = dateStr.split('-');
            return parts[2] + '/' + parts[1] + '/' + parts[0];
        }

        function updateHomeSummary() {
            const p = new Date(homeModalPickupDate.value);
            const r = new Date(homeModalReturnDate.value);
            if (isNaN(p) || isNaN(r)) return;
            let days = Math.ceil((r - p) / 86400000);
            if (days <= 0) {
                days = 1;
                const newReturn = new Date(p.getTime() + 86400000);
                homeModalReturnDate.value = newReturn.toISOString().split('T')[0];
            }
            document.getElementById('homeModalDateSummary').textContent =
                homeModalPickupTime.value + ' ' + formatDateVN(homeModalPickupDate.value) +
                ' - ' + homeModalReturnTime.value + ' ' + formatDateVN(homeModalReturnDate.value);
            document.getElementById('homeModalDaysSummary').textContent = days + ' ngày';
        }

        [homeModalPickupDate, homeModalReturnDate, homeModalPickupTime, homeModalReturnTime]
            .forEach(function (el) { el.addEventListener('change', updateHomeSummary); });

        updateHomeSummary();

        document.getElementById('homeBtnConfirmDate').addEventListener('click', function () {
            const pDate = homeModalPickupDate.value;
            const rDate = homeModalReturnDate.value;
            const pTime = homeModalPickupTime.value;
            const rTime = homeModalReturnTime.value;

            document.getElementById('homeDateFrom').value = pDate;
            document.getElementById('homeDateTo').value = rDate;

            function fmt(d) {
                const parts = d.split('-');
                return parts[2] + '/' + parts[1] + '/' + parts[0];
            }
            document.getElementById('homePickupText').textContent = fmt(pDate);
            document.getElementById('homePickupText').style.color = '#111827';
            document.getElementById('homeReturnText').textContent = fmt(rDate);
            document.getElementById('homeReturnText').style.color = '#111827';
        });

        homeModalPickupDate.addEventListener('change', function () {
            homeModalReturnDate.min = this.value;
            if (homeModalReturnDate.value <= this.value) {
                const next = new Date(this.value);
                next.setDate(next.getDate() + 1);
                homeModalReturnDate.value = next.toISOString().split('T')[0];
            }
            updateHomeSummary();
        });
    }

    /* ── Search form validation ── */
    const searchForm = document.getElementById('searchForm');
    const homeDateFrom = document.getElementById('homeDateFrom');
    const homeDateTo = document.getElementById('homeDateTo');
    if (searchForm) {
        searchForm.addEventListener('submit', function (e) {
            const from = homeDateFrom ? homeDateFrom.value : '';
            const to   = homeDateTo   ? homeDateTo.value   : '';

            if (from && to && to <= from) {
                e.preventDefault();
                alert('Ngày trả xe phải sau ngày nhận xe!');
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

document.addEventListener('DOMContentLoaded', function() {
  const hPickup = document.getElementById('homeModalPickupDate');
  const hReturn = document.getElementById('homeModalReturnDate');
  const hPickupTime = document.getElementById('homeModalPickupTime');
  const hReturnTime = document.getElementById('homeModalReturnTime');

  if (!hPickup) return;

  // Set min dates
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const minStr = tomorrow.toISOString().split('T')[0];
  hPickup.min = minStr;
  hReturn.min = minStr;
  hPickup.value = minStr;
  const dayAfter = new Date(tomorrow);
  dayAfter.setDate(dayAfter.getDate() + 1);
  hReturn.value = dayAfter.toISOString().split('T')[0];

  function fmt(d) {
    const parts = d.split('-');
    return parts[2] + '/' + parts[1] + '/' + parts[0];
  }

  function updateSummary() {
    const p = new Date(hPickup.value);
    const r = new Date(hReturn.value);
    if (isNaN(p) || isNaN(r)) return;
    let days = Math.ceil((r - p) / 86400000);
    if (days <= 0) {
      days = 1;
      const nr = new Date(p.getTime() + 86400000);
      hReturn.value = nr.toISOString().split('T')[0];
    }
    document.getElementById('homeModalDateSummary').textContent =
      hPickupTime.value + ' ' + fmt(hPickup.value) + ' - ' + hReturnTime.value + ' ' + fmt(hReturn.value);
    document.getElementById('homeModalDaysSummary').textContent =
      days + ' ngày';
  }

  [hPickup, hReturn, hPickupTime, hReturnTime]
    .forEach(function (el) { el.addEventListener('change', updateSummary); });
  updateSummary();

  hPickup.addEventListener('change', function() {
    hReturn.min = this.value;
    if (hReturn.value <= this.value) {
      const nr = new Date(this.value);
      nr.setDate(nr.getDate() + 1);
      hReturn.value = nr.toISOString().split('T')[0];
    }
    updateSummary();
  });

  document.getElementById('homeBtnConfirmDate')
    .addEventListener('click', function() {
      document.getElementById('homeDateFrom').value = hPickup.value;
      document.getElementById('homeDateTo').value = hReturn.value;

      document.getElementById('homePickupText').textContent = fmt(hPickup.value);
      document.getElementById('homePickupText').style.color = '#18181B';
      document.getElementById('homeReturnText').textContent = fmt(hReturn.value);
      document.getElementById('homeReturnText').style.color = '#18181B';
    });
});