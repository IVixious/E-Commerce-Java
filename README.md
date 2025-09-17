# E-Commerce System JAVA & OOP Concepts 🛒✨ Uni-Project

This project is a **E-Commerce System** designed with clear assumptions and role-based access control.  
It incorporates **object-oriented programming (OOP)** principles and is built to provide a user-friendly experience,  
drawing inspiration from platforms like **Lazada** and **Shopee**.  

New users will find it intuitive and easy to learn without hesitation.

---

## Key Features & Assumptions

In this repo, I have implemented the following core features:
- **Role-Based Access Control (RBAC)**  
  - Three main roles: **Admin**, **Seller**, and **Customer**.  
  - Each user must register and log in to authenticate.  
  - Access is strictly limited to role-specific functionalities.  

- **Logging System**  
  - Every user activity (login, email/password updates, account changes) is logged.  

- **Revenue Assumption**  
  - Revenue data from payment records is **simulated**, not processed in real-time.  

- **Design Philosophy**  
  - User interface (UI) is clean and intuitive.  
  - Focused on **familiar e-commerce flows** without complex or advanced features.  

---

## Roles & Functionalities

### 🔹 Admin
- Manage product categories.  
- Add or remove sellers/customers.  
- View **sales reports** with details like:  
  - Total orders  
  - Page views  
  - New users  
  - Revenue generated  
- Supervise the system and provide business insights.  

### 🔹 Seller
- Manage personal products and customer orders.  
- Edit own account details (cannot edit other sellers’ data).  
- Create promotions (vouchers, discounts, campaigns).  
- Manage orders:  
  - Update status to **“Ready for Delivery”** once payment is confirmed.  
  - View payment & order statuses.  

### 🔹 Customer
- Register and manage personal account details.  
- Browse available products with personalized search.  
- Manage cart (add, modify, remove products).  
- Place orders and process payments.  
- View **order history**, including:  
  - Seller’s name  
  - Date  
  - Product quantity  
  - Product name  
  - Total cost  

---

## Object-Oriented Design
The system is designed with OOP principles, making it modular and maintainable:
- **Encapsulation**: User data and role functionalities are well-structured.  
- **Inheritance**: Base `User` class extended by `Admin`, `Seller`, and `Customer`.  
- **Polymorphism**: Role-specific actions are defined through shared methods.

---

## Extras 💡
- 🔐 **SHA-256 password handling** for security  
- 📜 **Logs for login & account changes**  
- 📊 **Customer report records** (orders, payments, activity)  
- 📦 **Custom data serialization** for system data handling

  ## GitHub Lesson Learnt 🔍📝
Through this project, I explored more than just coding:  
- Practicing structured commits & reflections  
- Strengthening **object-oriented design** skills (encapsulation, inheritance, polymorphism)

  ## End of Lesson - Thank You! 🙌
This project is a **comfortable & user-friendly e-commerce simulation**,  
built with clean Java logic and fun features for anyone to explore! 🚀
