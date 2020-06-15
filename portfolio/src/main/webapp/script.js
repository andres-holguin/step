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

// Object that holds user data, once loaded.
let user;

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
 * Example: loadHTML("templates/header.html", "body", Adjacent.BEFORE);
 * inserts the header before <body>
 */
function loadHTML(src, selector, position) {
  return fetch(src)
    .then(response => response.text())
    .then(text => {
      document.querySelector(selector)?.insertAdjacentHTML(position, text);
    });
}
// "Enum" for key positions used in insertAdjacentHTML() and loadHTML()
const Adjacent = {
  BEFORE: "beforebegin",
  PREPEND: "afterbegin",
  APPEND: "beforeend",
  AFTER: "afterend"
};
 
/** Fetches and loads header, footer and other dynamic content for each page. */
function onBodyLoad() {
  // Fetch & load header template
  loadHTML("templates/header.html", "body", Adjacent.BEFORE)
  // Fetch & load comments template and data from /data for authenticated users
  // Note: A feature depends on the header being loaded, so it is necessary to await
  // for the header fetch to resolve.
    .then(loadUserFeatures);
  // Fetch & load footer template
  loadHTML("templates/footer.html", "body", Adjacent.AFTER);
  // Load the gallery. Nothing will happen if a gallery element is not on the page.
  loadGallery();
}

/**
 * Clear comments, then fetch them from the server and 
 * load them to the list of comments, if it exists.
 */
function loadComments() {
  const commentsEl = document.getElementById("comments-list");
  if (!commentsEl) return; // Short-circuit if no comments element.

  clearComments();
  let numComments = document.getElementById("num-comments")?.value;

  fetch(`/data?num-comments=${numComments}`).then(response => response.json()).then(comments => {
    comments.forEach(comment => {
      const liElement = document.createElement('li');
      liElement.innerText = `${comment.email}: ${comment.text}`;
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
  fetch('/delete-data', {method: 'POST'})
    .then(loadComments);
}

/** Load content based on whether user is logged in or not */
function loadUserFeatures() {
  fetch('/login').then(response => response.json()).then(userData => {
    user = userData;
    if (user.isLoggedIn) {
      loadLoggedInFeatures();
    } else {
      loadLoggedOutFeatures();
    }
  });
}

/** Load the features and text that depend on the user being logged in. */
function loadLoggedInFeatures() {
  // Insert Logout link to <header>
  document.querySelector("nav")?.insertAdjacentHTML(Adjacent.APPEND,
    `<a id="login-link" href="${user.logoutUrl}">Logout</a>`
  );
  // Load comments template
  loadHTML("templates/comments.html", "#comments", Adjacent.APPEND)
    .then(loadComments);
}

/** Load the features and text that depend on the user being logged out. */
function loadLoggedOutFeatures() {
  // Insert Login link to <header>
  document.querySelector("nav")?.insertAdjacentHTML(Adjacent.APPEND,
    `<a id="login-link" href="${user.loginUrl}">Login</a>`
  );
  // Notify to login for comments
  document.querySelector("#comments")?.insertAdjacentHTML(Adjacent.APPEND,
    `<p>Please <a href="${user.loginUrl}">login</a> to view or post comments.</p>`
  );
}

/**
 * Fetch the Url for a new image upload, which is then forwarded to /gallery-image for storage.
 * Once url is obtained, the image upload form is revealed. */
function fetchBlobstoreUrlAndShowForm() {
  fetch('/blobstore-upload-url')
    .then((response) => response.text())
    .then((imageUploadUrl) => {
      const messageForm = document.getElementById('image-upload-form');
      messageForm.action = imageUploadUrl;
      messageForm.classList.remove('hidden');
    });
}
 /** Fetch the urls for all uploaded gallery images, and load them to the gallery component. */
function loadGallery() {
  const galleryEl = document.getElementById("gallery");
  if (!galleryEl) return; // Short-circuit if no gallery element

  fetch("/gallery-images").then(response => response.json()).then(galleryImages => {
    galleryImages.forEach(image => {
      const aElement = document.createElement('a');
      aElement.href = image;
      aElement.innerHTML = `<img src="${image}">`
      galleryEl.appendChild(aElement);
    });
  });
}
