import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    static class Product {
        int id; String name; String description; double price; String imageUrl; int stock;
        Product(int id, String name, String description, double price, String imageUrl, int stock) {
            this.id=id; this.name=name; this.description=description; this.price=price; this.imageUrl=imageUrl; this.stock=stock;
        }
        String toJson() {
            return String.format(Locale.US,
                    "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"imageUrl\":\"%s\",\"stock\":%d}",
                    id, escape(name), escape(description), price, escape(imageUrl), stock);
        }
    }

    static class CartItem { Product product; int quantity; CartItem(Product p, int q){product=p; quantity=q;} }
    static class Cart {
        Map<Integer, CartItem> items = new LinkedHashMap<>();
        void add(Product p, int q){ items.compute(p.id, (k,v)-> v==null? new CartItem(p,q): new CartItem(p, v.quantity+q)); }
        void remove(int id){ items.remove(id); }
        void update(int id, int q){ if(items.containsKey(id)) items.get(id).quantity=q; }
        double total(){ return items.values().stream().mapToDouble(ci-> ci.product.price*ci.quantity).sum(); }
        int count(){ return items.values().stream().mapToInt(ci->ci.quantity).sum(); }
        String toJson(){
            StringBuilder sb = new StringBuilder();
            sb.append("{\"items\":{");
            boolean first=true;
            for (Map.Entry<Integer, CartItem> e: items.entrySet()) {
                if(!first) sb.append(','); first=false;
                CartItem ci=e.getValue();
                sb.append('"').append(e.getKey()).append('"').append(':')
                  .append('{').append("\"product\":").append(ci.product.toJson())
                  .append(',').append("\"quantity\":").append(ci.quantity).append('}');
            }
            sb.append("},\"total\":").append(String.format(Locale.US, "%.2f", total()))
              .append(",\"itemCount\":").append(count()).append('}');
            return sb.toString();
        }
    }

    static String escape(String s){ return s.replace("\\","\\\\").replace("\"","\\\""); }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        Path webRoot = Paths.get("src/main/webapp").toAbsolutePath();
        List<Product> products = List.of(
                new Product(1, "Laptop", "High-performance laptop", 82999, "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1000&q=80", 10),
                new Product(2, "Smartphone", "Latest smartphone model", 58099, "https://images.unsplash.com/photo-1592286927505-1def25115558?w=1000&h=800&fit=crop&q=80", 15),
                new Product(3, "Headphones", "Wireless noise-canceling headphones", 16599, "https://images.unsplash.com/photo-1487215078519-e21cc028cb29?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1000&q=80", 20),
                new Product(4, "Tablet", "10-inch tablet", 37349, "https://images.unsplash.com/photo-1579321572759-e3fb039a6e3f?w=1000&h=800&fit=crop&q=80", 12),
                new Product(5, "Smartwatch", "Fitness tracking smartwatch", 24899, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1000&q=80", 25),
                new Product(6, "Camera", "Digital camera 4K", 66399, "https://images.unsplash.com/photo-1606986628025-35d57e735ae0?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1000&q=80", 8)
        );
        Cart cart = new Cart();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/products", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) { send(exchange, 405, ""); return; }
            String query = exchange.getRequestURI().getQuery();
            String body;
            if (query != null && query.startsWith("id=")) {
                int id = Integer.parseInt(query.substring(3));
                Product p = products.stream().filter(pp->pp.id==id).findFirst().orElse(null);
                body = p==null? "null" : p.toJson();
            } else {
                StringBuilder sb = new StringBuilder("[");
                for (int i=0;i<products.size();i++) { if (i>0) sb.append(','); sb.append(products.get(i).toJson()); }
                sb.append(']');
                body = sb.toString();
            }
            json(exchange, 200, body);
        });

        server.createContext("/api/cart", new HttpHandler() {
            @Override public void handle(HttpExchange exchange) throws IOException {
                switch (exchange.getRequestMethod()) {
                    case "GET": json(exchange, 200, cart.toJson()); break;
                    case "DELETE": cart.items.clear(); json(exchange, 200, "{\"status\":\"success\"}"); break;
                    case "POST":
                        String req = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                        Map<String,String> map = parseJsonToMap(req);
                        String action = map.getOrDefault("action", "");
                        int productId = Integer.parseInt(map.getOrDefault("productId", "0"));
                        int quantity = Integer.parseInt(map.getOrDefault("quantity", "1"));
                        if ("add".equals(action)) {
                            Product p = products.stream().filter(pp->pp.id==productId).findFirst().orElseGet(() -> new Product(productId, "Product "+productId, "Description", 99.99, "images/placeholder.jpg", 10));
                            cart.add(p, quantity);
                        } else if ("remove".equals(action)) {
                            cart.remove(productId);
                        } else if ("update".equals(action)) {
                            cart.update(productId, quantity);
                        }
                        json(exchange, 200, cart.toJson());
                        break;
                    default: send(exchange, 405, "");
                }
            }
        });

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) path = "/index.html";
            Path file = webRoot.resolve(path.substring(1)).normalize();
            if (!file.startsWith(webRoot) || !Files.exists(file) || Files.isDirectory(file)) { send(exchange, 404, "Not Found"); return; }
            Headers h = exchange.getResponseHeaders();
            String mime = URLConnection.guessContentTypeFromName(file.toString());
            if (mime != null) h.add("Content-Type", mime);
            byte[] data = Files.readAllBytes(file);
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(data); }
        });

        System.out.println("Server running at http://localhost:"+port);
        server.start();
    }

    static void json(HttpExchange ex, int code, String body) throws IOException {
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }

    static void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }

    static Map<String,String> parseJsonToMap(String json) {
        Map<String,String> map = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) json = json.substring(1, json.length()-1);
        for (String part : json.split(",")) {
            String[] kv = part.split(":",2);
            if (kv.length==2) {
                String k = kv[0].trim().replaceAll("^\"|\"$", "");
                String v = kv[1].trim().replaceAll("^\"|\"$", "");
                map.put(k, v);
            }
        }
        return map;
    }
}
