import axios from 'axios'

export type RestaurantApiResponse = {
  id: number
  name: string
  cuisine: string
  rating: number
  deliveryTimeMinutes: number
  priceTier: string
  imageUrl: string | null
  tag: string | null
  accent: string | null
  available: boolean
  reviewCount: number
}

export type Review = { id: number; userId: number; userName: string; rating: number; content: string; createdAt: string; updatedAt: string; ownReview: boolean }
export type ReviewPage = { content: Review[]; page: number; size: number; totalElements: number; totalPages: number; averageRating: number; reviewCount: number }

type PageResponse<T> = { content: T[] }

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://127.0.0.1:8080/api',
  timeout: 5000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('platter_access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export const restaurantService = {
  async list(search = '', cuisine = ''): Promise<RestaurantApiResponse[]> {
    const response = await api.get<PageResponse<RestaurantApiResponse>>('/restaurants', { params: { search: search || undefined, cuisine: cuisine || undefined } })
    return response.data.content
  },
  async reviews(id: number, page = 0): Promise<ReviewPage> {
    const response = await api.get<ReviewPage>(`/restaurants/${id}/reviews`, { params: { page, size: 10, sort: 'createdAt' } })
    return response.data
  },
  async createReview(id: number, rating: number, content: string): Promise<Review> {
    const response = await api.post<Review>(`/restaurants/${id}/reviews`, { rating, content })
    return response.data
  },
  async updateReview(id: number, rating: number, content: string): Promise<Review> {
    const response = await api.put<Review>(`/reviews/${id}`, { rating, content })
    return response.data
  },
  async deleteReview(id: number): Promise<void> {
    await api.delete(`/reviews/${id}`)
  },
}

export default api
