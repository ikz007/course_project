CREATE TABLE IF NOT EXISTS Alert (
    AlertId INT NOT NULL AUTO_INCREMENT,
    Iban VARCHAR(50) NOT NULL,
    TransactionReferences VARCHAR(500) NOT NULL,
    AlertedCondition VARCHAR(2000) NOT NULL,
    DateCreated VARCHAR(25) NOT NULL,
    PRIMARY KEY ( AlertId )
);
