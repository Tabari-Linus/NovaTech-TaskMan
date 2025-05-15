package com.novatech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novatech.model.User;
import com.novatech.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleApiLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            String username = null;
            String password = null;


            String contentType = request.getContentType();

            if (contentType != null && contentType.contains("application/json")) {

                StringBuilder sb = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                String requestBody = sb.toString();
                System.out.println("Request Body: " + (requestBody.isEmpty() ? "[EMPTY]" : requestBody));


                if (requestBody.isEmpty()) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Empty request body. Please provide username and password.");
                    return;
                }

                Map<String, String> credentials = objectMapper.readValue(requestBody, Map.class);
                username = credentials.get("username");
                password = credentials.get("password");
            } else if (contentType != null && contentType.contains("multipart/form-data")) {

                try {
                    Part usernamePart = request.getPart("username");
                    Part passwordPart = request.getPart("password");

                    if (usernamePart != null) {
                        username = readPartAsString(usernamePart);
                    }

                    if (passwordPart != null) {
                        password = readPartAsString(passwordPart);
                    }

                    System.out.println("Multipart form data - Username: " +
                            (username != null ? username : "[NULL]") +
                            ", Password: " + (password != null ? "[REDACTED]" : "[NULL]"));
                } catch (ServletException e) {
                    System.out.println("Error parsing multipart request: " + e.getMessage());
                    e.printStackTrace();
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Error processing multipart form data: " + e.getMessage());
                    return;
                }
            } else {

                username = request.getParameter("username");
                password = request.getParameter("password");
                System.out.println("Form parameters - Username: " +
                        (username != null ? username : "[NULL]") +
                        ", Password: " + (password != null ? "[REDACTED]" : "[NULL]"));
            }


            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Missing username or password");
                return;
            }


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
                System.out.println("Login successful for user: " + username);
            } else {

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "Invalid username or password");

                out.print(objectMapper.writeValueAsString(result));
                System.out.println("Login failed for user: " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Error: " + e.getMessage());
        }
    }


    private String readPartAsString(Part part) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }


    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setStatus(statusCode);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(result));
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