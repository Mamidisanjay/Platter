import './style.css'
import { restaurantService, type RestaurantApiResponse, type ReviewPage } from './services/api'

type Restaurant = { id: number; name: string; cuisine: string; rating: string; time: string; price: string; image: string; tag: string; accent: string; reviewCount: number }
type CartItem = { name: string; price: number; restaurant: string }

let restaurants: Restaurant[] = [
  { id: 1, name: 'Saffron Street', cuisine: 'North Indian, Biryani', rating: '4.8', time: '28 min', price: '$$', tag: 'Best seller', accent: '#ef6c45', image: 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?auto=format&fit=crop&w=900&q=85', reviewCount: 42 },
  { id: 2, name: 'Tokyo Table', cuisine: 'Japanese, Ramen', rating: '4.7', time: '34 min', price: '$$$', tag: 'Top rated', accent: '#5878c9', image: 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=900&q=85', reviewCount: 31 },
  { id: 3, name: 'The Green Room', cuisine: 'Healthy, Salads', rating: '4.6', time: '22 min', price: '$$', tag: 'Fresh pick', accent: '#6d9c70', image: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=900&q=85', reviewCount: 28 },
  { id: 4, name: 'Ember & Grain', cuisine: 'American, Grill', rating: '4.5', time: '41 min', price: '$$$', tag: 'New on Platter', accent: '#b88754', image: 'https://images.unsplash.com/photo-1550547660-d9450f859349?auto=format&fit=crop&w=900&q=85', reviewCount: 19 },
]

let activeTab = 'discover'
let activeFilter = 'All'
let searchTerm = ''
let cart: CartItem[] = []
let orderPlaced = false
let selectedRestaurant: Restaurant | null = null
let reviewPage: ReviewPage | null = null
let reviewLoading = false
let reviewError = ''

const app = document.querySelector<HTMLDivElement>('#app')!
const money = (value: number) => `$${value.toFixed(2)}`

function mapRestaurant(restaurant: RestaurantApiResponse): Restaurant {
  return {
    id: restaurant.id,
    name: restaurant.name,
    cuisine: restaurant.cuisine,
    rating: restaurant.rating.toFixed(1),
    time: `${restaurant.deliveryTimeMinutes} min`,
    price: restaurant.priceTier,
    tag: restaurant.tag || 'Popular nearby',
    accent: restaurant.accent || '#ef6c45',
    image: restaurant.imageUrl || '',
    reviewCount: restaurant.reviewCount || 0,
  }
}

function render() {
  const filtered = restaurants.filter((restaurant) => `${restaurant.name} ${restaurant.cuisine}`.toLowerCase().includes(searchTerm.toLowerCase()) && (activeFilter === 'All' || restaurant.cuisine.includes(activeFilter)))
  const total = cart.reduce((sum, item) => sum + item.price, 0)
  app.innerHTML = `<div class="shell"><aside class="sidebar"><div class="brand"><span class="brand-mark">p</span><span>platter</span></div><div class="location"><span class="pin">⌖</span><div><small>DELIVERING TO</small><strong>Indiranagar, Bengaluru</strong></div><span>⌄</span></div><nav class="nav-list"><button class="nav-item ${activeTab === 'discover' ? 'active' : ''}" data-tab="discover"><span>⌂</span> Discover</button><button class="nav-item ${activeTab === 'orders' ? 'active' : ''}" data-tab="orders"><span>◷</span> Your orders</button><button class="nav-item ${activeTab === 'favorites' ? 'active' : ''}" data-tab="favorites"><span>♡</span> Favorites</button></nav><div class="sidebar-bottom"><div class="status-dot"></div><div><strong>Everything is fresh</strong><small>We curate the good stuff.</small></div></div><button class="profile"><span class="avatar">AS</span><span><strong>Arjun Sharma</strong><small>Personal account</small></span><span>···</span></button></aside><main class="main-content"><header class="topbar"><div class="mobile-brand"><span class="brand-mark">p</span> platter</div><div class="search-wrap"><span>⌕</span><input id="search" placeholder="Search dishes, restaurants, cuisines" value="${searchTerm}" /></div><button class="icon-btn" title="Notifications">♧<i></i></button><button class="icon-btn admin-toggle" title="Switch to admin view">▦</button></header>${activeTab === 'discover' ? renderDiscover(filtered) : activeTab === 'orders' ? renderOrders() : renderFavorites()}</main>${renderCart(total)}</div>`
  if (selectedRestaurant) app.insertAdjacentHTML('beforeend', renderReviewPanel())
  bindEvents()
}

function renderDiscover(filtered: Restaurant[]) {
  const filters = ['All', 'Indian', 'Japanese', 'Healthy', 'American']
  return `<section class="content-wrap"><div class="eyebrow">GOOD FOOD, GOOD MOOD <span>✦</span></div><div class="intro"><div><h1>Find your<br><em>next favorite.</em></h1><p>Thoughtfully curated places, made for your everyday.</p></div><div class="weather"><span>☼</span><div><strong>24°</strong><small>Perfect for a delivery</small></div></div></div><div class="filter-row">${filters.map((filter) => `<button class="filter ${activeFilter === filter ? 'selected' : ''}" data-filter="${filter}">${filter}</button>`).join('')}</div><div class="section-heading"><div><h2>Popular near you</h2><p>Based on what people are loving today</p></div><button class="view-all">View all <span>→</span></button></div><div class="restaurant-grid">${filtered.length ? filtered.map(renderRestaurant).join('') : '<div class="empty-state">No places match that search. Try another craving.</div>'}</div><div class="small-note"><span>✦</span> New places, every week. <button>Explore all collections →</button></div></section>`
}

function renderRestaurant(restaurant: Restaurant) {
  return `<article class="restaurant-card" style="--card-accent:${restaurant.accent}"><div class="image-wrap"><img src="${restaurant.image}" alt="${restaurant.name}"/><span class="tag">${restaurant.tag}</span><button class="heart" data-favorite="${restaurant.id}">♡</button></div><div class="card-body"><div class="card-title"><h3>${restaurant.name}</h3><button class="rating review-link" data-review="${restaurant.id}">★ ${restaurant.rating} · ${restaurant.reviewCount}</button></div><p>${restaurant.cuisine}</p><div class="card-meta"><span>◷ ${restaurant.time}</span><span>${restaurant.price}</span><button class="quick-add" data-add="${restaurant.id}">+ Add</button></div></div></article>`
}

function renderReviewPanel() {
  if (!selectedRestaurant) return ''
  const reviews = reviewPage?.content || []
  return `<div class="review-overlay"><section class="review-panel"><button class="review-close" data-close-reviews>×</button><div class="eyebrow">COMMUNITY NOTES <span>✦</span></div><h2>${selectedRestaurant.name}</h2><p class="review-summary"><strong>★ ${reviewPage?.averageRating?.toFixed(1) || selectedRestaurant.rating}</strong> · ${reviewPage?.reviewCount ?? selectedRestaurant.reviewCount} reviews</p>${reviewLoading ? '<div class="review-state">Loading reviews...</div>' : reviewError ? `<div class="review-state error">${reviewError}</div>` : `<div class="review-list">${reviews.length ? reviews.map((review) => `<article class="review-item"><div class="review-item-top"><strong>${review.userName}</strong><span>${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}</span></div><p>${review.content}</p><small>${new Date(review.createdAt).toLocaleDateString()}</small>${review.ownReview ? `<div class="review-actions"><button data-edit-review="${review.id}">Edit</button><button data-delete-review="${review.id}">Delete</button></div>` : ''}</article>`).join('') : '<div class="review-state">No reviews yet. Be the first to share a note.</div>'}</div>`}<form class="review-form" data-review-form><label>Your rating <span id="rating-value">5</span>/5</label><input name="rating" type="range" min="1" max="5" value="5" /><textarea name="content" maxlength="1000" placeholder="What did you think?" required></textarea><button class="primary-btn" type="submit">Share your review <span>→</span></button></form></section></div>`
}

function renderOrders() {
  return `<section class="content-wrap orders-view"><div class="eyebrow">YOUR TABLE <span>✦</span></div><h1 class="page-title">Orders & <em>rituals.</em></h1><div class="order-card active-order"><div class="order-top"><div><span class="live-pill"><i></i> LIVE ORDER</span><h2>${orderPlaced ? 'Saffron Street' : 'Nothing cooking yet'}</h2><p>${orderPlaced ? '2 items · Arriving in 18–24 min' : 'Your next meal is a few taps away.'}</p></div><span class="order-total">${orderPlaced ? '$24.50' : '—'}</span></div>${orderPlaced ? '<div class="progress"><div class="progress-line"><i></i><i></i><i class="muted"></i><i class="muted"></i></div><div class="progress-labels"><span>Confirmed</span><span>Preparing</span><span>On the way</span><span>Delivered</span></div></div>' : '<button class="primary-btn" data-tab="discover">Browse restaurants <span>→</span></button>'}</div><h2 class="subheading">Past orders</h2><div class="past-order"><div class="past-icon">ST</div><div><strong>Saffron Street</strong><p>Chicken biryani, Garlic naan · Aug 28</p></div><span class="past-price">$31.20</span><button class="reorder">Reorder</button></div></section>`
}

function renderFavorites() {
  return `<section class="content-wrap empty-page"><div class="eyebrow">YOUR COLLECTION <span>✦</span></div><h1 class="page-title">Saved for <em>later.</em></h1><div class="collection-placeholder"><span>♡</span><h2>Your favorite places belong here.</h2><p>Tap the heart on any restaurant to make it easy to find again.</p><button class="primary-btn" data-tab="discover">Find something good <span>→</span></button></div></section>`
}

function renderCart(total: number) {
  return `<aside class="cart-panel"><div class="cart-header"><div><span class="eyebrow">YOUR BAG</span><h2>${cart.length ? `${cart.length} ${cart.length === 1 ? 'item' : 'items'}` : 'Ready when you are'}</h2></div><span class="bag-icon">♧</span></div>${cart.length ? `<div class="cart-items">${cart.map((item, index) => `<div class="cart-item"><div class="item-thumb">${item.name.slice(0, 2).toUpperCase()}</div><div><strong>${item.name}</strong><small>${item.restaurant}</small></div><span>${money(item.price)}</span><button data-remove="${index}">×</button></div>`).join('')}</div><div class="cart-total"><span>Subtotal</span><strong>${money(total)}</strong></div><button class="checkout-btn" data-checkout>Checkout <span>→</span></button><small class="secure-note">⌁ Secure checkout · Cashless & simple</small>` : '<div class="cart-empty"><div class="bowl">⌒</div><p>Add something delicious<br>and it will show up here.</p></div><div class="cart-perks"><span>⚡ Fast delivery</span><span>♡ Curated picks</span></div>'}</aside>`
}

async function openReviews(restaurantId: number) {
  selectedRestaurant = restaurants.find((restaurant) => restaurant.id === restaurantId) || null
  reviewPage = null
  reviewError = ''
  reviewLoading = true
  render()
  try { reviewPage = await restaurantService.reviews(restaurantId) } catch { reviewError = 'Reviews are unavailable right now.' }
  reviewLoading = false
  render()
}

function bindEvents() {
  document.querySelector<HTMLInputElement>('#search')?.addEventListener('input', (event) => { searchTerm = (event.target as HTMLInputElement).value; render() })
  document.querySelectorAll<HTMLElement>('[data-tab]').forEach((element) => element.addEventListener('click', () => { activeTab = element.dataset.tab || 'discover'; render() }))
  document.querySelectorAll<HTMLElement>('[data-filter]').forEach((element) => element.addEventListener('click', () => { activeFilter = element.dataset.filter || 'All'; render() }))
  document.querySelectorAll<HTMLElement>('[data-add]').forEach((element) => element.addEventListener('click', () => { const restaurant = restaurants.find((item) => item.id === Number(element.dataset.add)); if (restaurant) { cart.push({ name: restaurant.id === 1 ? 'Butter chicken bowl' : `${restaurant.name} special`, price: restaurant.id === 1 ? 18.5 : 16, restaurant: restaurant.name }); render() } }))
  document.querySelectorAll<HTMLElement>('[data-remove]').forEach((element) => element.addEventListener('click', () => { cart.splice(Number(element.dataset.remove), 1); render() }))
  document.querySelector<HTMLElement>('[data-checkout]')?.addEventListener('click', () => { orderPlaced = true; activeTab = 'orders'; cart = []; render() })
  document.querySelector<HTMLElement>('.admin-toggle')?.addEventListener('click', showAdmin)
  document.querySelectorAll<HTMLElement>('[data-review]').forEach((element) => element.addEventListener('click', () => { void openReviews(Number(element.dataset.review)) }))
  document.querySelector<HTMLElement>('[data-close-reviews]')?.addEventListener('click', () => { selectedRestaurant = null; reviewPage = null; render() })
  const ratingInput = document.querySelector<HTMLInputElement>('.review-form input[name="rating"]')
  ratingInput?.addEventListener('input', () => { const value = document.querySelector<HTMLElement>('#rating-value'); if (value) value.textContent = ratingInput.value })
  document.querySelector<HTMLFormElement>('[data-review-form]')?.addEventListener('submit', async (event) => {
    event.preventDefault()
    if (!selectedRestaurant) return
    const form = new FormData(event.currentTarget as HTMLFormElement)
    reviewLoading = true
    reviewError = ''
    render()
    try { await restaurantService.createReview(selectedRestaurant.id, Number(form.get('rating')), String(form.get('content'))); reviewPage = await restaurantService.reviews(selectedRestaurant.id) } catch { reviewError = 'Sign in and complete an order before sharing a review.' }
    reviewLoading = false
    render()
  })
  document.querySelectorAll<HTMLElement>('[data-delete-review]').forEach((element) => element.addEventListener('click', async () => {
    try { await restaurantService.deleteReview(Number(element.dataset.deleteReview)); if (selectedRestaurant) reviewPage = await restaurantService.reviews(selectedRestaurant.id); render() } catch { reviewError = 'Unable to delete this review.'; render() }
  }))
  document.querySelectorAll<HTMLElement>('[data-edit-review]').forEach((element) => element.addEventListener('click', async () => {
    const review = reviewPage?.content.find((item) => item.id === Number(element.dataset.editReview))
    if (!review) return
    const content = window.prompt('Update your review', review.content)
    const rating = window.prompt('Rating from 1 to 5', String(review.rating))
    if (!content || !rating || !selectedRestaurant) return
    try { await restaurantService.updateReview(review.id, Number(rating), content); reviewPage = await restaurantService.reviews(selectedRestaurant.id); render() } catch { reviewError = 'Unable to update this review.'; render() }
  }))
}

function showAdmin() {
  app.innerHTML = `<div class="admin-shell"><header class="admin-top"><div class="brand"><span class="brand-mark">p</span><span>platter <small>OPS</small></span></div><div class="admin-user"><span class="status-dot"></span> All systems operational <span class="avatar">AS</span></div></header><main class="admin-main"><div class="admin-heading"><div><span class="eyebrow">OPERATIONS CENTER <span>✦</span></span><h1>Good morning, <em>Arjun.</em></h1><p>Here is what is happening across Platter today.</p></div><button class="back-btn" id="back-to-app">← Back to app</button></div><div class="stat-grid"><div class="stat-card"><span>ORDERS TODAY</span><strong>1,284</strong><small class="up">↗ 12.8% <i>vs yesterday</i></small></div><div class="stat-card"><span>GROSS SALES</span><strong>$38,420</strong><small class="up">↗ 8.4% <i>vs yesterday</i></small></div><div class="stat-card"><span>AVG. DELIVERY</span><strong>31 <small>min</small></strong><small class="down">↘ 4 min <i>faster this week</i></small></div><div class="stat-card"><span>ACTIVE PROMOS</span><strong>06</strong><small class="neutral">● 2 ending today</small></div></div><div class="admin-columns"><section class="panel"><div class="panel-heading"><div><h2>Live order flow</h2><p>Real-time order activity</p></div><span class="live-pill"><i></i> LIVE</span></div><div class="flow-chart"><div class="chart-labels"><span>1.2k</span><span>900</span><span>600</span><span>300</span><span>0</span></div><div class="chart-lines"></div><svg viewBox="0 0 700 180" preserveAspectRatio="none"><path d="M0,145 C40,135 60,146 95,118 S145,125 180,105 S225,110 260,82 S310,92 345,95 S385,56 430,72 S470,88 505,50 S540,72 580,38 S630,52 700,18"/><path class="area" d="M0,145 C40,135 60,146 95,118 S145,125 180,105 S225,110 260,82 S310,92 345,95 S385,56 430,72 S470,88 505,50 S540,72 580,38 S630,52 700,18 L700,180 L0,180 Z"/></svg></div><div class="chart-days"><span>10 AM</span><span>12 PM</span><span>2 PM</span><span>4 PM</span><span>6 PM</span><span>8 PM</span></div></section><section class="panel promo-panel"><div class="panel-heading"><div><h2>Promo health</h2><p>Campaign performance</p></div><button class="dots">···</button></div><div class="promo-row"><span class="promo-code">FIRSTBITE</span><div><strong>72% redeemed</strong><small>1,240 uses · 6 days left</small></div><span class="promo-dot green"></span></div><div class="promo-row"><span class="promo-code orange">WEEKEND20</span><div><strong>48% redeemed</strong><small>842 uses · 2 days left</small></div><span class="promo-dot yellow"></span></div><div class="promo-row"><span class="promo-code blue">WELCOME</span><div><strong>31% redeemed</strong><small>420 uses · 12 days left</small></div><span class="promo-dot blue-dot"></span></div><button class="manage-btn">Manage promotions <span>→</span></button></section></div></main></div>`
  document.querySelector('#back-to-app')?.addEventListener('click', render)
}

async function initialize() {
  try {
    const remoteRestaurants = await restaurantService.list()
    if (remoteRestaurants.length) restaurants = remoteRestaurants.map(mapRestaurant)
  } catch {
    console.warn('Backend unavailable; retaining the local preview data.')
  }
  render()
}

void initialize()
