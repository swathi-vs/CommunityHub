// In-browser fake backend. Answers the same paths and returns the same
// { success, message, code, data?, errors? } envelope as the real API, so the UI
// works without Tomcat/MySQL. Data is kept in localStorage; call resetMockData()
// (or use the "Reset demo data" link on the sign-in page) to start over.
//
// Demo logins (password for all: demo123)
//   swathi@email.com  – admin of Sri Ganesh Hostel
//   rajesh@test.com   – manager
//   priya@test.com    – tenant

const KEY = "ch-mock-db";
const PASSWORD = "demo123";

const ago = (days) => new Date(Date.now() - days * 864e5);
const d = (days) => ago(days).toISOString().slice(0, 10);
const ts = (days) => ago(days).toISOString().slice(0, 19);

function seed() {
  const users = [
    { id: 4, fullName: "Swathi", email: "swathi@email.com", phone: "9876500001", gender: "female", nationality: "Indian", status: "active" },
    { id: 3, fullName: "Rajesh Kumar", email: "rajesh@test.com", phone: "9876500002", gender: "male", nationality: "Indian", status: "active" },
    { id: 5, fullName: "Priya Sharma", email: "priya@test.com", phone: "9876500003", gender: "female", nationality: "Indian", status: "active" },
    { id: 6, fullName: "Arjun Mehta", email: "arjun@test.com", phone: "9876500004", gender: "male", nationality: "Indian", status: "active" },
    { id: 7, fullName: "Meera Iyer", email: "meera@test.com", phone: "9876500005", gender: "female", nationality: "Indian", status: "active" },
    { id: 8, fullName: "Karthik Rao", email: "karthik@test.com", phone: "9876500006", gender: "male", nationality: "Indian", status: "active" },
  ].map((u) => ({ dob: null, role: "tenant", ...u }));

  const roles = [
    { id: 1, name: "admin", description: "Organization owner - all capabilities" },
    { id: 2, name: "manager", description: "Property manager - day-to-day operations" },
    { id: 3, name: "tenant", description: "Resident - sees only their own data" },
  ];

  const userRoles = [
    { organizationId: 1, userId: 4, roleId: 1, propertyId: null },
    { organizationId: 1, userId: 3, roleId: 2, propertyId: null },
    ...[5, 6, 7, 8].map((userId) => ({ organizationId: 1, userId, roleId: 3, propertyId: null })),
  ];

  const organizations = [
    { id: 1, name: "Sri Ganesh Hostel", gstNumber: "29ABCDE1234F1Z5", businessEmail: "contact@sriganesh.in", businessPhone: "8040001234",
      addressLine1: "12, 5th Cross, Koramangala", city: "Bengaluru", state: "Karnataka", pincode: "560034", status: "active" },
  ];

  const propertyTypes = [{ id: 1, name: "Hostel" }, { id: 2, name: "PG" }, { id: 3, name: "Apartment" }, { id: 4, name: "Co-living" }];

  const properties = [
    { id: 1, organizationId: 1, propertyTypeId: 1, name: "Ganesh Residency – Koramangala", addressLine1: "12, 5th Cross, Koramangala", city: "Bengaluru", state: "Karnataka", pincode: "560034", status: "active" },
    { id: 2, organizationId: 1, propertyTypeId: 2, name: "Ganesh Ladies PG – Indiranagar", addressLine1: "88, 100 Feet Road, Indiranagar", city: "Bengaluru", state: "Karnataka", pincode: "560038", status: "active" },
    { id: 3, organizationId: 1, propertyTypeId: 4, name: "Ganesh Co-living – HSR", addressLine1: "21, Sector 2, HSR Layout", city: "Bengaluru", state: "Karnataka", pincode: "560102", status: "draft" },
  ];

  const rooms = [];
  const beds = [];
  let rid = 1, bid = 1;
  const addRoom = (propertyId, roomNumber, roomType, capacity, monthlyRent, hasAc) => {
    const room = { id: rid++, organizationId: 1, propertyId, roomNumber, roomType, capacity, monthlyRent, hasAc, status: "active" };
    rooms.push(room);
    for (let i = 0; i < capacity; i++) beds.push({ id: bid++, roomId: room.id, bedLabel: `${roomNumber}-${String.fromCharCode(65 + i)}`, status: "available" });
  };
  addRoom(1, "101", "Double sharing", 2, 8500, true);
  addRoom(1, "102", "Triple sharing", 3, 6500, false);
  addRoom(1, "201", "Single", 1, 12000, true);
  addRoom(2, "G1", "Double sharing", 2, 9000, true);
  addRoom(2, "G2", "Single", 1, 13500, true);

  const tenancies = [
    { id: 1, organizationId: 1, propertyId: 1, roomId: 1, bedId: 1, userId: 5, startDate: d(120), expectedEndDate: d(-245), depositAmount: 17000, depositStatus: "held", status: "active" },
    { id: 2, organizationId: 1, propertyId: 1, roomId: 2, bedId: 3, userId: 6, startDate: d(60), expectedEndDate: d(-120), depositAmount: 13000, depositStatus: "held", status: "active" },
    { id: 3, organizationId: 1, propertyId: 2, roomId: 4, bedId: 7, userId: 7, startDate: d(30), expectedEndDate: null, depositAmount: 18000, depositStatus: "held", status: "active" },
    { id: 4, organizationId: 1, propertyId: 1, roomId: 3, bedId: 6, userId: 8, startDate: d(400), expectedEndDate: d(35), depositAmount: 24000, depositStatus: "refunded", status: "vacated" },
  ];
  tenancies.filter((t) => t.status === "active").forEach((t) => (beds.find((b) => b.id === t.bedId).status = "occupied"));
  beds.find((b) => b.id === 5).status = "maintenance";

  let inv = 1;
  const invoice = (tenancyId, invoiceType, totalAmount, amountPaid, dueDays, status, description) => {
    const t = tenancies.find((x) => x.id === tenancyId);
    return { id: inv, organizationId: 1, propertyId: t.propertyId, tenancyId, userId: t.userId, invoiceNumber: `INV-1-${1000 + inv++}`,
      invoiceType, description, totalAmount, amountPaid, dueDate: d(dueDays), status };
  };
  const invoices = [
    invoice(1, "deposit", 17000, 17000, 118, "paid", "Security deposit"),
    invoice(1, "rent", 8500, 8500, 25, "paid", "Rent – last month"),
    invoice(1, "rent", 8500, 0, -5, "pending", "Rent – this month"),
    invoice(2, "rent", 6500, 3000, 10, "partial", "Rent – last month"),
    invoice(2, "mess", 3200, 0, 3, "overdue", "Mess charges"),
    invoice(3, "deposit", 18000, 18000, 28, "paid", "Security deposit"),
    invoice(3, "rent", 9000, 0, -5, "pending", "Rent – this month"),
    invoice(3, "utility", 850, 0, -12, "pending", "Electricity"),
  ];

  const payments = [
    { id: 1, organizationId: 1, invoiceId: 1, amount: 17000, method: "bank_transfer", reference: "NEFT-88213", paidAt: ts(118), recordedUserId: 3, notes: null },
    { id: 2, organizationId: 1, invoiceId: 2, amount: 8500, method: "upi", reference: "UPI-4410029", paidAt: ts(24), recordedUserId: 3, notes: null },
    { id: 3, organizationId: 1, invoiceId: 4, amount: 3000, method: "cash", reference: null, paidAt: ts(8), recordedUserId: 4, notes: "Balance next week" },
    { id: 4, organizationId: 1, invoiceId: 6, amount: 18000, method: "upi", reference: "UPI-5520911", paidAt: ts(29), recordedUserId: 4, notes: null },
  ];

  const posts = [
    { id: 1, organizationId: 1, propertyId: 1, authorUserId: 4, title: "Water supply maintenance on Saturday", body: "Water will be unavailable from 10am to 1pm this Saturday while the overhead tank is cleaned. Please store water in advance.", visibility: "property", publishAt: ts(2), expiresAt: null },
    { id: 2, organizationId: 1, propertyId: 1, authorUserId: 3, title: "Rent due by the 5th", body: "A gentle reminder that this month's rent is due by the 5th. You can pay by UPI or at the front desk.", visibility: "org_wide", publishAt: ts(6), expiresAt: null },
    { id: 3, organizationId: 1, propertyId: 2, authorUserId: 4, title: "Wi-Fi upgrade complete", body: "All rooms now have 100 Mbps Wi-Fi. The password is on the notice board in the lobby.", visibility: "residents_only", publishAt: ts(12), expiresAt: null },
    { id: 4, organizationId: 1, propertyId: 1, authorUserId: 4, title: "Staff meeting – Monday 9am", body: "All wardens and kitchen staff, please join in the office.", visibility: "admins_only", publishAt: ts(1), expiresAt: null },
  ];

  const forms = [
    { id: 1, organizationId: 1, formName: "Maintenance request", description: "Report something that needs fixing in your room.", isActive: true, createdAt: ts(90) },
    { id: 2, organizationId: 1, formName: "Property details", description: "Extra facts about a property.", isActive: true, createdAt: ts(100) },
  ];
  const fields = [
    { id: 1, formId: 1, fieldKey: "issue_type", fieldLabel: "Issue type", fieldType: "enum", isRequired: true, displayOrder: 1, helpText: null, defaultValue: null,
      options: [["plumbing", "Plumbing"], ["electrical", "Electrical"], ["furniture", "Furniture"], ["cleaning", "Cleaning"], ["other", "Other"]] },
    { id: 2, formId: 1, fieldKey: "details", fieldLabel: "What's wrong?", fieldType: "textarea", isRequired: true, displayOrder: 2, helpText: "Describe the problem and where it is." },
    { id: 3, formId: 1, fieldKey: "urgent", fieldLabel: "Urgent", fieldType: "boolean", isRequired: false, displayOrder: 3, helpText: null },
    { id: 4, formId: 2, fieldKey: "floors", fieldLabel: "Number of floors", fieldType: "number", isRequired: false, displayOrder: 1 },
    { id: 5, formId: 2, fieldKey: "has_lift", fieldLabel: "Lift available", fieldType: "boolean", isRequired: false, displayOrder: 2 },
    { id: 6, formId: 2, fieldKey: "warden_phone", fieldLabel: "Warden phone", fieldType: "text", isRequired: false, displayOrder: 3 },
  ].map((f, i) => ({
    organizationId: 1, defaultValue: null, helpText: null, isActive: true, columnName: `col_${f.id}`, ...f,
    options: f.options?.map(([optionValue, optionLabel], j) => ({ id: i * 10 + j + 1, fieldDefinitionId: f.id, optionValue, optionLabel, displayOrder: j + 1 })),
  }));
  const formLinks = [{ propertyId: 1, formId: 2 }];
  const propertyValues = { 1: { floors: "3", has_lift: "false", warden_phone: "9845012345" } };
  const records = [
    { id: 1, formId: 1, organizationId: 1, tenancyId: 1, createdAt: ts(4), values: { issue_type: "plumbing", details: "Bathroom tap is leaking.", urgent: "false" } },
    { id: 2, formId: 1, organizationId: 1, tenancyId: 2, createdAt: ts(1), values: { issue_type: "electrical", details: "Fan regulator not working.", urgent: "true" } },
  ];

  return { users, roles, userRoles, organizations, propertyTypes, properties, rooms, beds, tenancies, invoices, payments, posts,
    forms, fields, formLinks, propertyValues, records, session: null, nextId: 100 };
}

