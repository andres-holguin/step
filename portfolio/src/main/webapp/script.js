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
  const greetings = [
    'Hello world!',
    '¡Hola Mundo!',
    '你好，世界！',
    'Bonjour le monde!',
    '\"Whether you think you can or you think you can\'t, you\'re right.\" -Stewie Griffin',
    '\"What in Oblivion is that?\" -General Tullius'
  ];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * Loads the HTML retrieved from `src` url and inserts it at the `position` relative to `selector`
 * Example: loadHTML("templates/header.html", "body", Adjacent.BEFORE_BEGIN);
 * inserts the header before <body>
 */
function loadHTML(src, selector, position) {
  fetch(src)
    .then(response => response.text())
    .then(text => {
      document.querySelector(selector).insertAdjacentHTML(position, text);
    });
}
// "Enum" for key positions used in insertAdjacentHTML() and loadHTML()
const Adjacent = {
  BEFORE_BEGIN: "beforebegin",
  AFTER_BEGIN: "afterbegin",
  BEFORE_END: "beforeend",
  AFTER_END: "afterend"
};
 
/** Fetches and loads header, footer and other dynamic content for each page. */
function onBodyLoad() {
  // Fetch & load header
  loadHTML("templates/header.html", "body", Adjacent.BEFORE_BEGIN);
  // Fetch & load footer
  loadHTML("templates/footer.html", "body", Adjacent.AFTER_END);
  // Fetch & load comments from /data for authenticated users
  loadUserFeatures();
}

/**
 * Clear comments, then fetch them from the server and 
 * load them to the list of comments, if it exists.
 */
function loadComments() {
  clearComments();
  let numComments = document.getElementById("num-comments")?.value;

  fetch("/data?num-comments=" + numComments).then(response => response.json()).then(comments => {
    const commentsEl = document.getElementById("comments-list");
    comments.forEach(comment => {
      const liElement = document.createElement('li');
      liElement.innerText = comment;
      commentsEl?.appendChild(liElement);
    });
  });
}

/** Clear comments from comments list. */
function clearComments() {
  const commentsEl = document.getElementById("comments-list");
  if (commentsEl) commentsEl.innerHTML = "";
}

/** Delete comments from Datastore, and reload comments */
function deleteAllComments() {
  fetch('/delete-data', {method: 'POST'});
  loadComments();
}

function loadUserFeatures() {
  fetch('/login').then(response => response.json()).then(user => {
    if (user.isLoggedIn) {
      loadHTML("templates/comments.html", "#comments", Adjacent.BEFORE_END);
      loadComments();
    } else {
      document.querySelector("#comments").insertAdjacentHTML(Adjacent.BEFORE_END,
        "<p>Please login to view or post comments.</p>"
      );
    }
  });
}