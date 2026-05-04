import { useCallback, useEffect, useState } from "react";
import "./App.css";
import { api } from "./api";

const TOKEN_KEY = "election_system_token";

const emptyElectionForm = {
  title: "",
  description: "",
  startTime: "",
  endTime: "",
};

function App() {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY) || "");
  const [currentUser, setCurrentUser] = useState(null);
  const [elections, setElections] = useState([]);
  const [users, setUsers] = useState([]);
  const [candidatesByElection, setCandidatesByElection] = useState({});
  const [selectedElectionId, setSelectedElectionId] = useState("");
  const [selectedResultsElectionId, setSelectedResultsElectionId] = useState("");
  const [results, setResults] = useState(null);
  const [statusMessage, setStatusMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [registerForm, setRegisterForm] = useState({
    username: "",
    email: "",
    password: "",
    role: "VOTER",
  });
  const [electionForm, setElectionForm] = useState(emptyElectionForm);
  const [editingElectionId, setEditingElectionId] = useState("");
  const [candidateForm, setCandidateForm] = useState({
    electionId: "",
    name: "",
  });
  const [voteForm, setVoteForm] = useState({
    electionId: "",
    candidateId: "",
  });

  const isAdmin =
    currentUser?.role === "ADMIN" || currentUser?.role === "SUPER_ADMIN";
  const isSuperAdmin = currentUser?.role === "SUPER_ADMIN";
  const isVoter = currentUser?.role === "VOTER";

  const loadUsersIfNeeded = useCallback(async (activeToken = token, user = currentUser) => {
    if (user?.role !== "SUPER_ADMIN") {
      setUsers([]);
      return;
    }
    const response = await api.getUsers(activeToken);
    setUsers(response.data || []);
  }, [currentUser, token]);

  const loadElections = useCallback(async (activeToken = token) => {
    const response = await api.getElections(activeToken);
    const nextElections = response.data || [];
    setElections(nextElections);

    if (!selectedElectionId && nextElections[0]?.id) {
      setSelectedElectionId(nextElections[0].id);
      setCandidateForm((prev) => ({ ...prev, electionId: nextElections[0].id }));
      setVoteForm((prev) => ({ ...prev, electionId: nextElections[0].id }));
    }

    if (!selectedResultsElectionId && nextElections[0]?.id) {
      setSelectedResultsElectionId(nextElections[0].id);
    }
  }, [selectedElectionId, selectedResultsElectionId, token]);

  const loadCandidates = useCallback(async (electionId, activeToken = token) => {
    if (!electionId || !activeToken) {
      return;
    }
    try {
      const response = await api.getCandidates(activeToken, electionId);
      setCandidatesByElection((prev) => ({ ...prev, [electionId]: response.data || [] }));
    } catch (error) {
      setErrorMessage(error.message);
    }
  }, [token]);

  const hydrateSession = useCallback(async (activeToken) => {
    setLoading(true);
    setErrorMessage("");
    try {
      const meResponse = await api.me(activeToken);
      setCurrentUser(meResponse.data);
      await Promise.all([loadElections(activeToken), loadUsersIfNeeded(activeToken, meResponse.data)]);
    } catch (error) {
      clearSession();
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }, [loadElections, loadUsersIfNeeded]);

  useEffect(() => {
    if (!token) {
      return;
    }
    hydrateSession(token);
  }, [hydrateSession, token]);

  useEffect(() => {
    if (selectedElectionId) {
      loadCandidates(selectedElectionId);
    }
  }, [loadCandidates, selectedElectionId]);

  async function handleLogin(event) {
    event.preventDefault();
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      const response = await api.login(loginForm);
      const nextToken = response.data.token;
      localStorage.setItem(TOKEN_KEY, nextToken);
      setToken(nextToken);
      setStatusMessage("Welcome back. Your command center is ready.");
      setLoginForm({ email: "", password: "" });
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      const response = await api.register(registerForm);
      const pendingCopy =
        registerForm.role === "ADMIN"
          ? "Registration sent. A super admin must approve this admin account before login."
          : response.message;
      setStatusMessage(pendingCopy);
      setRegisterForm({
        username: "",
        email: "",
        password: "",
        role: "VOTER",
      });
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleLogout() {
    setLoading(true);
    try {
      if (token) {
        await api.logout(token);
      }
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      clearSession();
      setLoading(false);
    }
  }

  async function handleElectionSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      if (editingElectionId) {
        await api.updateElection(token, editingElectionId, electionForm);
        setStatusMessage("Election updated.");
      } else {
        await api.createElection(token, electionForm);
        setStatusMessage("Election created.");
      }
      setElectionForm(emptyElectionForm);
      setEditingElectionId("");
      await loadElections();
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleDeleteElection(electionId) {
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      await api.deleteElection(token, electionId);
      setStatusMessage("Election deleted.");
      setCandidatesByElection((prev) => {
        const next = { ...prev };
        delete next[electionId];
        return next;
      });
      await loadElections();
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  function startEditingElection(election) {
    setEditingElectionId(election.id);
    setElectionForm({
      title: election.title,
      description: election.description,
      startTime: toInputDateTime(election.startTime),
      endTime: toInputDateTime(election.endTime),
    });
  }

  async function handleCandidateSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      await api.addCandidate(token, candidateForm);
      setStatusMessage("Candidate added.");
      setCandidateForm((prev) => ({ ...prev, name: "" }));
      await loadCandidates(candidateForm.electionId);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleRemoveCandidate(electionId, candidateId) {
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      await api.removeCandidate(token, candidateId);
      setStatusMessage("Candidate removed.");
      await loadCandidates(electionId);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleCastVote(event) {
    event.preventDefault();
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      await api.castVote(token, voteForm);
      setStatusMessage("Vote cast successfully.");
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleLoadResults() {
    if (!selectedResultsElectionId) {
      return;
    }
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      const response = await api.getResults(token, selectedResultsElectionId);
      setResults(response.data);
      setStatusMessage("Results loaded.");
    } catch (error) {
      setResults(null);
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleApproveAdmin(userId) {
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      await api.approveAdmin(token, userId);
      setStatusMessage("Admin account approved.");
      await loadUsersIfNeeded();
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleDeleteUser(userId) {
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      await api.deleteUser(token, userId);
      setStatusMessage("User removed.");
      await loadUsersIfNeeded();
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    setToken("");
    setCurrentUser(null);
    setUsers([]);
    setElections([]);
    setCandidatesByElection({});
    setResults(null);
  }

  const selectedCandidates = candidatesByElection[selectedElectionId] || [];
  const voteCandidates = candidatesByElection[voteForm.electionId] || [];

  return (
    <div className="app-shell">
      {!currentUser ? (
        <main className="landing-layout">
          <section className="hero-panel">
            <div className="hero-image-wrap">
              <img
                className="hero-image"
                src="/election_image.png"
                alt="Election themed visual"
              />
              <div className="hero-image-overlay">
                <span className="eyebrow">Secure Voting Portal</span>
                <h1>Election System</h1>
              </div>
            </div>
          </section>

          <section className="auth-panel">
            <div className="panel-stack">
              <form className="glass-card form-card" onSubmit={handleLogin}>
                <div className="card-head">
                  <span className="eyebrow">Access</span>
                  <h2>Sign in</h2>
                </div>

                <label>
                  Email
                  <input
                    type="email"
                    value={loginForm.email}
                    onChange={(event) =>
                      setLoginForm((prev) => ({ ...prev, email: event.target.value }))
                    }
                    placeholder="you@example.com"
                    required
                  />
                </label>

                <label>
                  Password
                  <input
                    type="password"
                    value={loginForm.password}
                    onChange={(event) =>
                      setLoginForm((prev) => ({ ...prev, password: event.target.value }))
                    }
                    placeholder="Your password"
                    required
                  />
                </label>

                <button className="primary-button" disabled={loading} type="submit">
                  {loading ? "Opening session..." : "Login"}
                </button>
              </form>

              <form className="glass-card form-card" onSubmit={handleRegister}>
                <div className="card-head">
                  <span className="eyebrow">Enrollment</span>
                  <h2>Create account</h2>
                </div>

                <label>
                  Username
                  <input
                    value={registerForm.username}
                    onChange={(event) =>
                      setRegisterForm((prev) => ({ ...prev, username: event.target.value }))
                    }
                    placeholder="campaign.operator"
                    required
                  />
                </label>

                <label>
                  Email
                  <input
                    type="email"
                    value={registerForm.email}
                    onChange={(event) =>
                      setRegisterForm((prev) => ({ ...prev, email: event.target.value }))
                    }
                    placeholder="you@example.com"
                    required
                  />
                </label>

                <label>
                  Password
                  <input
                    type="password"
                    minLength="8"
                    value={registerForm.password}
                    onChange={(event) =>
                      setRegisterForm((prev) => ({ ...prev, password: event.target.value }))
                    }
                    placeholder="At least 8 characters"
                    required
                  />
                </label>

                <label>
                  Role
                  <select
                    value={registerForm.role}
                    onChange={(event) =>
                      setRegisterForm((prev) => ({ ...prev, role: event.target.value }))
                    }
                  >
                    <option value="VOTER">Voter</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                </label>

                <button className="primary-button secondary-glow" disabled={loading} type="submit">
                  Register
                </button>
              </form>
            </div>
          </section>
        </main>
      ) : (
        <main className="dashboard-layout">
          <header className="glass-card command-bar">
            <div>
              <p className="eyebrow">Connected to {api.baseUrl}</p>
              <h1>Civic Command Deck</h1>
            </div>

            <div className="user-rail">
              <div className="identity-chip">
                <span>{currentUser.username}</span>
                <small>
                  {currentUser.role} • {currentUser.approvalStatus}
                </small>
              </div>
              <button className="ghost-button" onClick={() => hydrateSession(token)} type="button">
                Refresh
              </button>
              <button className="primary-button" onClick={handleLogout} type="button">
                Logout
              </button>
            </div>
          </header>

          {(statusMessage || errorMessage) && (
            <section className={`feedback-banner ${errorMessage ? "error-banner" : "success-banner"}`}>
              {errorMessage || statusMessage}
            </section>
          )}

          <section className="dashboard-grid">
            <article className="glass-card wide-panel">
              <div className="section-head">
                <div>
                  <span className="eyebrow">Live registry</span>
                  <h2>Elections</h2>
                </div>
              </div>

              <div className="election-list">
                {elections.map((election) => (
                  <div className="election-card" key={election.id}>
                    <div>
                      <h3>{election.title}</h3>
                      <p>{election.description}</p>
                    </div>
                    <div className="meta-row">
                      <span className={`status-pill status-${(election.status || "").toLowerCase()}`}>
                        {election.status}
                      </span>
                      <small>{formatDateRange(election.startTime, election.endTime)}</small>
                    </div>
                    {isAdmin && (
                      <div className="action-row">
                        <button
                          className="ghost-button"
                          onClick={() => startEditingElection(election)}
                          type="button"
                        >
                          Edit
                        </button>
                        <button
                          className="danger-button"
                          onClick={() => handleDeleteElection(election.id)}
                          type="button"
                        >
                          Delete
                        </button>
                      </div>
                    )}
                  </div>
                ))}
                {!elections.length && <p className="empty-state">No elections available yet.</p>}
              </div>
            </article>

            {isAdmin && (
              <article className="glass-card">
                <div className="section-head">
                  <div>
                    <span className="eyebrow">Management</span>
                    <h2>{editingElectionId ? "Edit election" : "Create election"}</h2>
                  </div>
                </div>

                <form className="form-card compact-form" onSubmit={handleElectionSubmit}>
                  <label>
                    Title
                    <input
                      value={electionForm.title}
                      onChange={(event) =>
                        setElectionForm((prev) => ({ ...prev, title: event.target.value }))
                      }
                      required
                    />
                  </label>
                  <label>
                    Description
                    <textarea
                      rows="4"
                      value={electionForm.description}
                      onChange={(event) =>
                        setElectionForm((prev) => ({ ...prev, description: event.target.value }))
                      }
                      required
                    />
                  </label>
                  <label>
                    Start time
                    <input
                      type="datetime-local"
                      value={electionForm.startTime}
                      onChange={(event) =>
                        setElectionForm((prev) => ({ ...prev, startTime: event.target.value }))
                      }
                      required
                    />
                  </label>
                  <label>
                    End time
                    <input
                      type="datetime-local"
                      value={electionForm.endTime}
                      onChange={(event) =>
                        setElectionForm((prev) => ({ ...prev, endTime: event.target.value }))
                      }
                      required
                    />
                  </label>
                  <div className="action-row">
                    <button className="primary-button" disabled={loading} type="submit">
                      {editingElectionId ? "Save changes" : "Create election"}
                    </button>
                    {editingElectionId && (
                      <button
                        className="ghost-button"
                        onClick={() => {
                          setEditingElectionId("");
                          setElectionForm(emptyElectionForm);
                        }}
                        type="button"
                      >
                        Cancel
                      </button>
                    )}
                  </div>
                </form>
              </article>
            )}

            {isAdmin && (
              <article className="glass-card">
                <div className="section-head">
                  <div>
                    <span className="eyebrow">Ballot design</span>
                    <h2>Candidates</h2>
                  </div>
                </div>

                <form className="form-card compact-form" onSubmit={handleCandidateSubmit}>
                  <label>
                    Election
                    <select
                      value={candidateForm.electionId}
                      onChange={(event) => {
                        setCandidateForm((prev) => ({ ...prev, electionId: event.target.value }));
                        setSelectedElectionId(event.target.value);
                      }}
                      required
                    >
                      <option value="">Select election</option>
                      {elections.map((election) => (
                        <option key={election.id} value={election.id}>
                          {election.title}
                        </option>
                      ))}
                    </select>
                  </label>

                  <label>
                    Candidate name
                    <input
                      value={candidateForm.name}
                      onChange={(event) =>
                        setCandidateForm((prev) => ({ ...prev, name: event.target.value }))
                      }
                      required
                    />
                  </label>

                  <button className="primary-button" disabled={loading} type="submit">
                    Add candidate
                  </button>
                </form>

                <div className="candidate-list">
                  {selectedCandidates.map((candidate) => (
                    <div className="candidate-row" key={candidate.id}>
                      <span>{candidate.name}</span>
                      <button
                        className="danger-button"
                        onClick={() => handleRemoveCandidate(selectedElectionId, candidate.id)}
                        type="button"
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                  {!selectedCandidates.length && (
                    <p className="empty-state">Select an election to inspect candidates.</p>
                  )}
                </div>
              </article>
            )}

            {isVoter && (
              <article className="glass-card">
                <div className="section-head">
                  <div>
                    <span className="eyebrow">Participation</span>
                    <h2>Cast vote</h2>
                  </div>
                </div>

                <form className="form-card compact-form" onSubmit={handleCastVote}>
                  <label>
                    Election
                    <select
                      value={voteForm.electionId}
                      onChange={(event) => {
                        const nextElectionId = event.target.value;
                        setVoteForm({ electionId: nextElectionId, candidateId: "" });
                        loadCandidates(nextElectionId);
                      }}
                      required
                    >
                      <option value="">Select election</option>
                      {elections.map((election) => (
                        <option key={election.id} value={election.id}>
                          {election.title}
                        </option>
                      ))}
                    </select>
                  </label>

                  <label>
                    Candidate
                    <select
                      value={voteForm.candidateId}
                      onChange={(event) =>
                        setVoteForm((prev) => ({ ...prev, candidateId: event.target.value }))
                      }
                      required
                    >
                      <option value="">Select candidate</option>
                      {voteCandidates.map((candidate) => (
                        <option key={candidate.id} value={candidate.id}>
                          {candidate.name}
                        </option>
                      ))}
                    </select>
                  </label>

                  <button className="primary-button" disabled={loading} type="submit">
                    Submit vote
                  </button>
                </form>
              </article>
            )}

            <article className="glass-card">
              <div className="section-head">
                <div>
                  <span className="eyebrow">Transparency</span>
                  <h2>Results</h2>
                </div>
              </div>

              <div className="results-controls">
                <select
                  value={selectedResultsElectionId}
                  onChange={(event) => setSelectedResultsElectionId(event.target.value)}
                >
                  <option value="">Select election</option>
                  {elections.map((election) => (
                    <option key={election.id} value={election.id}>
                      {election.title}
                    </option>
                  ))}
                </select>

                <button className="ghost-button" onClick={handleLoadResults} type="button">
                  Load results
                </button>
              </div>

              <div className="results-list">
                {results?.votesPerCandidate &&
                  Object.entries(results.votesPerCandidate).map(([candidateName, count]) => (
                    <div className="result-bar" key={candidateName}>
                      <div className="result-label">
                        <span>{candidateName}</span>
                        <strong>{count}</strong>
                      </div>
                      <div className="bar-track">
                        <div
                          className="bar-fill"
                          style={{ width: `${Math.min(100, count * 18)}%` }}
                        />
                      </div>
                    </div>
                  ))}
                {!results && (
                  <p className="empty-state">
                    Results appear here after you select an election and the backend marks it ended.
                  </p>
                )}
              </div>
            </article>

            {isSuperAdmin && (
              <article className="glass-card wide-panel">
                <div className="section-head">
                  <div>
                    <span className="eyebrow">Governance</span>
                    <h2>User control</h2>
                  </div>
                </div>

                <div className="user-table">
                  {users.map((user) => (
                    <div className="user-row" key={user.id}>
                      <div>
                        <strong>{user.username}</strong>
                        <p>
                          {user.email} • {user.role} • {user.approvalStatus}
                        </p>
                      </div>
                      <div className="action-row">
                        {user.role === "ADMIN" && user.approvalStatus === "PENDING" && (
                          <button
                            className="ghost-button"
                            onClick={() => handleApproveAdmin(user.id)}
                            type="button"
                          >
                            Approve admin
                          </button>
                        )}
                        {user.role !== "SUPER_ADMIN" && (
                          <button
                            className="danger-button"
                            onClick={() => handleDeleteUser(user.id)}
                            type="button"
                          >
                            Remove user
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </article>
            )}
          </section>
        </main>
      )}

      {(loading && currentUser) && <div className="loading-badge">Syncing with backend...</div>}
      {!currentUser && (statusMessage || errorMessage) && (
        <div className={`floating-toast ${errorMessage ? "error-banner" : "success-banner"}`}>
          {errorMessage || statusMessage}
        </div>
      )}
    </div>
  );
}

function formatDateRange(start, end) {
  return `${formatDate(start)} to ${formatDate(end)}`;
}

function formatDate(value) {
  if (!value) {
    return "Unknown";
  }
  return new Date(value).toLocaleString();
}

function toInputDateTime(value) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  const offset = date.getTimezoneOffset();
  const localDate = new Date(date.getTime() - offset * 60 * 1000);
  return localDate.toISOString().slice(0, 16);
}

export default App;