let db = load();
function load() {
  try { const s = localStorage.getItem(KEY); if (s) return JSON.parse(s); } catch { /* storage unavailable */ }
  return seed();
}
const save = () => { try { localStorage.setItem(KEY, JSON.stringify(db)); } catch { /* storage unavailable */ } };
export function resetMockData() { db = seed(); save(); }

/* ---------- helpers ---------- */
const ok = (message, data) => ({ success: true, message, code: 200, data });
const fail = (code, message, errors) => ({ success: false, code, message, ...(errors ? { errors } : {}) });
const bad = (errors) => fail(400, "Validation failed", errors);
const nid = () => ++db.nextId;
const num = (v) => (v === undefined || v === "" ? undefined : Number(v));

const NUMERIC = new Set(["id", "propertyTypeId", "capacity", "monthlyRent", "roomId", "bedId", "userId", "depositAmount", "totalAmount",
  "amount", "displayOrder", "roleId", "formId"]);
const BOOL = new Set(["hasAc", "isRequired"]);
function pick(params, prefix) {
  const o = {};
  for (const [k, v] of Object.entries(params)) {
    if (!k.startsWith(prefix + ".")) continue;
    const key = k.slice(prefix.length + 1);
    o[key] = NUMERIC.has(key) ? num(v) : BOOL.has(key) ? v === "true" : v;
  }
  return o;
}
const clean = (o) => Object.fromEntries(Object.entries(o).filter(([, v]) => v !== undefined && v !== ""));
function page(rows, p) {
  const size = Math.min(num(p.size) || 20, 100);
  const start = ((num(p.page) || 1) - 1) * size;
  return rows.slice(start, start + size);
}
const me = () => db.users.find((u) => u.id === db.session);
const roleNames = (userId, org) =>
  db.userRoles.filter((r) => r.userId === userId && r.organizationId === org).map((r) => db.roles.find((x) => x.id === r.roleId)?.name);
