const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

async function request(path, options = {}) {
  let response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
    })
  } catch {
    throw new Error('Could not reach QuickBite. Make sure the Spring Boot backend is running.')
  }

  const data = await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(data?.message || 'Something went wrong. Please try again.')
  }
  return data
}

export function getMenu() {
  return request('/menu')
}

export function createPaymentOrder(order) {
  return request('/payments/create-order', {
    method: 'POST',
    body: JSON.stringify(order),
  })
}

export function verifyPayment(payment) {
  return request('/payments/verify', {
    method: 'POST',
    body: JSON.stringify(payment),
  })
}
