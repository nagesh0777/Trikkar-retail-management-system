const API_BASE = 'http://localhost:8080/api';

const getToken = () => localStorage.getItem('trikaar_token');

const headers = () => ({
    'Content-Type': 'application/json',
    ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {}),
});

const handleResponse = async (res) => {
    if (res.status === 401) {
        localStorage.removeItem('trikaar_token');
        localStorage.removeItem('trikaar_user');
        window.location.href = '/login';
        return;
    }
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Request failed');
    return data;
};

const api = {
    get: (path) => fetch(`${API_BASE}${path}`, { headers: headers() }).then(handleResponse),
    post: (path, body) => fetch(`${API_BASE}${path}`, { method: 'POST', headers: headers(), body: JSON.stringify(body) }).then(handleResponse),
    put: (path, body) => fetch(`${API_BASE}${path}`, { method: 'PUT', headers: headers(), body: JSON.stringify(body) }).then(handleResponse),
    patch: (path, body) => fetch(`${API_BASE}${path}`, { method: 'PATCH', headers: headers(), body: body ? JSON.stringify(body) : undefined }).then(handleResponse),
    delete: (path) => fetch(`${API_BASE}${path}`, { method: 'DELETE', headers: headers() }).then(handleResponse),
};

export default api;
