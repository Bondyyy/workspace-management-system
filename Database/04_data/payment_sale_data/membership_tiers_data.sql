INSERT INTO MembershipTiers (tier_name, min_points, discount_percent, description)
VALUES ('BRONZE', 0, 0, 'Default tier for newly registered customers');

INSERT INTO MembershipTiers (tier_name, min_points, discount_percent, description)
VALUES ('SILVER', 200, 5, 'Silver Tier: 5% discount on all services');

INSERT INTO MembershipTiers (tier_name, min_points, discount_percent, description)
VALUES ('GOLD', 1000, 10, 'Gold Tier: 10% discount on all services and priority booking');

INSERT INTO MembershipTiers (tier_name, min_points, discount_percent, description)
VALUES ('DIAMOND', 5000, 20, 'Diamond Tier: 20% discount on all services, priority booking, and free drinks');