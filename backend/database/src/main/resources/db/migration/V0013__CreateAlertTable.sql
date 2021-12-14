CREATE TABLE IF NOT EXISTS Alert (
    AlertId INT NOT NULL AUTO_INCREMENT,
    Subject VARCHAR(50),
    SubjectType VARCHAR(50),
    TransactionReferences VARCHAR(500),
    AlertedCondition VARCHAR(250),
    AlertedValue VARCHAR(250),
    DateCreated DATETIME,
    ScenarioName VARCHAR(50),
    PRIMARY KEY ( AlertId )
);
