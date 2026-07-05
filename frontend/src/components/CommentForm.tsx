import { useState, type FormEvent } from 'react'

// Props describe the data/methods that App must give this component.
type CommentFormProps = {
  // The Promise<boolean> tells the form later whether creation succeeded.
  onCreate: (text: string) => Promise<boolean>
}

/**
 * CommentForm responsibility: collect text from the user.
 * It owns temporary form state, but App still owns the actual create workflow.
 * App creates this component and passes handleCreateComment into onCreate.
 */
export function CommentForm({ onCreate }: CommentFormProps) {
  // Stores exactly what is currently displayed inside the textarea.
  const [text, setText] = useState('')

  // Used to disable the button while the POST request is waiting.
  const [submitting, setSubmitting] = useState(false)

  // React calls this method when the form is submitted.
  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    // Stop the browser's normal form behaviour, which would refresh the entire page.
    event.preventDefault()

    // Remove spaces from the start/end so a comment cannot contain only spaces.
    const cleanedText = text.trim()
    if (!cleanedText) return

    setSubmitting(true)

    try {
      // Report the cleaned text upward to App and wait for its API result.
      const created = await onCreate(cleanedText)

      // Only clear what the user typed when PostgreSQL actually accepted it.
      if (created) setText('')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="create-panel">
      <div>
        <h2>Test comment intake</h2>
        <p>Simulate a community member submitting a comment.</p>
      </div>

      <form onSubmit={handleSubmit}>
        {/* htmlFor connects this label to the textarea for users and screen readers. */}
        <label className="field-label" htmlFor="comment-text">
          Comment text
        </label>

        {/* value + onChange make this a controlled input owned by React state. */}
        <textarea
          id="comment-text"
          value={text}
          onChange={event => setText(event.target.value)}
          placeholder="Write a comment..."
          maxLength={5000}
          required
        />

        {/* Prevent duplicate requests and prevent blank comments. */}
        <button
          className="submit-button"
          type="submit"
          disabled={submitting || !text.trim()}
        >
          {submitting ? 'Submitting...' : 'Submit comment'}
        </button>
      </form>
    </section>
  )
}
