// Global variables
const API_BASE_URL = '/api';

// Fetch all products
async function fetchProducts() {
    try {
        const response = await fetch(`${API_BASE_URL}/products`);
        if (!response.ok) throw new Error('Failed to fetch products');
        return await response. json();
    } catch (error) {
        console.error('Error fetching products:', error);
        return [];
    }
}

// Fetch cart
async function fetchCart() {
    try {
        const response = await fetch(`${API_BASE_URL}/cart`);
        if (!response.ok) throw new Error('Failed to fetch cart');
        return await response.json();
    } catch (error) {
        console.error('Error fetching cart:', error);
        return { items: {}, total: 0, itemCount: 0 };
    }
}

// Add to cart
async function addToCart(productId, quantity = 1) {
    try {
        const response = await fetch(`${API_BASE_URL}/cart`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                action: 'add',
                productId: productId,
                quantity: quantity
            })
        });
        
        if (!response. ok) throw new Error('Failed to add to cart');
        
        const cart = await response.json();
        updateCartCount();
        
        // Show success message
        showNotification('Product added to cart!');
        
        return cart;
    } catch (error) {
        console.error('Error adding to cart:', error);
        showNotification('Failed to add product to cart', 'error');
    }
}

// Remove from cart
async function removeFromCart(productId) {
    try {
        const response = await fetch(`${API_BASE_URL}/cart`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                action: 'remove',
                productId: productId
            })
        });
        
        if (!response.ok) throw new Error('Failed to remove from cart');
        
        updateCartCount();
        
        // Reload cart page if on cart page
        if (window. location.pathname. includes('cart. html')) {
            location.reload();
        }
    } catch (error) {
        console.error('Error removing from cart:', error);
    }
}

// Update cart
async function updateCart(productId, action, quantity = 1) {
    try {
        const response = await fetch(`${API_BASE_URL}/cart`, {
            method: 'POST',
            headers:  {
                'Content-Type':  'application/json',
            },
            body: JSON.stringify({
                action: action,
                productId: productId,
                quantity: quantity
            })
        });
        
        if (! response.ok) throw new Error('Failed to update cart');
        return await response.json();
    } catch (error) {
        console.error('Error updating cart:', error);
    }
}

// Update cart count in navbar
async function updateCartCount() {
    try {
        const cart = await fetchCart();
        const count = cart.itemCount || 0;
        const cartCountElements = document.querySelectorAll('#cart-count');
        cartCountElements.forEach(el => {
            el.textContent = count;
        });
    } catch (error) {
        console.error('Error updating cart count:', error);
    }
}

// Show notification
function showNotification(message, type = 'success') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 80px;
        right: 20px;
        background: ${type === 'success' ? '#27ae60' : '#e74c3c'};
        color:  white;
        padding: 1rem 2rem;
        border-radius: 5px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        z-index: 1000;
        animation: slideIn 0.3s ease-out;
    `;
    
    document.body.appendChild(notification);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

// Add CSS animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform:  translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    updateCartCount();
}); 