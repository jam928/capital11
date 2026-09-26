-- Runs once, when the mysql container initializes an empty data volume.
-- Table and column names match the legacy capital_11 database.

CREATE TABLE customer (
    cid      INT          NOT NULL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    birthday VARCHAR(10),
    gender   VARCHAR(10)
);

CREATE TABLE account (
    acct_num  INT            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    acct_type VARCHAR(20)    NOT NULL,
    cid       INT            NOT NULL UNIQUE,
    balance   DECIMAL(15, 2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_account_customer FOREIGN KEY (cid) REFERENCES customer (cid)
);

CREATE TABLE transaction (
    tid       INT            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tr_type   VARCHAR(20)    NOT NULL,
    acct_num  INT            NOT NULL,
    amount    DECIMAL(15, 2) NOT NULL,
    dateTrans DATETIME       NOT NULL,
    cid       INT            NOT NULL,
    CONSTRAINT fk_transaction_account FOREIGN KEY (acct_num) REFERENCES account (acct_num),
    CONSTRAINT fk_transaction_customer FOREIGN KEY (cid) REFERENCES customer (cid)
);
