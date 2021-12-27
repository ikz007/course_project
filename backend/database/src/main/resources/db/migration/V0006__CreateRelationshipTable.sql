CREATE TABLE IF NOT EXISTS Relationship(
    RelationshipID VARCHAR(50) NOT NULL,
    CustomerID VARCHAR(50) NOT NULL,
    IBAN VARCHAR(50) NOT NULL,
    StartDate VARCHAR(30) NOT NULL,
    EndDate VARCHAR(30) DEFAULT '9999-01-01',
    FOREIGN KEY (CustomerID)
    REFERENCES Customer(CustomerID),
    FOREIGN KEY (IBAN)
    REFERENCES Account(IBAN)
);

CREATE UNIQUE INDEX Relationship_RelationshipID
ON Relationship(RelationshipID);