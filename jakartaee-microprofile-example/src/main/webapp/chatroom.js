    var messagesTableBody = document.getElementById('messagesTableBody');
    var thinkingRow = document.createElement('tr');
    thinkingRow.setAttribute('id', 'thinking');
    thinkingRow.innerHTML = '<td><p class=\"thinking-msg\">thinking...</p></td>' + 
                            '<td></td>';

    function getTime() {
        var now = new Date();
        var hours = now.getHours();
        hours = hours < 10 ? '0' + hours : hours;
        var minutes = now.getMinutes();
        minutes = minutes < 10 ? '0' + minutes : minutes;
        var seconds = now.getSeconds();
        seconds = seconds < 10 ? '0' + seconds : seconds;
        var time = hours + ":" + minutes + ":" + seconds;
        return time;
    }

    function sendMessage() {
        var myMessageRow = document.createElement('tr');
        var myMessage = document.getElementById('myMessage').value;
        myMessageRow.innerHTML = '<td><p class=\"my-msg\">' + myMessage + '</p></td>' + 
                                 '<td>' + getTime() + '</td>';
        messagesTableBody.appendChild(myMessageRow);
        messagesTableBody.appendChild(thinkingRow);
        webSocket.send(myMessage);
        document.getElementById('myMessage').value = "";
    }

    // Getting the used url from browser
    var loc = window.location, uri;
    if (loc.protocol === "https:") {
        uri = "wss:";
    } else {
        uri = "ws:";
    }
    uri += "//" + loc.host;
    uri += loc.pathname + "chat";
    // buildign websocket
    const webSocket = new WebSocket(uri);

    webSocket.onopen = function (event) {
        console.log(event);
    };
 
    webSocket.onmessage = function (event) {
        var data = event.data;
        messagesTableBody.removeChild(thinkingRow);
        var agentMessageRow = document.createElement('tr');
        agentMessageRow.innerHTML = '<td><p class=\"agent-msg\">' + data + '</p></td>' +
                                    '<td>' + getTime() + '</td>';
        messagesTableBody.appendChild(agentMessageRow);
    };

    webSocket.onerror = function (event) {
        console.log(event);
    };