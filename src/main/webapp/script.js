const API_BASE_URL = '/api';

async function fetchProducts() {
    try {
        const response = await fetch(`${API_BASE_URL}/products`);
        if (!response.ok) throw new Error('Failed to fetch products');
        return await response.json();
    } catch {
        return [];
    }
}

async function fetchCart() {
    try {
        const response = await fetch(`${API_BASE_URL}/cart`);
        if (!response.ok) throw new Error('Failed to fetch cart');
        return await response.json();
    } catch {
        return { items: {}, total: 0, itemCount: 0 };
    }
}

async function addToCart(productId, quantity = 1) {
    try {
        const response = await fetch(`${API_BASE_URL}/cart`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'add', productId, quantity })
        });
        if (!response.ok) throw new Error('Failed to add to cart');
        const cart = await response.json();
        updateCartCount();
        showNotification('Product added to cart!');
        return cart;
    } catch (e) {
        showNotification('Failed to add product to cart', 'error');
    }
}

async function removeFromCart(productId) {
    try {
        const response = await fetch(`${API_BASE_URL}/cart`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'remove', productId })
        });
        if (!response.ok) throw new Error('Failed to remove from cart');
        updateCartCount();
        if (window.location.pathname.includes('cart.html')) location.reload();
    } catch {}
}

async function updateCart(productId, action, quantity = 1) {
    try {
        const response = await fetch(`${API_BASE_URL}/cart`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action, productId, quantity })
        });
        if (!response.ok) throw new Error('Failed to update cart');
        return await response.json();
    } catch {}
}

async function updateCartCount() {
    try {
        const cart = await fetchCart();
        const count = cart.itemCount || 0;
        document.querySelectorAll('#cart-count').forEach(el => el.textContent = count);
    } catch {}
}

function displayProducts(products, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    if (products.length === 0) {
        container.innerHTML = '<p class="error">No products found.</p>';
        return;
    }
    container.innerHTML = '';
    products.forEach(product => {
        const imageUrl = (product.imageUrl && product.imageUrl.startsWith('http'))
            ? product.imageUrl
            : `https://via.placeholder.com/400x250?text=${encodeURIComponent(product.name || 'Product')}`;
        const productCard = document.createElement('div');
        productCard.className = 'product-card';
        productCard.innerHTML = `
            <img src="${imageUrl}" alt="${product.name}">
            <div class="product-info">
                <h3>${product.name}</h3>
                <p>${product.description}</p>
                <div class="product-price">$${product.price.toFixed(2)}</div>
                <p class="product-stock">In Stock: ${product.stock}</p>
                <button class="btn btn-primary" onclick="addToCart(${product.id})">Add to Cart</button>
            </div>`;
        container.appendChild(productCard);
    });
}

function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    notification.style.cssText = `position:fixed;top:80px;right:20px;background:${type === 'success' ? '#27ae60' : '#e74c3c'};color:white;padding:1rem 2rem;border-radius:5px;box-shadow:0 4px 6px rgba(0,0,0,0.1);z-index:1000;animation:slideIn 0.3s ease-out;`;
    document.body.appendChild(notification);
    setTimeout(() => { notification.style.animation = 'slideOut 0.3s ease-out'; setTimeout(() => { document.body.removeChild(notification); }, 300); }, 3000);
}

const style = document.createElement('style');
style.textContent = `@keyframes slideIn{from{transform:translateX(400px);opacity:0}to{transform:translateX(0);opacity:1}}@keyframes slideOut{from{transform:translateX(0);opacity:1}to{transform:translateX(400px);opacity:0}}`;
document.head.appendChild(style);

document.addEventListener('DOMContentLoaded', () => { updateCartCount(); });

