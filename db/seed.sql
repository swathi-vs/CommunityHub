-- Seed data for CommunityHub. Run once after creating the tables:
--   mysql -u root -p CommunityHub < db/seed.sql
-- Safe to re-run: nothing is inserted twice.

INSERT IGNORE INTO capabilities (code, name, category) VALUES
  ('organization.view', 'Organization View', 'organization'),
  ('organization.update', 'Organization Update', 'organization'),
  ('property.view', 'Property View', 'property'),
  ('property.create', 'Property Create', 'property'),
  ('property.update', 'Property Update', 'property'),
  ('property.delete', 'Property Delete', 'property'),
  ('room.view', 'Room View', 'room'),
  ('room.update', 'Room Update', 'room'),
  ('tenancy.view', 'Tenancy View', 'tenancy'),
  ('tenancy.create', 'Tenancy Create', 'tenancy'),
  ('tenancy.update', 'Tenancy Update', 'tenancy'),
  ('invoice.view', 'Invoice View', 'invoice'),
  ('invoice.create', 'Invoice Create', 'invoice'),
  ('invoice.update', 'Invoice Update', 'invoice'),
  ('payment.view', 'Payment View', 'payment'),
  ('payment.create', 'Payment Create', 'payment'),
  ('post.view', 'Post View', 'post'),
  ('post.create.own', 'Post Create Own', 'post'),
  ('post.delete.own', 'Post Delete Own', 'post'),
  ('post.delete.any', 'Post Delete Any', 'post'),
  ('form.view', 'Form View', 'form'),
  ('form.create', 'Form Create', 'form'),
  ('field.create', 'Field Create', 'field'),
  ('role.assign', 'Role Assign', 'role'),
  ('user.view.any', 'User View Any', 'user'),
  ('user.update', 'User Update', 'user'),
  ('user.deactivate.any', 'User Deactivate Any', 'user');

-- Built-in roles (organization_id NULL = available to every organization)
INSERT INTO roles (organization_id, name, description, is_builtin)
SELECT NULL, v.name, v.description, 1 FROM (
  SELECT 'admin' AS name, 'Organization owner - all capabilities' AS description
  UNION ALL SELECT 'manager', 'Property manager - day-to-day operations'
  UNION ALL SELECT 'tenant', 'Resident - sees only their own data'
) v
WHERE NOT EXISTS (SELECT 1 FROM roles r WHERE r.name = v.name AND r.organization_id IS NULL);

-- admin: everything
INSERT IGNORE INTO role_capabilities (role_id, capability_id)
SELECT r.id, c.id FROM roles r CROSS JOIN capabilities c
WHERE r.name = 'admin' AND r.organization_id IS NULL;

-- manager: everything except organization settings, deleting properties, roles and deactivation
INSERT IGNORE INTO role_capabilities (role_id, capability_id)
SELECT r.id, c.id FROM roles r JOIN capabilities c
  ON c.code NOT IN ('organization.update', 'property.delete', 'role.assign', 'user.deactivate.any')
WHERE r.name = 'manager' AND r.organization_id IS NULL;

-- tenant: read access (limited to their own data by the app) and submitting forms
INSERT IGNORE INTO role_capabilities (role_id, capability_id)
SELECT r.id, c.id FROM roles r JOIN capabilities c
  ON c.code IN ('organization.view', 'property.view', 'room.view', 'tenancy.view',
                'invoice.view', 'payment.view', 'post.view', 'form.view')
WHERE r.name = 'tenant' AND r.organization_id IS NULL;

-- System property types
INSERT INTO property_types (organization_id, name, is_system)
SELECT NULL, v.name, 1 FROM (
  SELECT 'PG' AS name UNION ALL SELECT 'Hostel' UNION ALL SELECT 'Apartment' UNION ALL SELECT 'Co-living'
) v
WHERE NOT EXISTS (SELECT 1 FROM property_types p WHERE p.name = v.name AND p.organization_id IS NULL);
