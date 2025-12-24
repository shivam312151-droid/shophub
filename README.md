# ShopHub E-Commerce Store

A lightweight, full-stack e-commerce website with a Java HTTP server backend and vanilla JavaScript frontend.

## Features
- Browse products with filtering and sorting
- Add/remove items to shopping cart
- Checkout with order summary
- Session-based cart persistence
- Responsive design
- JSON REST APIs

## Tech Stack
- **Backend:** Java 11 (com.sun.net.httpserver) — no external frameworks
- **Frontend:** HTML5, CSS3, Vanilla JavaScript
- **Build:** Java compiler (javac)
- **Deploy:** Docker

## Local Development

### Quick Start
```powershell
cd "c:\Users\ASUS\.vscode\E-Commces Wesite\Front-End"
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot"
$env:Path="$env:JAVA_HOME\bin;" + $env:Path
javac -d simple-server\bin simple-server\Main.java
java -cp simple-server\bin Main
```
Open http://localhost:8080

### Using Docker
```bash
docker build -t shophub .
docker run -e PORT=8080 -p 8080:8080 shophub
```
Open http://localhost:8080

## Project Structure
```
Front-End/
├── src/main/
│   ├── java/com/ecommerce/
│   │   ├── models/
│   │   │   ├── Product.java
│   │   │   └── Cart.java
│   │   └── servlets/
│   │       ├── ProductServlet.java
│   │       ├── CartServlet.java
│   │       └── CheckoutServlet.java
│   └── webapp/
│       ├── index.html
│       ├── products.html
│       ├── cart.html
│       ├── checkout.html
│       ├── script.js
│       ├── style.css
│       └── WEB-INF/web.xml
├── simple-server/
│   └── Main.java (standalone HTTP server)
├── Dockerfile
└── pom.xml (Maven support)
```

## API Endpoints

### Products
- `GET /api/products` — List all products
- `GET /api/products?id=1` — Get single product

### Cart
- `GET /api/cart` — View cart
- `POST /api/cart` — Add/update/remove items
  ```json
  { "action": "add", "productId": 1, "quantity": 2 }
  { "action": "remove", "productId": 1 }
  { "action": "update", "productId": 1, "quantity": 3 }
  ```
- `DELETE /api/cart` — Clear cart

### Checkout
- `POST /api/checkout` — Place order with shipping/payment info

## Deploy to Your Domain

### Option 1: Cloud Hosting (Render, Railway, Fly.io)
1. Push this repo to GitHub
2. On your hosting platform:
   - Create a new app from Docker
   - Select this Dockerfile
   - Platform auto-sets `PORT` env var
   - Deploy
3. Point your domain DNS:
   - CNAME to the platform-provided URL, or
   - A record to platform IP
4. Enable HTTPS (auto-provided by most platforms)

### Option 2: VPS/Self-Hosted
```bash
docker build -t shophub .
docker run -d --name shophub -e PORT=80 -p 80:80 shophub
```
- Point domain A record to server IP
- Use Caddy or nginx as reverse proxy for HTTPS

### Option 3: Docker Hub
```bash
docker build -t yourusername/shophub .
docker push yourusername/shophub
```
Deploy from your hub on any Docker-capable host.

## Environment Variables
- `PORT` (default 8080) — Server listening port

## Performance Notes
- Lightweight standalone server (~50KB runtime memory)
- No database (in-memory products/carts)
- Suitable for ~100 concurrent users
- For scale: migrate to Spring Boot + PostgreSQL + load balancer

## Browser Support
- Modern browsers (Chrome, Firefox, Safari, Edge)
- ES6 JavaScript support required
- Mobile-responsive

## License
MIT

## Support
For issues, check:
1. Browser DevTools Console for client errors
2. Server logs (terminal) for backend errors
3. Verify `http://localhost:8080/api/products` responds with JSON
