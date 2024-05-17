var clientId = localStorage.getItem('clientId');
document.addEventListener('DOMContentLoaded', function() {
    if (!clientId) {
        window.location.href = "/";
    }
});
function setClientId() {
    document.getElementById('clientIdField').value = clientId;
}
