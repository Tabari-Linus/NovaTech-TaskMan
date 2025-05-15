package com.novatech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novatech.model.User;
import com.novatech.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ApiAuthServlet", urlPatterns = {"/api/auth/login", "/api/auth/logout"})
public class ApiAuthServlet extends HttpServlet {
    private UserService userService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        userService = new UserService();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/api/auth/login".equals(path)) {
            handleApiLogin(request, response);
        } else if ("/api/auth/logout".equals(path)) {
            handleApiLogout(request, response);
        }
    }

    private void handleApiLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        try {

            Map<String, String> credentials = objectMapper.readValue(sb.toString(), Map.class);
            String username = credentials.get("username");
            String password = credentials.get("password");


            User user = userService.authenticate(username, password);

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            if (user != null) {

                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());


                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Login successful");
                result.put("user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "fullName", user.getFullName()
                ));

                out.print(objectMapper.writeValueAsString(result));
            } else {

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "Invalid username or password");

                out.print(objectMapper.writeValueAsString(result));
            }
        } catch (SQLException e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Database error: " + e.getMessage());

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(objectMapper.writeValueAsString(result));
        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Invalid request format");

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(objectMapper.writeValueAsString(result));
        }
    }

    private void handleApiLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }


        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Logout successful");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(result));
    }
}