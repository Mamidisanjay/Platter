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
export type AdminDashboard = { ordersToday: number; grossSales: number; averageDeliveryTimeMinutes: number; activePromotions: number; orderFlow: Record<string, number>; promoPerformance: unknown[] }
export type AdminUser = { id: number; name: string; email: string; role: string }
export type AdminPayment = { id: number; orderId: number; amount: number; status: string; stripePaymentIntentId: string }
export type AuditLog = { id: number; userId: number | null; action: string; entityType: string; entityId: number | null; timestamp: string; ipAddress: string | null; metadata: string | null }
export type Delivery = { id: number; orderId: number; deliveryPartnerId: number; deliveryPartnerName: string; status: string; pickupTime: string; pickedUpAt: string | null; deliveredAt: string | null; deliveryAddress: string; latitude: number | null; longitude: number | null; createdAt: string; updatedAt: string }

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

export const adminService = {
  dashboard: async () => (await api.get<AdminDashboard>('/admin/dashboard')).data,
  users: async () => (await api.get<AdminUser[]>('/admin/users')).data,
  restaurants: async () => (await api.get<RestaurantApiResponse[]>('/admin/restaurants')).data,
  orders: async () => (await api.get<unknown[]>('/admin/orders')).data,
  payments: async () => (await api.get<AdminPayment[]>('/admin/payments')).data,
  audits: async () => (await api.get<AuditLog[]>('/admin/audit-logs')).data,
  reviews: async () => (await api.get<Review[]>('/admin/reviews')).data,
  deliveries: async () => (await api.get<Delivery[]>('/admin/deliveries')).data,
}

export const deliveryService = {
  list: async () => (await api.get<Delivery[]>('/delivery/orders')).data,
  get: async (id: number) => (await api.get<Delivery>(`/delivery/orders/${id}`)).data,
  accept: async (id: number) => (await api.post<Delivery>(`/delivery/orders/${id}/accept`)).data,
  updateStatus: async (id: number, status: string) => (await api.patch<Delivery>(`/delivery/orders/${id}/status`, { status })).data,
  updateLocation: async (id: number, latitude: number, longitude: number) => (await api.patch<Delivery>(`/delivery/orders/${id}/location`, { latitude, longitude })).data,
}

export default api
