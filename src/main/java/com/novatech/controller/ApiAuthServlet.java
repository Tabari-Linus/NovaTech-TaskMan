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
@MultipartConfig
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Add CORS headers
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String path = request.getServletPath();

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

        Map<String, Object> result = new HashMap<>();
        result.put("error", "Method not allowed");
        result.put("message", "This endpoint only accepts POST requests");

        if ("/api/auth/login".equals(path)) {
            result.put("example", Map.of(
                    "method", "POST",
                    "contentType", "application/json",
                    "body", Map.of("username", "your_username", "password", "your_password")
            ));
        }

        PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(result));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Add CORS headers
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String path = request.getServletPath();

        if ("/api/auth/login".equals(path)) {
            handleApiLogin(request, response);
        } else if ("/api/auth/logout".equals(path)) {
            handleApiLogout(request, response);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Handle preflight CORS requests
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleApiLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Log request details for debugging
        System.out.println("=== API Auth Login Request ===");
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Content-Length: " + request.getContentLength());

        try {
            String username = null;
            String password = null;

            // Check content type
            String contentType = request.getContentType();

            if (contentType != null && contentType.contains("application/json")) {
                // JSON parsing
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                String requestBody = sb.toString();
                System.out.println("Request Body: " + (requestBody.isEmpty() ? "[EMPTY]" : requestBody));

                // Handle empty request body
                if (requestBody.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("message", "Empty request body. Please provide username and password.");

                    response.setContentType("application/json");
                    PrintWriter out = response.getWriter();
                    out.print(objectMapper.writeValueAsString(result));
                    return;
                }

                Map<String, String> credentials = objectMapper.readValue(requestBody, Map.class);
                username = credentials.get("username");
                password = credentials.get("password");
            } else {
                // Form data
                username = request.getParameter("username");
                password = request.getParameter("password");
                System.out.println("Form parameters - Username: " + username + ", Password: [REDACTED]");
            }

            // Validate credentials
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "Missing username or password");

                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                out.print(objectMapper.writeValueAsString(result));
                return;
            }

            // Authenticate user
            User user = userService.authenticate(username, password);

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            if (user != null) {
                // Login successful
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
                System.out.println("Login successful for user: " + username);
            } else {
                // Login failed
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "Invalid username or password");

                out.print(objectMapper.writeValueAsString(result));
                System.out.println("Login failed for user: " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Database error: " + e.getMessage());

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());

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
            System.out.println("API: User logged out");
        } else {
            System.out.println("API: Logout requested, but no active session found");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Logout successful");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(result));
    }
}