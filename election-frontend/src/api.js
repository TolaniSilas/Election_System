const API_BASE_URL =
  process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

async function request(path, { method = "GET", token, body } = {}) {
  const headers = {};

  if (body) {
    headers["Content-Type"] = "application/json";
  }

  if (token) {
    headers["X-Auth-Token"] = token;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const error = new Error(payload?.message || "Request failed");
    error.status = response.status;
    error.payload = payload;
    throw error;
  }

  return payload;
}

export const api = {
  baseUrl: API_BASE_URL,
  register: (body) => request("/api/auth/register", { method: "POST", body }),
  login: (body) => request("/api/auth/login", { method: "POST", body }),
  logout: (token) => request("/api/auth/logout", { method: "POST", token }),
  me: (token) => request("/api/auth/me", { token }),
  getUsers: (token) => request("/api/auth/users", { token }),
  approveAdmin: (token, userId) =>
    request(`/api/auth/users/${userId}/approve-admin`, {
      method: "POST",
      token,
    }),
  deleteUser: (token, userId) =>
    request(`/api/auth/users/${userId}`, { method: "DELETE", token }),
  getElections: (token) => request("/api/elections", { token }),
  createElection: (token, body) =>
    request("/api/elections", { method: "POST", token, body }),
  updateElection: (token, electionId, body) =>
    request(`/api/elections/${electionId}`, {
      method: "PUT",
      token,
      body,
    }),
  deleteElection: (token, electionId) =>
    request(`/api/elections/${electionId}`, { method: "DELETE", token }),
  getCandidates: (token, electionId) =>
    request(`/api/candidates/election/${electionId}`, { token }),
  addCandidate: (token, body) =>
    request("/api/candidates", { method: "POST", token, body }),
  removeCandidate: (token, candidateId) =>
    request(`/api/candidates/${candidateId}`, { method: "DELETE", token }),
  castVote: (token, body) =>
    request("/api/votes", { method: "POST", token, body }),
  getResults: (token, electionId) =>
    request(`/api/votes/results/${electionId}`, { token }),
};