const tenantOnly = (org) => { const r = roleNames(db.session, org); return r.length > 0 && r.every((x) => x === "tenant"); };
const memberships = (userId) => {
  const orgs = {};
  db.userRoles.filter((r) => r.userId === userId).forEach((r) => {
    const o = (orgs[r.organizationId] ||= { organizationId: r.organizationId, organizationName: db.organizations.find((x) => x.id === r.organizationId)?.name, roles: [] });
    o.roles.push({ role: db.roles.find((x) => x.id === r.roleId)?.name, propertyId: r.propertyId });
  });
  return Object.values(orgs);
};
const publicUser = (u) => u && { id: u.id, fullName: u.fullName, email: u.email, phone: u.phone, dob: u.dob, gender: u.gender, nationality: u.nationality, status: u.status, role: u.role };
const stripRecord = ({ values, ...r }) => r;
const fieldOut = (f) => ({ ...f, options: f.fieldType === "enum" ? f.options || [] : undefined });
const required = (obj, names) => {
  const e = {};
  for (const [k, label] of names) if (obj[k.split(".").pop()] === undefined || obj[k.split(".").pop()] === "") e[k] = `${label} required`;
  return Object.keys(e).length ? e : null;
};
const paidStatus = (paid, total, current) => (paid >= total - 0.005 ? "paid" : paid > 0 ? "partial" : ["paid", "partial"].includes(current) ? "pending" : current);

