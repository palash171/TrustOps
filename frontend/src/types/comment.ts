export type ModerationStatus =
    |'PENDING'
    | 'APPROVED'
    | 'REJECTED';

export type Comment = {
    id: string;
    text: string;
    status: ModerationStatus;
    receivedAt: string
}
export type CommentPage = {
    content: Comment[]
    number: number
    totalPages: number
    totalElements: number
    first: boolean
    last: boolean
}