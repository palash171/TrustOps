// Adds readable checks such as toBeInTheDocument() and toHaveValue().
import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'

// Remove the previous test's rendered React page so tests cannot affect each other.
afterEach(() => {
  cleanup()
})