/* ---------- routes ---------- */
// Each route: [regex, (match, params) => response]. Checked in order.
const routes = [
  [/^login$/, (m, p) => {
    const u = db.users.find((x) => x.email === p.email);
    if (!p.email || !p.password) return bad({ ...(p.email ? {} : { email: "Invalid email" }), ...(p.password ? {} : { password: "Password is required" }) });
    if (!u || (p.password !== PASSWORD && p.password !== u.password)) return fail(401, "Invalid email or password");
    if (u.status === "inactive") return fail(403, "Account is deactivated");
    db.session = u.id;
    return ok("Logged in", { user: publicUser(u), organizations: memberships(u.id) });
  }],
  [/^signup$/, (m, p) => {
    const u = pick(p, "user");
    const e = {};
    if (!/^[A-Za-z ]{3,50}$/.test(u.fullName || "")) e["user.fullName"] = "Invalid name";
    if (!/^[^@\s]+@[^@\s]+$/.test(u.email || "")) e["user.email"] = "Invalid email";
    else if (db.users.some((x) => x.email === u.email)) e["user.email"] = "Email is already registered";
    if (!/^[0-9]{10}$/.test(u.phone || "")) e["user.phone"] = "Invalid phone";
    if (!p.password || p.password.length < 6) e.password = "Password must be at least 6 characters";
    if (Object.keys(e).length) return bad(e);
    const user = { id: nid(), status: "active", role: "tenant", dob: null, gender: null, nationality: null, ...u, password: p.password };
    db.users.push(user);
    return ok("Account created", publicUser(user));
  }],
  [/^auth\/logout$/, () => { db.session = null; return ok("Logged out"); }],
  [/^auth\/change-password$/, (m, p) => {
    if (!p.newPassword || p.newPassword.length < 6) return bad({ newPassword: "New password must be at least 6 characters" });
    if (p.newPassword !== p.confirmPassword) return bad({ confirmPassword: "Passwords do not match" });
    me().password = p.newPassword;
    return ok("Password changed");
  }],
  [/^roles$/, () => ok("Roles", db.roles)],
  [/^property-types$/, () => ok("Property types", db.propertyTypes)],
  [/^organization\/create$/, (m, p) => {
    const o = pick(p, "organization");
    const e = required(o, [["organization.name", "Organization name"], ["organization.businessEmail", "Business email"], ["organization.businessPhone", "Business phone"]]);
    if (e) return bad(e);
    const org = { id: nid(), status: "active", ...o };
    db.organizations.push(org);
    db.userRoles.push({ organizationId: org.id, userId: db.session, roleId: 1, propertyId: null });
    return ok("Organization created", org);
  }],

  // ----- organization-scoped routes: the org id is m[1]
  [/^organizations\/(\d+)\/update$/, (m, p, org) => { Object.assign(org, clean(pick(p, "organization"))); return ok("Organization updated", org); }],

  // properties
  [/^organizations\/(\d+)\/properties\/create$/, (m, p, org) => {
    const x = pick(p, "property");
    const e = required(x, [["property.name", "Property name"], ["property.addressLine1", "Address"], ["property.city", "City"], ["property.state", "State"]]);
    if (e) return bad(e);
    if (!/^[0-9]{6}$/.test(x.pincode || "")) return bad({ "property.pincode": "Pincode must be 6 digits" });
    const prop = { id: nid(), organizationId: org.id, propertyTypeId: x.propertyTypeId || 1, status: "draft", ...x };
    db.properties.push(prop);
    return ok("Property created", prop);
  }],
  [/^organizations\/(\d+)\/properties\/(\d+)\/rooms\/(\d+)\/beds\/create$/, (m, p) => {
    const b = pick(p, "bed");
    if (!b.bedLabel) return bad({ "bed.bedLabel": "Bed label is required" });
    const bed = { id: nid(), roomId: +m[3], bedLabel: b.bedLabel, status: "available" };
    db.beds.push(bed);
    return ok("Bed created", bed);
  }],
  [/^organizations\/(\d+)\/properties\/(\d+)\/rooms\/(\d+)\/beds$/, (m, p) =>
    ok("Beds", db.beds.filter((b) => b.roomId === +m[3] && (!p.status || b.status === p.status)))],
  [/^organizations\/(\d+)\/properties\/(\d+)\/rooms\/(\d+)\/delete$/, (m) => {
    if (db.tenancies.some((t) => t.roomId === +m[3] && t.status === "active")) return fail(409, "Room has active tenancies. End them first.");
    db.rooms = db.rooms.filter((r) => r.id !== +m[3]);
    db.beds = db.beds.filter((b) => b.roomId !== +m[3]);
    return ok("Room deleted");
  }],
  [/^organizations\/(\d+)\/properties\/(\d+)\/rooms\/create$/, (m, p, org) => {
    const r = pick(p, "room");
    if (!r.roomNumber) return bad({ "room.roomNumber": "Room number is required" });
    if (!r.capacity || r.capacity < 1) return bad({ "room.capacity": "Capacity must be at least 1" });
    if (db.rooms.some((x) => x.propertyId === +m[2] && x.roomNumber === r.roomNumber)) return fail(409, "Room number already exists in this property");
    const room = { id: nid(), organizationId: org.id, propertyId: +m[2], status: "active", roomType: null, monthlyRent: 0, hasAc: false, ...clean(r) };
    db.rooms.push(room);
    return ok("Room created", room);
  }],
  [/^organizations\/(\d+)\/properties\/(\d+)\/rooms$/, (m) => ok("Rooms", db.rooms.filter((r) => r.propertyId === +m[2]))],
  [/^organizations\/(\d+)\/properties\/(\d+)\/users\/(\d+)\/tenancies\/create$/, (m, p, org) => {
    const t = pick(p, "tenancy");
    const e = required(t, [["tenancy.roomId", "Room"], ["tenancy.bedId", "Bed"], ["tenancy.startDate", "Start date"]]);
    if (e) return bad(e);
    const bed = db.beds.find((b) => b.id === t.bedId);
    if (!bed || bed.status !== "available") return fail(409, "Bed is not available");
    if (db.tenancies.some((x) => x.userId === +m[3] && x.organizationId === org.id && x.status === "active"))
      return fail(409, "User already has an active tenancy in this organization");
    bed.status = "occupied";
    const ten = { id: nid(), organizationId: org.id, propertyId: +m[2], userId: +m[3], expectedEndDate: null, depositAmount: 0, depositStatus: "held", ...clean(t), status: "active" };
    db.tenancies.push(ten);
    return ok("Tenancy created", ten);
  }],
  [/^organizations\/(\d+)\/properties\/(\d+)\/users\/(\d+)\/roles\/assign$/, (m, p, org) => assignRole(org, +m[3], p, +m[2])],
  [/^organizations\/(\d+)\/properties\/(\d+)\/tenancies\/(\d+)\/invoices\/create$/, (m, p, org) => {
    const i = pick(p, "invoice");
    const e = required(i, [["invoice.totalAmount", "Amount"], ["invoice.dueDate", "Due date"]]);
    if (e) return bad(e);
    const t = db.tenancies.find((x) => x.id === +m[3]);
    const id = nid();
    const invoice = { id, organizationId: org.id, propertyId: t.propertyId, tenancyId: t.id, userId: t.userId, invoiceNumber: `INV-${org.id}-${1000 + id}`,
      invoiceType: i.invoiceType || "rent", description: i.description || null, totalAmount: i.totalAmount, amountPaid: 0, dueDate: i.dueDate, status: "pending" };
    db.invoices.push(invoice);
    return ok("Invoice created", invoice);
  }],
  [/^organizations\/(\d+)\/properties\/(\d+)\/forms\/associate$/, (m, p) => {
    const formId = num(p["formRelationship.formId"]);
    if (!formId) return bad({ "formRelationship.formId": "Form required" });
    const link = db.formLinks.find((l) => l.propertyId === +m[2]);
    if (link && link.formId !== formId) delete db.propertyValues[m[2]];
    db.formLinks = db.formLinks.filter((l) => l.propertyId !== +m[2]).concat({ propertyId: +m[2], formId });
    return ok("Form linked to property");
  }],
  [/^organizations\/(\d+)\/properties\/(\d+)\/details\/submit$/, (m, p) => {
    const link = db.formLinks.find((l) => l.propertyId === +m[2]);
    if (!link) return fail(400, "No form is linked to this property. Link a form first.");
    const vals = payloadValues(p.payload, link.formId);
    if (vals.code) return vals;
    db.propertyValues[m[2]] = { ...(db.propertyValues[m[2]] || {}), ...vals };
    return ok("Property details saved");
  }],
  [/^organizations\/(\d+)\/properties\/(\d+)\/update$/, (m, p) => {
    const prop = db.properties.find((x) => x.id === +m[2]);
    Object.assign(prop, clean(pick(p, "property")));
    return ok("Property updated", prop);
  }],
  [/^organizations\/(\d+)\/properties\/(\d+)\/delete$/, (m) => {
    if (db.tenancies.some((t) => t.propertyId === +m[2] && t.status === "active")) return fail(409, "Property has active tenancies. End them first.");
    db.properties = db.properties.filter((x) => x.id !== +m[2]);
    return ok("Property deleted");
  }],
  [/^organizations\/(\d+)\/properties\/(\d+)$/, (m) => {
    const prop = db.properties.find((x) => x.id === +m[2]);
    if (!prop) return fail(404, "Property not found");
    const formId = db.formLinks.find((l) => l.propertyId === prop.id)?.formId ?? null;
    const fixedFields = [["name", "Property Name"], ["city", "City"], ["state", "State"], ["pincode", "Pincode"], ["addressLine1", "Address"], ["status", "Status"]]
      .map(([fieldKey, fieldLabel]) => ({ fieldKey, fieldLabel, fieldType: "text", value: prop[fieldKey] }));
    const vals = db.propertyValues[prop.id] || {};
    const customFields = formId ? db.fields.filter((f) => f.formId === formId && f.isActive).map((f) => ({ fieldDefId: f.id, fieldKey: f.fieldKey, fieldLabel: f.fieldLabel, fieldType: f.fieldType, value: vals[f.fieldKey] ?? null })) : [];
    return ok("Property", { property: prop, formId, fixedFields, customFields });
  }],
  [/^organizations\/(\d+)\/properties$/, (m, p, org) =>
    ok("Properties", page(db.properties.filter((x) => x.organizationId === org.id && (!p.status || x.status === p.status) && (!p.city || x.city === p.city)), p))],

  // beds
  [/^organizations\/(\d+)\/beds\/(\d+)\/update$/, (m, p) => {
    const bed = db.beds.find((b) => b.id === +m[2]);
    const b = pick(p, "bed");
    if (b.status === "occupied") return fail(400, "Beds become occupied by creating a tenancy");
    if (bed.status === "occupied" && b.status && b.status !== "occupied") return fail(409, "Bed is held by an active tenancy");
    Object.assign(bed, clean(b));
    return ok("Bed updated");
  }],

  // users
  [/^organizations\/(\d+)\/users\/(\d+)\/roles\/assign$/, (m, p, org) => assignRole(org, +m[2], p, null)],
  [/^organizations\/(\d+)\/users\/(\d+)\/deactivate$/, (m, p, org) => {
    if (+m[2] === db.session) return fail(400, "You cannot deactivate yourself");
    if (db.tenancies.some((t) => t.userId === +m[2] && t.organizationId === org.id && t.status === "active")) return fail(409, "User has an active tenancy. End it first.");
    db.userRoles = db.userRoles.filter((r) => !(r.userId === +m[2] && r.organizationId === org.id));
    return ok("User removed from organization");
  }],
  [/^organizations\/(\d+)\/users\/(\d+)\/update$/, (m, p) => {
    const u = db.users.find((x) => x.id === +m[2]);
    Object.assign(u, clean(pick(p, "user")));
    return ok("User updated", publicUser(u));
  }],
  [/^organizations\/(\d+)\/users\/(\d+)$/, (m) => ok("User", publicUser(db.users.find((x) => x.id === +m[2])))],
  [/^organizations\/(\d+)\/users$/, (m, p, org) => {
    const ids = new Set(db.userRoles.filter((r) => r.organizationId === org.id && (!p.role || db.roles.find((x) => x.id === r.roleId)?.name === p.role)).map((r) => r.userId));
    return ok("Users", page(db.users.filter((u) => ids.has(u.id)).map(publicUser), p));
  }],

  // tenancies
  [/^organizations\/(\d+)\/tenancies\/(\d+)\/update$/, (m, p) => {
    const t = db.tenancies.find((x) => x.id === +m[2]);
    const c = clean(pick(p, "tenancy"));
    const leaving = t.status === "active" && c.status && c.status !== "active";
    const moving = t.status === "active" && c.bedId && c.bedId !== t.bedId;
    if (moving) {
      const nb = db.beds.find((b) => b.id === c.bedId);
      if (!nb || nb.status !== "available") return fail(409, "Bed is not available");
      nb.status = "occupied";
    }
    if ((leaving || moving) && t.bedId) { const ob = db.beds.find((b) => b.id === t.bedId); if (ob) ob.status = "available"; }
    Object.assign(t, c);
    return ok("Tenancy updated", t);
  }],
  [/^organizations\/(\d+)\/tenancies\/(\d+)\/user$/, (m) => ok("Tenancy user", { userId: db.tenancies.find((x) => x.id === +m[2])?.userId })],
  [/^organizations\/(\d+)\/tenancies\/(\d+)$/, (m) => ok("Tenancy", db.tenancies.find((x) => x.id === +m[2]))],
  [/^organizations\/(\d+)\/tenancies$/, (m, p, org) => {
    const mine = tenantOnly(org.id);
    return ok("Tenancies", page(db.tenancies.filter((t) => t.organizationId === org.id && (!mine || t.userId === db.session)
      && (!p.status || t.status === p.status) && (!p.propertyId || t.propertyId === +p.propertyId)).sort((a, b) => b.startDate.localeCompare(a.startDate)), p));
  }],

  // invoices & payments
  [/^organizations\/(\d+)\/invoices\/(\d+)\/payments\/create$/, (m, p, org) => {
    const pay = pick(p, "payment");
    if (!pay.amount || pay.amount <= 0) return bad({ "payment.amount": "Amount required" });
    const inv = db.invoices.find((x) => x.id === +m[2]);
    const outstanding = inv.totalAmount - inv.amountPaid;
    if (inv.status === "cancelled") return fail(409, "Invoice has been cancelled");
    if (outstanding <= 0) return fail(409, "Invoice is already fully paid");
    if (pay.amount > outstanding + 0.005) return fail(400, `Amount exceeds the outstanding balance of ${outstanding}`);
    const payment = { id: nid(), organizationId: org.id, invoiceId: inv.id, method: "cash", reference: null, notes: null, ...clean(pay), paidAt: new Date().toISOString().slice(0, 19), recordedUserId: db.session };
    db.payments.push(payment);
    inv.amountPaid += pay.amount;
    inv.status = paidStatus(inv.amountPaid, inv.totalAmount, inv.status);
    return ok("Payment recorded", payment);
  }],
  [/^organizations\/(\d+)\/invoices\/update$/, (m, p) => {
    const c = clean(pick(p, "invoice"));
    const inv = db.invoices.find((x) => x.id === c.id);
    if (!inv) return fail(404, "Invoice not found");
    delete c.amountPaid;
    if (c.status === "paid" && inv.amountPaid < (c.totalAmount ?? inv.totalAmount)) return fail(400, "Invoice is not fully paid. Record a payment instead.");
    Object.assign(inv, c);
    if (!["cancelled", "overdue", "pending"].includes(c.status)) inv.status = paidStatus(inv.amountPaid, inv.totalAmount, inv.status);
    return ok("Invoice updated", inv);
  }],
  [/^organizations\/(\d+)\/invoices\/(\d+)$/, (m) => ok("Invoice", db.invoices.find((x) => x.id === +m[2]))],
  [/^organizations\/(\d+)\/invoices$/, (m, p, org) => {
    const mine = tenantOnly(org.id);
    return ok("Invoices", page(db.invoices.filter((i) => i.organizationId === org.id && (!mine || i.userId === db.session) && (!p.status || i.status === p.status))
      .sort((a, b) => b.dueDate.localeCompare(a.dueDate)), p));
  }],
  [/^organizations\/(\d+)\/payments$/, (m, p, org) => {
    const mine = tenantOnly(org.id);
    const myInvoices = new Set(db.invoices.filter((i) => i.userId === db.session).map((i) => i.id));
    return ok("Payments", page(db.payments.filter((x) => x.organizationId === org.id && (!mine || myInvoices.has(x.invoiceId)) && (!p.method || x.method === p.method))
      .sort((a, b) => b.paidAt.localeCompare(a.paidAt)), p));
  }],

  // posts
  [/^organizations\/(\d+)\/properties\/(\d+)\/posts\/create$/, (m, p, org) => {
    const x = pick(p, "post");
    const e = required(x, [["post.title", "Title"], ["post.body", "Body"]]);
    if (e) return bad(e);
    const post = { id: nid(), organizationId: org.id, propertyId: +m[2], authorUserId: db.session, visibility: "org_wide", publishAt: new Date().toISOString().slice(0, 19), expiresAt: null, ...clean(x) };
    db.posts.push(post);
    return ok("Post created", post);
  }],
  [/^organizations\/(\d+)\/posts\/update$/, (m, p) => {
    const c = clean(pick(p, "post"));
    const post = db.posts.find((x) => x.id === c.id);
    if (post.authorUserId !== db.session) return fail(403, "You can only edit your own posts");
    Object.assign(post, c);
    return ok("Post updated", post);
  }],
  [/^organizations\/(\d+)\/posts\/(\d+)\/delete$/, (m) => { db.posts = db.posts.filter((x) => x.id !== +m[2]); return ok("Post deleted"); }],
  [/^organizations\/(\d+)\/posts\/(\d+)$/, (m) => ok("Post", db.posts.find((x) => x.id === +m[2]))],
  [/^organizations\/(\d+)\/posts$/, (m, p, org) => {
    const mine = tenantOnly(org.id);
    const myProp = db.tenancies.find((t) => t.userId === db.session && t.organizationId === org.id && t.status === "active")?.propertyId;
    return ok("Posts", page(db.posts.filter((x) => x.organizationId === org.id
      && (!mine || (x.visibility !== "admins_only" && (x.visibility === "org_wide" || x.propertyId === myProp))))
      .sort((a, b) => (b.publishAt || "").localeCompare(a.publishAt || "")), p));
  }],

  // forms
  [/^organizations\/(\d+)\/form\/create$/, (m, p, org) => {
    const f = pick(p, "customForm");
    if (!f.formName) return bad({ "customForm.formName": "Form name required" });
    const form = { id: nid(), organizationId: org.id, formName: f.formName, description: f.description || null, isActive: true, createdAt: new Date().toISOString().slice(0, 19) };
    db.forms.push(form);
    return ok("Form created", form);
  }],
  [/^organizations\/(\d+)\/forms\/(\d+)\/field-definitions\/(\d+)\/options\/create$/, (m, p) => {
    const o = pick(p, "fieldOption");
    const f = db.fields.find((x) => x.id === +m[3]);
    const opt = { id: nid(), fieldDefinitionId: f.id, optionValue: o.optionValue, optionLabel: o.optionLabel, displayOrder: (f.options?.length || 0) + 1 };
    (f.options ||= []).push(opt);
    return ok("Option created", opt);
  }],
  [/^organizations\/(\d+)\/forms\/(\d+)\/field-definitions\/(\d+)\/delete$/, (m) => { const f = db.fields.find((x) => x.id === +m[3]); if (f) f.isActive = false; return ok("Field deleted"); }],
  [/^organizations\/(\d+)\/forms\/(\d+)\/field-definitions\/create$/, (m, p, org) => {
    const f = pick(p, "fieldDefinition");
    const e = {};
    if (!/^[a-z][a-z0-9_]{0,49}$/.test(f.fieldKey || "")) e["fieldDefinition.fieldKey"] = "Field key required: lowercase letters, digits and underscores, starting with a letter";
    if (!f.fieldLabel) e["fieldDefinition.fieldLabel"] = "Field label required";
    if (Object.keys(e).length) return bad(e);
    if (db.fields.some((x) => x.formId === +m[2] && x.isActive && x.fieldKey === f.fieldKey)) return fail(409, "A field with this key already exists in the form");
    const id = nid();
    const options = [];
    for (let i = 0; p[`options[${i}].optionValue`] !== undefined; i++)
      options.push({ id: nid(), fieldDefinitionId: id, optionValue: p[`options[${i}].optionValue`], optionLabel: p[`options[${i}].optionLabel`], displayOrder: i + 1 });
    if (f.fieldType === "enum" && !options.length) return bad({ options: "Options required for enum type" });
    const field = { id, formId: +m[2], organizationId: org.id, fieldType: "text", isRequired: false, defaultValue: null, helpText: null, displayOrder: 0, isActive: true, columnName: `col_${id}`, ...clean(f), options };
    db.fields.push(field);
    return ok("Field created", fieldOut(field));
  }],
  [/^organizations\/(\d+)\/forms\/(\d+)\/fields$/, (m) =>
    ok("Form fields", db.fields.filter((f) => f.formId === +m[2] && f.isActive).sort((a, b) => a.displayOrder - b.displayOrder).map(fieldOut))],
  [/^organizations\/(\d+)\/forms\/(\d+)\/records\/submit$/, (m, p, org) => {
    const t = db.tenancies.find((x) => x.userId === db.session && x.organizationId === org.id && x.status === "active");
    if (!t) return fail(400, "You need an active tenancy in this organization to submit this form");
    const vals = payloadValues(p.payload, +m[2]);
    if (vals.code) return vals;
    const rec = { id: nid(), formId: +m[2], organizationId: org.id, tenancyId: t.id, createdAt: new Date().toISOString().slice(0, 19), values: vals };
    db.records.push(rec);
    return ok("Record saved", { recordId: rec.id });
  }],
  [/^organizations\/(\d+)\/forms\/(\d+)\/records\/(\d+)$/, (m) => {
    const rec = db.records.find((r) => r.id === +m[3]);
    if (!rec) return fail(404, "Record not found");
    return ok("Record", db.fields.filter((f) => f.formId === rec.formId && f.isActive)
      .map((f) => ({ fieldDefId: f.id, fieldKey: f.fieldKey, fieldLabel: f.fieldLabel, fieldType: f.fieldType, value: rec.values[f.fieldKey] ?? null })));
  }],
  [/^organizations\/(\d+)\/forms\/(\d+)\/records$/, (m, p, org) => {
    const mine = tenantOnly(org.id);
    const myTen = new Set(db.tenancies.filter((t) => t.userId === db.session).map((t) => t.id));
    return ok("Records", db.records.filter((r) => r.formId === +m[2] && (!mine || myTen.has(r.tenancyId))).map(stripRecord).reverse());
  }],
  [/^organizations\/(\d+)\/forms\/(\d+)\/delete$/, (m) => { const f = db.forms.find((x) => x.id === +m[2]); if (f) f.isActive = false; return ok("Form deleted"); }],
  [/^organizations\/(\d+)\/forms\/(\d+)$/, (m) => ok("Form", db.forms.find((x) => x.id === +m[2]))],
  [/^organizations\/(\d+)\/forms$/, (m, p, org) =>
    ok("Forms", page(db.forms.filter((f) => f.organizationId === org.id && (p.isActive === undefined || String(f.isActive) === p.isActive)), p))],

  [/^organizations\/(\d+)$/, (m, p, org) => ok("Organization", org)],
];

