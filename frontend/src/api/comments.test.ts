import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { Comment, CommentPage } from '../types/comment'
import { createComment, getComments, updateCommentStatus } from './comments'

/**
 * These are API unit tests.
 * We replace the browser's real fetch with a mock so no Spring server is needed.
 * The tests check that our frontend builds the correct URLs, methods, and JSON bodies.
 */
const fetchMock = vi.fn()

// Reusable fake backend data keeps each test short and focused.
const comment: Comment = {
  id: 'comment-1',
  text: 'A test comment',
  status: 'PENDING',
  receivedAt: '2026-07-05T08:00:00Z',
}

const commentPage: CommentPage = {
  content: [comment],
  number: 2,
  totalPages: 3,
  totalElements: 41,
  first: false,
  last: true,
}

// Build a Response-like object whose json() method returns our chosen fake data.
function successfulResponse(body: unknown): Response {
  return {
    ok: true,
    json: vi.fn().mockResolvedValue(body),
  } as unknown as Response
}

// Before every test, give global fetch a fresh empty mock.
beforeEach(() => {
  fetchMock.mockReset()
  vi.stubGlobal('fetch', fetchMock)
})

// After every test, restore the real global environment.
afterEach(() => {
  vi.unstubAllGlobals()
})

describe('comments API', () => {
  it('requests a filtered page of comments', async () => {
    // Arrange: decide what the fake backend will return.
    fetchMock.mockResolvedValue(successfulResponse(commentPage))

    // Act: call the real frontend API function.
    await expect(getComments('APPROVED', 2)).resolves.toEqual(commentPage)

    // Assert: confirm the query string matches the Spring endpoint.
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/comments?page=2&size=20&status=APPROVED',
    )
  })

  it('sends new comment text as JSON', async () => {
    // Arrange.
    fetchMock.mockResolvedValue(successfulResponse(comment))

    // Act.
    await createComment('A test comment')

    // Assert that POST, header, and body are correct.
    expect(fetchMock).toHaveBeenCalledWith('/api/v1/comments', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ text: 'A test comment' }),
    })
  })

  it('sends the status update to the status endpoint', async () => {
    // Arrange an updated comment response.
    fetchMock.mockResolvedValue(
      successfulResponse({ ...comment, status: 'APPROVED' }),
    )

    // Act.
    await updateCommentStatus(comment.id, 'APPROVED')

    // Assert that PATCH uses /{id}/status and sends the enum word as JSON.
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/comments/comment-1/status',
      {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: 'APPROVED' }),
      },
    )
  })

  it('throws when the backend rejects a request', async () => {
    // A non-ok response simulates a backend 400/500 response.
    fetchMock.mockResolvedValue({ ok: false } as Response)

    // Our function must turn that response into an Error for App to display.
    await expect(getComments()).rejects.toThrow('Could not load comments')
  })
})
