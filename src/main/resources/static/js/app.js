/**
 * Reusable Code Snippet Manager - Frontend REST API Client
 * Connects to Spring Boot Backend (/api/snippets) with automatic local fallback
 */
const API_BASE_URL = '/api/snippets';

async function fetchSnippetsFromApi(keyword = '', language = '', category = '') {
    const params = new URLSearchParams();
    if (keyword) params.append('keyword', keyword);
    if (language) params.append('language', language);
    if (category) params.append('category', category);
    const response = await fetch(`${API_BASE_URL}?${params.toString()}`);
    if (!response.ok) throw new Error('Backend API unreachable');
    return await response.json();
}

async function createSnippetApi(payload) {
    const response = await fetch(API_BASE_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (!response.ok) throw new Error('Failed to create snippet');
    return await response.json();
}

async function updateSnippetApi(id, payload) {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (!response.ok) throw new Error('Failed to update snippet');
    return await response.json();
}

async function deleteSnippetApi(id) {
    const response = await fetch(`${API_BASE_URL}/${id}`, { method: 'DELETE' });
    if (!response.ok) throw new Error('Failed to delete snippet');
    return await response.json();
}
