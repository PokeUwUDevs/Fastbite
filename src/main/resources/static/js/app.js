// ==================== AUTH UTILITIES ====================
const Auth = {
    TOKEN_KEY: 'fastbite_token',
    USER_KEY: 'fastbite_user',

    saveAuth(authResponse) {
        localStorage.setItem(this.TOKEN_KEY, authResponse.token);
        localStorage.setItem(this.USER_KEY, JSON.stringify({
            id: authResponse.userId,
            email: authResponse.email,
            name: authResponse.name,
            role: authResponse.role
        }));
    },

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    getUser() {
        const user = localStorage.getItem(this.USER_KEY);
        return user ? JSON.parse(user) : null;
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        window.location.href = '/login';
    },

    redirectBasedOnRole() {
        const user = this.getUser();
        if (!user) {
            window.location.href = '/login';
            return;
        }

        switch (user.role) {
            case 'CLIENTE':
                window.location.href = '/customer/menu';
                break;
            case 'COCINA':
                window.location.href = '/kitchen/dashboard';
                break;
            case 'REPARTIDOR':
                window.location.href = '/delivery/dashboard';
                break;
            default:
                window.location.href = '/login';
        }
    }
};

// ==================== API UTILITIES ====================
const API = {
    baseUrl: '/api',

    async request(endpoint, options = {}) {
        const token = Auth.getToken();
        
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(`${this.baseUrl}${endpoint}`, config);
            
            if (response.status === 401) {
                Auth.logout();
                return null;
            }

            if (!response.ok) {
                const error = await response.text();
                throw new Error(error || 'Error en la solicitud');
            }

            const text = await response.text();
            return text ? JSON.parse(text) : null;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },

    // Auth
    async register(data) {
        return this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    async login(email, password) {
        return this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
    },

    // Products
    async getProducts() {
        return this.request('/products');
    },

    // Orders
    async createOrder(data) {
        return this.request('/orders', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    async getMyOrders() {
        return this.request('/orders/my');
    },

    async getOrder(id) {
        return this.request(`/orders/${id}`);
    },

    async getKitchenOrders() {
        return this.request('/orders/kitchen');
    },

    async updateOrderStatus(orderId, status) {
        return this.request(`/orders/${orderId}/status`, {
            method: 'PATCH',
            body: JSON.stringify({ status })
        });
    },

    // Comments
    async getComments(orderId) {
        return this.request(`/orders/${orderId}/comments`);
    },

    async addComment(orderId, message) {
        return this.request(`/orders/${orderId}/comments`, {
            method: 'POST',
            body: JSON.stringify({ message })
        });
    }
};

// ==================== SSE UTILITIES ====================
const SSE = {
    connections: {},

    connect(endpoint, onMessage, onError) {
        const url = `/api${endpoint}`;
        const eventSource = new EventSource(url);
        
        eventSource.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                onMessage(data);
            } catch (e) {
                console.error('SSE parse error:', e);
            }
        };

        eventSource.onerror = (error) => {
            console.error('SSE error:', error);
            if (onError) onError(error);
        };

        this.connections[endpoint] = eventSource;
        return eventSource;
    },

    disconnect(endpoint) {
        if (this.connections[endpoint]) {
            this.connections[endpoint].close();
            delete this.connections[endpoint];
        }
    },

    disconnectAll() {
        Object.keys(this.connections).forEach(key => {
            this.connections[key].close();
        });
        this.connections = {};
    }
};

// ==================== CART UTILITIES ====================
const Cart = {
    STORAGE_KEY: 'fastbite_cart',

    getItems() {
        const cart = localStorage.getItem(this.STORAGE_KEY);
        return cart ? JSON.parse(cart) : [];
    },

    saveItems(items) {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(items));
        this.updateUI();
    },

    addItem(product) {
        const items = this.getItems();
        const existing = items.find(item => item.productId === product.id);
        
        if (existing) {
            existing.quantity += 1;
        } else {
            items.push({
                productId: product.id,
                productName: product.name,
                unitPrice: product.price,
                quantity: 1
            });
        }
        
        this.saveItems(items);
    },

    removeItem(productId) {
        const items = this.getItems().filter(item => item.productId !== productId);
        this.saveItems(items);
    },

    updateQuantity(productId, quantity) {
        const items = this.getItems();
        const item = items.find(item => item.productId === productId);
        
        if (item) {
            if (quantity <= 0) {
                this.removeItem(productId);
            } else {
                item.quantity = quantity;
                this.saveItems(items);
            }
        }
    },

    getTotal() {
        return this.getItems().reduce((total, item) => {
            return total + (parseFloat(item.unitPrice) * item.quantity);
        }, 0);
    },

    getCount() {
        return this.getItems().reduce((count, item) => count + item.quantity, 0);
    },

    clear() {
        localStorage.removeItem(this.STORAGE_KEY);
        this.updateUI();
    },

    updateUI() {
        const cartCount = document.querySelector('.cart-count');
        if (cartCount) {
            cartCount.textContent = this.getCount();
        }
        window.dispatchEvent(new CustomEvent('cartUpdated', { 
            detail: { items: this.getItems(), total: this.getTotal() } 
        }));
    }
};

