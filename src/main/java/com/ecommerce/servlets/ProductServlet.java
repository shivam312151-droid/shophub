package com.ecommerce.servlets;

import com.ecommerce.models.Product;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/products")
public class ProductServlet extends HttpServlet {
    private List<Product> products;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        gson = new Gson();
        products = new ArrayList<>();
        products.add(new Product(1, "Laptop", "High-performance laptop", 999.99,
            "https://images.unsplash.com/photo-1588405748488-1d7d1b92c3d5?w=500&h=400&fit=crop&q=80", 10));
        products.add(new Product(2, "Smartphone", "Latest smartphone model", 699.99,
            "https://images.unsplash.com/photo-1592286927505-1def25e329c7?w=500&h=400&fit=crop&q=80", 15));
        products.add(new Product(3, "Headphones", "Wireless noise-canceling headphones", 199.99,
            "https://images.unsplash.com/photo-1487215078519-e21cc028cb29?w=500&h=400&fit=crop&q=80", 20));
        products.add(new Product(4, "Tablet", "10-inch tablet", 449.99,
            "https://images.unsplash.com/photo-1544716278-ca5e3af8abd8?w=500&h=400&fit=crop&q=80", 12));
        products.add(new Product(5, "Smartwatch", "Fitness tracking smartwatch", 299.99,
            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&h=400&fit=crop&q=80", 25));
        products.add(new Product(6, "Camera", "Digital camera 4K", 799.99,
            "https://images.unsplash.com/photo-1610933015033-333e1a1ebf24?w=500&h=400&fit=crop&q=80", 8));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String productId = request.getParameter("id");

        if (productId != null) {
            int id = Integer.parseInt(productId);
            Product product = products.stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElse(null);
            response.getWriter().write(gson.toJson(product));
        } else {
            response.getWriter().write(gson.toJson(products));
        }
    }
}
