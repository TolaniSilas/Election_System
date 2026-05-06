import { useCallback, useEffect, useMemo, useState } from "react";
import "./App.css";
import { api } from "./api";

const TOKEN_KEY = "election_system_token";
const STATES = [
  "ABIA", "ADAMAWA", "AKWA IBOM", "ANAMBRA", "BAUCHI", "BAYELSA", "BENUE",
  "BORNO", "CROSS RIVER", "DELTA", "EBONYI", "EDO", "EKITI", "ENUGU", "FCT",
  "GOMBE", "IMO", "JIGAWA", "KADUNA", "KANO", "KATSINA", "KEBBI", "KOGI",
  "KWARA", "LAGOS", "NASARAWA", "NIGER", "OGUN", "ONDO", "OSUN", "OYO",
  "PLATEAU", "RIVERS", "SOKOTO", "TARABA", "YOBE", "ZAMFARA",
];

const emptyElectionForm = {
  title: "",
  description: "",
  startTime: "",
  endTime: "",
  category: "PRESIDENT",
  scope: "NATIONAL",
  state: "",
};

const ELECTION_CATEGORIES = ["PRESIDENT", "GOVERNOR", "CHAIRMAN"];
const ELECTION_SCOPES = ["NATIONAL", "STATE"];

function App() {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY) || "");
  const [currentUser, setCurrentUser] = useState(null);
  const [elections, setElections] = useState([]);
  const [users, setUsers] = useState([]);
  const [candidatesByElection, setCandidatesByElection] = useState({});
  const [selectedElectionId, setSelectedElectionId] = useState("");
  const [selectedResultsElectionId, setSelectedResultsElectionId] = useState("");
  const [results, setResults] = useState(null);
  const [participation, setParticipation] = useState({ totalsByCategory: {}, elections: [] });
  const [statusMessage, setStatusMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [sharePrompt, setSharePrompt] = useState(null);

  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [registerForm, setRegisterForm] = useState({
    username: "",
    email: "",
    password: "",
    role: "VOTER",
    nin: "",
    stateOfOrigin: "LAGOS",
  });
  const [electionForm, setElectionForm] = useState(emptyElectionForm);
  const [editingElectionId, setEditingElectionId] = useState("");
  const [candidateForm, setCandidateForm] = useState({
    electionId: "",
    name: "",
    party: "",
    biography: "",
    imageFile: null,
  });
  const [editingCandidateId, setEditingCandidateId] = useState("");
  const [selectedCandidateProfile, setSelectedCandidateProfile] = useState(null);
  const [voteForm, setVoteForm] = useState({
    electionId: "",
    candidateId: "",
  });

  const isAdmin =
    currentUser?.role === "ADMIN" || currentUser?.role === "SUPER_ADMIN";
  const isSuperAdmin = currentUser?.role === "SUPER_ADMIN";
  const isVoter = currentUser?.role === "VOTER";
  const canVote = currentUser?.voterApprovalStatus === "APPROVED";
  const selectedCandidates = candidatesByElection[selectedElectionId] || [];
  const voteCandidates = candidatesByElection[voteForm.electionId] || [];

  const pendingVoters = useMemo(
    () => users.filter((user) => user.role === "VOTER" && user.voterApprovalStatus !== "APPROVED"),
    [users]
  );
  const pendingAdmins = useMemo(
    () => users.filter((user) => user.role === "ADMIN" && user.approvalStatus === "PENDING"),
    [users]
  );

  const loadUsers = useCallback(
    async (activeToken = token, user = currentUser) => {
      if (!user || (user.role !== "ADMIN" && user.role !== "SUPER_ADMIN")) {
        setUsers([]);
        return;
      }
      const response = await api.getUsers(activeToken);
      setUsers(response.data || []);
    },
    [currentUser, token]
  );

  const loadElections = useCallback(
    async (activeToken = token) => {
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
    },
    [selectedElectionId, selectedResultsElectionId, token]
  );

  const loadCandidates = useCallback(
    async (electionId, activeToken = token) => {
      if (!electionId || !activeToken) {
        return;
      }
      const response = await api.getCandidates(activeToken, electionId);
      setCandidatesByElection((prev) => ({ ...prev, [electionId]: response.data || [] }));
    },
    [token]
  );

  const loadParticipation = useCallback(
    async (activeToken = token) => {
      if (!activeToken) {
        return;
      }
      const response = await api.getParticipation(activeToken);
      setParticipation(response.data || { totalsByCategory: {}, elections: [] });
    },
    [token]
  );

  const hydrateSession = useCallback(
    async (activeToken) => {
      setLoading(true);
      setErrorMessage("");
      try {
        const meResponse = await api.me(activeToken);
        setCurrentUser(meResponse.data);
        await Promise.all([
          loadElections(activeToken),
          loadUsers(activeToken, meResponse.data),
          loadParticipation(activeToken),
        ]);
      } catch (error) {
        clearSession();
        setErrorMessage(error.message);
      } finally {
        setLoading(false);
      }
    },
    [loadElections, loadParticipation, loadUsers]
  );

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

  useEffect(() => {
    if (!currentUser || !selectedResultsElectionId || !token) {
      return;
    }
    const timer = window.setInterval(async () => {
      try {
        const [resultResponse, participationResponse] = await Promise.all([
          api.getResults(token, selectedResultsElectionId),
          api.getParticipation(token),
        ]);
        setResults(resultResponse.data);
        setParticipation(participationResponse.data || { totalsByCategory: {}, elections: [] });
      } catch (error) {
        // Keep live refresh quiet; explicit button still surfaces errors.
      }
    }, 12000);
    return () => window.clearInterval(timer);
  }, [currentUser, selectedResultsElectionId, token]);

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
      setStatusMessage("Welcome back to your election dashboard.");
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
      const submittedForm = { ...registerForm };
      await api.register(submittedForm);

      if (submittedForm.role === "VOTER") {
        const loginResponse = await api.login({
          email: submittedForm.email,
          password: submittedForm.password,
        });
        const nextToken = loginResponse.data.token;
        localStorage.setItem(TOKEN_KEY, nextToken);
        setToken(nextToken);
        setStatusMessage(
          "Registration successful. You can enter the portal now, but voting stays locked until an admin approves you."
        );
      } else {
        setStatusMessage(
          "Registration sent. A super admin must approve this admin account before login."
        );
      }

      setRegisterForm({
        username: "",
        email: "",
        password: "",
        role: "VOTER",
        nin: "",
        stateOfOrigin: "LAGOS",
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
      category: election.category || "PRESIDENT",
      scope: election.scope || "NATIONAL",
      state: election.state || "",
    });
  }

  async function handleCandidateSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      if (!candidateForm.imageFile && !editingCandidateId) {
        throw new Error("Please select a candidate image");
      }
      const body = new FormData();
      body.append("electionId", candidateForm.electionId);
      body.append("name", candidateForm.name);
      body.append("party", candidateForm.party);
      body.append("biography", candidateForm.biography);
      if (candidateForm.imageFile) {
        body.append("image", candidateForm.imageFile);
      }
      if (editingCandidateId) {
        await api.updateCandidate(token, editingCandidateId, body);
        setStatusMessage("Candidate updated.");
      } else {
        await api.addCandidate(token, body);
        setStatusMessage("Candidate added.");
      }
      setCandidateForm((prev) => ({ ...prev, name: "", party: "", biography: "", imageFile: null }));
      setEditingCandidateId("");
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

  function startEditingCandidate(candidate) {
    setEditingCandidateId(candidate.id);
    setCandidateForm((prev) => ({
      ...prev,
      electionId: candidate.electionId,
      name: candidate.name,
      party: candidate.party,
      biography: candidate.biography,
      imageFile: null,
    }));
  }

  async function handleCastVote(event) {
    event.preventDefault();
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      const votedElection = elections.find((election) => election.id === voteForm.electionId);
      await api.castVote(token, voteForm);
      setStatusMessage("Thank you for casting your vote.");
      setSharePrompt(votedElection);
      setVoteForm((prev) => ({ ...prev, candidateId: "" }));
      await loadParticipation();
      if (selectedResultsElectionId === voteForm.electionId) {
        await handleLoadResults(voteForm.electionId);
      }
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleLoadResults(electionId = selectedResultsElectionId) {
    if (!electionId) {
      return;
    }
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      const response = await api.getResults(token, electionId);
      setResults(response.data);
      await loadParticipation();
      setStatusMessage("Live vote counts loaded.");
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
      await loadUsers();
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleApproveVoter(userId) {
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      await api.approveVoter(token, userId);
      setStatusMessage("Voter approved to cast votes.");
      await loadUsers();
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleRejectVoter(userId) {
    setLoading(true);
    setErrorMessage("");
    setStatusMessage("");
    try {
      await api.rejectVoter(token, userId);
      setStatusMessage("Voter rejected and removed from the registry.");
      await loadUsers();
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
      await loadUsers();
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
    setParticipation({ totalsByCategory: {}, elections: [] });
    setSharePrompt(null);
    setSelectedCandidateProfile(null);
  }

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
                    placeholder="citizen.name"
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
                  NIN
                  <input
                    value={registerForm.nin}
                    maxLength="11"
                    onChange={(event) =>
                      setRegisterForm((prev) => ({ ...prev, nin: event.target.value.replace(/\D/g, "") }))
                    }
                    placeholder="11-digit NIN"
                    required
                  />
                </label>

                <label>
                  State of origin
                  <select
                    value={registerForm.stateOfOrigin}
                    onChange={(event) =>
                      setRegisterForm((prev) => ({ ...prev, stateOfOrigin: event.target.value }))
                    }
                  >
                    {STATES.map((state) => (
                      <option key={state} value={state}>
                        {state}
                      </option>
                    ))}
                  </select>
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
              <h1>Election Dashboard</h1>
            </div>

            <div className="user-rail">
              <div className="identity-chip">
                <span>{currentUser.username}</span>
                <small>
                  {currentUser.role} • {currentUser.stateOfOrigin} • Vote {currentUser.voterApprovalStatus}
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

          {isVoter && !canVote && (
            <section className="feedback-banner warning-banner">
              Your account has been created, but you are not yet approved to vote. An admin must approve your NIN and state record before your ballot opens.
            </section>
          )}

          {(statusMessage || errorMessage) && (
            <section className={`feedback-banner ${errorMessage ? "error-banner" : "success-banner"}`}>
              {errorMessage || statusMessage}
            </section>
          )}

          <section className="dashboard-grid">
            <article className="glass-card wide-panel">
              <div className="section-head">
                <div>
                  <span className="eyebrow">Active elections</span>
                  <h2>Available ballots</h2>
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
                      <small>
                        {election.category} {election.state ? `• ${election.state}` : "• NATIONAL"}
                      </small>
                    </div>
                    <small>{formatDateRange(election.startTime, election.endTime)}</small>
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
                {!elections.length && <p className="empty-state">No elections available for your profile yet.</p>}
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
                  <label>
                    Election category
                    <select
                      value={electionForm.category}
                      onChange={(event) =>
                        setElectionForm((prev) => ({ ...prev, category: event.target.value }))
                      }
                    >
                      {ELECTION_CATEGORIES.map((category) => (
                        <option key={category} value={category}>
                          {category}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Election scope
                    <select
                      value={electionForm.scope}
                      onChange={(event) =>
                        setElectionForm((prev) => ({
                          ...prev,
                          scope: event.target.value,
                          state: event.target.value === "NATIONAL" ? "" : prev.state,
                        }))
                      }
                    >
                      {ELECTION_SCOPES.map((scope) => (
                        <option key={scope} value={scope}>
                          {scope}
                        </option>
                      ))}
                    </select>
                  </label>
                  {electionForm.scope === "STATE" && (
                    <label>
                      State
                      <select
                        value={electionForm.state}
                        onChange={(event) =>
                          setElectionForm((prev) => ({ ...prev, state: event.target.value }))
                        }
                        required
                      >
                        <option value="">Select state</option>
                        {STATES.map((state) => (
                          <option key={state} value={state}>
                            {state}
                          </option>
                        ))}
                      </select>
                    </label>
                  )}
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
                  <label>
                    Party
                    <input
                      value={candidateForm.party}
                      onChange={(event) =>
                        setCandidateForm((prev) => ({ ...prev, party: event.target.value.toUpperCase() }))
                      }
                      placeholder="APC / PDP / ADP"
                      required
                    />
                  </label>
                  <label>
                    Candidate image
                    <input
                      type="file"
                      accept="image/png,image/jpeg,image/webp"
                      onChange={(event) =>
                        setCandidateForm((prev) => ({ ...prev, imageFile: event.target.files?.[0] || null }))
                      }
                      required={!editingCandidateId}
                    />
                  </label>
                  <label>
                    Biography
                    <textarea
                      rows="4"
                      value={candidateForm.biography}
                      onChange={(event) =>
                        setCandidateForm((prev) => ({ ...prev, biography: event.target.value }))
                      }
                      placeholder="Short biography for voters"
                      required
                    />
                  </label>

                  <div className="action-row">
                    <button className="primary-button" disabled={loading} type="submit">
                      {editingCandidateId ? "Save candidate" : "Add candidate"}
                    </button>
                    {editingCandidateId && (
                      <button
                        className="ghost-button"
                        onClick={() => {
                          setEditingCandidateId("");
                          setCandidateForm((prev) => ({
                            ...prev,
                            name: "",
                            party: "",
                            biography: "",
                            imageFile: null,
                          }));
                        }}
                        type="button"
                      >
                        Cancel edit
                      </button>
                    )}
                  </div>
                </form>

                <div className="candidate-list">
                  {selectedCandidates.map((candidate) => (
                    <div className="candidate-row" key={candidate.id}>
                      <div>
                        <strong>{candidate.name}</strong>
                        <p>{candidate.party}</p>
                      </div>
                      <button
                        className="ghost-button"
                        onClick={() => startEditingCandidate(candidate)}
                        type="button"
                      >
                        Edit
                      </button>
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
                    <h2>Cast your vote</h2>
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
                      disabled={!canVote}
                    >
                      <option value="">Select candidate</option>
                      {voteCandidates.map((candidate) => (
                        <option key={candidate.id} value={candidate.id}>
                          {candidate.name}
                        </option>
                      ))}
                    </select>
                  </label>

                  <button className="primary-button" disabled={loading || !canVote} type="submit">
                    {canVote ? "Submit vote" : "Awaiting approval"}
                  </button>
                </form>
                <div className="candidate-gallery">
                  {voteCandidates.map((candidate) => (
                    <button
                      className="candidate-profile-card"
                      key={candidate.id}
                      onClick={() => setSelectedCandidateProfile(candidate)}
                      type="button"
                    >
                      <img src={resolveImageUrl(candidate.imageUrl)} alt={candidate.name} />
                      <div>
                        <strong>{candidate.name}</strong>
                        <p>{candidate.party}</p>
                      </div>
                    </button>
                  ))}
                  {!voteCandidates.length && (
                    <p className="empty-state">
                      Candidates for this election will appear here with full biography.
                    </p>
                  )}
                </div>
              </article>
            )}

            <article className="glass-card">
              <div className="section-head">
                <div>
                  <span className="eyebrow">Transparency</span>
                  <h2>Live vote count</h2>
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

                <button className="ghost-button" onClick={() => handleLoadResults()} type="button">
                  Refresh tally
                </button>
              </div>

              {results && (
                <div className="results-summary">
                  <strong>{results.electionTitle}</strong>
                  <small>
                    Total votes: {results.totalVotes} {results.state ? `• ${results.state}` : "• NATIONAL"}
                  </small>
                </div>
              )}

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
                          style={{
                            width: `${results.totalVotes ? (count / results.totalVotes) * 100 : 0}%`,
                          }}
                        />
                      </div>
                    </div>
                  ))}
                {!results && (
                  <p className="empty-state">
                    Choose any election to see live aggregate counts. Individual votes stay private.
                  </p>
                )}
              </div>
              <div className="participation-summary">
                <h3>Real-time participation by election type</h3>
                <div className="stats-grid">
                  {ELECTION_CATEGORIES.map((category) => (
                    <div className="stat-card" key={category}>
                      <small>{category}</small>
                      <strong>{participation.totalsByCategory?.[category] || 0}</strong>
                      <span>participants</span>
                    </div>
                  ))}
                </div>
                <div className="participation-list">
                  {(participation.elections || []).map((item) => (
                    <div className="result-bar" key={item.electionId}>
                      <div className="result-label">
                        <span>
                          {item.electionTitle} ({item.category}
                          {item.state ? ` - ${item.state}` : ""})
                        </span>
                        <strong>{item.participantCount}</strong>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </article>

            {isAdmin && (
              <article className="glass-card wide-panel">
                <div className="section-head">
                  <div>
                    <span className="eyebrow">Approvals</span>
                    <h2>Voter verification</h2>
                  </div>
                </div>

                <div className="user-table">
                  {pendingVoters.map((user) => (
                    <div className="user-row" key={user.id}>
                      <div>
                        <strong>{user.username}</strong>
                        <p>
                          {user.email} • {user.stateOfOrigin} • NIN {user.nin} • Vote {user.voterApprovalStatus}
                        </p>
                      </div>
                      <div className="action-row">
                        <button
                          className="ghost-button"
                          onClick={() => handleApproveVoter(user.id)}
                          type="button"
                        >
                          Approve
                        </button>
                        <button
                          className="danger-button"
                          onClick={() => handleRejectVoter(user.id)}
                          type="button"
                        >
                          Reject
                        </button>
                      </div>
                    </div>
                  ))}
                  {!pendingVoters.length && (
                    <p className="empty-state">No pending voters at the moment.</p>
                  )}
                </div>
              </article>
            )}

            {(isAdmin || isSuperAdmin) && (
              <article className="glass-card wide-panel">
                <div className="section-head">
                  <div>
                    <span className="eyebrow">Governance</span>
                    <h2>User control</h2>
                  </div>
                </div>

                <div className="user-table">
                  {pendingAdmins.map((user) => (
                    <div className="user-row" key={user.id}>
                      <div>
                        <strong>{user.username}</strong>
                        <p>
                          {user.email} • {user.role} • {user.approvalStatus}
                        </p>
                      </div>
                      <div className="action-row">
                        {isSuperAdmin && (
                          <button
                            className="ghost-button"
                            onClick={() => handleApproveAdmin(user.id)}
                            type="button"
                          >
                            Approve admin
                          </button>
                        )}
                        {isSuperAdmin && (
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
                  {!pendingAdmins.length && (
                    <p className="empty-state">No pending admin approvals right now.</p>
                  )}
                </div>
              </article>
            )}
          </section>
        </main>
      )}

      {sharePrompt && (
        <div className="share-modal-backdrop" onClick={() => setSharePrompt(null)} role="presentation">
          <div className="share-modal glass-card" onClick={(event) => event.stopPropagation()} role="dialog" aria-modal="true">
            <span className="eyebrow">Thank you</span>
            <h2>Thank you for casting your vote</h2>
            <p>
              Your ballot for the {sharePrompt.title} has been recorded successfully. Your exact choice remains private.
            </p>
            <div className="share-links">
              <a
                className="ghost-button"
                href={buildShareLink("twitter", sharePrompt.title)}
                rel="noreferrer"
                target="_blank"
              >
                Share on X
              </a>
              <a
                className="ghost-button"
                href={buildShareLink("facebook", sharePrompt.title)}
                rel="noreferrer"
                target="_blank"
              >
                Share on Facebook
              </a>
              <a
                className="ghost-button"
                href={buildShareLink("instagram", sharePrompt.title)}
                rel="noreferrer"
                target="_blank"
              >
                Open Instagram
              </a>
            </div>
            <button className="primary-button" onClick={() => setSharePrompt(null)} type="button">
              Close
            </button>
          </div>
        </div>
      )}

      {selectedCandidateProfile && (
        <div
          className="share-modal-backdrop"
          onClick={() => setSelectedCandidateProfile(null)}
          role="presentation"
        >
          <div className="share-modal glass-card" onClick={(event) => event.stopPropagation()} role="dialog" aria-modal="true">
            <span className="eyebrow">Candidate Profile</span>
            <h2>{selectedCandidateProfile.name}</h2>
            <img
              className="candidate-modal-image"
              src={resolveImageUrl(selectedCandidateProfile.imageUrl)}
              alt={selectedCandidateProfile.name}
            />
            <p>
              <strong>Party:</strong> {selectedCandidateProfile.party}
            </p>
            <p>{selectedCandidateProfile.biography}</p>
            <button className="primary-button" onClick={() => setSelectedCandidateProfile(null)} type="button">
              Close
            </button>
          </div>
        </div>
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

function buildShareLink(platform, electionTitle) {
  const text = encodeURIComponent(`I have successfully cast my vote in the ${electionTitle}.`);
  if (platform === "twitter") {
    return `https://twitter.com/intent/tweet?text=${text}`;
  }
  if (platform === "facebook") {
    return `https://www.facebook.com/sharer/sharer.php?quote=${text}`;
  }
  return "https://www.instagram.com/";
}

function resolveImageUrl(imageUrl) {
  if (!imageUrl) {
    return "";
  }
  if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
    return imageUrl;
  }
  return `${api.baseUrl}${imageUrl}`;
}

export default App;
