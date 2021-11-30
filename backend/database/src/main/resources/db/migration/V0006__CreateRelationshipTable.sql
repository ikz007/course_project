CREATE TABLE IF NOT EXISTS Relationship(
    RelationshipID VARCHAR(50),
    CustomerID VARCHAR(50),
    IBAN VARCHAR(50),
    StartDate DATE,
    EndDate DATE DEFAULT '9999-01-01',
    FOREIGN KEY (CustomerID)
    REFERENCES Customer(CustomerID),
    FOREIGN KEY (IBAN)
    REFERENCES Account(IBAN)
);

CREATE UNIQUE INDEX Relationship_RelationshipID
ON Relationship(RelationshipID);