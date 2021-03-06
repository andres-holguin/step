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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.gson.Gson;

/** Servlet that checks if user is logged in */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  private UserService userService = UserServiceFactory.getUserService();
  private User user = new User();

  private class User {
    public boolean isLoggedIn;
    public String loginUrl;
    public String logoutUrl;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String redirectUrl = "/index.html";

    user.isLoggedIn = userService.isUserLoggedIn();
    user.loginUrl = userService.createLoginURL(redirectUrl);
    user.logoutUrl = userService.createLogoutURL(redirectUrl);

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(user));    
  }
}