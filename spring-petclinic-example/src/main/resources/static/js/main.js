"use strict";

var usernamePage = document.querySelector("#username-page");
var chatPage = document.querySelector("#chat-page");
var usernameForm = document.querySelector("#usernameForm");
var messageForm = document.querySelector("#messageForm");
var messageInput = document.querySelector("#message");
var messageArea = document.querySelector("#messageArea");
var connectingElement = document.querySelector(".connecting");

var stompClient = null;
var username = null;
//mycode
var password = null;

var colors = [
  "#2196F3",
  "#32c787",
  "#00BCD4",
  "#ff5652",
  "#ffc107",
  "#ff85af",
  "#FF9800",
  "#39bbb0",
  "#fcba03",
  "#fc0303",
  "#de5454",
  "#b9de54",
  "#54ded7",
  "#54ded7",
  "#1358d6",
  "#d611c6",
];

function connect(event) {
  username = document.querySelector("#name").value.trim();
  if (username) {
      usernamePage.classList.add("hidden");
      chatPage.classList.remove("hidden");
      var socket = new SockJS("/websocket");
      stompClient = Stomp.over(socket);
      stompClient.connect({}, onConnected, onError);
  }
  event.preventDefault();
}

function onConnected() {
  // Subscribe to the Public Topic
  stompClient.subscribe("/topic/public", onMessageReceived);

  // Tell your username to the server
  stompClient.send(
    "/app/chat.register",
    {},
    JSON.stringify({ sender: username, type: "JOIN" })
  );

  connectingElement.classList.add("hidden");
}

function onError(error) {
  connectingElement.textContent =
    "Could not connect to WebSocket! Please refresh the page and try again or contact your administrator.";
  connectingElement.style.color = "red";
}

function send(event) {
  var messageContent = messageInput.value.trim();

  if (messageContent && stompClient) {
    var chatMessage = {
      sender: username,
      content: messageInput.value,
      type: "CHAT",
    };
    addMessage(chatMessage);

    stompClient.send("/app/chat.send", {}, JSON.stringify(chatMessage));
    messageInput.value = "";
  }
  event.preventDefault();
}

/**
 * Handles the received message and updates the chat interface accordingly.
 * param {Object} payload - The payload containing the message data.
 */
function onMessageReceived(payload) {
  var message = JSON.parse(payload.body);
  addMessage(message)
  messageArea.scrollTop = messageArea.scrollHeight;
}

function addMessage(message){
  var messageElement = document.createElement("li");

  if (message.type === "JOIN") {
    message.content = message.sender + " joined!";
  } else if (message.type === "LEAVE") {
    message.content = message.sender + " left!";
  } else {
    var usernameElement = document.createElement("span");
    var usernameText = document.createTextNode(message.sender);
    usernameElement.appendChild(usernameText);
    messageElement.appendChild(usernameElement);
    // * update
    usernameElement.style["color"] = getAvatarColor(message.sender);
    //* update end
  }

  var textElement = document.createElement("p");
  var messageText = document.createTextNode(message.content);
  textElement.appendChild(messageText);

  messageElement.appendChild(textElement);
  // * update
  if (message.sender === username && message.type !== "JOIN") {
    // Add a class to float the message to the right
    messageElement.classList.add("own-message");
  } else if (message.type === "JOIN" || message.type === "LEAVE") {
    messageElement.classList.add("event-message");
  }
  messageArea.appendChild(messageElement);
}

function getAvatarColor(messageSender) {
  var hash = 0;
  for (var i = 0; i < messageSender.length; i++) {
    hash = 31 * hash + messageSender.charCodeAt(i);
  }

  var index = Math.abs(hash % colors.length);
  return colors[index];
}

usernameForm.addEventListener("submit", connect, true);
messageForm.addEventListener("submit", send, true);
