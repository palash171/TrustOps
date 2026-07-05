import type { Comment, ModerationStatus } from '../types/comment'

// App -> CommentQueue -> CommentCard passes these values down as props.
type CommentCardProps = {
  // The comment this one card must display.
  comment: Comment

  // true while this exact comment is waiting for a PATCH response.
  updating: boolean

  // Method supplied by App so the card can report an Accept/Reject click upward.
  onStatusUpdate: (id: string, status: ModerationStatus) => void
}

/**
 * CommentCard responsibility: display one comment and its allowed actions.
 * It does not fetch or store data itself.
 */
export function CommentCard({
  comment,
  updating,
  onStatusUpdate,
}: CommentCardProps) {
  return (
    <article className="comment-card">
      {/* Top row: moderation status on the left and received time on the right. */}
      <div className="comment-details">
        {/* The status word also becomes a CSS class: PENDING -> pending. */}
        <span className={`status ${comment.status.toLowerCase()}`}>
          {comment.status}
        </span>

        {/* dateTime stores the machine-readable value; the inside shows a friendly local value. */}
        <time dateTime={comment.receivedAt}>
          {new Date(comment.receivedAt).toLocaleString()}
        </time>
      </div>

      {/* This is the actual community content. React safely displays it as text. */}
      <p>{comment.text}</p>

      {/* Do not show a button that would set the status it already has. */}
      <div className="actions">
        {comment.status !== 'APPROVED' && (
          /* Report the ID and requested Java enum value back to App. */
          <button
            className="approve-button"
            type="button"
            disabled={updating}
            onClick={() => onStatusUpdate(comment.id, 'APPROVED')}
          >
            Accept
          </button>
        )}

        {comment.status !== 'REJECTED' && (
          <button
            className="reject-button"
            type="button"
            disabled={updating}
            onClick={() => onStatusUpdate(comment.id, 'REJECTED')}
          >
            Reject
          </button>
        )}
      </div>
    </article>
  )
}
