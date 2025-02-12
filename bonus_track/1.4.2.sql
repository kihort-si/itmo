-- PostgreSQL inserts data into the Diploma table

CREATE TABLE Diploma(
    Student_Id INTEGER,
    Name_of_Subject VARCHAR(25) NOT NULL,
    Total_Hours INTEGER CHECK(Total_Hours>30),
    Grade Integer CHECK(Grade IN(3, 4, 5)),
    Teacher_Name VARCHAR(50),
    PRIMARY KEY(Student_Id,Name_of_Subject)
);

INSERT INTO Diploma VALUES(55, 'Algebra', 64, 4,'Petrov V.');
INSERT INTO Diploma VALUES(14,'Ivanova O.', 32, 5,'Logic');
INSERT INTO Diploma VALUES(55, 'Law', 64, 5, 'Ivanova O.');
INSERT INTO Diploma (Student_Id, Name_of_Subject, Grade, Total_Hours) VALUES(33, 'Microeconomics', 5, 96);

DROP TABLE Diploma;

-- Неудачные запросы:
-- INSERT INTO Diploma VALUES(55, 'Logic', 23, 5,'Ivanova O.');
-- INSERT INTO Diploma VALUES(55, 'Algebra', 32, 5,'Petrov V.');
-- INSERT INTO Diploma VALUES(55, 'Microeconomics', 23, 5,'Ivanova O.');
-- INSERT INTO Diploma VALUES(55, 'Foreign Language', 23, 5,'Kugot S.');
-- INSERT INTO Diploma (Student_Id, Teacher_Name, Grade, Total_Hours) VALUES(33, 'Lodkin', 96, 5);
-- INSERT INTO Diploma (Student_Id, Teacher_Name, Grade, Total_Hours) VALUES(33, 'Lodkin', 4, 96);
