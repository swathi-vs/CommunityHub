# CommunityHub

Run your hostel or PG from one place: rooms, residents, rent and notices.

## Why CommunityHub

Many hostels and PGs are still run on registers, spreadsheets and WhatsApp groups. That works for a few rooms, but it breaks down as the business grows:

- Nobody knows which beds are free without walking the building or calling the warden.
- Rent is tracked across notebooks and bank statements, so partial payments and dues get missed.
- Announcements get lost in group chats, and residents miss what matters to them.
- Maintenance requests arrive by phone and are forgotten.
- Staff and residents can see either too much or too little, because there is no proper access control.

CommunityHub replaces these with one system that owners, staff and residents all use.

## Who it is for

- **Owners** running one or several hostels, PGs or co-living spaces.
- **Managers and wardens** who handle check-ins, rent collection and day-to-day issues.
- **Residents** who want to see their dues, payment history and notices without asking.

## How it helps

| Instead of | With CommunityHub |
|---|---|
| Checking a register to find a free bed | Occupancy for every property, room and bed at a glance |
| Writing down who stays where | Tenancies that assign a bed and release it when the resident moves out |
| Reconciling rent by hand | Invoices that update as payments come in, including partial payments |
| Chasing overdue rent from memory | A dashboard of what is overdue or due soon |
| Posting in group chats | Notices for the whole organization or a single property, with publish and expiry dates |
| Taking complaints over the phone | Forms that residents fill in, and that staff can track |
| Sharing one login | Separate access for admins, managers and tenants |

Every organization's data is kept separate, so one installation can serve several businesses.

## What a typical day looks like

1. A new resident is added and assigned a bed. The bed is marked occupied straight away.
2. The manager raises the month's rent invoice. The resident sees it on their own account.
3. The resident pays part of it. The invoice shows as partially paid, with the balance outstanding.
4. A notice about water maintenance goes out to that property only.
5. The resident reports a leaking tap through the maintenance form, and staff see it in their list.
6. When the resident leaves, the tenancy is ended and the bed becomes available again.

## Roles

| Role | What they can do |
|---|---|
| Admin | Everything, including organization settings and assigning roles |
| Manager | Properties, rooms, tenancies, invoices, payments, notices and forms |
| Tenant | View their own tenancy, invoices, payments and notices, and submit forms |

## Getting started

These steps run CommunityHub on your own computer.

**Requirements:** Java 17, Apache Tomcat 9, MySQL 8, Node.js 18+

### Database

Create a MySQL database named `CommunityHub`, then load the default roles, permissions and property types. The app needs these to work:

```bash
mysql -u root -p CommunityHub < db/seed.sql
```

### Backend

Copy the config file and add your MySQL credentials:

```bash
cp src/resources/db.properties.example src/resources/db.properties
```

Then deploy the project to Tomcat 9 with the context path `/CommunityHub`.

### Frontend

```bash
cd frontend
npm install
npm run dev:real
```

Open http://localhost:5173, sign up, and create your organization. You become its admin.

To look around the interface without a database, run `npm run dev` instead. It uses built-in sample data.

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React 18, Vite |
| Backend | Java 17, Apache Struts 6, JSON API |
| Database | MySQL 8 |
| Server | Apache Tomcat 9 |
