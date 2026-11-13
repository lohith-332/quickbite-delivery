import { useEffect, useMemo, useState } from 'react'
import { createPaymentOrder, getMenu, verifyPayment } from './api.js'

let razorpayScriptPromise

function loadRazorpayCheckout() {
  if (window.Razorpay) return Promise.resolve()
  if (!razorpayScriptPromise) {
    razorpayScriptPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.src = 'https://checkout.razorpay.com/v1/checkout.js'
      script.async = true
      script.onload = resolve
      script.onerror = () => {
        razorpayScriptPromise = null
        reject(new Error('Could not load Razorpay Checkout. Check your internet connection and try again.'))
      }
      document.body.appendChild(script)
    })
  }
  return razorpayScriptPromise
}

const currency = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
})

function formatPrice(value) {
  return currency.format(Number(value))
}

function App() {
  const [menu, setMenu] = useState([])
  const [loading, setLoading] = useState(true)
  const [menuError, setMenuError] = useState('')
  const [activeCategory, setActiveCategory] = useState('All')
  const [cart, setCart] = useState({})
  const [customer, setCustomer] = useState({ fullName: '', email: '', deliveryAddress: '' })
  const [placingOrder, setPlacingOrder] = useState(false)
  const [checkoutError, setCheckoutError] = useState('')
  const [lastOrder, setLastOrder] = useState(null)

  useEffect(() => {
    getMenu()
      .then(setMenu)
      .catch((error) => setMenuError(error.message))
      .finally(() => setLoading(false))
  }, [])

  const categories = useMemo(() => ['All', ...new Set(menu.map((item) => item.category))], [menu])
  const visibleMenu = activeCategory === 'All'
    ? menu
    : menu.filter((item) => item.category === activeCategory)
  const cartItems = Object.values(cart)
  const itemCount = cartItems.reduce((sum, line) => sum + line.quantity, 0)
  const total = cartItems.reduce((sum, line) => sum + Number(line.item.price) * line.quantity, 0)

  function changeQuantity(item, amount) {
    setCheckoutError('')
    setLastOrder(null)
    setCart((current) => {
      const next = { ...current }
      const quantity = (next[item.id]?.quantity || 0) + amount
      if (quantity <= 0) delete next[item.id]
      else next[item.id] = { item, quantity }
      return next
    })
  }

  function updateCustomer(event) {
    setCustomer((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  async function handleCheckout(event) {
    event.preventDefault()
    setPlacingOrder(true)
    setCheckoutError('')
    try {
      await loadRazorpayCheckout()
      const paymentOrder = await createPaymentOrder({
        ...customer,
        items: cartItems.map(({ item, quantity }) => ({ itemId: item.id, quantity })),
      })

      let paymentCallbackReceived = false
      const checkout = new window.Razorpay({
        key: paymentOrder.keyId,
        amount: paymentOrder.amount,
        currency: paymentOrder.currency,
        name: 'QuickBite',
        description: `Food order #${paymentOrder.orderId}`,
        order_id: paymentOrder.razorpayOrderId,
        prefill: { name: customer.fullName, email: customer.email },
        notes: { quickbite_order_id: String(paymentOrder.orderId) },
        theme: { color: '#f17a3b' },
        handler: async (response) => {
          paymentCallbackReceived = true
          setPlacingOrder(true)
          setCheckoutError('')
          try {
            const order = await verifyPayment({
              orderId: paymentOrder.orderId,
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            })
            setLastOrder(order)
            setCart({})
          } catch (error) {
            setCheckoutError(error.message)
          } finally {
            setPlacingOrder(false)
          }
        },
        modal: {
          ondismiss: () => {
            if (!paymentCallbackReceived) {
              setCheckoutError('Checkout was closed before payment was confirmed. Your bag is still here; you can try again.')
            }
          },
        },
      })
      checkout.on('payment.failed', (event) => {
        setCheckoutError(event?.error?.description || 'Payment did not complete. Your bag is still here; please try again.')
      })
      checkout.open()
    } catch (error) {
      setCheckoutError(error.message)
    } finally {
      setPlacingOrder(false)
    }
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="QuickBite home">
          <span className="brand-mark">q<span>.</span></span>
          <span>quickbite</span>
        </a>
        <nav className="main-nav" aria-label="Main navigation">
          <a className="nav-active" href="#menu">Menu</a>
          <a href="#how-it-works">How it works</a>
        </nav>
        <a className="cart-shortcut" href="#your-order" aria-label={`Your bag, ${itemCount} items`}>
          <span className="bag-icon">♧</span>
          <span className="bag-label">Your bag</span>
          <span className="bag-count">{itemCount}</span>
        </a>
      </header>

      <main id="top">
        <section className="hero">
          <div className="hero-copy">
            <div className="eyebrow"><span className="eyebrow-dot" /> YOUR NEIGHBOURHOOD, DELIVERED</div>
            <h1>Good food.<br /><span>Good mood.</span></h1>
            <p>Your favourite local eats, made fresh and brought right to your door.</p>
            <a className="primary-button hero-button" href="#menu">Explore the menu <span>↘</span></a>
            <div className="hero-note"><span className="note-avatars">☺ ☻ ☺</span><span><strong>Loved around the corner</strong><br />Fresh picks, every single day</span></div>
          </div>
          <div className="hero-art" aria-label="Freshly baked pizza with basil">
            <div className="hero-orbit orbit-one" />
            <div className="hero-orbit orbit-two" />
            <div className="hero-label"><span>✦</span> made with care</div>
            <img src="https://images.unsplash.com/photo-1579751626657-72bc17010498?auto=format&fit=crop&w=1100&q=90" alt="Freshly baked margherita pizza" />
            <div className="hero-sticker"><span>✳</span><strong>Fresh<br />& fast</strong></div>
          </div>
          <div className="hero-bottom-line"><span>01 / 03</span><span className="line" /><span>GOOD THINGS ARE COOKING</span></div>
        </section>

        <section className="promise-strip" id="how-it-works">
          <div className="promise"><span className="promise-icon">⌁</span><span><strong>Freshly made</strong><small>Prepared when you order</small></span></div>
          <div className="promise"><span className="promise-icon">◷</span><span><strong>At your door in 30 min</strong><small>Quick delivery, no fuss</small></span></div>
          <div className="promise"><span className="promise-icon">♡</span><span><strong>Good food, always</strong><small>Local favourites you’ll love</small></span></div>
        </section>

        <section className="menu-layout" id="menu">
          <div className="menu-main">
            <div className="section-heading">
              <div>
                <div className="eyebrow muted-eyebrow">THE GOOD STUFF</div>
                <h2>What sounds good?</h2>
                <p>Made fresh, picked just for you.</p>
              </div>
              <div className="menu-count"><span>{menu.length.toString().padStart(2, '0')}</span> delicious picks</div>
            </div>

            <div className="category-list" aria-label="Filter menu by category">
              {categories.map((category) => (
                <button key={category} className={activeCategory === category ? 'category-chip selected' : 'category-chip'} onClick={() => setActiveCategory(category)}>
                  {category === 'All' ? <span className="chip-sparkle">✳</span> : null}{category}
                </button>
              ))}
            </div>

            {loading && <div className="state-card"><span className="loader" />Fetching the menu…</div>}
            {!loading && menuError && <div className="state-card error-state"><span>!</span><div><strong>We couldn’t load the menu.</strong><p>{menuError}</p><small>Start the database and Spring Boot API, then refresh this page.</small></div></div>}
            {!loading && !menuError && visibleMenu.length === 0 && <div className="state-card">No dishes in this category yet.</div>}

            {!loading && !menuError && (
              <div className="food-grid">
                {visibleMenu.map((item, index) => {
                  const quantity = cart[item.id]?.quantity || 0
                  return (
                    <article className="food-card" key={item.id}>
                      <div className="food-image-wrap">
                        <img className="food-image" src={item.imageUrl} alt={item.name} loading="lazy" />
                        {index === 0 && activeCategory === 'All' ? <span className="popular-tag">✦ crowd favourite</span> : null}
                        {item.vegetarian ? <span className="diet-tag"><i /> veg</span> : null}
                        {quantity > 0 ? <span className="in-cart-tag">{quantity} in your bag</span> : null}
                      </div>
                      <div className="food-card-body">
                        <div className="food-title-row"><h3>{item.name}</h3><span className="food-price">{formatPrice(item.price)}</span></div>
                        <p>{item.description}</p>
                        <div className="food-card-footer">
                          <span className="food-category">{item.category}</span>
                          <button className={quantity ? 'add-button added' : 'add-button'} onClick={() => changeQuantity(item, 1)} aria-label={`Add ${item.name} to your bag`}>
                            {quantity ? <><span>+</span> Add more</> : <><span>+</span> Add</>}
                          </button>
                        </div>
                      </div>
                    </article>
                  )
                })}
              </div>
            )}
          </div>

          <aside className="order-panel" id="your-order">
            <div className="order-panel-heading"><div><div className="eyebrow muted-eyebrow">THE BEST PART</div><h2>Your order <span>({itemCount})</span></h2></div><span className="order-doodle">✳</span></div>

            {lastOrder ? (
              <div className="success-card">
                <div className="success-check">✓</div>
                <span className="success-kicker">ORDER CONFIRMED</span>
                <h3>We’re on it, {lastOrder.fullName.split(' ')[0]}!</h3>
                <p>Payment is confirmed and order <strong>#{lastOrder.orderId}</strong> is being prepared. We’ve got the details and it’ll be with you soon.</p>
                <div className="success-total"><span>Order total</span><strong>{formatPrice(lastOrder.total)}</strong></div>
                <button className="text-button" onClick={() => setLastOrder(null)}>Start a new order <span>→</span></button>
              </div>
            ) : cartItems.length === 0 ? (
              <div className="empty-cart">
                <div className="empty-cart-art"><span>✳</span><span>♡</span><span>⌁</span><div>＋</div></div>
                <h3>Your bag is taking a nap</h3>
                <p>Add something delicious from the menu and we’ll get things moving.</p>
                <a className="text-button" href="#menu">Find your favourite <span>→</span></a>
              </div>
            ) : (
              <>
                <div className="cart-lines">
                  {cartItems.map(({ item, quantity }) => (
                    <div className="cart-line" key={item.id}>
                      <img src={item.imageUrl} alt="" />
                      <div className="cart-line-details"><strong>{item.name}</strong><span>{formatPrice(Number(item.price) * quantity)}</span></div>
                      <div className="quantity-stepper">
                        <button onClick={() => changeQuantity(item, -1)} aria-label={`Remove one ${item.name}`}>−</button>
                        <span>{quantity}</span>
                        <button onClick={() => changeQuantity(item, 1)} aria-label={`Add one ${item.name}`}>+</button>
                      </div>
                    </div>
                  ))}
                </div>

                <form className="checkout-form" onSubmit={handleCheckout}>
                  <div className="checkout-divider" />
                  <h3>Where should we bring it?</h3>
                  <label>Full name<input name="fullName" value={customer.fullName} onChange={updateCustomer} placeholder="e.g. Aanya Sharma" required maxLength="120" /></label>
                  <label>Email address<input type="email" name="email" value={customer.email} onChange={updateCustomer} placeholder="you@example.com" required maxLength="180" /></label>
                  <label>Delivery address<textarea name="deliveryAddress" value={customer.deliveryAddress} onChange={updateCustomer} placeholder="Flat, street, area and any helpful directions" required maxLength="700" rows="2" /></label>
                  {checkoutError ? <div className="checkout-error" role="alert">{checkoutError}</div> : null}
                  <div className="total-row"><span>Subtotal <small>· delivery included</small></span><strong>{formatPrice(total)}</strong></div>
                  <button className="primary-button checkout-button" type="submit" disabled={placingOrder}>
                    {placingOrder ? 'Opening secure checkout…' : <>Continue to payment <span>↗</span></>}
                  </button>
                  <p className="checkout-note">Secure Razorpay test checkout. Test mode does not charge real money.</p>
                </form>
              </>
            )}
            <div className="order-footnote"><span>✦</span> A little joy, delivered.</div>
          </aside>
        </section>
      </main>

      <footer className="footer"><a className="brand footer-brand" href="#top"><span className="brand-mark">q<span>.</span></span><span>quickbite</span></a><span>Made for your next “mmm.”</span><span>© 2026 QuickBite</span></footer>
    </div>
  )
}

export default App
