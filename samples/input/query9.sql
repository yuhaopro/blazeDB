SELECT Enrolled.E, SUM(Enrolled.H * Enrolled.H) FROM Enrolled GROUP BY Enrolled.E;
