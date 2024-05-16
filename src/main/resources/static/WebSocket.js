var stompClient = null;

function setConnected(connected) {
    if (connected) {
        document.getElementById("connect").setAttribute("disabled", "");
        document.getElementById("disconnect").removeAttribute("disabled");
        document.getElementById("conversation").style.display = "block";
    }else {
        document.getElementById("conversation").style.display = "hidden";
        document.getElementById("connect").removeAttribute("disabled");
        document.getElementById("disconnect").setAttribute("disabled", "");
    }
    document.getElementById("messages").innerHTML = "";
}

function connect() {
    var socket = new SockJS("/my-websocket");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log("Connected: " + frame);
        stompClient.subscribe("/topic/messages", function (message) {
            showMessage(message.toString());
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
        }
        setConnected(false);
        console.log("Disconnected");
}

function showMessage(message) {
    console.log("Received message:", message);
    // document.getElementById("messages").insertRow();
    // document.getElementById("messages").append(message);
}

window.addEventListener('load',
    function () {
        document.getElementById("connect").addEventListener('click', (e) => {
            e.preventDefault();
            connect();
        });
        document.getElementById("disconnect").addEventListener('click', (e) => {
            e.preventDefault();
            disconnect();
        });
    }, false);
