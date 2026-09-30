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
}

type PageResponse<T> = { content: T[] }

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://127.0.0.1:8080/api',
  timeout: 5000,
  headers: { 'Content-Type': 'application/json' },
})

export const restaurantService = {
  async list(search = '', cuisine = ''): Promise<RestaurantApiResponse[]> {
    const response = await api.get<PageResponse<RestaurantApiResponse>>('/restaurants', { params: { search: search || undefined, cuisine: cuisine || undefined } })
    return response.data.content
  },
}

export default api
