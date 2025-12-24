package com.ecommerce.servlets;

import com.ecommerce.models.Cart;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/checkout")
public class CheckoutServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Cart cart = (Cart) session.getAttribute("cart");

        BufferedReader reader = request.getReader();
        Map<String, Object> orderData = gson.fromJson(reader, Map.class);

        Map<String, Object> result = new HashMap<>();

        if (cart == null || cart.getItems().isEmpty()) {
            result.put("status", "error");
            result.put("message", "Cart is empty");
        } else {
            String orderId = "ORD" + System.currentTimeMillis();
            result.put("status", "success");
            result.put("orderId", orderId);
            result.put("total", cart.getTotal());
            result.put("message", "Order placed successfully!");
            session.removeAttribute("cart");
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(result));
    }
}
