import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  createComment,
  getComments,
  updateCommentStatus,
} from './api/comments'
import App from './App'
import type { Comment, CommentPage } from './types/comment'

/**
 * These are frontend behaviour tests.
 * React Testing Library renders App into jsdom and userEvent types/clicks like a user.
 * The API module is mocked because these tests focus on the React workflow, not PostgreSQL.
 */
vi.mock('./api/comments', () => ({
  createComment: vi.fn(),
  getComments: vi.fn(),
  updateCommentStatus: vi.fn(),
}))

// Keep strongly typed references to the three mocked API methods.
const getCommentsMock = vi.mocked(getComments)
const createCommentMock = vi.mocked(createComment)
const updateCommentStatusMock = vi.mocked(updateCommentStatus)

// Reusable fake comment returned by our mocked API.
const pendingComment: Comment = {
  id: 'comment-1',
  text: 'Please review this comment',
  status: 'PENDING',
  receivedAt: '2026-07-05T08:00:00Z',
}

// Helper builds the same page shape that Spring Boot normally returns as JSON.
function makePage(content: Comment[] = [pendingComment]): CommentPage {
  return {
    content,
    number: 0,
    totalPages: 1,
    totalElements: content.length,
    first: true,
    last: true,
  }
}

// Reset every mock before each test so one test cannot change another test's result.
beforeEach(() => {
  getCommentsMock.mockReset()
  createCommentMock.mockReset()
  updateCommentStatusMock.mockReset()

  getCommentsMock.mockResolvedValue(makePage())
  createCommentMock.mockResolvedValue(pendingComment)
  updateCommentStatusMock.mockResolvedValue({
    ...pendingComment,
    status: 'APPROVED',
  })
})

describe('moderation dashboard', () => {
  it('loads and displays comments', async () => {
    // Render calls App, which runs its loading useEffect.
    render(<App />)

    // First loading is visible, then the mocked GET comment appears.
    expect(screen.getByText('Loading comments...')).toBeInTheDocument()
    expect(await screen.findByText(pendingComment.text)).toBeInTheDocument()
    expect(getCommentsMock).toHaveBeenCalledWith(undefined, 0)
  })

  it('submits trimmed comment text and reloads the queue', async () => {
    // userEvent provides realistic typing and clicking methods.
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText(pendingComment.text)

    // Type spaces around the text so we can prove App trims them.
    const textArea = screen.getByLabelText('Comment text')
    await user.type(textArea, '  New community comment  ')
    await user.click(screen.getByRole('button', { name: 'Submit comment' }))

    // Wait for asynchronous POST and follow-up GET work to finish.
    await waitFor(() => {
      expect(createCommentMock).toHaveBeenCalledWith('New community comment')
      expect(getCommentsMock).toHaveBeenCalledTimes(2)
    })
    expect(textArea).toHaveValue('')
  })

  it('requests comments again when the status filter changes', async () => {
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText(pendingComment.text)

    // Selecting APPROVED should cause useEffect to perform a new filtered GET.
    await user.selectOptions(
      screen.getByRole('combobox', { name: 'Filter comments by status' }),
      'APPROVED',
    )

    await waitFor(() => {
      expect(getCommentsMock).toHaveBeenLastCalledWith('APPROVED', 0)
    })
  })

  it('approves a pending comment and reloads the queue', async () => {
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText(pendingComment.text)

    // Accept reports APPROVED to App, which sends PATCH and then performs another GET.
    await user.click(screen.getByRole('button', { name: 'Accept' }))

    await waitFor(() => {
      expect(updateCommentStatusMock).toHaveBeenCalledWith(
        pendingComment.id,
        'APPROVED',
      )
      expect(getCommentsMock).toHaveBeenCalledTimes(2)
    })
  })
})
