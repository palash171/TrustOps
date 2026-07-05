import { useEffect, useState } from 'react'
import {
  createComment,
  getComments,
  updateCommentStatus,
} from './api/comments'
import { CommentForm } from './components/CommentForm'
import { CommentQueue } from './components/CommentQueue'
import type {
  CommentPage,
  ModerationStatus,
  StatusFilter,
} from './types/comment'
import './App.css'

/**
 * App is the main coordinator for the frontend.
 *
 * React calls App when the page first loads and whenever its state changes.
 * App owns data shared by multiple child components and calls the API methods.
 * The smaller components below only display data or report user actions back.
 */
function App() {
  // Stores the current page returned by Spring Boot. It is null before the first GET finishes.
  const [commentPage, setCommentPage] = useState<CommentPage | null>(null)

  // Stores the dropdown choice. ALL means do not send a status query parameter.
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')

  // Spring pages start from 0, so page 0 is the first page.
  const [page, setPage] = useState(0)

  // This number has no business meaning. Changing it tells useEffect to load again.
  const [reloadNumber, setReloadNumber] = useState(0)

  // Stores the ID being updated so only that comment's buttons become disabled.
  const [updatingId, setUpdatingId] = useState<string | null>(null)

  // Loading controls the message shown while the GET request is waiting.
  const [loading, setLoading] = useState(true)

  // null means no error. A string means display that message to the user.
  const [error, setError] = useState<string | null>(null)

  /**
   * React runs this after App appears.
   * It runs again whenever the filter, page, or reload number changes.
   */
  useEffect(() => {
    async function loadComments() {
      setLoading(true)
      setError(null)

      try {
        // getComments expects undefined when no status filter should be sent.
        const requestedStatus = statusFilter === 'ALL' ? undefined : statusFilter

        // Wait for the backend and store its returned JSON page in React state.
        const loadedPage = await getComments(requestedStatus, page)
        setCommentPage(loadedPage)
      } catch {
        setError('Could not load comments.')
      } finally {
        setLoading(false)
      }
    }

    // void means we intentionally start this Promise without returning it from useEffect.
    void loadComments()
  }, [statusFilter, page, reloadNumber])

  /**
   * CommentForm calls this method after the user submits text.
   * App performs the POST request because App owns the overall workflow.
   */
  async function handleCreateComment(text: string): Promise<boolean> {
    setError(null)

    try {
      await createComment(text)

      // Show the newly created comment by returning to all statuses and page 0.
      setStatusFilter('ALL')
      setPage(0)

      // Trigger a fresh GET so the screen matches PostgreSQL.
      setReloadNumber(current => current + 1)
      return true
    } catch {
      setError('Could not create comment.')
      return false
    }
  }

  /**
   * CommentCard calls this when Accept or Reject is clicked.
   * After PATCH succeeds, reload from PostgreSQL instead of guessing the new screen data.
   */
  async function handleStatusUpdate(id: string, status: ModerationStatus) {
    setUpdatingId(id)
    setError(null)

    try {
      await updateCommentStatus(id, status)
      setPage(0)
      setReloadNumber(current => current + 1)
    } catch {
      setError('Could not update comment.')
    } finally {
      setUpdatingId(null)
    }
  }

  // CommentQueue calls this when the dropdown changes. New filters start at page 0.
  function handleFilterChange(status: StatusFilter) {
    setStatusFilter(status)
    setPage(0)
  }

  return (
    <main className="dashboard">
      {/* Top section: application name and count returned by the backend. */}
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">TrustOps</p>
          <h1>Moderation dashboard</h1>
          <p>Review and manage community comments.</p>
        </div>

        <div className="summary">
          <strong>{commentPage?.totalElements ?? 0}</strong>
          <span>{statusFilter === 'ALL' ? 'Total comments' : 'Matching comments'}</span>
        </div>
      </header>

      {/* Child component receives a method instead of talking to the API directly. */}
      <CommentForm onCreate={handleCreateComment} />

      {/* App passes shared state down through props. Queue reports actions back through methods. */}
      <CommentQueue
        commentPage={commentPage}
        statusFilter={statusFilter}
        loading={loading}
        error={error}
        updatingId={updatingId}
        onFilterChange={handleFilterChange}
        onPageChange={setPage}
        onStatusUpdate={handleStatusUpdate}
      />
    </main>
  )
}

export default App
