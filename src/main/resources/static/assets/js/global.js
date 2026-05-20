(function () {
    'use strict';

    /* ── Navbar: đổi opacity khi scroll ── */
    const navbar = document.querySelector('.dv-navbar');
    if (navbar) {
        const onScroll = () => {
            if (window.scrollY > 40) {
                navbar.style.background = 'rgba(22,24,28,.98)';
            } else {
                navbar.style.background = 'rgba(22,24,28,.92)';
            }
        };
        window.addEventListener('scroll', onScroll, { passive: true });
    }

    /* ── Active nav link theo pathname ── */
    const currentPath = window.location.pathname;
    document.querySelectorAll('.dv-navbar__link').forEach(link => {
        const href = link.getAttribute('href');
        if (href && href !== '#' && currentPath.startsWith(href) && href !== '/') {
            link.classList.add('active');
        } else if (href === '/' && currentPath === '/') {
            link.classList.add('active');
        }
    });

})();