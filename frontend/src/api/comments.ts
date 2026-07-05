import type {
    Comment,
    CommentPage,
    ModerationStatus,
} from '../types/comment'

/**
 * API file responsibility: convert frontend actions into HTTP requests.
 * Components do not need to know URLs, headers, or how JSON bodies are created.
 *
 * Fetches one page of comments from Spring Boot.
 *
 * @param status Optional moderation status used to filter comments.
 * @param page Page number to request. Defaults to the first page.
 * @returns A promise containing the requested comment page.
 * @throws Error when the backend request fails.
 */
export async function getComments(status?: ModerationStatus, page = 0): Promise<CommentPage> {
    // Build the page=0&size=20 part of the URL safely.
    const parameters = new URLSearchParams({ page: String(page), size: '20' })

    // Only add status when a specific filter was selected.
    if (status) parameters.set('status', status)

    // fetch sends GET by default. await pauses only this function until the response arrives.
    const response = await fetch(`/api/v1/comments?${parameters}`)

    // HTTP 400/500 responses do not automatically throw, so we check them ourselves.
    if (!response.ok) {
        throw new Error('Could not load comments')
    }

    // Convert the response's JSON text into a JavaScript object.
    return response.json()
}

/**
 * Sends a PATCH request to change one existing comment's status.
 * The URL must match @PatchMapping("/{id}/status") in CommentWebApi.
 */
export async function updateCommentStatus(id: string, status: ModerationStatus): Promise<Comment> {
    const response = await fetch(
        `/api/v1/comments/${id}/status`,
        {
            // PATCH means update part of an existing item.
            method: 'PATCH',
            headers: {
                // Tell Spring that the body contains JSON rather than plain text.
                'Content-Type': 'application/json',
            },
            // Convert { status: ... } into JSON text for @RequestBody.
            body: JSON.stringify({ status }),
        },
    )

    if (!response.ok) {
        throw new Error('Could not update comment')
    }

    return response.json()
}

/**
 * Sends a POST request to create a new comment.
 * Spring gives new comments their UUID, PENDING status, and received time.
 */
export async function createComment(text: string): Promise<Comment> {
    const response = await fetch('/api/v1/comments', {
        // POST means create a new item.
        method: 'POST',

        headers: {
            'Content-Type': 'application/json',
        },

        // This becomes the CreateCommentRequest object in the Java controller.
        body: JSON.stringify({ text }),
    })

    if (!response.ok) {
        throw new Error('Could not create comment')
    }

    return response.json()
}
