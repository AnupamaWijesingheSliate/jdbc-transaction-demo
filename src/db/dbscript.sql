
-- Let's drop all the tables first
DROP TABLE IF EXISTS Employee_Salary;
DROP TABLE IF EXISTS Employee_ETF;
DROP TABLE IF EXISTS Employee;

CREATE TABLE Employee
(
    id   VARCHAR(5) PRIMARY KEY,
    name VARCHAR(20)
);

CREATE TABLE Employee_Salary
(
    id     VARCHAR(5) PRIMARY KEY,
    salary DECIMAL,
    CONSTRAINT FOREIGN KEY fk_employee_salary (id) REFERENCES Employee (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Employee_ETF
(
    id  VARCHAR(5) PRIMARY KEY,
    eft DECIMAL,
    CONSTRAINT FOREIGN KEY fk_employee_etf (id) REFERENCES Employee (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Inserting records to Employee table
INSERT INTO Employee
VALUES ('E001', 'Sanoj');
INSERT INTO Employee
VALUES ('E002', 'Gaka');
INSERT INTO Employee
VALUES ('E003', 'Appu');
INSERT INTO Employee
VALUES ('E004', 'Weerea');

# Inserting records to Employee salary table
INSERT INTO Employee_Salary
VALUES ('E001', 2000);
INSERT INTO Employee_Salary
VALUES ('E002', 1500);
INSERT INTO Employee_Salary
VALUES ('E003', 3000);
INSERT INTO Employee_Salary
VALUES ('E004', 3000);

-- ================================================================================
-- TRIGGERS SECTION
-- ================================================================================
DROP TRIGGER IF EXISTS tr_add_etf;
CREATE TRIGGER tr_add_etf AFTER INSERT ON Employee_Salary FOR EACH ROW
    BEGIN
        INSERT INTO Employee_ETF VALUES (NEW.id, (NEW.salary * 10)/100);
    end;

DROP TRIGGER IF EXISTS tr_update_etf;
CREATE TRIGGER tr_update_etf AFTER UPDATE ON Employee_Salary FOR EACH ROW
BEGIN
    UPDATE Employee_ETF SET eft = (NEW.salary * 10)/100 WHERE id= NEW.id;
end;

DROP TRIGGER IF EXISTS tr_update_salary;
CREATE TRIGGER tr_update_salary BEFORE DELETE ON Employee FOR EACH ROW
BEGIN
    DECLARE increment DECIMAL DEFAULT 0;
    SELECT salary/((SELECT COUNT(*) FROM Employee_Salary)-1) INTO increment FROM Employee_Salary WHERE id = OLD.id;
    UPDATE Employee_Salary SET salary = salary + increment;
end;