// ==================== UI UTILITIES ====================
const UI = {
    currentToast: null,
    
    showAlert(message, type = 'info') {
        // Remover toast anterior si existe
        if (this.currentToast) {
            this.currentToast.remove();
        }
        
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <span>${message}</span>
            <button onclick="this.parentElement.remove()">&times;</button>
        `;
        
        // Agregar estilos inline si no existen
        if (!document.getElementById('toast-styles')) {
            const style = document.createElement('style');
            style.id = 'toast-styles';
            style.textContent = `
                .toast {
                    position: fixed;
                    top: 20px;
                    right: 20px;
                    padding: 15px 20px;
                    border-radius: 8px;
                    color: white;
                    font-weight: 500;
                    display: flex;
                    align-items: center;
                    gap: 15px;
                    box-shadow: 0 4px 20px rgba(0,0,0,0.2);
                    z-index: 9999;
                    animation: slideIn 0.3s ease;
                    max-width: 350px;
                }
                .toast button {
                    background: none;
                    border: none;
                    color: white;
                    font-size: 1.3rem;
                    cursor: pointer;
                    opacity: 0.8;
                }
                .toast button:hover { opacity: 1; }
                .toast-success { background: #28a745; }
                .toast-error { background: #dc3545; }
                .toast-info { background: #17a2b8; }
                .toast-warning { background: #ffc107; color: #333; }
                @keyframes slideIn {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
                @keyframes slideOut {
                    from { transform: translateX(0); opacity: 1; }
                    to { transform: translateX(100%); opacity: 0; }
                }
            `;
            document.head.appendChild(style);
        }
        
        document.body.appendChild(toast);
        this.currentToast = toast;
        
        // Auto-remover después de 3 segundos
        setTimeout(() => {
            if (toast.parentElement) {
                toast.style.animation = 'slideOut 0.3s ease forwards';
                setTimeout(() => toast.remove(), 300);
            }
        }, 3000);
    },

    formatPrice(price) {
        return `$${parseFloat(price).toFixed(2)}`;
    },

    formatTime(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleTimeString('es-MX', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });
    },

    formatDateTime(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleString('es-MX', {
            day: '2-digit',
            month: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    getStatusLabel(status) {
        const labels = {
            'RECIBIDO': '📥 Recibido',
            'PREPARANDO': '👨‍🍳 Preparando',
            'LISTO': '✅ Listo',
            'EN_CAMINO': '🛵 En camino',
            'ENTREGADO': '🎉 Entregado'
        };
        return labels[status] || status;
    },

    getRoleIcon(role) {
        const icons = {
            'CLIENTE': '👤',
            'COCINA': '👨‍🍳',
            'REPARTIDOR': '🛵'
        };
        return icons[role] || '👤';
    }
};

// ==================== INIT ====================
document.addEventListener('DOMContentLoaded', () => {
    Cart.updateUI();
    setupNavbar();
});

function setupNavbar() {
    const user = Auth.getUser();
    const userInfoEl = document.querySelector('.user-info');
    const authLinksEl = document.querySelector('.auth-links');
    
    if (userInfoEl && user) {
        userInfoEl.innerHTML = `
            <span>${user.name}</span>
            <span class="user-badge">${user.role}</span>
            <button onclick="Auth.logout()" class="btn btn-sm btn-outline">Salir</button>
        `;
        userInfoEl.classList.remove('hidden');
    }
    
    if (authLinksEl) {
        authLinksEl.classList.toggle('hidden', !!user);
    }
}