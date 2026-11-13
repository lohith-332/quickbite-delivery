# QuickBite

A beginner-friendly full-stack food delivery starter: React for the customer-facing page, Spring Boot for the REST API, and MySQL for saved menu items and orders.

## What is included

- Browse a seeded menu and filter it by category.
- Add and remove dishes in a shopping bag.
- Submit a delivery name, email, and address through Razorpay Checkout in test mode.
- Save orders and item price snapshots in MySQL only after payment is verified.
- Verify Razorpay signatures and confirm the captured payment against the saved order amount.
- View a paid order confirmation with its order number.

This is a local learning project. Razorpay test mode is for demonstration only and does not charge real money. Customer accounts, restaurant administration, webhooks, and live delivery tracking are not included.

## Before you start

Install:

- JDK 21
- IntelliJ IDEA
- MySQL Server 8.4
- MySQL Workbench
- Node.js 22.12 or newer

The frontend uses Vite. Its current setup guide requires Node.js 20.19+ or 22.12+; this project uses the 22.12+ line. Spring Boot 3.5.16 runs on Java 17+, and this project is set to Java 21.

## Run the project

### 1. Install and start MySQL Server

MySQL Workbench is the database client; it does not necessarily install the server. First, try opening its **Local instance** connection. If that connects, skip to the SQL setup below. If it does not, install MySQL Community Server 8.4 for Windows using the official [MySQL Server download](https://dev.mysql.com/downloads/mysql/) and follow the installer and MySQL Configurator prompts. Use port `3306` and configure the server as a Windows service so it starts locally.

In Workbench, connect to your local server as `root` using the root password you chose in the installer. Open a SQL tab and run:

```sql
CREATE DATABASE IF NOT EXISTS quickbite;
CREATE USER IF NOT EXISTS 'quickbite'@'localhost' IDENTIFIED BY 'quickbite';
GRANT ALL PRIVILEGES ON quickbite.* TO 'quickbite'@'localhost';
FLUSH PRIVILEGES;
```

Then create a Workbench connection for the app using:

- Hostname: `127.0.0.1`
- Port: `3306`
- Username: `quickbite`
- Password: `quickbite`
- Default schema: `quickbite`

If Workbench asks to save the password, enter `quickbite` for this local development user. The `docker-compose.yml` file is optional; it is only needed if you later choose to run MySQL through Docker instead.

### 2. Open the whole project in IntelliJ IDEA

1. In IntelliJ, choose **File → Open** and select the top-level `quickbite-delivery/pom.xml`.
2. Choose **Open as Project** if IntelliJ asks.
3. Set the project SDK to JDK 21 and allow Maven to load the project.

The project window now contains both `backend` and `frontend`. The root Maven file groups the Spring Boot backend as a module; the React/Vite frontend stays in its `frontend` folder in the same IntelliJ project.

### 3. Start the Spring Boot backend

1. Open `backend/src/main/java/com/quickbite/app/QuickBiteApplication.java`.
2. Click the green run icon beside `main`.

The API starts at `http://localhost:8080`. On first launch, it creates the database tables and adds sample menu items. You can also run it from a terminal inside `backend` with `mvn spring-boot:run` if Maven is installed. (A Maven wrapper is not included in this starter.)

### 4. Add Razorpay test keys in IntelliJ

1. Sign in to Razorpay and switch the Dashboard to **Test Mode**.
2. Create or view the test API keys. The Key ID starts with `rzp_test_`.
3. In IntelliJ, choose **Run → Edit Configurations…**, select `QuickBiteApplication`, then add these environment variables to that run configuration:

   ```text
   RAZORPAY_KEY_ID=rzp_test_your_key_id
   RAZORPAY_KEY_SECRET=your_test_key_secret
   ```

4. Save the run configuration and restart the backend. Keep the Key Secret private; never paste real keys into JavaScript, README files, or GitHub. `backend/.env.example` is a reminder template only; IntelliJ does not load it automatically.
5. In the Razorpay Dashboard, enable automatic capture for test payments. The backend checks Razorpay and marks an order paid only when the matching payment is captured.

The frontend loads Razorpay Checkout from Razorpay’s website, so the browser needs an internet connection. Checkout will stay unavailable until you add your own test credentials. For payment testing, use Razorpay’s published test payment details in **Test Mode**, not real card details.

### 5. Start the React frontend in IntelliJ

In IntelliJ, open the **Terminal** tool window. From the project root, run:

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

Open the local address printed by Vite, normally `http://127.0.0.1:5173`.

## API routes

| Method | Route | Purpose |
| --- | --- | --- |
| `GET` | `/api/menu` | List available menu items |
| `POST` | `/api/payments/create-order` | Calculate the menu total on the server and create a pending Razorpay order |
| `POST` | `/api/payments/verify` | Verify the Razorpay signature and captured payment, then confirm the QuickBite order |
| `GET` | `/api/orders/{orderId}` | Retrieve an order |

Example request for `/api/payments/create-order`:

```json
{
  "fullName": "Aanya Sharma",
  "email": "aanya@example.com",
  "deliveryAddress": "12 Lake Road, Bengaluru",
  "items": [
    { "itemId": 1, "quantity": 2 }
  ]
}
```

## Database settings

The default local database values are in `backend/src/main/resources/application.yml`. Override them with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables if you use a different MySQL server. The current `ddl-auto: update` setting is intended for local development; use database migrations before deploying this as a real service.

## Useful next steps

1. Add customer sign-in and saved addresses.
2. Add restaurant and menu management screens.
3. Add a signed Razorpay webhook to reconcile delayed payment notifications.
4. Add database migrations, authentication, and deployment configuration before any real-world use.
