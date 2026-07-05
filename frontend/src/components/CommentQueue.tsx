import type {
  CommentPage,
  ModerationStatus,
  StatusFilter,
} from '../types/comment'
import { CommentCard } from './CommentCard'

// CommentQueue needs shared state from App, but it never changes that state directly.
type CommentQueueProps = {
  // Current paginated backend result. null means the first request has not completed.
  commentPage: CommentPage | null
  statusFilter: StatusFilter
  loading: boolean
  error: string | null
  updatingId: string | null

  // These callback methods let the child report user actions back to App.
  onFilterChange: (status: StatusFilter) => void
  onPageChange: (page: number) => void
  onStatusUpdate: (id: string, status: ModerationStatus) => void
}

/**
 * CommentQueue responsibility: display filter, messages, cards, and page buttons.
 * App creates it and passes all backend data/methods through props.
 */
export function CommentQueue({
  commentPage,
  statusFilter,
  loading,
  error,
  updatingId,
  onFilterChange,
  onPageChange,
  onStatusUpdate,
}: CommentQueueProps) {
  // Before data exists, use safe defaults so JSX can still render.
  const comments = commentPage?.content ?? []
  const currentPage = commentPage?.number ?? 0

  return (
    <section className="queue">
      <div className="queue-heading">
        <h2>Comment queue</h2>

        {/* The hidden label gives the dropdown an accessible name without changing the design. */}
        <label>
          <span className="visually-hidden">Filter comments by status</span>
          {/* Browser values are general strings, so we tell TypeScript these options are StatusFilter. */}
          <select
            value={statusFilter}
            onChange={event =>
              onFilterChange(event.target.value as StatusFilter)
            }
          >
            <option value="ALL">All statuses</option>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </label>
      </div>

      {/* Only one of these messages should normally appear at a time. */}
      {loading && <p>Loading comments...</p>}
      {error && <p className="error" role="alert">{error}</p>}
      {!loading && !error && comments.length === 0 && <p>No comments found.</p>}

      <div className="comment-list">
        {/* Convert each Comment object into one CommentCard component. */}
        {comments.map(comment => (
          /* Only the card whose ID matches becomes disabled. */
          <CommentCard
            key={comment.id}
            comment={comment}
            updating={updatingId === comment.id}
            onStatusUpdate={onStatusUpdate}
          />
        ))}
      </div>

      {/* Backend page numbers start at 0; humans see currentPage + 1. */}
      <div className="pagination">
        <button
          type="button"
          disabled={commentPage?.first ?? true}
          onClick={() => onPageChange(currentPage - 1)}
        >
          Previous
        </button>

        <span>
          Page {currentPage + 1} of {Math.max(commentPage?.totalPages ?? 1, 1)}
        </span>

        <button
          type="button"
          disabled={commentPage?.last ?? true}
          onClick={() => onPageChange(currentPage + 1)}
        >
          Next
        </button>
      </div>
    </section>
  )
}
