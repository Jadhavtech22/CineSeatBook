const price = Number(document.currentScript.dataset.price);
const form = document.querySelector('.theatre');
function refresh() {
    const seats = [...form.querySelectorAll('input:checked')];
    document.querySelector('#amount').textContent = seats.length
        ? seats.map(input => input.value).join(', ') + ' · ₹' + seats.length * price
        : 'Select up to 8 seats.';
    document.querySelector('#review').disabled = seats.length === 0;
}
form.addEventListener('change', event => {
    if (form.querySelectorAll('input:checked').length > 8) {
        event.target.checked = false;
        refresh();
        document.querySelector('#amount').textContent += ' · Maximum 8 seats';
        return;
    }
    refresh();
});
window.addEventListener('pageshow', refresh);
refresh();
