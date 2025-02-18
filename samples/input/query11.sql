SELECT Student.B, Student.C FROM Student, Enrolled WHERE Student.A = Enrolled.A GROUP BY Student.B, Student.C ORDER BY Student.C, Student.B;
