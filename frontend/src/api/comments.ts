import type{
    CommentPage,
    ModerationStatus
} from '../types/comment'

export async function getComments( //fetches comments from the backend
    status?: ModerationStatus, // optional argument, if given must be a valid status
    page = 0, //default page 0
):
    //creates page=0&size=20
    Promise<CommentPage> {
    const parameters = new URLSearchParams({ //builds the query string part of a URL.
        page: String(page),
        size: '20',
    })

    if (status) {
        parameters.set('status', status)
    }

    const response = await fetch( //fetch sends an HTTP request to backend wait pauses until the response comes back.
        `/api/v1/comments?${parameters}`,
    )

    if (!response.ok) {
        throw new Error('Could not load comments')
    }

    return response.json() //reads the response body as JSON.
}