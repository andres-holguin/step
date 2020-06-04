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

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import com.google.gson.Gson;
import java.util.ArrayList;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private ArrayList<String> comments;
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    comments = getComments();

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String commentText = request.getParameter("comment-text");
    addNewComment(commentText);
    response.sendRedirect("/index.html");
  }

  /**
   * Helper function that returns an Array of strings with all the comments in Datastore
   */
  private ArrayList<String> getComments() {
    ArrayList<String> comments = new ArrayList<String>();

    Query query = new Query("Comment");
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      comments.add((String) entity.getProperty("text"));
    }

    return comments;
  }

  /**
   * Helper function to add the text from a new comment to Datastore.
   */
  private void addNewComment(String commentText) {
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("text", commentText);

    datastore.put(commentEntity);
  }
}
