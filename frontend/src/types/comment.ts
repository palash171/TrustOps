// These are the only status strings accepted by both Java and PostgreSQL.
export type ModerationStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'REJECTED'

// The dropdown also needs ALL, but ALL is not stored on a real Comment.
export type StatusFilter = ModerationStatus | 'ALL'

/**
 * Describes one Comment JSON object sent by Spring Boot.
 * UUID and Instant arrive through JSON as strings because JSON has no UUID or Instant type.
 * This type helps TypeScript check our code; it does not create an object at runtime.
 */
export type Comment = {
  id: string
  text: string
  status: ModerationStatus
  receivedAt: string
}

/**
 * Describes Spring's paginated response.
 * content stores this page's comments; the other fields describe page position and totals.
 */
export type CommentPage = {
  content: Comment[]
  number: number
  totalPages: number
  totalElements: number
  first: boolean
  last: boolean
}
