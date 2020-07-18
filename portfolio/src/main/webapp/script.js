// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
    ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!',
      'Quote1', 'Quote2', 'Quote3'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');

  greetingContainer.innerText = greeting;
}

/**
 * Fetches stats from the servers and adds them to the DOM.
 */
function getComment() {
  const statsListElement = document.getElementById('comment-container');
  statsListElement.innerHTML = '';
  var toStop = true;
  var userEmail = "";
  fetch('/login').then(response => response.json()).then((loginInfo) => {
    if (loginInfo.length == 1) {
      var form = document.getElementById("form-container")
      var parent = document.getElementById("content");
      var para = document.createElement("p");
      var loginLink = loginInfo[0];
      var a = document.createElement('a');
      a.href = loginLink;
      a.title = loginLink;
      a.appendChild(document.createTextNode("here"));
      para.appendChild(document.createTextNode("Login "));
      para.appendChild(a);
      parent.replaceChild(para, form);
    } else {
      userEmail = loginInfo[0];

      // creating the logout link
      var logoutLink = loginInfo[1];
      var a = document.createElement('a');
      a.href = logoutLink;
      a.title = logoutLink;
      a.appendChild(document.createTextNode("here"));
      var para = document.createElement("p");
      para.appendChild(document.createTextNode("Logout "));
      para.appendChild(a);
      var parent = document.getElementById("content");
      parent.appendChild(para);

      fetch('/data').then(response => response.json()).then((stats) => {
        // stats is an object, not a string, so we have to
        // reference its fields to create HTML content

        // stats should be empty if user is not logged in
        for (var i = 0; i < stats.length; i++) {
          statsListElement.appendChild(createListElement(stats[i]));
        }
      });
    }

  })
}

function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}