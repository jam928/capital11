// Theme button: cycles System -> Light -> Dark and remembers the choice in this browser.
// The inline script in fragments.html applies the saved choice before the page paints.
(function () {
    const order = ['system', 'light', 'dark'];
    const names = { system: 'System', light: 'Light', dark: 'Dark' };
    const root = document.documentElement;

    function current() {
        return order.includes(root.dataset.themePref) ? root.dataset.themePref : 'system';
    }

    function label(button, pref) {
        const next = order[(order.indexOf(pref) + 1) % order.length];
        const text = 'Theme: ' + names[pref] + ' (switch to ' + names[next] + ')';
        button.setAttribute('aria-label', text);
        button.title = text;
    }

    function apply(pref) {
        root.dataset.themePref = pref;
        if (pref === 'system') {
            delete root.dataset.theme;
        } else {
            root.dataset.theme = pref;
        }
        try {
            if (pref === 'system') {
                localStorage.removeItem('theme');
            } else {
                localStorage.setItem('theme', pref);
            }
        } catch (e) {
            // Storage blocked (e.g. private mode): the choice lasts for this page only.
        }
        document.querySelectorAll('[data-theme-toggle]').forEach(button => label(button, pref));
    }

    document.querySelectorAll('[data-theme-toggle]').forEach(button => {
        label(button, current());
        button.addEventListener('click', () => {
            apply(order[(order.indexOf(current()) + 1) % order.length]);
        });
    });
})();
