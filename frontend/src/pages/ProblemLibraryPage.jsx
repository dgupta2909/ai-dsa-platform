import { useState, useEffect } from 'react';
import { getProblems } from '../services/problemService';

function ProblemLibraryPage({ onSelectProblem }) {
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [search, setSearch] = useState('');
  const [difficulty, setDifficulty] = useState('');
  const [category, setCategory] = useState('');

  const fetchProblems = () => {
    setLoading(true);
    setError(null);
    getProblems({
      search: search.trim() || undefined,
      difficulty: difficulty || undefined,
      category: category || undefined,
    })
      .then((data) => setProblems(data || []))
      .catch((err) => setError(err.message || 'Failed to load problems'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchProblems();
  }, [difficulty, category]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchProblems();
  };

  const handleClearFilters = () => {
    setSearch('');
    setDifficulty('');
    setCategory('');
  };

  const categories = [
    'Array',
    'Linked List',
    'Binary Search',
    'Stack',
    'Intervals',
    'Dynamic Programming',
    'String',
  ];

  return (
    <div className="main-content-view">
      <div className="view-header">
        <div>
          <h1 className="view-title">Problem Library</h1>
          <p className="view-subtitle">Practice curated Data Structures and Algorithms problems.</p>
        </div>
      </div>

      <div className="filter-bar">
        <form onSubmit={handleSearchSubmit} className="search-form">
          <input
            type="text"
            className="search-input"
            placeholder="Search by problem title or tags..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button type="submit" className="primary-btn search-btn">
            Search
          </button>
        </form>

        <div className="select-filters">
          <select
            className="filter-select"
            value={difficulty}
            onChange={(e) => setDifficulty(e.target.value)}
          >
            <option value="">All Difficulties</option>
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>

          <select
            className="filter-select"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
          >
            <option value="">All Categories</option>
            {categories.map((c) => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>

          {(search || difficulty || category) && (
            <button
              type="button"
              className="clear-btn"
              onClick={handleClearFilters}
            >
              Clear Filters
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="alert alert-error">
          <span>{error}</span>
          <button type="button" className="retry-btn ml-2" onClick={fetchProblems}>
            Retry
          </button>
        </div>
      )}

      {loading ? (
        <div className="loading-state">Loading problems from library...</div>
      ) : problems.length === 0 ? (
        <div className="empty-state">
          <p>No problems found matching your filters.</p>
          <button type="button" className="secondary-btn" onClick={handleClearFilters}>
            Clear Filters
          </button>
        </div>
      ) : (
        <div className="problems-table-wrapper">
          <table className="problems-table">
            <thead>
              <tr>
                <th style={{ width: '60px' }}>#</th>
                <th>Title</th>
                <th>Category</th>
                <th>Difficulty</th>
                <th>Tags</th>
                <th style={{ width: '120px' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {problems.map((p, idx) => (
                <tr
                  key={p.id}
                  className="problem-row"
                  onClick={() => onSelectProblem(p.id)}
                >
                  <td className="problem-num">{idx + 1}</td>
                  <td className="problem-name">
                    <strong>{p.title}</strong>
                  </td>
                  <td>
                    <span className="category-tag">{p.category}</span>
                  </td>
                  <td>
                    <span className={`difficulty-badge badge-${p.difficulty.toLowerCase()}`}>
                      {p.difficulty}
                    </span>
                  </td>
                  <td>
                    <div className="tags-list">
                      {p.tags.map((tag, tIdx) => (
                        <span key={tIdx} className="tag-pill">{tag}</span>
                      ))}
                    </div>
                  </td>
                  <td>
                    <button
                      type="button"
                      className="view-btn"
                      onClick={(e) => {
                        e.stopPropagation();
                        onSelectProblem(p.id);
                      }}
                    >
                      Solve →
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default ProblemLibraryPage;
