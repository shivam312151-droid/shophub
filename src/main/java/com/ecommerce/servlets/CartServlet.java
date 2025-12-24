package com.ecommerce.servlets;

import com.ecommerce.models.Cart;
import com.ecommerce.models.Product;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/cart")
public class CartServlet extends HttpServlet {
    private final Gson gson = new Gson();

    private Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Cart cart = getCart(session);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(cart));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Cart cart = getCart(session);

        BufferedReader reader = request.getReader();
        Map<String, Object> data = gson.fromJson(reader, Map.class);

        String action = (String) data.get("action");
        int productId = ((Number) data.get("productId")).intValue();

        if ("add".equals(action)) {
            int quantity = data.containsKey("quantity") ?
                    ((Number) data.get("quantity")).intValue() : 1;

            Product product = new Product(productId, "Product " + productId,
                    "Description", 99.99, "images/placeholder.jpg", 10);
            cart.addItem(product, quantity);
        } else if ("remove".equals(action)) {
            cart.removeItem(productId);
        } else if ("update".equals(action)) {
            int quantity = ((Number) data.get("quantity")).intValue();
            cart.updateQuantity(productId, quantity);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(cart));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("cart");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\": \"success\"}");
    }
}
