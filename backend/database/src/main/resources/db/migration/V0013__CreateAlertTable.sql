CREATE TABLE IF NOT EXISTS Alert (
    AlertId INT NOT NULL AUTO_INCREMENT,
    Subject VARCHAR(50) NOT NULL,
    SubjectType VARCHAR(50) NOT NULL,
    TransactionReferences VARCHAR(500) NOT NULL,
    AlertedCondition VARCHAR(250) NOT NULL,
    AlertedValue VARCHAR(250) NOT NULL,
    DateCreated VARCHAR(25) NOT NULL,
    PRIMARY KEY ( AlertId )
);
