ALTER TABLE Complaint DROP CONSTRAINT CK_Complaint_type;
ALTER TABLE Complaint
ADD CONSTRAINT CK_Complaint_type
CHECK (type IN (
    'VehicleCondition','OwnerBehavior','LatePickup',
    'VehicleDamage','LateReturn','PricingIssue','Other'
));
