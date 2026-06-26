const BASE_URL = 'http://localhost:8080/api';
/**
 * 公共工具类：解析JWT Token
 */
function parseJwt(token) {
    try {
        return JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
        return null;
    }
}

/**
 * 获取当前登录用户信息（从token解析）
 * @returns { id, username }
 */
function getCurrentUser() {
    const token = localStorage.getItem("token");
    if (!token) return null;

    const payload = parseJwt(token);
    return payload || null;
}