function assignRole(org, userId, p, propertyId) {
  const roleId = num(p["userRole.roleId"]);
  if (!db.users.some((u) => u.id === userId)) return fail(404, "User not found");
  if (!roleId) return bad({ "userRole.roleId": "Role required" });
  if (db.userRoles.some((r) => r.organizationId === org.id && r.userId === userId && r.roleId === roleId && r.propertyId === propertyId))
    return fail(409, "User already has this role");
  db.userRoles.push({ organizationId: org.id, userId, roleId, propertyId });
  return ok("Role assigned");
}

function payloadValues(payload, formId) {
  let fields;
  try { fields = JSON.parse(payload).fields; } catch { return fail(400, "Payload must be JSON"); }
  const defs = db.fields.filter((f) => f.formId === formId && f.isActive);
  const vals = {};
  for (const f of fields || []) {
    const def = defs.find((x) => x.fieldKey === f.fieldKey || x.columnName === f.columnName);
    if (!def) return fail(400, "Unknown field in payload");
    vals[def.fieldKey] = f.value;
  }
  for (const def of defs) if (def.isRequired && !vals[def.fieldKey]) return fail(400, `${def.fieldLabel} is required`);
  return vals;
}

const PUBLIC = /^(login|signup)$/;
const TENANT_WRITES = /(records\/submit|auth\/)/;

export async function mockApi(path, params = {}, method = "POST") {
  await new Promise((r) => setTimeout(r, 120)); // feel like a network call
  if (!PUBLIC.test(path) && !me()) return fail(401, "Unauthorized");

  const om = path.match(/^organizations\/(\d+)/);
  let org = null;
  if (om) {
    org = db.organizations.find((o) => o.id === +om[1]);
    if (!org || !roleNames(db.session, org.id).length) return fail(403, "Forbidden - you are not a member of this organization");
    if (method === "POST" && tenantOnly(org.id) && !TENANT_WRITES.test(path)) return fail(403, "Forbidden - tenants can't make this change");
  }

  for (const [re, handler] of routes) {
    const m = path.match(re);
    if (m) {
      const res = handler(m, params, org);
      save();
      return JSON.parse(JSON.stringify(res));
    }
  }
  return fail(404, `No mock route for ${path}`);
}
